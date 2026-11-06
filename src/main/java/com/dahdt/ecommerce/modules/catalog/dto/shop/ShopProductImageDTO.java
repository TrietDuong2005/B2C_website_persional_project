package com.dahdt.ecommerce.modules.catalog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hình ảnh sản phẩm / biến thể cho gallery ảnh trang chi tiết.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductImageDTO {

    private Long id;
    private Long variantId;
    private String url;
    private Integer sortOrder;
    private boolean thumbnail;
}

