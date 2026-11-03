package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class ProductResponseDTO {
    private Long id;
    private CategoryResponseDTO category;
    private BrandResponseDTO brand;
    private String name;
    private String slug;
    private String description;
    private String status;
    private String productType;
    private List<ProductVariantResponseDTO> variants;
    private List<ProductImageResponseDTO> images;

    @Data
    public static class ProductVariantResponseDTO {
        private Long id;
        private String sku;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private BigDecimal weight;
        private String status;
        private Set<AttributeValueDTO> attributeValues;
    }

    @Data
    public static class AttributeValueDTO {
        private Long id;
        private String value;
        private String attributeName; // Tên thuộc tính (ví dụ: Màu sắc)
    }

    @Data
    public static class ProductImageResponseDTO {
        private Long id;
        private String url;
        private Integer sortOrder;
        private boolean isThumbnail;
    }
}
