package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosOrderSummaryDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosReceiptItemDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosReceiptResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ trích xuất hóa đơn bán lẻ POS phục vụ in ấn.
 * Áp dụng Store Scoping phân quyền chặt chẽ giữa các chi nhánh theo Quy tắc 14 của Project Brief.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PosReceiptServiceImpl implements PosReceiptService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    public PosReceiptResponseDTO getReceipt(Long userId, String orderCode) {
        if (orderCode == null || orderCode.trim().isBlank()) {
            throw new ResourceNotFoundException("Mã đơn hàng không được để trống.");
        }

        // 1. Tìm đơn hàng kèm thông tin chi tiết các mặt hàng snapshot (chống N+1)
        Order order = orderRepository.findByOrderCodeWithDetails(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // 2. Kiểm soát phân quyền truy cập hóa đơn theo Store Scoping
        validateReceiptAccess(userId, order);

        // 3. Lấy giao dịch thanh toán mới nhất của đơn hàng
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                .orElse(null);

        // 4. Lấy thông tin họ tên thu ngân
        String cashierName = "Thu ngân";
        if (order.getCashier() != null) {
            User cashier = order.getCashier();
            if (cashier.getProfile() != null && cashier.getProfile().getFullName() != null && !cashier.getProfile().getFullName().isBlank()) {
                cashierName = cashier.getProfile().getFullName();
            } else if (cashier.getEmail() != null) {
                cashierName = cashier.getEmail();
            }
        }

        // 5. Chuyển đổi danh sách snapshot mặt hàng sang DTO hóa đơn
        List<PosReceiptItemDTO> receiptItems = (order.getItems() != null)
                ? order.getItems().stream()
                .map(item -> PosReceiptItemDTO.builder()
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .color(item.getColor())
                        .size(item.getSize())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList()
                : Collections.emptyList();

        return PosReceiptResponseDTO.builder()
                .storeName(order.getStore() != null ? order.getStore().getName() : "")
                .storeAddress(order.getStore() != null ? order.getStore().getFullAddress() : "")
                .storePhone(order.getStore() != null ? order.getStore().getPhone() : "")
                .orderCode(order.getOrderCode())
                .orderTime(order.getCompletedAt() != null ? order.getCompletedAt() : order.getPlacedAt())
                .cashierName(cashierName)
                .items(receiptItems)
                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(payment != null ? payment.getMethod() : null)
                .cashReceived(payment != null ? payment.getCashReceived() : null)
                .changeAmount(payment != null ? payment.getChangeAmount() : null)
                .transactionRef(payment != null ? payment.getTransactionRef() : null)
                .note(order.getNote())
                .build();
    }

    @Override
    public Page<PosOrderSummaryDTO> getRecentOrders(Long userId, int page, int size) {
        if (userId == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Tài khoản không thuộc cửa hàng nào."));

        if (!employee.isActive()) {
            throw new BadRequestException("Tài khoản nhân viên đã bị khóa hoặc tạm dừng hoạt động.");
        }

        if (employee.getStore() == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        Long storeId = employee.getStore().getId();
        int pageIndex = Math.max(0, page);
        int pageSize = (size > 0) ? size : 15;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Order> orderPage = orderRepository.findByChannelAndStoreIdOrderByPlacedAtDesc(
                OrderChannel.POS, storeId, pageable);

        if (orderPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderIds = orderPage.getContent().stream().map(Order::getId).toList();
        List<Payment> payments = paymentRepository.findByOrderIdIn(orderIds);
        Map<Long, PaymentMethod> paymentMethodMap = payments.stream()
                .collect(Collectors.toMap(
                        p -> p.getOrder().getId(),
                        Payment::getMethod,
                        (p1, p2) -> p1
                ));

        List<PosOrderSummaryDTO> dtos = orderPage.getContent().stream()
                .map(order -> {
                    String cashierName = "Thu ngân";
                    if (order.getCashier() != null) {
                        User cashier = order.getCashier();
                        if (cashier.getProfile() != null && cashier.getProfile().getFullName() != null && !cashier.getProfile().getFullName().isBlank()) {
                            cashierName = cashier.getProfile().getFullName();
                        } else if (cashier.getEmail() != null) {
                            cashierName = cashier.getEmail();
                        }
                    }

                    LocalDateTime orderTime = order.getCompletedAt() != null ? order.getCompletedAt() : order.getPlacedAt();
                    PaymentMethod method = paymentMethodMap.get(order.getId());

                    return PosOrderSummaryDTO.builder()
                            .orderCode(order.getOrderCode())
                            .orderTime(orderTime)
                            .cashierName(cashierName)
                            .totalAmount(order.getTotalAmount())
                            .paymentMethod(method)
                            .receiptUrl("/pos/receipts/" + order.getOrderCode())
                            .build();
                })
                .toList();

        return new PageImpl<>(dtos, pageable, orderPage.getTotalElements());
    }

    private void validateReceiptAccess(Long userId, Order order) {
        if (userId == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để xem thông tin hóa đơn.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("Tài khoản người dùng không tồn tại."));

        // Nếu là ADMIN toàn hệ thống thì có quyền xem hóa đơn mọi chi nhánh
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()) || "ROLE_ADMIN".equalsIgnoreCase(r.getCode()));
        if (isAdmin) {
            return;
        }

        // Thu ngân hoặc Quản lý chi nhánh: phải có hồ sơ nhân viên và đúng chi nhánh
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không thuộc bất kỳ chi nhánh nào!"));

        if (employee.getStore() == null || order.getStore() == null ||
                !employee.getStore().getId().equals(order.getStore().getId())) {
            log.warn("Truy cập trái phép: Người dùng [{}] thuộc store [{}] đang cố xem hóa đơn [{}] của store [{}]",
                    userId, employee.getStore() != null ? employee.getStore().getId() : null,
                    order.getOrderCode(), order.getStore() != null ? order.getStore().getId() : null);
            throw new AccessDeniedException("Bạn không có quyền truy cập hóa đơn của chi nhánh khác!");
        }
    }
}

