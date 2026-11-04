-- V28: Dữ liệu mẫu các cửa hàng bán lẻ và kho tổng thương hiệu Triết Store tại TP.HCM
-- Gồm 1 kho tổng (Tân Bình) và 3 chi nhánh bán lẻ (Quận 1, Quận 3, Bình Thạnh)
-- Kèm tọa độ GPS thật để phục vụ tính khoảng cách theo công thức Haversine
-- Giữ nguyên toàn bộ mã chi nhánh (code: STORE-Q1, WH-TONG...), địa chỉ, tọa độ GPS

INSERT INTO stores (code, name, type, province, district, ward, detail_address, latitude, longitude, phone, status, created_at, updated_at)
VALUES 
(
    'WH-TONG', 
    N'Kho tổng Triết Store', 
    'WAREHOUSE', 
    N'Thành phố Hồ Chí Minh', 
    N'Quận Tân Bình', 
    N'Phường 2', 
    N'158 Bạch Đằng', 
    10.8172000, 
    106.6653000, 
    '02838441111', 
    'ACTIVE', 
    GETDATE(), 
    GETDATE()
),
(
    'STORE-Q1', 
    N'Triết Store - Quận 1', 
    'STORE', 
    N'Thành phố Hồ Chí Minh', 
    N'Quận 1', 
    N'Phường Bến Thành', 
    N'125 Nguyễn Trãi', 
    10.7705000, 
    106.6912000, 
    '02838332222', 
    'ACTIVE', 
    GETDATE(), 
    GETDATE()
),
(
    'STORE-Q3', 
    N'Triết Store - Quận 3', 
    'STORE', 
    N'Thành phố Hồ Chí Minh', 
    N'Quận 3', 
    N'Phường 10', 
    N'450 Cách Mạng Tháng 8', 
    10.7798000, 
    106.6741000, 
    '02838333333', 
    'ACTIVE', 
    GETDATE(), 
    GETDATE()
),
(
    'STORE-BT', 
    N'Triết Store - Bình Thạnh', 
    'STORE', 
    N'Thành phố Hồ Chí Minh', 
    N'Quận Bình Thạnh', 
    N'Phường 25', 
    N'602 Điện Biên Phủ', 
    10.8016000, 
    106.7142000, 
    '02838334444', 
    'ACTIVE', 
    GETDATE(), 
    GETDATE()
);
