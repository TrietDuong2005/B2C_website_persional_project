package com.dahdt.ecommerce.modules.catalog.entity;

/**
 * Trạng thái của biến thể sản phẩm (bảng product_variants, cột status):
 * - ACTIVE: Đang bán, có thể đặt hàng và quản lý tồn kho.
 * - INACTIVE: Tạm ngừng kinh doanh biến thể này (thay thế cho xoá cứng).
 */
public enum ProductVariantStatus {
    ACTIVE,
    INACTIVE
}

