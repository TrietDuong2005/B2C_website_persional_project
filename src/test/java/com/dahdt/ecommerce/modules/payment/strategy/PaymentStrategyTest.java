package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử các chiến lược thanh toán (PaymentStrategy & Registry)")
class PaymentStrategyTest {

    private CodPaymentStrategy codStrategy;
    private EWalletMockPaymentStrategy ewalletStrategy;
    private PaymentStrategyRegistry registry;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        codStrategy = new CodPaymentStrategy();
        ewalletStrategy = new EWalletMockPaymentStrategy();
        registry = new PaymentStrategyRegistry(List.of(codStrategy, ewalletStrategy));

        sampleOrder = Order.builder()
                .id(100L)
                .orderCode("DH202611070001")
                .totalAmount(new BigDecimal("750000"))
                .build();
    }

    @Test
    @DisplayName("CodPaymentStrategy: Khởi tạo thanh toán trạng thái PENDING cho đơn hàng")
    void testCodPaymentStrategy_Process() {
        Payment payment = codStrategy.processPayment(sampleOrder);

        assertNotNull(payment);
        assertEquals(sampleOrder, payment.getOrder());
        assertEquals(PaymentMethod.COD, payment.getMethod());
        assertMoneyEquals(new BigDecimal("750000"), payment.getAmount());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNull(payment.getTransactionRef());
        assertNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("EWalletMockPaymentStrategy: Giả lập thanh toán PAID ngay, có mã giao dịch và thời điểm")
    void testEWalletMockPaymentStrategy_Process() {
        Payment payment = ewalletStrategy.processPayment(sampleOrder);

        assertNotNull(payment);
        assertEquals(sampleOrder, payment.getOrder());
        assertEquals(PaymentMethod.EWALLET_MOCK, payment.getMethod());
        assertMoneyEquals(new BigDecimal("750000"), payment.getAmount());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertNotNull(payment.getTransactionRef());
        assertTrue(payment.getTransactionRef().startsWith("MOCK-WALLET-"));
        assertNotNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("PaymentStrategyRegistry: Chọn đúng Strategy trực tuyến cho COD và EWALLET_MOCK")
    void testRegistry_GetOnlineStrategy_Success() {
        assertSame(codStrategy, registry.getOnlineStrategy(PaymentMethod.COD));
        assertSame(ewalletStrategy, registry.getOnlineStrategy(PaymentMethod.EWALLET_MOCK));
    }

    @Test
    @DisplayName("PaymentStrategyRegistry: Ném BadRequestException nếu chọn phương thức không hỗ trợ online")
    void testRegistry_GetOnlineStrategy_UnsupportedMethod() {
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.CASH));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.CARD));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.QR));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(null));
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}

