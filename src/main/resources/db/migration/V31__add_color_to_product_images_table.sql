-- V31: Thêm cột color vào bảng product_images để hỗ trợ gắn ảnh theo màu sắc
-- Cho phép thêm MỘT ảnh cho một màu thì mọi size của màu đó dùng chung ảnh.
-- Cùng kiểu và độ dài với cột product_variants.color (NVARCHAR(50) NULL).
-- Giữ nguyên toàn bộ dữ liệu cũ (color nhận giá trị NULL cho ảnh cũ).

ALTER TABLE product_images ADD color NVARCHAR(50) NULL;

CREATE INDEX IX_product_images_color ON product_images(color);

