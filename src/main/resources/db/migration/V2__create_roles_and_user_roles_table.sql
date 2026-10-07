-- V2: Tạo bảng roles (vai trò) và user_roles (gán vai trò cho user)
-- Một user có thể có nhiều role (vd: vừa là ADMIN vừa là CUSTOMER)

CREATE TABLE roles (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    code        NVARCHAR(50)    NOT NULL,   -- mã dùng trong code: CUSTOMER, ADMIN, STAFF
    name        NVARCHAR(100)   NOT NULL,   -- tên hiển thị: "Khách hàng", "Quản trị viên"
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_roles PRIMARY KEY (id),
    CONSTRAINT UQ_roles_code UNIQUE (code)
);

-- Bảng trung gian: gán nhiều role cho 1 user (quan hệ nhiều-nhiều)
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,

    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
