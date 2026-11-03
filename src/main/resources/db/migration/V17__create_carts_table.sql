-- V17: Tạo bảng carts (giỏ hàng trực tuyến)
-- Lưu thông tin phiên giỏ hàng của khách hàng đã đăng nhập trên website

CREATE TABLE carts (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id     BIGINT               NOT NULL,
    status      NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_carts PRIMARY KEY (id),
    CONSTRAINT FK_carts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT CK_carts_status CHECK (status IN ('ACTIVE', 'ABANDONED', 'CONVERTED'))
);

CREATE INDEX IX_carts_user_id ON carts(user_id);

