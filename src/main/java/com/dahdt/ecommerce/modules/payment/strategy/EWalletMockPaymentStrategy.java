package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Giả lập thanh toán trực tuyến thành công ngay qua Ví điện tử (EWALLET_MOCK).
 * Lý do sử dụng:
 * - Thay thế cổng thanh toán thật (MoMo, ZaloPay, VNPay) trong phạm vi dự án môn học (Won't của brief).
 * - Tự động thiết lập trạng thái PAID ngay khi đặt đơn, tự sinh mã giao dịch giả
 *   dễ đối soát (dạng WALLET-yyyyMMdd-XXXX) và gán thời điểm thanh toán paidAt = now.
 * ============================================================================
 */
@Component
public class EWalletMockPaymentStrategy implements PaymentStrategy {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.EWALLET_MOCK;
    }

    @Override
    public boolean isSupportedOnline() {
        return true;
    }

    @Override
    public Payment processPayment(Order order) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String mockTxRef = "MOCK-WALLET-" + timestamp + "-" + randomSuffix;

        return Payment.builder()
                .order(order)
                .method(PaymentMethod.EWALLET_MOCK)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PAID)
                .transactionRef(mockTxRef)
                .paidAt(LocalDateTime.now())
                .build();
    }
}

