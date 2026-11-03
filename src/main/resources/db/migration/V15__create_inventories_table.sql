-- V15: Tạo bảng inventories (tồn kho sản phẩm theo cửa hàng)
-- Quản lý số lượng tồn thực tế của từng biến thể sản phẩm tại từng cửa hàng/kho.
-- Cột version phục vụ cơ chế khóa lạc quan (Optimistic Locking) chống bán vượt tồn.

CREATE TABLE inventories (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    variant_id        BIGINT               NOT NULL,
    store_id          BIGINT               NOT NULL,
    quantity_on_hand  INT                  NOT NULL DEFAULT 0,
    reorder_level     INT                  NOT NULL DEFAULT 5,
    version           BIGINT               NOT NULL DEFAULT 0,
    created_at        DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at        DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_inventories PRIMARY KEY (id),
    CONSTRAINT UQ_inventories_variant_store UNIQUE (variant_id, store_id),
    CONSTRAINT FK_inventories_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT FK_inventories_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT CK_inventories_quantity CHECK (quantity_on_hand >= 0),
    CONSTRAINT CK_inventories_reorder_level CHECK (reorder_level >= 0)
);

CREATE INDEX IX_inventories_store_id ON inventories(store_id);
CREATE INDEX IX_inventories_variant_store ON inventories(variant_id, store_id);

