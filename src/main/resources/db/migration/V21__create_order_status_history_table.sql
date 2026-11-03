-- V21: Tạo bảng order_status_history (lịch sử chuyển trạng thái đơn hàng)
-- Theo dõi vết toàn bộ quá trình biến chuyển trạng thái của đơn hàng (phục vụ State Pattern và kiểm soát vận hành)

CREATE TABLE order_status_history (
    id            BIGINT IDENTITY(1,1) NOT NULL,
    order_id      BIGINT               NOT NULL,
    from_status   NVARCHAR(30)         NULL,
    to_status     NVARCHAR(30)         NOT NULL,
    changed_by    BIGINT               NULL,
    note          NVARCHAR(500)        NULL,
    changed_at    DATETIME2            NOT NULL DEFAULT GETDATE(),
    created_at    DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_order_status_history PRIMARY KEY (id),
    CONSTRAINT FK_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT FK_order_status_history_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX IX_order_status_history_order_id ON order_status_history(order_id);

