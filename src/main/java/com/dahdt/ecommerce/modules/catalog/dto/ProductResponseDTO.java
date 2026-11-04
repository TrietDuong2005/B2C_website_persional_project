package com.dahdt.ecommerce.modules.catalog.dto;

import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị tóm tắt sản phẩm trong danh sách quản trị (phân trang, tìm kiếm, lọc).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

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
    private long variantCount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

