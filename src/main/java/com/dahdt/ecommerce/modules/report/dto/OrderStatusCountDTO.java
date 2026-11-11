package com.dahdt.ecommerce.modules.report.dto;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chỉ đọc thống kê số lượng đơn và tổng giá trị phân theo từng trạng thái đơn hàng trong kỳ.
 */
@Getter
@Builder
public class OrderStatusCountDTO {

    private final OrderStatus status;
    private final String statusLabel;
    private final String badgeClass;
    private final long orderCount;
    private final BigDecimal totalAmount;
    private final BigDecimal percentage;
}

