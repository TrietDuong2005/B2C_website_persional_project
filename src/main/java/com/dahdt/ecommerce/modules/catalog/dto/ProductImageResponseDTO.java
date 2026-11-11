package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin hình ảnh sản phẩm.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponseDTO {

    private Long id;
    private Long productId;
    private Long variantId;
    private String variantSku;
    private String variantName;
    private String color;
    private String url;
    private Integer sortOrder;
    private Boolean isThumbnail;
    private LocalDateTime createdAt;
}

