package com.dahdt.ecommerce.modules.order.entity;

/**
 * Trạng thái vòng đời đơn hàng.
 * Khớp với CHECK constraint trong migration V19:
 * (PENDING, CONFIRMED, PACKING, SHIPPING, COMPLETED, CANCELLED).
 */
public enum OrderStatus {
    /** Đơn hàng mới đặt, đang chờ hệ thống tiếp nhận và xác nhận */
    PENDING,

    /** Đơn hàng đã được xác nhận */
    CONFIRMED,

    /** Đơn hàng đang được kho đóng gói chuẩn bị giao */
    PACKING,

    /** Đơn hàng đang trên đường vận chuyển giao cho khách */
    SHIPPING,

    /** Đơn hàng đã giao thành công và hoàn tất */
    COMPLETED,

    /** Đơn hàng đã bị hủy */
    CANCELLED
}

