package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chỉ đọc cho Top sản phẩm bán chạy nhất trong kỳ (tính từ các đơn hàng COMPLETED).
 */
@Getter
@Builder
public class TopSellingProductDTO {

    private final String sku;
    private final String productName;
    private final String color;
    private final String size;
    private final long quantitySold;
    private final BigDecimal totalRevenue;
}

