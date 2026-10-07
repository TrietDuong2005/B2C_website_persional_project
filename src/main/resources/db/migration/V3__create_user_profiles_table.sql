-- V3: Tạo bảng user_profiles (thông tin cá nhân mở rộng)
-- Tách riêng khỏi users để bảng users gọn nhẹ (chỉ chứa thông tin đăng nhập)
-- Quan hệ 1-1 với users

CREATE TABLE user_profiles (
    id            BIGINT IDENTITY(1,1) NOT NULL,
    user_id       BIGINT          NOT NULL,
    full_name     NVARCHAR(255)   NULL,
    avatar_url    NVARCHAR(500)   NULL,
    gender        NVARCHAR(10)    NULL,     -- male / female / other
    date_of_birth DATE            NULL,
    created_at    DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_user_profiles PRIMARY KEY (id),
    CONSTRAINT UQ_user_profiles_user UNIQUE (user_id),
    CONSTRAINT FK_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);
