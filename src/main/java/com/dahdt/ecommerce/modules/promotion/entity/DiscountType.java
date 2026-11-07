package com.dahdt.ecommerce.modules.promotion.entity;

/**
 * Loại hình giảm giá của voucher khuyến mãi.
 * Khớp với CHECK constraint trong migration V23 (PERCENT, FIXED).
 */
public enum DiscountType {
    /** Giảm giá theo tỷ lệ phần trăm (bị giới hạn chặn trên bởi max_discount_value nếu có) */
    PERCENT,

    /** Giảm giá theo số tiền cố định */
    FIXED
}

