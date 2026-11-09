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
    private CashPaymentStrategy cashStrategy;
    private CardPaymentStrategy cardStrategy;
    private QrPaymentStrategy qrStrategy;
    private PaymentStrategyRegistry registry;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        codStrategy = new CodPaymentStrategy();
        ewalletStrategy = new EWalletMockPaymentStrategy();
        cashStrategy = new CashPaymentStrategy();
        cardStrategy = new CardPaymentStrategy();
        qrStrategy = new QrPaymentStrategy();

        registry = new PaymentStrategyRegistry(List.of(
                codStrategy,
                ewalletStrategy,
                cashStrategy,
                cardStrategy,
                qrStrategy
        ));

        sampleOrder = Order.builder()
                .id(100L)
                .orderCode("DH202611070001")
                .totalAmount(new BigDecimal("750000"))
                .build();
    }

    @Test
    @DisplayName("CodPaymentStrategy: Khởi tạo thanh toán trạng thái PENDING cho đơn hàng online")
    void testCodPaymentStrategy_Process() {
        assertTrue(codStrategy.isSupportedOnline());
        assertFalse(codStrategy.isSupportedPos());

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
        assertTrue(ewalletStrategy.isSupportedOnline());
        assertFalse(ewalletStrategy.isSupportedPos());

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
    @DisplayName("CashPaymentStrategy: Đủ tiền -> PAID, tính đúng tiền thừa")
    void testCashPaymentStrategy_SufficientCash() {
        assertFalse(cashStrategy.isSupportedOnline());
        assertTrue(cashStrategy.isSupportedPos());

        BigDecimal cashReceived = new BigDecimal("800000");
        Payment payment = cashStrategy.processPayment(sampleOrder, cashReceived);

        assertNotNull(payment);
        assertEquals(sampleOrder, payment.getOrder());
        assertEquals(PaymentMethod.CASH, payment.getMethod());
        assertMoneyEquals(new BigDecimal("750000"), payment.getAmount());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertMoneyEquals(cashReceived, payment.getCashReceived());
        assertMoneyEquals(new BigDecimal("50000"), payment.getChangeAmount());
        assertNotNull(payment.getPaidAt());
        assertNull(payment.getTransactionRef());
    }

    @Test
    @DisplayName("CashPaymentStrategy: Đúng bằng tổng tiền -> tiền thừa bằng 0")
    void testCashPaymentStrategy_ExactCash() {
        BigDecimal cashReceived = new BigDecimal("750000");
        Payment payment = cashStrategy.processPayment(sampleOrder, cashReceived);

        assertNotNull(payment);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertMoneyEquals(cashReceived, payment.getCashReceived());
        assertMoneyEquals(BigDecimal.ZERO, payment.getChangeAmount());
    }

    @Test
    @DisplayName("CashPaymentStrategy: Thiếu tiền -> ném BadRequestException")
    void testCashPaymentStrategy_InsufficientCash() {
        BigDecimal insufficientCash = new BigDecimal("700000");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cashStrategy.processPayment(sampleOrder, insufficientCash));
        assertTrue(ex.getMessage().contains("chưa đủ"));
    }

    @Test
    @DisplayName("CashPaymentStrategy: Tiền khách đưa null -> ném BadRequestException")
    void testCashPaymentStrategy_NullCash() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cashStrategy.processPayment(sampleOrder, null));
        assertTrue(ex.getMessage().contains("Vui lòng nhập số tiền khách đưa"));
    }

    @Test
    @DisplayName("CardPaymentStrategy: Giả lập PAID, có mã chuẩn y APPR-XXXXXX, tiền nhận và thối là null")
    void testCardPaymentStrategy_Process() {
        assertFalse(cardStrategy.isSupportedOnline());
        assertTrue(cardStrategy.isSupportedPos());

        Payment payment = cardStrategy.processPayment(sampleOrder);

        assertNotNull(payment);
        assertEquals(sampleOrder, payment.getOrder());
        assertEquals(PaymentMethod.CARD, payment.getMethod());
        assertMoneyEquals(new BigDecimal("750000"), payment.getAmount());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertNotNull(payment.getTransactionRef());
        assertTrue(payment.getTransactionRef().startsWith("APPR-"));
        assertNull(payment.getCashReceived());
        assertNull(payment.getChangeAmount());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("QrPaymentStrategy: Giả lập PAID, có mã giao dịch QR-..., tiền nhận và thối là null")
    void testQrPaymentStrategy_Process() {
        assertFalse(qrStrategy.isSupportedOnline());
        assertTrue(qrStrategy.isSupportedPos());

        Payment payment = qrStrategy.processPayment(sampleOrder);

        assertNotNull(payment);
        assertEquals(sampleOrder, payment.getOrder());
        assertEquals(PaymentMethod.QR, payment.getMethod());
        assertMoneyEquals(new BigDecimal("750000"), payment.getAmount());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertNotNull(payment.getTransactionRef());
        assertTrue(payment.getTransactionRef().startsWith("QR-"));
        assertNull(payment.getCashReceived());
        assertNull(payment.getChangeAmount());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("PaymentStrategyRegistry: Online chỉ chấp nhận COD và EWALLET_MOCK")
    void testRegistry_GetOnlineStrategy_SuccessAndFail() {
        assertSame(codStrategy, registry.getOnlineStrategy(PaymentMethod.COD));
        assertSame(ewalletStrategy, registry.getOnlineStrategy(PaymentMethod.EWALLET_MOCK));

        // Dùng phương thức của POS cho online -> Báo lỗi
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.CASH));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.CARD));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(PaymentMethod.QR));
        assertThrows(BadRequestException.class, () -> registry.getOnlineStrategy(null));
    }

    @Test
    @DisplayName("PaymentStrategyRegistry: POS chỉ chấp nhận CASH, CARD, QR")
    void testRegistry_GetPosStrategy_SuccessAndFail() {
        assertSame(cashStrategy, registry.getPosStrategy(PaymentMethod.CASH));
        assertSame(cardStrategy, registry.getPosStrategy(PaymentMethod.CARD));
        assertSame(qrStrategy, registry.getPosStrategy(PaymentMethod.QR));

        // Dùng phương thức của ONLINE cho POS -> Báo lỗi
        assertThrows(BadRequestException.class, () -> registry.getPosStrategy(PaymentMethod.COD));
        assertThrows(BadRequestException.class, () -> registry.getPosStrategy(PaymentMethod.EWALLET_MOCK));
        assertThrows(BadRequestException.class, () -> registry.getPosStrategy(null));
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}
