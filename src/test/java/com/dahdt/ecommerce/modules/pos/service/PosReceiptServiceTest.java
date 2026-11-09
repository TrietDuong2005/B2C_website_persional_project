package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosOrderSummaryDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosReceiptResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử nghiệp vụ trích xuất hóa đơn POS (PosReceiptService)")
class PosReceiptServiceTest {

    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private EmployeeRepository employeeRepository;
    private UserRepository userRepository;

    private PosReceiptService posReceiptService;

    private Store storeQ1;
    private Store storeQ3;
    private User cashierUserQ1;
    private User cashierUserQ3;
    private User adminUser;
    private Order sampleOrderQ1;
    private Payment samplePaymentQ1;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        userRepository = mock(UserRepository.class);

        posReceiptService = new PosReceiptServiceImpl(
                orderRepository,
                paymentRepository,
                employeeRepository,
                userRepository
        );

        storeQ1 = Store.builder().id(1L).code("STORE-Q1").name("Triết Store - Quận 1")
                .detailAddress("125 Nguyễn Trãi").ward("Phường Bến Thành").district("Quận 1").province("TP.HCM")
                .phone("02838332222").build();

        storeQ3 = Store.builder().id(2L).code("STORE-Q3").name("Triết Store - Quận 3")
                .detailAddress("450 CMT8").ward("Phường 10").district("Quận 3").province("TP.HCM")
                .phone("02838333333").build();

        cashierUserQ1 = User.builder().id(10L).email("cashier1@dahdt.com")
                .profile(UserProfile.builder().fullName("Thu Ngân Q1").build())
                .roles(Set.of(Role.builder().code("CASHIER").build()))
                .build();

        cashierUserQ3 = User.builder().id(20L).email("cashier2@dahdt.com")
                .profile(UserProfile.builder().fullName("Thu Ngân Q3").build())
                .roles(Set.of(Role.builder().code("CASHIER").build()))
                .build();

        adminUser = User.builder().id(99L).email("admin@dahdt.com")
                .roles(Set.of(Role.builder().code("ADMIN").build()))
                .build();

        OrderItem item = OrderItem.builder()
                .productName("Giày Sneaker Triết")
                .sku("SNK-01-42")
                .color("Trắng")
                .size("42")
                .unitPrice(new BigDecimal("500000"))
                .quantity(2)
                .lineTotal(new BigDecimal("1000000"))
                .build();

        sampleOrderQ1 = Order.builder()
                .id(100L)
                .orderCode("HD202611080001")
                .store(storeQ1)
                .cashier(cashierUserQ1)
                .items(List.of(item))
                .subtotal(new BigDecimal("1000000"))
                .discountTotal(new BigDecimal("50000"))
                .totalAmount(new BigDecimal("950000"))
                .placedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        samplePaymentQ1 = Payment.builder()
                .id(1L)
                .order(sampleOrderQ1)
                .method(PaymentMethod.CASH)
                .amount(new BigDecimal("950000"))
                .status(PaymentStatus.PAID)
                .cashReceived(new BigDecimal("1000000"))
                .changeAmount(new BigDecimal("50000"))
                .paidAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getReceipt: Thu ngân xem hóa đơn đúng cửa hàng mình -> Trả đầy đủ dữ liệu")
    void testGetReceipt_SameStore_Success() {
        when(orderRepository.findByOrderCodeWithDetails("HD202611080001")).thenReturn(Optional.of(sampleOrderQ1));
        when(userRepository.findById(10L)).thenReturn(Optional.of(cashierUserQ1));

        Employee empQ1 = Employee.builder().user(cashierUserQ1).store(storeQ1).position(EmployeePosition.CASHIER).status(EmployeeStatus.ACTIVE).build();
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(empQ1));
        when(paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(samplePaymentQ1));

        PosReceiptResponseDTO receipt = posReceiptService.getReceipt(10L, "HD202611080001");

