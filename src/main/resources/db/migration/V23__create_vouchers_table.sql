-- V23: Tạo bảng vouchers (mã giảm giá khuyến mãi)
-- Quản lý mã giảm giá toàn hệ thống: giảm theo phần trăm hoặc giảm số tiền cố định (phục vụ Strategy Pattern).

CREATE TABLE vouchers (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    code                NVARCHAR(50)         NOT NULL,
    name                NVARCHAR(255)        NOT NULL,
    discount_type       NVARCHAR(20)         NOT NULL,
    discount_value      DECIMAL(18,2)        NOT NULL,
    min_order_value     DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    max_discount_value  DECIMAL(18,2)        NULL,
    usage_limit         INT                  NULL,
    used_count          INT                  NOT NULL DEFAULT 0,
    start_at            DATETIME2            NOT NULL,
    end_at              DATETIME2            NOT NULL,
    status              NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at          DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_vouchers PRIMARY KEY (id),
    CONSTRAINT UQ_vouchers_code UNIQUE (code),
    CONSTRAINT CK_vouchers_discount_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT CK_vouchers_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT CK_vouchers_discount_value CHECK (discount_value >= 0),
    CONSTRAINT CK_vouchers_min_order CHECK (min_order_value >= 0),
    CONSTRAINT CK_vouchers_used_count CHECK (used_count >= 0)
);

CREATE INDEX IX_vouchers_code ON vouchers(code);

