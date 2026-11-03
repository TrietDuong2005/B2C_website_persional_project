-- V25: Tạo bảng pos_shifts (ca làm việc của thu ngân tại POS)
-- Quản lý quy trình mở/đóng ca thu ngân, bàn giao số dư tiền mặt đầu ca và đối soát tiền thực thu cuối ca

CREATE TABLE pos_shifts (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    store_id       BIGINT               NOT NULL,
    cashier_id     BIGINT               NOT NULL,
    opened_at      DATETIME2            NOT NULL DEFAULT GETDATE(),
    closed_at      DATETIME2            NULL,
    opening_cash   DECIMAL(18,2)        NOT NULL DEFAULT 0.00,
    closing_cash   DECIMAL(18,2)        NULL,
    expected_cash  DECIMAL(18,2)        NULL,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'OPEN',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_pos_shifts PRIMARY KEY (id),
    CONSTRAINT FK_pos_shifts_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT FK_pos_shifts_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
    CONSTRAINT CK_pos_shifts_status CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT CK_pos_shifts_opening_cash CHECK (opening_cash >= 0)
);

CREATE INDEX IX_pos_shifts_store_id ON pos_shifts(store_id);
CREATE INDEX IX_pos_shifts_cashier_id ON pos_shifts(cashier_id);

