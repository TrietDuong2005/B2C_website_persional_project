-- V7: Tạo bảng categories (danh mục sản phẩm)
-- Hỗ trợ danh mục đa cấp (cây cha - con) qua parent_id tự tham chiếu

CREATE TABLE categories (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    parent_id   BIGINT          NULL,
    name        NVARCHAR(255)   NOT NULL,
    slug        NVARCHAR(255)   NOT NULL,
    image_url   NVARCHAR(500)   NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_categories PRIMARY KEY (id),
    CONSTRAINT UQ_categories_slug UNIQUE (slug),
    CONSTRAINT FK_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);
