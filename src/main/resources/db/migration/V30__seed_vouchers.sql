-- ==============================================================================
-- V30: Dữ liệu mẫu 3 voucher khuyến mãi kiểm thử cho chuỗi cửa hàng Triết Store
-- Gồm: 1 voucher giảm %, 1 voucher giảm số tiền cố định, 1 voucher đã hết hạn để test
-- Giữ nguyên toàn bộ mã hiệu (HELLOSUMMER, GIAM50K, EXPIRED2025) và các thông số giảm giá
-- ==============================================================================

INSERT INTO vouchers (code, name, discount_type, discount_value, min_order_value, max_discount_value, usage_limit, used_count, start_at, end_at, status, created_at, updated_at)
VALUES
(
    'HELLOSUMMER',
    N'Khuyến mãi mùa hè - Giảm 10% tối đa 50.000đ',
    'PERCENT',
    10.00,
    200000.00,
    50000.00,
    100,
    0,
    '2026-01-01 00:00:00',
    '2026-12-31 23:59:59',
    'ACTIVE',
    GETDATE(),
    GETDATE()
),
(
    'GIAM50K',
    N'Ưu đãi tri ân - Giảm ngay 50.000đ cho đơn từ 300.000đ',
    'FIXED',
    50000.00,
    300000.00,
    50000.00,
    200,
    5,
    '2026-01-01 00:00:00',
    '2026-12-31 23:59:59',
    'ACTIVE',
    GETDATE(),
    GETDATE()
),
(
    'EXPIRED2025',
    N'Voucher năm cũ 2025 (Đã hết hạn để test)',
    'FIXED',
    100000.00,
    500000.00,
    100000.00,
    50,
    50,
    '2025-01-01 00:00:00',
    '2025-12-31 23:59:59',
    'INACTIVE',
    GETDATE(),
    GETDATE()
);
