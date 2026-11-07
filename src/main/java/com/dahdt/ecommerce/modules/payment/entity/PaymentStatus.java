package com.dahdt.ecommerce.modules.payment.entity;

/**
 * Trạng thái giao dịch thanh toán.
 * Khớp với CHECK constraint trong migration V22:
 * (PENDING, PAID, FAILED, REFUNDED).
 */
public enum PaymentStatus {
    /** Đang chờ thanh toán (COD khi mới tạo, POS khi chờ khách quẹt thẻ) */
    PENDING,

    /** Đã thanh toán thành công */
    PAID,

    /** Thanh toán thất bại */
    FAILED,

    /** Đã hoàn tiền giao dịch (khi hủy đơn/trả hàng) */
    REFUNDED
}

