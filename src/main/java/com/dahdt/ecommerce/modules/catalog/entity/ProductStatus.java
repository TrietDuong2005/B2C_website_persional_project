package com.dahdt.ecommerce.modules.catalog.entity;

/**
 * Trạng thái của sản phẩm cha (bảng products, cột status):
 * - ACTIVE: Đang kinh doanh, hiển thị trên website và cho phép xem chi tiết.
 * - HIDDEN: Ẩn khỏi hệ thống (thay thế cho việc xoá cứng).
 */
public enum ProductStatus {
    ACTIVE,
    HIDDEN
}

