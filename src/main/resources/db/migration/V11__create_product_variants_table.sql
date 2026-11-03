-- V11: Tạo bảng product_variants (biến thể sản phẩm)
-- Đơn vị lưu kho và bán hàng thực tế (SKU). Mỗi biến thể có thuộc tính màu sắc (color),
-- kích cỡ (size), giá bán, giá vốn và mã vạch riêng biệt.

CREATE TABLE product_variants (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    product_id     BIGINT               NOT NULL,
    sku            NVARCHAR(100)        NOT NULL,
    barcode        NVARCHAR(100)        NULL,
    color          NVARCHAR(50)         NOT NULL,
    size           NVARCHAR(50)         NOT NULL,
    price          DECIMAL(18,2)        NOT NULL,
    cost_price     DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    weight_gram    INT                  NOT NULL DEFAULT 0,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_product_variants PRIMARY KEY (id),
    CONSTRAINT UQ_product_variants_sku UNIQUE (sku),
    CONSTRAINT FK_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT CK_product_variants_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT CK_product_variants_price CHECK (price >= 0),
    CONSTRAINT CK_product_variants_cost_price CHECK (cost_price >= 0),
    CONSTRAINT CK_product_variants_weight CHECK (weight_gram >= 0)
);

CREATE INDEX IX_product_variants_product_id ON product_variants(product_id);
CREATE INDEX IX_product_variants_sku ON product_variants(sku);
CREATE UNIQUE INDEX UQ_product_variants_barcode ON product_variants(barcode) WHERE barcode IS NOT NULL;

