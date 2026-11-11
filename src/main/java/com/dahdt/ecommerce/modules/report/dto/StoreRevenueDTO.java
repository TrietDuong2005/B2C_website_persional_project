package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chỉ đọc thống kê doanh thu và số đơn hàng theo từng chi nhánh cửa hàng.
 * Chỉ dành cho Quản trị viên hệ thống (ADMIN) để đối chiếu doanh số toàn chuỗi.
 */
@Getter
@Builder
public class StoreRevenueDTO {

    private final Long storeId;
    private final String storeCode;
    private final String storeName;
    private final String storeType;
    private final long orderCount;
    private final BigDecimal revenue;
    private final BigDecimal percentage;
}

