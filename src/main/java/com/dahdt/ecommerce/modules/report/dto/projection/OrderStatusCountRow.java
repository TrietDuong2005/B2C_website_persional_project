package com.dahdt.ecommerce.modules.report.dto.projection;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp số lượng đơn và tổng tiền theo từng trạng thái.
 */
@Getter
public class OrderStatusCountRow {

    private final OrderStatus status;
    private final Long orderCount;
    private final BigDecimal totalAmount;

    public OrderStatusCountRow(OrderStatus status, Long orderCount, BigDecimal totalAmount) {
        this.status = status;
        this.orderCount = (orderCount != null) ? orderCount : 0L;
        this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
    }
}

