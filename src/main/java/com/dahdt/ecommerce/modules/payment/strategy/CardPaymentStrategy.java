package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Giả lập thanh toán Thẻ quẹt / Chạm POS tại quầy (CARD) cho kênh POS.
 * Lý do sử dụng:
 * - Thay thế máy POS quẹt thẻ ngân hàng vật lý thật trong phạm vi đồ án môn học.
 * - Tự động thiết lập trạng thái PAID, sinh mã chuẩn y giả (Approval Code do hệ thống sinh),
 *   đồng thời lưu cash_received và change_amount là null.
 * ============================================================================
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CARD;
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

        // Sinh mã chuẩn y giả lập gồm 6 chữ số ngẫu nhiên: APPR-XXXXXX
        int randomCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String approvalCode = "APPR-" + randomCode;

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        return Payment.builder()
                .order(order)
                .method(PaymentMethod.CARD)
                .amount(total)
                .status(PaymentStatus.PAID)
                .transactionRef(approvalCode)
                .cashReceived(null)
                .changeAmount(null)
                .paidAt(LocalDateTime.now())
                .build();
    }
}

