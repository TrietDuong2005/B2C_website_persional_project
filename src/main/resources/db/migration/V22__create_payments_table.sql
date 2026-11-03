-- V22: Tạo bảng payments (giao dịch thanh toán)
-- Lưu trữ thông tin thanh toán cho đơn hàng, hỗ trợ đa hình thức thanh toán (phục vụ Strategy Pattern).
-- Với POS: hỗ trợ lưu tiền khách đưa (cash_received) và tiền thối (change_amount).

CREATE TABLE payments (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    order_id        BIGINT               NOT NULL,
    method          NVARCHAR(30)         NOT NULL,
    amount          DECIMAL(18,2)        NOT NULL,
    status          NVARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    transaction_ref NVARCHAR(100)        NULL,
    cash_received   DECIMAL(18,2)        NULL,
    change_amount   DECIMAL(18,2)        NULL,
    paid_at         DATETIME2            NULL,
    created_at      DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_payments PRIMARY KEY (id),
    CONSTRAINT FK_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT CK_payments_method CHECK (method IN ('CASH', 'CARD', 'QR', 'COD', 'EWALLET_MOCK')),
    CONSTRAINT CK_payments_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    CONSTRAINT CK_payments_amount CHECK (amount >= 0)
);

CREATE INDEX IX_payments_order_id ON payments(order_id);

