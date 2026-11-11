package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp doanh thu theo từng chi nhánh.
 */
@Getter
public class StoreRevenueRow {

    private final Long storeId;
    private final Long orderCount;
    private final BigDecimal revenue;

    public StoreRevenueRow(Long storeId, Long orderCount, BigDecimal revenue) {
        this.storeId = storeId;
        this.orderCount = (orderCount != null) ? orderCount : 0L;
        this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
    }
}

