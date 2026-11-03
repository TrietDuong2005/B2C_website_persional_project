-- V13: Tạo bảng stores (cửa hàng và kho bãi)
-- Quản lý tất cả điểm bán lẻ (STORE) và kho trung chuyển/kho tổng (WAREHOUSE) trong toàn chuỗi

CREATE TABLE stores (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    code           NVARCHAR(50)         NOT NULL,
    name           NVARCHAR(255)        NOT NULL,
    type           NVARCHAR(20)         NOT NULL DEFAULT 'STORE',
    province       NVARCHAR(100)        NOT NULL,
    district       NVARCHAR(100)        NOT NULL,
    ward           NVARCHAR(100)        NOT NULL,
    detail_address NVARCHAR(500)        NOT NULL,
    latitude       DECIMAL(10,7)        NULL,
    longitude      DECIMAL(10,7)        NULL,
    phone          NVARCHAR(20)         NULL,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_stores PRIMARY KEY (id),
    CONSTRAINT UQ_stores_code UNIQUE (code),
    CONSTRAINT CK_stores_type CHECK (type IN ('STORE', 'WAREHOUSE')),
    CONSTRAINT CK_stores_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IX_stores_code ON stores(code);

