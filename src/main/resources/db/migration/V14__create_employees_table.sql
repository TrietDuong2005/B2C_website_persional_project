-- V14: Tạo bảng employees (hồ sơ nhân viên chuỗi)
-- Liên kết một tài khoản người dùng (users) với một cửa hàng cụ thể (stores) và chức vụ đảm nhiệm

CREATE TABLE employees (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    user_id        BIGINT               NOT NULL,
    store_id       BIGINT               NOT NULL,
    position       NVARCHAR(20)         NOT NULL DEFAULT 'STAFF',
    status         NVARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME2            NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2            NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_employees PRIMARY KEY (id),
    CONSTRAINT UQ_employees_user_id UNIQUE (user_id),
    CONSTRAINT FK_employees_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_employees_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT CK_employees_position CHECK (position IN ('MANAGER', 'CASHIER', 'STAFF')),
    CONSTRAINT CK_employees_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IX_employees_store_id ON employees(store_id);

