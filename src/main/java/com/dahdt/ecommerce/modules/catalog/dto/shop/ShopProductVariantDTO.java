package com.dahdt.ecommerce.modules.catalog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thông tin biến thể sản phẩm phục vụ chọn màu/size trên trang chi tiết:
 * id, sku, color, size, price, tồn kho online khả dụng, ảnh riêng theo biến thể.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductVariantDTO {

    private Long id;
    private String sku;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer availableStock;
    private String imageUrl;
}

