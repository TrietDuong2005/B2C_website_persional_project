package com.dahdt.ecommerce.modules.store.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.store.dto.StoreRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import com.dahdt.ecommerce.modules.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ trang Quản lý Chi nhánh & Kho bãi (/admin/stores).
 * Chỉ dành riêng cho vai trò Quản trị viên (ROLE_ADMIN).
 */
@Controller
@RequestMapping("/admin/stores")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminStorePageController {

    private final StoreService storeService;

    /**
     * Hiển thị danh sách chi nhánh có bộ lọc, phân trang và modal/form thêm/sửa.
     */
    @GetMapping
    public String listStores(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "type", required = false) StoreType type,
                             @RequestParam(value = "status", required = false) StoreStatus status,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "editId", required = false) Long editId,
                             Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StoreResponseDTO> storePage = storeService.getStores(keyword, type, status, pageRequest);

        model.addAttribute("storePage", storePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);

        if (editId != null) {
            StoreResponseDTO store = storeService.getStoreById(editId);
            StoreRequestDTO form = StoreRequestDTO.builder()
                    .id(store.getId())
                    .code(store.getCode())
                    .name(store.getName())
                    .type(store.getType())
                    .province(store.getProvince())
                    .district(store.getDistrict())
                    .ward(store.getWard())
                    .detailAddress(store.getDetailAddress())
                    .latitude(store.getLatitude())
                    .longitude(store.getLongitude())
                    .phone(store.getPhone())
                    .status(store.getStatus())
                    .build();
            model.addAttribute("storeForm", form);
            model.addAttribute("isEdit", true);
        } else if (!model.containsAttribute("storeForm")) {
            model.addAttribute("storeForm", new StoreRequestDTO());
            model.addAttribute("isEdit", false);
        }

        return "admin/store/store-list";
    }

    /**
     * Lưu thông tin chi nhánh (tạo mới hoặc cập nhật).
     */
    @PostMapping("/save")
    public String saveStore(@Valid @ModelAttribute("storeForm") StoreRequestDTO form,
                            BindingResult bindingResult,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "type", required = false) StoreType type,
                            @RequestParam(value = "status", required = false) StoreStatus status,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<StoreResponseDTO> storePage = storeService.getStores(keyword, type, status, pageRequest);
            model.addAttribute("storePage", storePage);
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedType", type);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("isEdit", form.getId() != null);
            return "admin/store/store-list";
        }

        try {
            if (form.getId() == null) {
                storeService.createStore(form);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm mới chi nhánh [" + form.getCode() + "] thành công!");
            } else {
                storeService.updateStore(form.getId(), form);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi nhánh [" + form.getCode() + "] thành công!");
            }
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("storeForm", form);
        } catch (Exception e) {
            log.error("Lỗi khi lưu chi nhánh: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi lưu thông tin chi nhánh: " + e.getMessage());
        }

        return "redirect:/admin/stores";
    }

    /**
     * Đổi trạng thái hoạt động của chi nhánh (ngưng hoạt động / kích hoạt lại).
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStoreStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            StoreResponseDTO store = storeService.getStoreById(id);
            if (StoreStatus.ACTIVE.equals(store.getStatus())) {
                storeService.deactivateStore(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã ngưng hoạt động chi nhánh [" + store.getName() + "]!");
            } else {
                storeService.activateStore(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt lại chi nhánh [" + store.getName() + "]!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/stores";
    }
}

