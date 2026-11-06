package com.dahdt.ecommerce.modules.catalog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chi tiết toàn diện của một sản phẩm dành cho trang Shop (/shop/products/{slug}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductDetailDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String thumbnailUrl;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    private Long brandId;
    private String brandName;

    @Builder.Default
    private List<ShopProductImageDTO> images = new ArrayList<>();

    @Builder.Default
    private List<ShopProductVariantDTO> variants = new ArrayList<>();

    @Builder.Default
    private List<String> colors = new ArrayList<>();

    @Builder.Default
    private List<String> sizes = new ArrayList<>();

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStock;
}

