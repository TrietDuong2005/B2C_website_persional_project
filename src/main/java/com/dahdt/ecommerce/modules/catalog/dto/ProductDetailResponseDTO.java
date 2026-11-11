package com.dahdt.ecommerce.modules.catalog.dto;

import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO hiển thị đầy đủ thông tin chi tiết một sản phẩm:
 * gồm thông tin sản phẩm cha, danh mục, thương hiệu, danh sách biến thể và hình ảnh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String thumbnailUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ProductVariantResponseDTO> variants = new ArrayList<>();

    @Builder.Default
    private List<ProductImageResponseDTO> images = new ArrayList<>();

    @Builder.Default
    private List<String> colors = new ArrayList<>();
}

