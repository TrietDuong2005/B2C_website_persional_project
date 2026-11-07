package com.dahdt.ecommerce.modules.order.entity;

/**
 * Kênh tiếp nhận và phát sinh đơn hàng.
 * Khớp với CHECK constraint trong migration V19 (ONLINE, POS).
 */
public enum OrderChannel {
    /** Đơn hàng đặt trực tuyến qua website Shop */
    ONLINE,

    /** Đơn hàng bán trực tiếp tại quầy thanh toán POS chi nhánh */
    POS
}

