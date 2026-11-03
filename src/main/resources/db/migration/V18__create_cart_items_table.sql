-- V18: Tạo bảng cart_items (chi tiết món hàng trong giỏ)
-- Lưu danh sách biến thể sản phẩm khách đã thêm vào giỏ.
-- Lưu ý: Không lưu giá tại bảng này; giá bán luôn được truy vấn mới nhất từ bảng product_variants.

CREATE TABLE cart_items (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    cart_id     BIGINT               NOT NULL,
    variant_id  BIGINT               NOT NULL,
    quantity    INT                  NOT NULL DEFAULT 1,
    created_at  DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_cart_items PRIMARY KEY (id),
    CONSTRAINT UQ_cart_items_cart_variant UNIQUE (cart_id, variant_id),
    CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT FK_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT CK_cart_items_quantity CHECK (quantity > 0)
);

CREATE INDEX IX_cart_items_cart_id ON cart_items(cart_id);

