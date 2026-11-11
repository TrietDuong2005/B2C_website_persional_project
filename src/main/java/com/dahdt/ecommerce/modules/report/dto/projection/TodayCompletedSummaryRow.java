package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp đơn hàng hoàn tất trong ngày HÔM NAY.
 */
@Getter
public class TodayCompletedSummaryRow {

    private final Long todayCount;
    private final BigDecimal todayRevenue;

    public TodayCompletedSummaryRow(Long todayCount, BigDecimal todayRevenue) {
        this.todayCount = (todayCount != null) ? todayCount : 0L;
        this.todayRevenue = (todayRevenue != null) ? todayRevenue : BigDecimal.ZERO;
    }
}

