package com.dahdt.ecommerce.modules.report.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chỉ đọc thống kê doanh thu và số giao dịch phân theo từng phương thức thanh toán.
 */
@Getter
@Builder
public class PaymentMethodRevenueDTO {

    private final PaymentMethod method;
    private final String methodName;
    private final long transactionCount;
    private final BigDecimal totalAmount;
    private final BigDecimal percentage;
}

