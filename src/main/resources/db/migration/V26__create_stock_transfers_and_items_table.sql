-- V26: Tạo bảng stock_transfers và stock_transfer_items (điều chuyển hàng liên cửa hàng)
-- Quản lý các phiếu xuất chuyển kho giữa các chi nhánh hoặc từ kho tổng đến cửa hàng

CREATE TABLE stock_transfers (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    code           NVARCHAR(50)         NOT NULL,
    from_store_id  BIGINT               NOT NULL,
    to_store_id    BIGINT               NOT NULL,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'DRAFT',
    note           NVARCHAR(500)        NULL,
    created_by     BIGINT               NULL,
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_stock_transfers PRIMARY KEY (id),
    CONSTRAINT UQ_stock_transfers_code UNIQUE (code),
    CONSTRAINT FK_stock_transfers_from_store FOREIGN KEY (from_store_id) REFERENCES stores(id),
    CONSTRAINT FK_stock_transfers_to_store FOREIGN KEY (to_store_id) REFERENCES stores(id),
    CONSTRAINT FK_stock_transfers_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT CK_stock_transfers_status CHECK (status IN ('DRAFT', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED'))
);

CREATE INDEX IX_stock_transfers_from_store ON stock_transfers(from_store_id);
CREATE INDEX IX_stock_transfers_to_store ON stock_transfers(to_store_id);

CREATE TABLE stock_transfer_items (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    transfer_id    BIGINT               NOT NULL,
    variant_id     BIGINT               NOT NULL,
    quantity       INT                  NOT NULL,
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_stock_transfer_items PRIMARY KEY (id),
    CONSTRAINT UQ_stock_transfer_items_transfer_variant UNIQUE (transfer_id, variant_id),
    CONSTRAINT FK_stock_transfer_items_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfers(id) ON DELETE CASCADE,
    CONSTRAINT FK_stock_transfer_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT CK_stock_transfer_items_quantity CHECK (quantity > 0)
);

CREATE INDEX IX_stock_transfer_items_transfer_id ON stock_transfer_items(transfer_id);

