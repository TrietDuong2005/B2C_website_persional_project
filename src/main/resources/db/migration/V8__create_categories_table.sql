-- V8: Tạo bảng categories (danh mục sản phẩm)
-- Lưu danh mục sản phẩm theo mô hình phân cấp cây cha - con (ví dụ: Thời trang nam -> Áo thun)

CREATE TABLE categories (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    parent_id   BIGINT               NULL,
    name        NVARCHAR(100)        NOT NULL,
    slug        NVARCHAR(150)        NOT NULL,
    sort_order  INT                  NOT NULL DEFAULT 0,
    is_active   BIT                  NOT NULL DEFAULT 1,
    created_at  DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_categories PRIMARY KEY (id),
    CONSTRAINT UQ_categories_slug UNIQUE (slug),
    CONSTRAINT FK_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE INDEX IX_categories_parent_id ON categories(parent_id);

