-- V1: Tạo bảng users (tài khoản người dùng)
-- Bảng này lưu thông tin đăng nhập cơ bản của tất cả người dùng trong hệ thống
-- (khách hàng, admin, nhân viên đều là user, phân biệt qua role)

CREATE TABLE users (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    email             NVARCHAR(255)   NOT NULL,
    phone             NVARCHAR(20)    NULL,
    password_hash     NVARCHAR(255)   NOT NULL,
    status            NVARCHAR(20)    NOT NULL DEFAULT 'pending',   -- active / locked / pending
    email_verified_at DATETIME2       NULL,
    phone_verified_at DATETIME2       NULL,
    is_deleted        BIT             NOT NULL DEFAULT 0,
    deleted_at        DATETIME2       NULL,
    created_at        DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at        DATETIME2       NOT NULL DEFAULT GETDATE(),
    created_by        BIGINT          NULL,
    updated_by        BIGINT          NULL,

    CONSTRAINT PK_users PRIMARY KEY (id),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT UQ_users_phone UNIQUE (phone)
);

-- Index trên email để tăng tốc truy vấn đăng nhập
CREATE INDEX IX_users_email ON users(email);
