-- V13: Tạo bảng product_images (ảnh sản phẩm / ảnh biến thể)
-- Tạm thời chỉ lưu URL ảnh

CREATE TABLE product_images (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    product_id  BIGINT          NOT NULL,
    variant_id  BIGINT          NULL, -- Nếu NULL thì là ảnh chung của sản phẩm, nếu có thì là ảnh riêng của biến thể
    url         NVARCHAR(1000)  NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    is_thumbnail BIT            NOT NULL DEFAULT 0,
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_product_images PRIMARY KEY (id),
    CONSTRAINT FK_product_images_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT FK_product_images_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

CREATE INDEX IX_product_images_product_id ON product_images(product_id);
