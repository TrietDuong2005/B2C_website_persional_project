package com.dahdt.ecommerce.modules.report.dto.projection;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Projection DTO nhận kết quả Top sản phẩm bán chạy nhất trong kỳ.
 */
@Getter
public class TopSellingProductRow {

    private final String sku;
    private final String productName;
    private final String color;
    private final String size;
    private final Long quantitySold;
    private final BigDecimal totalRevenue;

    public TopSellingProductRow(String sku, String productName, String color, String size, Long quantitySold, BigDecimal totalRevenue) {
        this.sku = sku;
        this.productName = productName;
        this.color = color;
        this.size = size;
        this.quantitySold = (quantitySold != null) ? quantitySold : 0L;
        this.totalRevenue = (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO;
    }
}

