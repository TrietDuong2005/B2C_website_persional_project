package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageRequestDTO {
    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    private Long variantId; // Có thể null

    @NotBlank(message = "URL ảnh không được để trống")
    private String url;

    private Integer sortOrder = 0;
    private boolean isThumbnail = false;
}
