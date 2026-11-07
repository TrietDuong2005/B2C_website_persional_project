package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị tóm tắt đơn hàng trong danh sách đơn của khách hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;
    private String orderCode;
    private OrderChannel channel;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String voucherCode;
    private Integer totalQuantity;
    private LocalDateTime placedAt;
    private String shipName;
    private String shipPhone;
    private String shipAddress;
}

