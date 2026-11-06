package com.dahdt.ecommerce.modules.cart.entity;

/**
 * Trạng thái giỏ hàng (bảng carts, khớp với CHECK constraint trong V17).
 */
public enum CartStatus {
    ACTIVE,
    ABANDONED,
    CONVERTED
}