        assertNotNull(receipt);
        assertEquals("Triết Store - Quận 1", receipt.getStoreName());
        assertTrue(receipt.getStoreAddress().contains("125 Nguyễn Trãi"));
        assertEquals("02838332222", receipt.getStorePhone());
        assertEquals("HD202611080001", receipt.getOrderCode());
        assertEquals("Thu Ngân Q1", receipt.getCashierName());
        assertEquals(1, receipt.getItems().size());
        assertEquals(0, new BigDecimal("1000000").compareTo(receipt.getSubtotal()));
        assertEquals(0, new BigDecimal("50000").compareTo(receipt.getDiscountTotal()));
        assertEquals(0, new BigDecimal("950000").compareTo(receipt.getTotalAmount()));
        assertEquals(PaymentMethod.CASH, receipt.getPaymentMethod());
        assertEquals(0, new BigDecimal("1000000").compareTo(receipt.getCashReceived()));
        assertEquals(0, new BigDecimal("50000").compareTo(receipt.getChangeAmount()));
    }

    @Test
    @DisplayName("getReceipt: Thu ngân xem hóa đơn của cửa hàng khác -> Ném AccessDeniedException (Quy tắc 14)")
    void testGetReceipt_OtherStore_ThrowsAccessDeniedException() {
        when(orderRepository.findByOrderCodeWithDetails("HD202611080001")).thenReturn(Optional.of(sampleOrderQ1));
        when(userRepository.findById(20L)).thenReturn(Optional.of(cashierUserQ3));

        Employee empQ3 = Employee.builder().user(cashierUserQ3).store(storeQ3).position(EmployeePosition.CASHIER).status(EmployeeStatus.ACTIVE).build();
        when(employeeRepository.findByUserId(20L)).thenReturn(Optional.of(empQ3));

        assertThrows(AccessDeniedException.class, () -> posReceiptService.getReceipt(20L, "HD202611080001"));
    }

    @Test
    @DisplayName("getReceipt: Quản trị viên (ADMIN) được phép xem hóa đơn của bất kỳ cửa hàng nào")
    void testGetReceipt_Admin_CanViewAnyStore() {
        when(orderRepository.findByOrderCodeWithDetails("HD202611080001")).thenReturn(Optional.of(sampleOrderQ1));
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
        when(paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(samplePaymentQ1));

        PosReceiptResponseDTO receipt = posReceiptService.getReceipt(99L, "HD202611080001");

        assertNotNull(receipt);
        assertEquals("Triết Store - Quận 1", receipt.getStoreName());
        assertEquals("HD202611080001", receipt.getOrderCode());
    }

    @Test
    @DisplayName("getReceipt: Mã đơn không tồn tại -> Ném ResourceNotFoundException")
    void testGetReceipt_NotFound_ThrowsException() {
        when(orderRepository.findByOrderCodeWithDetails("HD999999999999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> posReceiptService.getReceipt(10L, "HD999999999999"));
    }

    @Test
    @DisplayName("getRecentOrders: Chỉ trả đơn POS của đúng cửa hàng người đăng nhập, sắp xếp mới nhất trước, phân trang đúng")
    void testGetRecentOrders_SameStore_Success() {
        Employee empQ1 = Employee.builder()
                .user(cashierUserQ1)
                .store(storeQ1)
                .position(EmployeePosition.CASHIER)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(empQ1));

        Order order1 = Order.builder()
                .id(101L)
                .orderCode("HD202611080002")
                .channel(OrderChannel.POS)
                .store(storeQ1)
                .cashier(cashierUserQ1)
                .totalAmount(new BigDecimal("1500000"))
                .placedAt(LocalDateTime.of(2026, 11, 8, 15, 0))
                .completedAt(LocalDateTime.of(2026, 11, 8, 15, 0))
                .build();

        Order order2 = Order.builder()
                .id(100L)
                .orderCode("HD202611080001")
                .channel(OrderChannel.POS)
                .store(storeQ1)
                .cashier(cashierUserQ1)
                .totalAmount(new BigDecimal("950000"))
                .placedAt(LocalDateTime.of(2026, 11, 8, 14, 0))
                .completedAt(LocalDateTime.of(2026, 11, 8, 14, 0))
                .build();

        Page<Order> mockPage = new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 15), 2);

        when(orderRepository.findByChannelAndStoreIdOrderByPlacedAtDesc(
                eq(OrderChannel.POS),
                eq(1L),
                eq(PageRequest.of(0, 15))
        )).thenReturn(mockPage);

        Payment payment1 = Payment.builder().id(11L).order(order1).method(PaymentMethod.CARD).build();
        Payment payment2 = Payment.builder().id(12L).order(order2).method(PaymentMethod.CASH).build();

        when(paymentRepository.findByOrderIdIn(eq(List.of(101L, 100L))))
                .thenReturn(List.of(payment1, payment2));

        Page<PosOrderSummaryDTO> result = posReceiptService.getRecentOrders(10L, 0, 15);

        assertNotNull(result, "Kết quả phân trang không được null");
        assertEquals(2, result.getContent().size(), "Số lượng đơn hàng trả về phải là 2 (thực tế: " + result.getContent().size() + ")");
        assertEquals(2, result.getTotalElements(), "Tổng số bản ghi phải là 2 (thực tế: " + result.getTotalElements() + ")");
        assertEquals(1, result.getTotalPages(), "Tổng số trang phải là 1 (thực tế: " + result.getTotalPages() + ")");

        // Kiểm tra thứ tự mới nhất trước: order1 (15h) trước order2 (14h)
        PosOrderSummaryDTO firstItem = result.getContent().get(0);
        assertEquals("HD202611080002", firstItem.getOrderCode(), "Đơn mới nhất (HD202611080002) phải đứng đầu danh sách");
        assertEquals("Thu Ngân Q1", firstItem.getCashierName(), "Tên thu ngân phải là 'Thu Ngân Q1'");
        assertEquals(0, new BigDecimal("1500000").compareTo(firstItem.getTotalAmount()), "Tổng tiền đơn 1 phải là 1.500.000");
        assertEquals(PaymentMethod.CARD, firstItem.getPaymentMethod(), "Phương thức thanh toán phải là CARD");
        assertEquals("/pos/receipts/HD202611080002", firstItem.getReceiptUrl(), "Đường dẫn hóa đơn phải đúng định dạng");

        PosOrderSummaryDTO secondItem = result.getContent().get(1);
        assertEquals("HD202611080001", secondItem.getOrderCode(), "Đơn thứ 2 phải là HD202611080001");
        assertEquals(PaymentMethod.CASH, secondItem.getPaymentMethod(), "Phương thức thanh toán đơn 2 phải là CASH");
    }

    @Test
    @DisplayName("getRecentOrders: Tài khoản không có hồ sơ cửa hàng (Admin) -> Ném BadRequestException")
    void testGetRecentOrders_NoStore_ThrowsBadRequestException() {
        when(employeeRepository.findByUserId(99L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posReceiptService.getRecentOrders(99L, 0, 15),
                "Phải ném BadRequestException khi tài khoản không thuộc cửa hàng nào");

        assertTrue(ex.getMessage().contains("Tài khoản không thuộc cửa hàng nào"),
                "Thông điệp phải chứa 'Tài khoản không thuộc cửa hàng nào' (thực tế: " + ex.getMessage() + ")");
    }
}

