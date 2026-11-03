-- V9: Tạo bảng products (sản phẩm cha - SPU)
-- Mỗi sản phẩm thuộc 1 brand và 1 category (để đơn giản, dù thiết kế gốc là n-n với category, nhưng thường 1-n là đủ cho e-commerce thông dụng)
-- Sử dụng is_deleted cho soft delete

CREATE TABLE products (
    id               BIGINT IDENTITY(1,1) NOT NULL,
    category_id      BIGINT          NOT NULL,
    brand_id         BIGINT          NOT NULL,
    name             NVARCHAR(500)   NOT NULL,
    slug             NVARCHAR(500)   NOT NULL,
    description      NVARCHAR(MAX)   NULL,
    status           NVARCHAR(50)    NOT NULL DEFAULT 'draft', -- draft/active/hidden
    product_type     NVARCHAR(50)    NOT NULL DEFAULT 'variant', -- simple/variant
    meta_title       NVARCHAR(500)   NULL,
    meta_description NVARCHAR(1000)  NULL,
    is_deleted       BIT             NOT NULL DEFAULT 0,
    deleted_at       DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_products PRIMARY KEY (id),
    CONSTRAINT UQ_products_slug UNIQUE (slug),
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT FK_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id)
);

CREATE INDEX IX_products_category_id ON products(category_id);
CREATE INDEX IX_products_brand_id ON products(brand_id);
