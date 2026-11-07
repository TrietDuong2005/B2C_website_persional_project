package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ truy vấn đơn hàng (OrderQueryService)")
class OrderQueryServiceTest {

    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private OrderMapper orderMapper;
    private OrderQueryService orderQueryService;

    private User customer1;
    private User customer2;
    private Store store;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        orderStatusHistoryRepository = mock(OrderStatusHistoryRepository.class);
        orderMapper = Mappers.getMapper(OrderMapper.class);

        orderQueryService = new OrderQueryServiceImpl(
                orderRepository,
                paymentRepository,
                orderStatusHistoryRepository,
                orderMapper
        );

        customer1 = User.builder().id(1L).email("user1@trietstore.vn").build();
        customer2 = User.builder().id(2L).email("user2@trietstore.vn").build();
        store = Store.builder().id(10L).code("STORE-Q1").name("Cửa hàng Quận 1").build();
    }

    @Test
    @DisplayName("getCustomerOrders: Lấy danh sách đơn của khách hàng có phân trang thành công")
    void testGetCustomerOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = Order.builder()
                .id(100L)
                .orderCode("DH202611070001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .customer(customer1)
                .subtotal(new BigDecimal("500000"))
                .totalAmount(new BigDecimal("530000"))
                .placedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerIdOrderByPlacedAtDesc(1L, pageable)).thenReturn(orderPage);

        Page<OrderResponseDTO> result = orderQueryService.getCustomerOrders(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("DH202611070001", result.getContent().get(0).getOrderCode());
    }

    @Test
    @DisplayName("getCustomerOrderDetail: Lấy chi tiết đơn hàng thành công khi đúng chủ sở hữu")
    void testGetCustomerOrderDetail_OwnerSuccess() {
        Order order = Order.builder()
                .id(100L)
                .orderCode("DH202611070001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .customer(customer1)
                .store(store)
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .statusHistories(new ArrayList<>())
                .build();

        when(orderRepository.findByOrderCodeWithDetails("DH202611070001")).thenReturn(Optional.of(order));

        OrderDetailResponseDTO detail = orderQueryService.getCustomerOrderDetail(1L, "DH202611070001");

        assertNotNull(detail);
        assertEquals("DH202611070001", detail.getOrderCode());
        assertEquals(10L, detail.getStoreId());
    }

    @Test
    @DisplayName("getCustomerOrderDetail: Ném ResourceNotFoundException khi khách truy cập đơn của người khác")
    void testGetCustomerOrderDetail_DifferentCustomer_ThrowsNotFound() {
        // Đơn thuộc về customer2
        Order order = Order.builder()
                .id(200L)
                .orderCode("DH202611070002")
                .customer(customer2)
                .build();

        when(orderRepository.findByOrderCodeWithDetails("DH202611070002")).thenReturn(Optional.of(order));

        // Customer1 cố gắng xem đơn của Customer2
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderQueryService.getCustomerOrderDetail(1L, "DH202611070002"));

        // Tuyệt đối không để lộ thông tin đơn hàng tồn tại
        assertTrue(ex.getMessage().contains("Không tìm thấy đơn hàng"));
    }
}

