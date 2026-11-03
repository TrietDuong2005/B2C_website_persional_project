-- V20: Tạo bảng order_items (chi tiết mặt hàng trong đơn)
-- Lưu bản sao ảnh chụp dữ liệu (Snapshot) tại thời điểm mua: tên sản phẩm, mã SKU,
-- màu sắc, kích cỡ, đơn giá và thành tiền, nhằm bảo toàn tính toàn vẹn lịch sử giao dịch.

CREATE TABLE order_items (
    id            BIGINT IDENTITY(1,1) NOT NULL,
    order_id      BIGINT               NOT NULL,
    variant_id    BIGINT               NULL,
    product_name  NVARCHAR(255)        NOT NULL,
    sku           NVARCHAR(100)        NOT NULL,
    color         NVARCHAR(50)         NOT NULL,
    size          NVARCHAR(50)         NOT NULL,
    unit_price    DECIMAL(18,2)        NOT NULL,
    quantity      INT                  NOT NULL,
    line_total    DECIMAL(18,2)        NOT NULL,
    created_at    DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_order_items PRIMARY KEY (id),
    CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT FK_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT CK_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT CK_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT CK_order_items_line_total CHECK (line_total >= 0)
);

CREATE INDEX IX_order_items_order_id ON order_items(order_id);
CREATE INDEX IX_order_items_variant_id ON order_items(variant_id);

