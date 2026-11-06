package com.dahdt.ecommerce.modules.catalog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO hiển thị thẻ sản phẩm trên lưới danh sách sản phẩm hoặc trang chủ.
 * Chứa: id, slug, tên, thương hiệu, ảnh đại diện, giá thấp nhất (giá "từ..."), còn hàng hay không.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductItemDTO {

    private Long id;
    private String name;
    private String slug;
    private String brandName;
    private String categoryName;
    private String categorySlug;
    private String thumbnailUrl;
    private BigDecimal minPrice;
    private boolean inStock;
}

