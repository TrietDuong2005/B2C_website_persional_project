package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.order.state.OrderState;
import com.dahdt.ecommerce.modules.order.state.OrderStateFactory;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

/**
 * Controller phục vụ trang Quản lý đơn hàng đa kênh trên Admin (/admin/orders).
 * Phân quyền URL: Dành cho Quản trị viên (ADMIN) và Quản lý cửa hàng (STORE_MANAGER).
 * Áp dụng mô hình chuẩn Post-Redirect-Get (PRG), tích hợp bảo vệ CSRF và modal xác nhận.
 */
@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderPageController {

    private final OrderStatusService orderStatusService;
    private final OrderStateFactory orderStateFactory;
    private final StoreScopeService storeScopeService;
    private final StoreRepository storeRepository;

    /**
     * Hiển thị danh sách đơn hàng đa kênh với bộ lọc nâng cao và phân trang (15 dòng/trang).
     * - STORE_MANAGER chỉ thấy đơn hàng của đúng chi nhánh mình phụ trách.
     * - ADMIN thấy toàn bộ đơn hàng của toàn chuỗi và có quyền lọc theo chi nhánh.
     */
    @GetMapping
    public String listOrders(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "channel", required = false) OrderChannel channel,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {

        boolean isStoreManager = storeScopeService.isCurrentStoreManager();
        Long filterStoreId = storeId;

        if (isStoreManager) {
            filterStoreId = storeScopeService.getAssignedStoreId();
            if (filterStoreId == null) {
                throw new AccessDeniedException("Tài khoản của bạn chưa được phân công chi nhánh!");
            }
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        Page<OrderResponseDTO> orderPage = orderStatusService.getOrdersForAdmin(
                filterStoreId, status, channel, keyword, startDate, endDate, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("selectedStoreId", filterStoreId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("allChannels", OrderChannel.values());
        model.addAttribute("isStoreManager", isStoreManager);

        if (!isStoreManager) {
            model.addAttribute("stores", storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE));
        }

        return "admin/order/order-list";
    }

    /**
     * Xem chi tiết một đơn hàng: snapshot sản phẩm, tạm tính, giảm giá, phí ship, tổng tiền,
     * thông tin nhận hàng/cửa hàng bán, thanh toán và dòng thời gian lịch sử trạng thái.
     * Chỉ hiển thị các nút thao tác hợp lệ theo State Pattern.
     */
    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id, Model model) {
        OrderDetailResponseDTO order = orderStatusService.getOrderDetailForAdmin(id);

        boolean isStoreManager = storeScopeService.isCurrentStoreManager();
        boolean isAdmin = storeScopeService.isCurrentAdmin();
        boolean isPos = (order.getChannel() == OrderChannel.POS);

        boolean canChangeStatus = false;
        boolean canCancel = false;
        Set<OrderStatus> validNextStatuses = Collections.emptySet();

        // Đơn POS chỉ xem (không đổi trạng thái, không hủy)
        // Đơn ONLINE do kho tổng phụ trách nên chỉ ADMIN mới có quyền đổi trạng thái hoặc hủy
        if (!isPos && isAdmin) {
            canChangeStatus = true;
            OrderState currentState = orderStateFactory.getState(order.getStatus());
            validNextStatuses = currentState.getValidNextStatuses();
            canCancel = currentState.canCancel();
        }

        model.addAttribute("order", order);
        model.addAttribute("isPos", isPos);
        model.addAttribute("isStoreManager", isStoreManager);
        model.addAttribute("canChangeStatus", canChangeStatus);
        model.addAttribute("canCancel", canCancel);
        model.addAttribute("validNextStatuses", validNextStatuses);

        return "admin/order/order-detail";
    }

    /**
     * Đổi trạng thái đơn hàng sang trạng thái kế tiếp hợp lệ.
     * Áp dụng Post-Redirect-Get và hiển thị flash message thân thiện.
     */
    @PostMapping("/{id}/status")
    public String changeOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("targetStatus") OrderStatus targetStatus,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes) {

        User currentUser = storeScopeService.getCurrentUser();
        Long actorId = (currentUser != null) ? currentUser.getId() : null;

        try {
            orderStatusService.changeStatus(id, targetStatus, actorId, note);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Chuyển trạng thái đơn hàng sang [" + targetStatus + "] thành công!");
        } catch (BadRequestException | AccessDeniedException ex) {
            log.warn("Không thể chuyển trạng thái đơn hàng [{}]: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi khi chuyển trạng thái đơn hàng [{}]: ", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Đã xảy ra lỗi khi cập nhật trạng thái đơn hàng: " + ex.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    /**
     * Hủy đơn hàng và thực thi quy trình hoàn kho qua Observer Pattern.
     * Áp dụng Post-Redirect-Get và hiển thị flash message thân thiện.
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        User currentUser = storeScopeService.getCurrentUser();
        Long actorId = (currentUser != null) ? currentUser.getId() : null;

        try {
            String cancelNote = (reason != null && !reason.trim().isBlank())
                    ? reason.trim()
                    : "Quản trị viên hủy đơn hàng";

            orderStatusService.changeStatus(id, OrderStatus.CANCELLED, actorId, cancelNote);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy đơn hàng thành công! Số lượng hàng đã được tự động hoàn lại vào kho.");
        } catch (BadRequestException | AccessDeniedException ex) {
            log.warn("Không thể hủy đơn hàng [{}]: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi khi hủy đơn hàng [{}]: ", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Đã xảy ra lỗi khi hủy đơn hàng: " + ex.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}

