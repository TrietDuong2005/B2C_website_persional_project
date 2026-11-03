-- V19: Tạo bảng orders (đơn hàng đa kênh)
-- Bảng dùng chung cho cả 2 kênh bán: Trực tuyến (ONLINE) và Tại quầy (POS).
-- Chứa thông tin tổng thanh toán, voucher áp dụng, thông tin giao hàng (snapshot độc lập cho đơn online).

CREATE TABLE orders (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    order_code      NVARCHAR(50)         NOT NULL,
    channel         NVARCHAR(20)         NOT NULL,
    store_id        BIGINT               NOT NULL,
    customer_id     BIGINT               NULL,
    cashier_id      BIGINT               NULL,
    status          NVARCHAR(30)         NOT NULL,
    subtotal        DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    discount_total  DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    shipping_fee    DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    voucher_code    NVARCHAR(50)         NULL,
    ship_name       NVARCHAR(255)        NULL,
    ship_phone      NVARCHAR(20)         NULL,
    ship_address    NVARCHAR(500)        NULL,
    ship_latitude   DECIMAL(10,7)        NULL,
    ship_longitude  DECIMAL(10,7)        NULL,
    note            NVARCHAR(500)        NULL,
    placed_at       DATETIME2            NOT NULL DEFAULT GETDATE(),
    completed_at    DATETIME2            NULL,
    created_at      DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_orders PRIMARY KEY (id),
    CONSTRAINT UQ_orders_order_code UNIQUE (order_code),
    CONSTRAINT FK_orders_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT FK_orders_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
    CONSTRAINT CK_orders_channel CHECK (channel IN ('ONLINE', 'POS')),
    CONSTRAINT CK_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PACKING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_orders_subtotal CHECK (subtotal >= 0),
    CONSTRAINT CK_orders_discount_total CHECK (discount_total >= 0),
    CONSTRAINT CK_orders_shipping_fee CHECK (shipping_fee >= 0),
    CONSTRAINT CK_orders_total_amount CHECK (total_amount >= 0)
);

CREATE INDEX IX_orders_order_code ON orders(order_code);
CREATE INDEX IX_orders_store_id ON orders(store_id);
CREATE INDEX IX_orders_placed_at ON orders(placed_at);
CREATE INDEX IX_orders_customer_id ON orders(customer_id);

