package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO chỉ đọc chứa số liệu doanh thu và số đơn hoàn thành theo từng ngày trong kỳ.
 * Đảm bảo ngày không có đơn vẫn xuất hiện với orderCount = 0 và revenue = 0 đ.
 */
@Getter
@Builder
public class DailyRevenueDTO {

    private final LocalDate date;
    private final long orderCount;
    private final BigDecimal revenue;
}

