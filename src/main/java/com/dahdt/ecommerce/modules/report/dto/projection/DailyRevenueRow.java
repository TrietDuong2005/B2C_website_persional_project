package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection DTO nhận kết quả doanh thu và số đơn theo từng ngày.
 */
@Getter
public class DailyRevenueRow {

    private final LocalDate date;
    private final Long orderCount;
    private final BigDecimal revenue;

    public DailyRevenueRow(LocalDate date, Long orderCount, BigDecimal revenue) {
        this.date = date;
        this.orderCount = (orderCount != null) ? orderCount : 0L;
        this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
    }
}

