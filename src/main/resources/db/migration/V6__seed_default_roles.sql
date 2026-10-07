-- V6: Dữ liệu mặc định — tạo sẵn 2 vai trò cơ bản
-- CUSTOMER: gán tự động khi đăng ký tài khoản mới
-- ADMIN: gán thủ công qua DB hoặc API quản trị

INSERT INTO roles (code, name) VALUES ('CUSTOMER', N'Khách hàng');
INSERT INTO roles (code, name) VALUES ('ADMIN', N'Quản trị viên');
