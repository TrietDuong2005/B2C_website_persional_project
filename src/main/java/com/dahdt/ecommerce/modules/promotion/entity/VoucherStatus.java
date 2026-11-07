package com.dahdt.ecommerce.modules.promotion.entity;

/**
 * Trạng thái hoạt động của voucher.
 * Khớp với CHECK constraint trong migration V23 (ACTIVE, INACTIVE).
 */
public enum VoucherStatus {
    /** Đang hoạt động, có thể áp dụng */
    ACTIVE,

    /** Tạm khóa hoặc ngừng kích hoạt */
    INACTIVE
}

