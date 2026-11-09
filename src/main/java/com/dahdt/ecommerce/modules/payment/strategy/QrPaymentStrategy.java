package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Giả lập thanh toán Quét mã VietQR tại quầy (QR) cho kênh POS.
 * Lý do sử dụng:
 * - Thay thế cổng thanh toán VietQR động thật trong phạm vi đồ án môn học.
 * - Tự động thiết lập trạng thái PAID, sinh mã giao dịch giả lập (transaction_ref)
 *   dễ đối soát dạng QR-yyyyMMddHHmmss-XXXX, lưu cash_received và change_amount là null.
 * ============================================================================
 */
@Component
public class QrPaymentStrategy implements PaymentStrategy {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.QR;
    }

    @Override
    public boolean isSupportedOnline() {
        return false;
    }

    @Override
    public boolean isSupportedPos() {
        return true;
    }

    @Override
    public Payment processPayment(Order order) {
        if (order == null) {
            throw new BadRequestException("Đơn hàng không được để trống khi thanh toán.");
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String mockTxRef = "QR-" + timestamp + "-" + randomSuffix;

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        return Payment.builder()
                .order(order)
                .method(PaymentMethod.QR)
                .amount(total)
                .status(PaymentStatus.PAID)
                .transactionRef(mockTxRef)
                .cashReceived(null)
                .changeAmount(null)
                .paidAt(LocalDateTime.now())
                .build();
    }
}

