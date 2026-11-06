package com.dahdt.ecommerce.modules.catalog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa các tiêu chí lọc sản phẩm trên trang Shop.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductFilterDTO {

    /** Từ khóa tìm kiếm (theo tên sản phẩm hoặc tên thương hiệu) */
    private String keyword;

    /** Slug danh mục cần lọc (bao gồm danh mục gốc hoặc danh mục con) */
    private String category;

    /** Danh sách slug thương hiệu cần lọc */
    @Builder.Default
    private List<String> brands = new ArrayList<>();

    /** Mức giá tối thiểu */
    private BigDecimal minPrice;

    /** Mức giá tối đa */
    private BigDecimal maxPrice;

    /** Màu sắc cần lọc */
    private String color;

    /** Kích cỡ size cần lọc */
    private String size;

    /** Chỉ hiển thị sản phẩm còn hàng trong kho online */
    private Boolean inStockOnly;

    /** Tiêu chí sắp xếp: latest (mới nhất), price_asc (giá tăng), price_desc (giá giảm), name_asc (tên A-Z) */
    @Builder.Default
    private String sort = "latest";
}

