package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Xử lý thanh toán cho hình thức Nhận hàng trả tiền (COD - Cash On Delivery).
 * Lý do sử dụng:
 * - Đối với hình thức COD, đơn hàng khi vừa đặt trực tuyến sẽ có giao dịch thanh toán
 *   ở trạng thái chờ PENDING. Khi nào shipper giao hàng thành công và thu tiền mặt,
 *   trạng thái thanh toán mới chuyển sang PAID.
 * ============================================================================
 */
@Component
public class CodPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public boolean isSupportedOnline() {
        return true;
    }

    @Override
    public Payment processPayment(Order order) {
        return Payment.builder()
                .order(order)
                .method(PaymentMethod.COD)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .transactionRef(null)
                .paidAt(null)
                .build();
    }
}

