package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp đơn hàng HOÀN TẤT (COMPLETED) trong kỳ.
 * Sử dụng trong mệnh đề SELECT new của JPQL để đảm bảo an toàn kiểu tuyệt đối.
 */
@Getter
public class CompletedOrderSummaryRow {

    private final Long completedCount;
    private final BigDecimal totalRevenue;
    private final BigDecimal posRevenue;
    private final BigDecimal onlineRevenue;

    public CompletedOrderSummaryRow(Long completedCount, BigDecimal totalRevenue, BigDecimal posRevenue, BigDecimal onlineRevenue) {
        this.completedCount = (completedCount != null) ? completedCount : 0L;
        this.totalRevenue = (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO;
        this.posRevenue = (posRevenue != null) ? posRevenue : BigDecimal.ZERO;
        this.onlineRevenue = (onlineRevenue != null) ? onlineRevenue : BigDecimal.ZERO;
    }
}

