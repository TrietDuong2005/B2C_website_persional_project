-- V24: Tạo bảng voucher_usages (lịch sử sử dụng mã giảm giá)
-- Lưu vết từng lượt áp dụng voucher cho từng đơn hàng và tài khoản khách hàng

CREATE TABLE voucher_usages (
    id            BIGINT IDENTITY(1,1) NOT NULL,
    voucher_id    BIGINT               NOT NULL,
    order_id      BIGINT               NOT NULL,
    user_id       BIGINT               NULL,
    used_at       DATETIME2            NOT NULL DEFAULT GETDATE(),
    created_at    DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_voucher_usages PRIMARY KEY (id),
    CONSTRAINT FK_voucher_usages_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    CONSTRAINT FK_voucher_usages_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT FK_voucher_usages_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IX_voucher_usages_voucher_id ON voucher_usages(voucher_id);
CREATE INDEX IX_voucher_usages_order_id ON voucher_usages(order_id);
CREATE INDEX IX_voucher_usages_user_id ON voucher_usages(user_id);

