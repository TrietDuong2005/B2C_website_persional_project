package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin giao dịch thanh toán của đơn hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private Long id;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;
}

