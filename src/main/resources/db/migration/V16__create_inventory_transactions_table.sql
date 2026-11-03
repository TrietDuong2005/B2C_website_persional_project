-- V16: Tạo bảng inventory_transactions (sổ cái giao dịch kho)
-- Bảng ghi nhật ký biến động kho (Audit Log / Append-Only), tuyệt đối không sửa/xóa.
-- Mọi thao tác xuất/nhập/bán/hủy/chuyển kho đều được lưu vết chi tiết tại đây.

CREATE TABLE inventory_transactions (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    variant_id        BIGINT               NOT NULL,
    store_id          BIGINT               NOT NULL,
    type              NVARCHAR(30)         NOT NULL,
    quantity_change   INT                  NOT NULL,
    quantity_after    INT                  NOT NULL,
    reference_type    NVARCHAR(50)         NULL,
    reference_id      BIGINT               NULL,
    note              NVARCHAR(500)        NULL,
    created_by        BIGINT               NULL,
    created_at        DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_inventory_transactions PRIMARY KEY (id),
    CONSTRAINT FK_inventory_transactions_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT FK_inventory_transactions_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT FK_inventory_transactions_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT CK_inventory_transactions_type CHECK (type IN ('IMPORT', 'SALE', 'CANCEL', 'TRANSFER_IN', 'TRANSFER_OUT', 'ADJUST')),
    CONSTRAINT CK_inventory_transactions_qty_after CHECK (quantity_after >= 0)
);

CREATE INDEX IX_inventory_transactions_var_store ON inventory_transactions(variant_id, store_id);
CREATE INDEX IX_inventory_transactions_created_at ON inventory_transactions(created_at);
CREATE INDEX IX_inventory_transactions_store_id ON inventory_transactions(store_id);

