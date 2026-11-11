package com.dahdt.ecommerce.modules.report.dto.projection;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp doanh thu theo từng phương thức thanh toán.
 */
@Getter
public class PaymentMethodRevenueRow {

    private final PaymentMethod method;
    private final Long transactionCount;
    private final BigDecimal totalAmount;

    public PaymentMethodRevenueRow(PaymentMethod method, Long transactionCount, BigDecimal totalAmount) {
        this.method = method;
        this.transactionCount = (transactionCount != null) ? transactionCount : 0L;
        this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
    }
}

