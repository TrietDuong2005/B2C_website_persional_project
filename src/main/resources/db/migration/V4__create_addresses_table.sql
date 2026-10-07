-- V4: Tạo bảng addresses (sổ địa chỉ giao hàng / thanh toán)
-- Mỗi user có thể có nhiều địa chỉ, 1 trong số đó là mặc định (is_default = 1)
-- Có sẵn cột latitude/longitude để sau này tích hợp tính phí ship theo khoảng cách

CREATE TABLE addresses (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    user_id         BIGINT          NOT NULL,
    recipient_name  NVARCHAR(255)   NOT NULL,   -- tên người nhận (có thể khác tên tài khoản)
    phone           NVARCHAR(20)    NOT NULL,   -- SĐT người nhận
    province        NVARCHAR(100)   NOT NULL,   -- tỉnh/thành phố
    district        NVARCHAR(100)   NOT NULL,   -- quận/huyện
    ward            NVARCHAR(100)   NOT NULL,   -- phường/xã
    detail_address  NVARCHAR(500)   NOT NULL,   -- số nhà, tên đường...
    is_default      BIT             NOT NULL DEFAULT 0,
    type            NVARCHAR(20)    NOT NULL DEFAULT 'shipping',  -- shipping / billing
    latitude        DECIMAL(10,7)   NULL,       -- vĩ độ (cho tính phí ship GIS)
    longitude       DECIMAL(10,7)   NULL,       -- kinh độ
    created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_addresses PRIMARY KEY (id),
    CONSTRAINT FK_addresses_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IX_addresses_user_id ON addresses(user_id);
