package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Xử lý thanh toán Tiền mặt tại quầy (CASH) cho kênh bán hàng POS.
 * Lý do sử dụng:
 * - Đóng gói thuật toán kiểm tra số tiền khách đưa (cash_received) và tính tiền thối (change_amount).
 * - Kiểm tra số tiền khách đưa phải lớn hơn hoặc bằng tổng giá trị đơn hàng (so sánh bằng compareTo).
 * - Thiết lập trạng thái thanh toán PAID ngay tại thời điểm giao dịch thành công.
 * ============================================================================
 */
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
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
        return processPayment(order, null);
    }

    @Override
    public Payment processPayment(Order order, BigDecimal cashReceived) {
        if (order == null) {
            throw new BadRequestException("Đơn hàng không được để trống khi thanh toán.");
        }

        if (cashReceived == null) {
            throw new BadRequestException("Vui lòng nhập số tiền khách đưa.");
        }

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        // So sánh tiền bằng compareTo theo quy tắc Javadoc MoneyUtil
        if (cashReceived.compareTo(total) < 0) {
            throw new BadRequestException("Số tiền khách đưa (" + MoneyUtil.format(cashReceived) +
                    ") chưa đủ để thanh toán tổng tiền (" + MoneyUtil.format(total) + ").");
        }

        BigDecimal changeAmount = MoneyUtil.roundMoney(cashReceived.subtract(total));

        return Payment.builder()
                .order(order)
                .method(PaymentMethod.CASH)
                .amount(total)
                .status(PaymentStatus.PAID)
                .cashReceived(MoneyUtil.roundMoney(cashReceived))
                .changeAmount(changeAmount)
                .transactionRef(null)
                .paidAt(LocalDateTime.now())
                .build();
    }
}

