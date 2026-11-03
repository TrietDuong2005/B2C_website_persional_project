-- V11: Tạo bảng product_variants (biến thể bán được - SKU)
-- Gắn liền với giá, tồn kho thực tế của sản phẩm

CREATE TABLE product_variants (
    id               BIGINT IDENTITY(1,1) NOT NULL,
    product_id       BIGINT          NOT NULL,
    sku              NVARCHAR(100)   NOT NULL,
    price            DECIMAL(18,2)   NOT NULL, -- Giá bán hiện tại
    compare_at_price DECIMAL(18,2)   NULL,     -- Giá gốc (để hiển thị gạch ngang)
    cost_price       DECIMAL(18,2)   NULL,     -- Giá vốn (lợi nhuận)
    weight           DECIMAL(10,2)   NULL,     -- Khối lượng (gram) cho vận chuyển
    dimensions       NVARCHAR(100)   NULL,     -- DxCXR
    barcode          NVARCHAR(100)   NULL,     -- Mã vạch
    status           NVARCHAR(50)    NOT NULL DEFAULT 'active',
    is_deleted       BIT             NOT NULL DEFAULT 0,
    deleted_at       DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_product_variants PRIMARY KEY (id),
    CONSTRAINT UQ_product_variants_sku UNIQUE (sku),
    CONSTRAINT FK_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX IX_product_variants_product_id ON product_variants(product_id);
