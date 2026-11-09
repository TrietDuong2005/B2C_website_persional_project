package com.dahdt.ecommerce.modules.store.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.pos.dto.PosOrderSummaryDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosReceiptResponseDTO;
import com.dahdt.ecommerce.modules.pos.service.PosReceiptService;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Controller phục vụ các trang giao diện POS tại quầy (Thymeleaf):
 * 1. GET /pos: Màn hình bán hàng chính cho Thu ngân / Quản lý cửa hàng.
 * 2. GET /pos/receipts/{orderCode}: Xem và in phiếu hóa đơn bán lẻ 80mm.
 * 3. GET /pos/orders: Danh sách đơn hàng POS gần đây của chi nhánh.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class PosPageController {

    private final PosReceiptService posReceiptService;
    private final EmployeeRepository employeeRepository;

    /**
     * Hiển thị màn hình bán hàng tại quầy POS.
     * Nếu là ADMIN không thuộc chi nhánh nào, hiển thị thông báo thân thiện kèm nút về Admin.
     */
    @GetMapping("/pos")
    public String posHome(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

        Optional<Employee> empOpt = (userId != null) ? employeeRepository.findByUserId(userId) : Optional.empty();

        if (empOpt.isEmpty() || empOpt.get().getStore() == null || !empOpt.get().isActive()) {
            model.addAttribute("adminWithoutStore", true);
            model.addAttribute("cashierName", getDisplayName(userDetails));
            return "pos/index";
        }

        Employee employee = empOpt.get();
        model.addAttribute("adminWithoutStore", false);
        model.addAttribute("storeName", employee.getStore().getName());
        model.addAttribute("storeAddress", employee.getStore().getFullAddress());
        model.addAttribute("storePhone", employee.getStore().getPhone());
        model.addAttribute("cashierName", getDisplayName(userDetails));
        model.addAttribute("cashierEmail", userDetails.getUsername());
        model.addAttribute("isManager", employee.isManager());

        return "pos/index";
    }

    /**
     * Xem và in phiếu hóa đơn bán lẻ 80mm theo mã đơn hàng.
     * Quy tắc 14: Thu ngân chỉ xem được hóa đơn thuộc cửa hàng mình, vi phạm ném AccessDeniedException (403).
     */
    @GetMapping("/pos/receipts/{orderCode}")
    public String posReceipt(@PathVariable("orderCode") String orderCode,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;
        PosReceiptResponseDTO receipt = posReceiptService.getReceipt(userId, orderCode);
        model.addAttribute("receipt", receipt);
        return "pos/receipt";
    }

    /**
     * Danh sách các đơn bán hàng POS gần đây tại chi nhánh của người đăng nhập (phân trang 15 dòng).
     */
    @GetMapping("/pos/orders")
    public String posRecentOrders(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 Model model) {
        Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;
        Optional<Employee> empOpt = (userId != null) ? employeeRepository.findByUserId(userId) : Optional.empty();

        if (empOpt.isEmpty() || empOpt.get().getStore() == null || !empOpt.get().isActive()) {
            model.addAttribute("adminWithoutStore", true);
            model.addAttribute("orders", Page.empty());
            model.addAttribute("cashierName", getDisplayName(userDetails));
            return "pos/order-list";
        }

        Employee employee = empOpt.get();
        Page<PosOrderSummaryDTO> orders = posReceiptService.getRecentOrders(userId, page, 15);

        model.addAttribute("adminWithoutStore", false);
        model.addAttribute("orders", orders);
        model.addAttribute("storeName", employee.getStore().getName());
        model.addAttribute("cashierName", getDisplayName(userDetails));
        model.addAttribute("isManager", employee.isManager());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "pos/order-list";
    }

    private String getDisplayName(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUser() != null) {
            if (userDetails.getUser().getProfile() != null &&
                    userDetails.getUser().getProfile().getFullName() != null &&
                    !userDetails.getUser().getProfile().getFullName().isBlank()) {
                return userDetails.getUser().getProfile().getFullName();
            }
            return userDetails.getUsername();
        }
        return "Thu ngân";
    }
}
