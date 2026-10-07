-- V5: Tạo bảng auth_tokens (refresh token / phiên đăng nhập)
--     và otp_verifications (mã OTP xác thực)

CREATE TABLE auth_tokens (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id     BIGINT          NOT NULL,
    token_hash  NVARCHAR(500)   NOT NULL,   -- hash của refresh token
    device_info NVARCHAR(500)   NULL,       -- thông tin thiết bị (browser, mobile...)
    expires_at  DATETIME2       NOT NULL,   -- thời điểm hết hạn
    revoked_at  DATETIME2       NULL,       -- thời điểm bị thu hồi (đăng xuất), null = còn hiệu lực
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_auth_tokens PRIMARY KEY (id),
    CONSTRAINT FK_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE otp_verifications (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id     BIGINT          NOT NULL,
    code_hash   NVARCHAR(255)   NOT NULL,   -- hash của mã OTP
    purpose     NVARCHAR(30)    NOT NULL,   -- register / reset_password
    expires_at  DATETIME2       NOT NULL,
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_otp_verifications PRIMARY KEY (id),
    CONSTRAINT FK_otp_verifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IX_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX IX_otp_verifications_user_id ON otp_verifications(user_id);
