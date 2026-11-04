package com.dahdt.ecommerce.modules.catalog.dto;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết một biến thể sản phẩm.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String barcode;
    private String color;
    private String size;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer weightGram;
    private ProductVariantStatus status;
    private boolean sellable;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

