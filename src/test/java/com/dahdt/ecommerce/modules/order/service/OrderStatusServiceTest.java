package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.*;
import com.dahdt.ecommerce.modules.order.event.OrderCancelledEvent;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.state.*;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử OrderStatusService: Điều phối vòng đời, hoàn kho và phân quyền đơn hàng")
class OrderStatusServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private PaymentRepository paymentRepository;
    private VoucherRepository voucherRepository;
    private UserRepository userRepository;
    private OrderStateFactory orderStateFactory;
    private ApplicationEventPublisher eventPublisher;
    private StoreScopeService storeScopeService;
    private OrderMapper orderMapper;

    private OrderStatusService service;

    private User adminUser;
    private User customerUser;
    private User customerUser2;
    private Store mainStore;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderStatusHistoryRepository = mock(OrderStatusHistoryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        voucherRepository = mock(VoucherRepository.class);
        userRepository = mock(UserRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        storeScopeService = mock(StoreScopeService.class);
        orderMapper = mock(OrderMapper.class);

        List<OrderState> states = List.of(
                new PendingState(),
                new ConfirmedState(),
                new PackingState(),
                new ShippingState(),
                new CompletedState(),
                new CancelledState()
        );
        orderStateFactory = new OrderStateFactory(states);

        service = new OrderStatusServiceImpl(
                orderRepository,
                orderStatusHistoryRepository,
                paymentRepository,
                voucherRepository,
                userRepository,
                orderStateFactory,
                eventPublisher,
                storeScopeService,
                orderMapper
        );

        adminUser = User.builder().id(1L).email("admin@dahdt.com").build();
        customerUser = User.builder().id(10L).email("customer@dahdt.com").build();
        customerUser2 = User.builder().id(11L).email("customer2@dahdt.com").build();
        mainStore = Store.builder().id(100L).code("WH-TONG").name("Kho Tổng").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(userRepository.findById(11L)).thenReturn(Optional.of(customerUser2));
    }

    // =========================================================================
    // 1. CHUYỂN TRẠNG THÁI HỢP LỆ VÀ GHI LỊCH SỬ
    // =========================================================================
    @Test
    @DisplayName("changeStatus: PENDING -> CONFIRMED ghi nhật ký lịch sử đầy đủ")
    void testChangeStatus_PendingToConfirmed() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .orderCode("DH001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(mainStore)
                .customer(customerUser)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusConditionally(eq(10L), eq(OrderStatus.PENDING), eq(OrderStatus.CONFIRMED), any(), any()))
                .thenReturn(1);

        service.changeStatus(10L, OrderStatus.CONFIRMED, 1L, "Admin duyệt đơn");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus(), "Trạng thái đơn phải cập nhật sang CONFIRMED");

        ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository, times(1)).save(historyCaptor.capture());

        OrderStatusHistory history = historyCaptor.getValue();
        assertEquals(OrderStatus.PENDING, history.getFromStatus());
        assertEquals(OrderStatus.CONFIRMED, history.getToStatus());
        assertEquals(adminUser, history.getChangedBy());
        assertEquals("Admin duyệt đơn", history.getNote());
    }

    // =========================================================================
    // 2. CHUYỂN SANG COMPLETED: CẬP NHẬT completed_at VÀ COD CHUYỂN PAID
    // =========================================================================
    @Test
    @DisplayName("changeStatus: SHIPPING -> COMPLETED cập nhật completed_at và COD payment chuyển PAID")
    void testChangeStatus_CompletedUpdatesPayment() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .orderCode("DH001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.SHIPPING)
                .store(mainStore)
                .customer(customerUser)
                .build();

        Payment codPayment = Payment.builder()
                .id(50L)
                .order(order)
                .method(PaymentMethod.COD)
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal("500000"))
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusConditionally(eq(10L), eq(OrderStatus.SHIPPING), eq(OrderStatus.COMPLETED), any(), any()))
                .thenReturn(1);
        when(paymentRepository.findByOrderId(10L)).thenReturn(List.of(codPayment));

        service.changeStatus(10L, OrderStatus.COMPLETED, 1L, "Giao thành công");

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedAt(), "completedAt phải được cập nhật thời điểm hiện tại");
        assertEquals(PaymentStatus.PAID, codPayment.getStatus(), "Thanh toán COD phải chuyển sang PAID");
        assertNotNull(codPayment.getPaidAt(), "paidAt phải được gán thời điểm giao thành công");
        verify(paymentRepository, times(1)).save(codPayment);
    }

    // =========================================================================
    // 3. HỦY ĐƠN: PHÁT EVENT, HOÀN TIỀN VÀ HOÀN LƯỢT VOUCHER
    // =========================================================================
    @Test
    @DisplayName("changeStatus: CANCELLED phát OrderCancelledEvent, hoàn tiền và hoàn lượt voucher")
    void testChangeStatus_CancelledSideEffects() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(20L)
                .orderCode("DH002")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(mainStore)
                .customer(customerUser)
                .voucherCode("GIAM50K")
                .build();

        Payment paidPayment = Payment.builder()
                .id(51L)
                .order(order)
                .method(PaymentMethod.EWALLET_MOCK)
                .status(PaymentStatus.PAID)
                .amount(new BigDecimal("300000"))
                .build();

        Voucher voucher = Voucher.builder()
                .id(99L)
                .code("GIAM50K")
                .usedCount(5)
                .build();

        when(orderRepository.findByIdWithDetails(20L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusConditionally(eq(20L), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), any(), any()))
                .thenReturn(1);
        when(paymentRepository.findByOrderId(20L)).thenReturn(List.of(paidPayment));
        when(voucherRepository.findByCode("GIAM50K")).thenReturn(Optional.of(voucher));
        when(voucherRepository.decrementUsedCountConditionally(eq(99L), any())).thenReturn(1);

        service.changeStatus(20L, OrderStatus.CANCELLED, 1L, "Khách yêu cầu hủy");

        // 1. Trạng thái order là CANCELLED
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // 2. Thanh toán PAID chuyển sang REFUNDED
        assertEquals(PaymentStatus.REFUNDED, paidPayment.getStatus(), "Payment PAID phải chuyển sang REFUNDED");
        verify(paymentRepository, times(1)).save(paidPayment);

        // 3. Voucher usedCount giảm nguyên tử
        verify(voucherRepository, times(1)).decrementUsedCountConditionally(eq(99L), any());

        // 4. Phát sự kiện OrderCancelledEvent để Observer hoàn kho
        verify(eventPublisher, times(1)).publishEvent(any(OrderCancelledEvent.class));
    }

    // =========================================================================
    // 4. CHUYỂN TRẠNG THÁI KHÔNG HỢP LỆ BỊ TỪ CHỐI
    // =========================================================================
    @Test
    @DisplayName("changeStatus: Chuyển sai quy tắc (nhảy cóc PENDING -> SHIPPING) bị từ chối")
    void testChangeStatus_InvalidTransition() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.changeStatus(10L, OrderStatus.SHIPPING, 1L, "Nhảy cóc"));
        assertTrue(ex.getMessage().contains("Không thể chuyển trạng thái"), "Thông báo lỗi: " + ex.getMessage());

        verify(orderRepository, never()).updateStatusConditionally(any(), any(), any(), any(), any());
    }

    // =========================================================================
    // 5. ĐƠN POS BỊ TỪ CHỐI MỌI THAO TÁC ĐỔI TRẠNG THÁI
    // =========================================================================
    @Test
    @DisplayName("changeStatus: Đơn hàng POS bị từ chối chuyển đổi hoặc hủy với thông báo rõ")
    void testChangeStatus_PosOrderRejected() {
        Order posOrder = Order.builder()
                .id(30L)
                .orderCode("POS001")
                .channel(OrderChannel.POS)
                .status(OrderStatus.COMPLETED)
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(30L)).thenReturn(Optional.of(posOrder));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.changeStatus(30L, OrderStatus.CANCELLED, 1L, "Hủy đơn POS"));
        assertTrue(ex.getMessage().contains("Đơn hàng tại quầy POS đã hoàn tất"), "Thông báo lỗi: " + ex.getMessage());

        verify(orderRepository, never()).updateStatusConditionally(any(), any(), any(), any(), any());
    }

    // =========================================================================
    // 6. CHỐNG ĐỔI HAI LẦN CÙNG LÚC (RACE CONDITION)
    // =========================================================================
    @Test
    @DisplayName("changeStatus: affectedRows == 0 do tranh chấp đồng thời ném BadRequestException")
    void testChangeStatus_ConcurrencyConflict() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));
        // Giả lập câu UPDATE trả về 0 dòng do tab khác đã đổi trước
        when(orderRepository.updateStatusConditionally(eq(10L), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), any(), any()))
                .thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.changeStatus(10L, OrderStatus.CANCELLED, 1L, "Hủy lần hai"));
        assertTrue(ex.getMessage().contains("thay đổi trạng thái bởi một thao tác khác"), "Thông báo lỗi: " + ex.getMessage());

        // Hiệu ứng phụ không được chạy
        verify(eventPublisher, never()).publishEvent(any());
        verify(voucherRepository, never()).decrementUsedCountConditionally(any(), any());
    }

    // =========================================================================
    // 7. PHÂN QUYỀN STORE_MANAGER KHÔNG ĐƯỢC ĐỔI ĐƠN ONLINE
    // =========================================================================
    @Test
    @DisplayName("changeStatus: STORE_MANAGER đổi đơn online bị chặn ném AccessDeniedException")
    void testChangeStatus_StoreManagerOnlineDenied() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(true);

        Order order = Order.builder()
                .id(10L)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
                () -> service.changeStatus(10L, OrderStatus.CONFIRMED, 2L, "Quản lý cửa hàng đổi đơn"));

        verify(orderRepository, never()).updateStatusConditionally(any(), any(), any(), any(), any());
    }

    // =========================================================================
    // 8. KHÁCH HÀNG TỰ HỦY ĐƠN: CHÍNH CHỦ VÀ PENDING THÀNH CÔNG, NGƯỜI KHÁC BỊ CHẶN
    // =========================================================================
    @Test
    @DisplayName("changeStatus: Khách hàng tự hủy đơn PENDING của chính mình thành công")
    void testChangeStatus_CustomerSelfCancelSuccess() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(false);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .customer(customerUser)
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusConditionally(eq(10L), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), any(), any()))
                .thenReturn(1);

        service.changeStatus(10L, OrderStatus.CANCELLED, 10L, "Khách tự hủy");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(OrderCancelledEvent.class));
    }

    @Test
    @DisplayName("changeStatus: Khách hàng cố tình hủy đơn của khách khác bị chặn AccessDeniedException")
    void testChangeStatus_CustomerCancelOtherDenied() {
        when(storeScopeService.isCurrentAdmin()).thenReturn(false);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        Order order = Order.builder()
                .id(10L)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .customer(customerUser) // Đơn của user 10
                .store(mainStore)
                .build();

        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(order));

        // User 11 cố hủy đơn của User 10
        assertThrows(AccessDeniedException.class,
                () -> service.changeStatus(10L, OrderStatus.CANCELLED, 11L, "Xâm nhập"));
    }
}

