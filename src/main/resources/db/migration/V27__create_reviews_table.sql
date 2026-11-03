-- V27: Tạo bảng reviews (đánh giá và nhận xét sản phẩm)
-- Lưu nhận xét và chấm điểm sao của khách hàng đối với sản phẩm đã mua qua từng dòng đơn hàng (order_items)

CREATE TABLE reviews (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    order_item_id  BIGINT               NOT NULL,
    user_id        BIGINT               NOT NULL,
    product_id     BIGINT               NOT NULL,
    rating         INT                  NOT NULL,
    comment        NVARCHAR(1000)       NULL,
    status         NVARCHAR(20)         NOT NULL DEFAULT 'APPROVED',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_reviews PRIMARY KEY (id),
    CONSTRAINT UQ_reviews_order_item_id UNIQUE (order_item_id),
    CONSTRAINT FK_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT CK_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT CK_reviews_status CHECK (status IN ('PENDING', 'APPROVED', 'HIDDEN'))
);

CREATE INDEX IX_reviews_product_id ON reviews(product_id);
CREATE INDEX IX_reviews_user_id ON reviews(user_id);

