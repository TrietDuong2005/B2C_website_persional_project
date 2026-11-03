-- V7: Bổ sung các vai trò hệ thống mới (STORE_MANAGER, CASHIER) và kiểm tra tọa độ trong sổ địa chỉ
-- STORE_MANAGER: Quản lý chi nhánh cửa hàng (POS + Admin chi nhánh)
-- CASHIER: Thu ngân bán lẻ tại quầy (POS)
-- addresses: Đảm bảo có đầy đủ cột latitude/longitude phục vụ tính phí ship và gợi ý cửa hàng gần nhất

IF NOT EXISTS (SELECT 1 FROM roles WHERE code = 'STORE_MANAGER')
BEGIN
    INSERT INTO roles (code, name) VALUES ('STORE_MANAGER', N'Quản lý cửa hàng');
END

IF NOT EXISTS (SELECT 1 FROM roles WHERE code = 'CASHIER')
BEGIN
    INSERT INTO roles (code, name) VALUES ('CASHIER', N'Thu ngân');
END

-- Đảm bảo cột latitude, longitude tồn tại trong bảng addresses (đã có ở V4, kiểm tra để tương thích ngược)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('addresses') AND name = 'latitude')
BEGIN
    ALTER TABLE addresses ADD latitude DECIMAL(10,7) NULL;
END

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('addresses') AND name = 'longitude')
BEGIN
    ALTER TABLE addresses ADD longitude DECIMAL(10,7) NULL;
END

