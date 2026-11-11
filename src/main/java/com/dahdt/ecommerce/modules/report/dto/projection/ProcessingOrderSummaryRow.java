package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả tổng hợp đơn hàng ĐANG XỬ LÝ (PENDING, CONFIRMED, PACKING, SHIPPING).
 */
@Getter
public class ProcessingOrderSummaryRow {

    private final Long processingCount;
    private final BigDecimal expectedRevenue;

    public ProcessingOrderSummaryRow(Long processingCount, BigDecimal expectedRevenue) {
        this.processingCount = (processingCount != null) ? processingCount : 0L;
        this.expectedRevenue = (expectedRevenue != null) ? expectedRevenue : BigDecimal.ZERO;
    }
}

