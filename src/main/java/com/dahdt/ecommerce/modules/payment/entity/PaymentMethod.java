package com.dahdt.ecommerce.modules.payment.entity;

/**
 * Phương thức thanh toán được hỗ trợ trong hệ thống.
 * Khớp với CHECK constraint trong migration V22:
 * (CASH, CARD, QR, COD, EWALLET_MOCK).
 */
public enum PaymentMethod {
    /** Tiền mặt tại quầy (dành cho POS) */
    CASH,

    /** Thẻ ngân hàng / POS quẹt thẻ */
    CARD,

    /** Quét mã VietQR động */
    QR,

    /** Thanh toán khi nhận hàng (giao hàng COD - Online) */
    COD,

    /** Giả lập ví điện tử trực tuyến (MoMo, ZaloPay, VNPay mock - Online) */
    EWALLET_MOCK
}

