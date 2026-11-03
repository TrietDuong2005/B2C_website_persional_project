package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantRequestDTO {
    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @NotNull(message = "Giá không được để trống")
    private BigDecimal price;

    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;
    private BigDecimal weight;
    private String dimensions;
    private String barcode;
    private String status = "active";
    
    // Danh sách ID của các AttributeValue (Ví dụ: ID của Đỏ, ID của XL)
    private List<Long> attributeValueIds;
}
