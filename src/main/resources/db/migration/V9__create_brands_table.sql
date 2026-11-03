-- V9: Tạo bảng brands (thương hiệu sản phẩm)
-- Lưu danh sách các thương hiệu thời trang cung cấp hàng hóa cho chuỗi

CREATE TABLE brands (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    name        NVARCHAR(100)        NOT NULL,
    slug        NVARCHAR(150)        NOT NULL,
    is_active   BIT                  NOT NULL DEFAULT 1,
    created_at  DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_brands PRIMARY KEY (id),
    CONSTRAINT UQ_brands_slug UNIQUE (slug)
);

