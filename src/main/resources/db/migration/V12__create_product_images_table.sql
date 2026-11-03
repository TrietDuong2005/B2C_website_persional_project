-- V12: Tạo bảng product_images (hình ảnh sản phẩm)
-- Lưu danh sách ảnh của sản phẩm hoặc gắn riêng cho từng biến thể cụ thể (ví dụ ảnh theo màu)

CREATE TABLE product_images (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    product_id     BIGINT               NOT NULL,
    variant_id     BIGINT               NULL,
    url            NVARCHAR(500)        NOT NULL,
    sort_order     INT                  NOT NULL DEFAULT 0,
    is_thumbnail   BIT                  NOT NULL DEFAULT 0,
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_product_images PRIMARY KEY (id),
    CONSTRAINT FK_product_images_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT FK_product_images_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

CREATE INDEX IX_product_images_product_id ON product_images(product_id);
CREATE INDEX IX_product_images_variant_id ON product_images(variant_id);

