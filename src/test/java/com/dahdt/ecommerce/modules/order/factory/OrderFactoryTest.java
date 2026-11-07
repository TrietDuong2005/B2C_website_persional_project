package com.dahdt.ecommerce.modules.order.factory;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử nhà máy tạo đơn hàng (OrderFactory)")
class OrderFactoryTest {

    private OrderRepository orderRepository;
    private OrderCodeGenerator codeGenerator;
    private OrderFactory orderFactory;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        codeGenerator = new OrderCodeGenerator(orderRepository);
        orderFactory = new OrderFactory(codeGenerator);
    }

    @Test
    @DisplayName("createOnlineOrder: Khởi tạo đúng kênh ONLINE, trạng thái PENDING, snapshot đầy đủ")
    void testCreateOnlineOrder_Success() {
        Store store = Store.builder().id(1L).code("WH-TONG").name("Kho Tổng Triết Store").build();
        User customer = User.builder().id(20L).email("customer@trietstore.vn").build();

        Order order = orderFactory.createOnlineOrder(
                store,
                customer,
                "Nguyễn Văn A",
                "0901234567",
                "Số 123 Đường Lê Lợi, Quận 1, TP.HCM",
                new BigDecimal("10.776889"),
                new BigDecimal("106.700806"),
                "Giao giờ hành chính"
        );

        assertNotNull(order);
        assertNotNull(order.getOrderCode());
        assertTrue(order.getOrderCode().startsWith("DH"));
        assertEquals(OrderChannel.ONLINE, order.getChannel());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(store, order.getStore());
        assertEquals(customer, order.getCustomer());
        assertNull(order.getCashier());

        // Snapshot nhận hàng
        assertEquals("Nguyễn Văn A", order.getShipName());
        assertEquals("0901234567", order.getShipPhone());
        assertEquals("Số 123 Đường Lê Lợi, Quận 1, TP.HCM", order.getShipAddress());
        assertEquals(0, new BigDecimal("10.776889").compareTo(order.getShipLatitude()));
        assertEquals(0, new BigDecimal("106.700806").compareTo(order.getShipLongitude()));
        assertEquals("Giao giờ hành chính", order.getNote());
        assertNotNull(order.getPlacedAt());
        assertNull(order.getCompletedAt());

        // Giá trị ban đầu
        assertMoneyEquals(BigDecimal.ZERO, order.getSubtotal());
        assertMoneyEquals(BigDecimal.ZERO, order.getDiscountTotal());
        assertMoneyEquals(BigDecimal.ZERO, order.getShippingFee());
        assertMoneyEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }

    @Test
    @DisplayName("OrderCodeGenerator: Sinh mã có định dạng chuẩn DH + yyyyMMdd + 4 chữ số")
    void testOrderCodeFormat() {
        String code = codeGenerator.generateUniqueOrderCode();
        assertNotNull(code);
        // Định dạng DH + 8 ký tự ngày (yyyyMMdd) + 4 chữ số = 14 ký tự
        assertTrue(code.matches("^DH\\d{12}$"), "Mã đơn hàng phải khớp regex DH + 8 số ngày + 4 số: " + code);
    }
}

