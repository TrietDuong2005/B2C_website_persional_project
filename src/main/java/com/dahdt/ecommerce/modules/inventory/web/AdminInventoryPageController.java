package com.dahdt.ecommerce.modules.inventory.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.catalog.service.CategoryService;
import com.dahdt.ecommerce.modules.inventory.dto.*;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.service.InventoryService;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.store.service.StoreService;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller phục vụ trang Quản lý Tồn kho và Sổ cái kho (/admin/inventory, /admin/inventory/ledger).
 * Phân quyền:
 * - ADMIN: Toàn quyền chọn xem và thao tác tồn kho mọi chi nhánh.
 * - STORE_MANAGER: Cố định chi nhánh của mình phụ trách, bị chặn nếu cố tình đổi ID chi nhánh.
 */
@Controller
@RequestMapping("/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class AdminInventoryPageController {

    private final InventoryService inventoryService;
    private final StoreService storeService;
    private final CategoryService categoryService;
    private final StoreScopeService storeScopeService;

    /**
     * Bảng danh sách tồn kho theo chi nhánh kèm bộ lọc và phân trang.
     */
    @GetMapping
    public String listInventory(@RequestParam(value = "storeId", required = false) Long storeId,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "stockStatus", required = false) String stockStatus,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "15") int size,
                                Model model) {
        // Giải quyết ID chi nhánh hợp lệ (STORE_MANAGER bị ghim chi nhánh mình, ADMIN được chọn)
        Long activeStoreId = storeScopeService.resolveStoreId(storeId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "variant.sku"));
        Page<InventoryResponseDTO> inventoryPage = Page.empty();
        StoreResponseDTO currentStore = null;

        if (activeStoreId != null) {
            inventoryPage = inventoryService.getStoreInventory(activeStoreId, keyword, categoryId, stockStatus, pageRequest);
            currentStore = storeService.getStoreById(activeStoreId);
        }

        List<StoreResponseDTO> activeStores = storeService.getActiveStores();

        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("currentStore", currentStore);
        model.addAttribute("selectedStoreId", activeStoreId);
        model.addAttribute("stores", activeStores);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStockStatus", stockStatus);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("isAdmin", storeScopeService.isCurrentAdmin());

        return "admin/inventory/inventory-list";
    }

    /**
     * Thao tác Nhập kho (IMPORT).
     */
    @PostMapping("/import")
    public String importStock(@Valid @ModelAttribute("importForm") InventoryImportRequestDTO form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(form.getStoreId());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng nhập không hợp lệ (phải >= 1)!");
            return "redirect:/admin/inventory?storeId=" + form.getStoreId();
        }

        try {
            User currentUser = storeScopeService.getCurrentUser();
            Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

            inventoryService.importStock(form, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công cho biến thể!");
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Xung đột khóa lạc quan khi nhập kho: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu tồn kho vừa thay đổi, vui lòng tải lại và thử lại");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi nhập kho: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nhập kho: " + e.getMessage());
        }

        return "redirect:/admin/inventory?storeId=" + form.getStoreId();
    }

    /**
     * Thao tác Điều chỉnh tồn kho (ADJUST).
     */
    @PostMapping("/adjust")
    public String adjustStock(@Valid @ModelAttribute("adjustForm") InventoryAdjustRequestDTO form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(form.getStoreId());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ số lượng điều chỉnh và lý do!");
            return "redirect:/admin/inventory?storeId=" + form.getStoreId();
        }

        try {
            User currentUser = storeScopeService.getCurrentUser();
            Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

            inventoryService.adjustStock(form, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Điều chỉnh tồn kho thành công!");
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Xung đột khóa lạc quan khi điều chỉnh kho: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu tồn kho vừa thay đổi, vui lòng tải lại và thử lại");
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể điều chỉnh: " + e.getMessage());
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi điều chỉnh tồn kho: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi điều chỉnh kho: " + e.getMessage());
        }

        return "redirect:/admin/inventory?storeId=" + form.getStoreId();
    }

    /**
     * Cập nhật ngưỡng đặt lại hàng (reorder_level).
     */
    @PostMapping("/reorder-level")
    public String updateReorderLevel(@RequestParam("inventoryId") Long inventoryId,
                                     @RequestParam("storeId") Long storeId,
                                     @RequestParam("reorderLevel") Integer reorderLevel,
                                     RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(storeId);

        try {
            InventoryReorderLevelRequestDTO dto = InventoryReorderLevelRequestDTO.builder()
                    .inventoryId(inventoryId)
                    .reorderLevel(reorderLevel)
                    .build();
            inventoryService.updateReorderLevel(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mức đặt lại hàng thành công!");
        } catch (ObjectOptimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu tồn kho vừa thay đổi, vui lòng tải lại và thử lại");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/inventory?storeId=" + storeId;
    }

    /**
     * Trang xem Sổ cái giao dịch kho (Audit Log - Append Only).
     */
    @GetMapping("/ledger")
    public String viewLedger(@RequestParam(value = "storeId", required = false) Long storeId,
                             @RequestParam(value = "sku", required = false) String sku,
                             @RequestParam(value = "type", required = false) InventoryTransactionType type,
                             @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                             @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "15") int size,
                             Model model) {
        Long targetStoreId;
        if (storeScopeService.isCurrentStoreManager()) {
            targetStoreId = storeScopeService.resolveStoreId(storeId);
        } else {
            targetStoreId = storeId; // ADMIN có thể xem toàn chuỗi (null) hoặc chọn chi nhánh cụ thể
        }

        LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InventoryTransactionResponseDTO> ledgerPage = inventoryService.getLedger(
                targetStoreId, sku, type, startDateTime, endDateTime, pageRequest
        );

        model.addAttribute("ledgerPage", ledgerPage);
        model.addAttribute("selectedStoreId", targetStoreId);
        model.addAttribute("stores", storeService.getActiveStores());
        model.addAttribute("sku", sku);
        model.addAttribute("selectedType", type);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("types", InventoryTransactionType.values());
        model.addAttribute("isAdmin", storeScopeService.isCurrentAdmin());

        return "admin/inventory/ledger-list";
    }
}

