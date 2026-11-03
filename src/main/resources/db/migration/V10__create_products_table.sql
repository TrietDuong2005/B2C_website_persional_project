-- V10: Tạo bảng products (sản phẩm cha)
-- Bảng đại diện cho một mẫu sản phẩm chung (ví dụ: "Áo sơ mi nam Oxford"),
-- các màu sắc và kích cỡ cụ thể sẽ được định nghĩa ở bảng product_variants

CREATE TABLE products (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    category_id    BIGINT               NOT NULL,
    brand_id       BIGINT               NULL,
    name           NVARCHAR(255)        NOT NULL,
    slug           NVARCHAR(300)        NOT NULL,
    description    NVARCHAR(MAX)        NULL,
    thumbnail_url  NVARCHAR(500)        NULL,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_products PRIMARY KEY (id),
    CONSTRAINT UQ_products_slug UNIQUE (slug),
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT FK_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT CK_products_status CHECK (status IN ('ACTIVE', 'HIDDEN'))
);

CREATE INDEX IX_products_category_id ON products(category_id);
CREATE INDEX IX_products_brand_id ON products(brand_id);

