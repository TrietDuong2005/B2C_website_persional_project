-- V29: Dữ liệu mẫu danh mục cây cha-con, thương hiệu, 30 sản phẩm, biến thể, ảnh và tồn kho các cửa hàng

-- 1. DANH MỤC CHA VÀ CON
INSERT INTO categories (parent_id, name, slug, sort_order, is_active, created_at, updated_at)
VALUES 
(NULL, N'Thời trang nam', 'thoi-trang-nam', 1, 1, GETDATE(), GETDATE()),
(NULL, N'Thời trang nữ', 'thoi-trang-nu', 2, 1, GETDATE(), GETDATE()),
(NULL, N'Giày dép', 'giay-dep', 3, 1, GETDATE(), GETDATE()),
(NULL, N'Phụ kiện', 'phu-kien', 4, 1, GETDATE(), GETDATE());

-- Danh mục con
DECLARE @cat_nam BIGINT = (SELECT id FROM categories WHERE slug = 'thoi-trang-nam');
DECLARE @cat_nu BIGINT = (SELECT id FROM categories WHERE slug = 'thoi-trang-nu');
DECLARE @cat_giay BIGINT = (SELECT id FROM categories WHERE slug = 'giay-dep');
DECLARE @cat_phukien BIGINT = (SELECT id FROM categories WHERE slug = 'phu-kien');

INSERT INTO categories (parent_id, name, slug, sort_order, is_active, created_at, updated_at)
VALUES
(@cat_nam, N'Áo thun nam', 'ao-thun-nam', 1, 1, GETDATE(), GETDATE()),
(@cat_nam, N'Quần jean nam', 'quan-jean-nam', 2, 1, GETDATE(), GETDATE()),
(@cat_nam, N'Dây nịt nam', 'day-nit-nam', 3, 1, GETDATE(), GETDATE()),

(@cat_nu, N'Áo thun nữ', 'ao-thun-nu', 1, 1, GETDATE(), GETDATE()),
(@cat_nu, N'Quần jean nữ', 'quan-jean-nu', 2, 1, GETDATE(), GETDATE()),
(@cat_nu, N'Ví nữ', 'vi-nu', 3, 1, GETDATE(), GETDATE()),

(@cat_giay, N'Giày sneaker', 'giay-sneaker', 1, 1, GETDATE(), GETDATE()),
(@cat_giay, N'Dép thời trang', 'dep-thoi-trang', 2, 1, GETDATE(), GETDATE()),

(@cat_phukien, N'Ba lô', 'ba-lo', 1, 1, GETDATE(), GETDATE()),
(@cat_phukien, N'Ví nam', 'vi-nam', 2, 1, GETDATE(), GETDATE()),
(@cat_phukien, N'Nón thời trang', 'non-thoi-trang', 3, 1, GETDATE(), GETDATE()),
(@cat_phukien, N'Đồng hồ', 'dong-ho', 4, 1, GETDATE(), GETDATE());

-- 2. THƯƠNG HIỆU
INSERT INTO brands (name, slug, is_active, created_at, updated_at)
VALUES 
(N'Routine', 'routine', 1, GETDATE(), GETDATE()),
(N'Coolmate', 'coolmate', 1, GETDATE(), GETDATE()),
(N'Biti''s', 'bitis', 1, GETDATE(), GETDATE()),
(N'An Phước', 'an-phuoc', 1, GETDATE(), GETDATE()),
(N'Casio', 'casio', 1, GETDATE(), GETDATE()),
(N'Lee & Tee', 'lee-and-tee', 1, GETDATE(), GETDATE());

-- 3. 30 SẢN PHẨM CHA
DECLARE @b_routine BIGINT = (SELECT id FROM brands WHERE slug = 'routine');
DECLARE @b_coolmate BIGINT = (SELECT id FROM brands WHERE slug = 'coolmate');
DECLARE @b_bitis BIGINT = (SELECT id FROM brands WHERE slug = 'bitis');
DECLARE @b_anphuoc BIGINT = (SELECT id FROM brands WHERE slug = 'an-phuoc');
DECLARE @b_casio BIGINT = (SELECT id FROM brands WHERE slug = 'casio');
DECLARE @b_leetee BIGINT = (SELECT id FROM brands WHERE slug = 'lee-and-tee');

DECLARE @c_aothun_nam BIGINT = (SELECT id FROM categories WHERE slug = 'ao-thun-nam');
DECLARE @c_quanjean_nam BIGINT = (SELECT id FROM categories WHERE slug = 'quan-jean-nam');
DECLARE @c_daynit_nam BIGINT = (SELECT id FROM categories WHERE slug = 'day-nit-nam');
DECLARE @c_aothun_nu BIGINT = (SELECT id FROM categories WHERE slug = 'ao-thun-nu');
DECLARE @c_quanjean_nu BIGINT = (SELECT id FROM categories WHERE slug = 'quan-jean-nu');
DECLARE @c_vi_nu BIGINT = (SELECT id FROM categories WHERE slug = 'vi-nu');
DECLARE @c_giay_snk BIGINT = (SELECT id FROM categories WHERE slug = 'giay-sneaker');
DECLARE @c_dep BIGINT = (SELECT id FROM categories WHERE slug = 'dep-thoi-trang');
DECLARE @c_balo BIGINT = (SELECT id FROM categories WHERE slug = 'ba-lo');
DECLARE @c_vi_nam BIGINT = (SELECT id FROM categories WHERE slug = 'vi-nam');
DECLARE @c_non BIGINT = (SELECT id FROM categories WHERE slug = 'non-thoi-trang');
DECLARE @c_dongho BIGINT = (SELECT id FROM categories WHERE slug = 'dong-ho');

INSERT INTO products (category_id, brand_id, name, slug, description, thumbnail_url, status, created_at, updated_at)
VALUES
-- Áo thun nam (1-3)
(@c_aothun_nam, @b_coolmate, N'Áo thun nam Cotton Compact Coolmate', 'ao-thun-nam-cotton-compact-coolmate', N'Chất liệu 100% cotton compact mềm mại, thấm hút mồ hôi tối ưu.', 'https://placehold.co/600x600?text=Ao+Thun+Coolmate', 'ACTIVE', GETDATE(), GETDATE()),
(@c_aothun_nam, @b_routine, N'Áo thun nam cổ tròn basic Routine', 'ao-thun-nam-co-tron-basic-routine', N'Form dáng regular fit hiện đại, dễ phối đồ hàng ngày.', 'https://placehold.co/600x600?text=Ao+Thun+Routine', 'ACTIVE', GETDATE(), GETDATE()),
(@c_aothun_nam, @b_coolmate, N'Áo polo nam thể thao Coolmate', 'ao-polo-nam-the-thao-coolmate', N'Vải thể thao co giãn 4 chiều, nhanh khô, kháng khuẩn.', 'https://placehold.co/600x600?text=Polo+Coolmate', 'ACTIVE', GETDATE(), GETDATE()),

-- Áo thun nữ (4-6)
(@c_aothun_nu, @b_routine, N'Áo thun nữ dáng suông Routine', 'ao-thun-nu-dang-suong-routine', N'Áo thun phom rộng trẻ trung, phong cách năng động.', 'https://placehold.co/600x600?text=Ao+Thun+Nu+Routine', 'ACTIVE', GETDATE(), GETDATE()),
(@c_aothun_nu, @b_routine, N'Áo thun nữ croptop năng động', 'ao-thun-nu-croptop-nang-dong', N'Thiết kế croptop thời thượng, tôn dáng phái đẹp.', 'https://placehold.co/600x600?text=Croptop+Nu', 'ACTIVE', GETDATE(), GETDATE()),
(@c_aothun_nu, @b_anphuoc, N'Áo polo nữ thêu họa tiết An Phước', 'ao-polo-nu-theu-hoa-tiet-an-phuoc', N'Polo thanh lịch, phom dáng chuẩn công sở trẻ.', 'https://placehold.co/600x600?text=Polo+Nu+An+Phuoc', 'ACTIVE', GETDATE(), GETDATE()),

-- Quần jean nam (7-9)
(@c_quanjean_nam, @b_routine, N'Quần jean nam Slim Fit Routine', 'quan-jean-nam-slim-fit-routine', N'Chất denim co giãn nhẹ, ôm vừa vặn và tôn chiều cao.', 'https://placehold.co/600x600?text=Jean+Nam+Slimfit', 'ACTIVE', GETDATE(), GETDATE()),
(@c_quanjean_nam, @b_routine, N'Quần jean nam ống suông Straight', 'quan-jean-nam-ong-suong-straight', N'Kiểu dáng ống đứng cổ điển, thoải mái cả ngày.', 'https://placehold.co/600x600?text=Jean+Nam+Straight', 'ACTIVE', GETDATE(), GETDATE()),
(@c_quanjean_nam, @b_coolmate, N'Quần jean nam co giãn Coolmate Copper Denim', 'quan-jean-nam-coolmate-copper-denim', N'Denim dệt kỹ thuật số, siêu bền và giữ phom.', 'https://placehold.co/600x600?text=Jean+Coolmate', 'ACTIVE', GETDATE(), GETDATE()),

-- Quần jean nữ (10-12)
(@c_quanjean_nu, @b_routine, N'Quần jean nữ cạp cao Skinny Routine', 'quan-jean-nu-cap-cao-skinny-routine', N'Cạp cao ôm sát tạo đường cong quyến rũ, co giãn tốt.', 'https://placehold.co/600x600?text=Jean+Nu+Skinny', 'ACTIVE', GETDATE(), GETDATE()),
(@c_quanjean_nu, @b_routine, N'Quần jean nữ ống loe Retro', 'quan-jean-nu-ong-loe-retro', N'Phong cách vintage thanh lịch thập niên 90.', 'https://placehold.co/600x600?text=Jean+Nu+Ong+Loe', 'ACTIVE', GETDATE(), GETDATE()),
(@c_quanjean_nu, @b_routine, N'Quần short jean nữ trẻ trung', 'quan-short-jean-nu-tre-trung', N'Short jean năng động cho mùa hè rực rỡ.', 'https://placehold.co/600x600?text=Short+Jean+Nu', 'ACTIVE', GETDATE(), GETDATE()),

-- Giày sneaker (13-15)
(@c_giay_snk, @b_bitis, N'Giày sneaker Biti''s Hunter Street Nam', 'giay-sneaker-bitis-hunter-street-nam', N'Đế LiteFLEX siêu nhẹ, êm ái, bám đường cực tốt.', 'https://placehold.co/600x600?text=Bitis+Hunter+Street', 'ACTIVE', GETDATE(), GETDATE()),
(@c_giay_snk, @b_bitis, N'Giày sneaker Biti''s Hunter X Nữ', 'giay-sneaker-bitis-hunter-x-nu', N'Đế bọt khí định hình chuyển động, êm chân suốt ngày dài.', 'https://placehold.co/600x600?text=Bitis+Hunter+X', 'ACTIVE', GETDATE(), GETDATE()),
(@c_giay_snk, @b_routine, N'Giày sneaker Canvas cổ thấp Casual', 'giay-sneaker-canvas-co-thap-casual', N'Chất vải Canvas bền màu, lót đệm êm, kiểu dáng thanh thoát.', 'https://placehold.co/600x600?text=Sneaker+Canvas', 'ACTIVE', GETDATE(), GETDATE()),

-- Dép thời trang (16-18)
(@c_dep, @b_bitis, N'Dép quai ngang EVA Biti''s', 'dep-quai-ngang-eva-bitis', N'Chất liệu mút đúc EVA êm chân, chống nước tuyệt đối.', 'https://placehold.co/600x600?text=Dep+Quai+Ngang+Bitis', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dep, @b_coolmate, N'Dép xỏ ngón đi biển Coolmate', 'dep-xo-ngon-di-bien-coolmate', N'Dép xỏ ngón cao su tự nhiên siêu bám, nhẹ nhàng.', 'https://placehold.co/600x600?text=Dep+Xo+Ngon+Coolmate', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dep, @b_bitis, N'Dép Sandal quai dán năng động', 'dep-sandal-quai-dan-nang-dong', N'Sandal quai dù bền bỉ, thích hợp đi học, đi làm, dạo phố.', 'https://placehold.co/600x600?text=Sandal+Bitis', 'ACTIVE', GETDATE(), GETDATE()),

-- Ba lô (19-21)
(@c_balo, @b_coolmate, N'Ba lô laptop chống nước Coolmate', 'ba-lo-laptop-chong-nuoc-coolmate', N'Chứa vừa laptop 15.6 inch, kháng nước trượt mưa hoàn hảo.', 'https://placehold.co/600x600?text=Balo+Coolmate', 'ACTIVE', GETDATE(), GETDATE()),
(@c_balo, @b_leetee, N'Ba lô mini da nữ Lee & Tee', 'ba-lo-mini-da-nu-lee-and-tee', N'Chất da tổng hợp cao cấp, vân da tự nhiên, nhỏ xinh cá tính.', 'https://placehold.co/600x600?text=Balo+Mini+LeeTee', 'ACTIVE', GETDATE(), GETDATE()),
(@c_balo, @b_routine, N'Ba lô du lịch thể thao Casual Routine', 'ba-lo-du-lich-the-thao-casual-routine', N'Thể tích 25L, nhiều ngăn tiện dụng cho các chuyến đi chơi.', 'https://placehold.co/600x600?text=Balo+Du+Lich+Routine', 'ACTIVE', GETDATE(), GETDATE()),

-- Ví (22-24)
(@c_vi_nam, @b_anphuoc, N'Ví da nam đứng cao cấp An Phước', 'vi-da-nam-dung-cao-cap-an-phuoc', N'Da bò thật 100%, đường may tinh xảo, chống trầy xước.', 'https://placehold.co/600x600?text=Vi+Nam+An+Phuoc', 'ACTIVE', GETDATE(), GETDATE()),
(@c_vi_nam, @b_leetee, N'Ví da nam dáng ngang Lee & Tee', 'vi-da-nam-dang-ngang-lee-and-tee', N'Dáng ngang cổ điển, nhiều ngăn để thẻ và tiền mặt.', 'https://placehold.co/600x600?text=Vi+Nam+LeeTee', 'ACTIVE', GETDATE(), GETDATE()),
(@c_vi_nu, @b_leetee, N'Ví cầm tay nữ dài Lee & Tee', 'vi-cam-tay-nu-dai-lee-and-tee', N'Khóa kéo kim loại sáng bóng, thanh lịch và quý phái.', 'https://placehold.co/600x600?text=Vi+Nu+LeeTee', 'ACTIVE', GETDATE(), GETDATE()),

-- Dây nịt (25-26)
(@c_daynit_nam, @b_anphuoc, N'Dây nịt nam da bò khóa kim An Phước', 'day-nit-nam-da-bo-khoa-kim-an-phuoc', N'Mặt khóa hợp kim sáng bóng, dây da bò 2 lớp dẻo dai.', 'https://placehold.co/600x600?text=Day+Nit+Khoa+Kim', 'ACTIVE', GETDATE(), GETDATE()),
(@c_daynit_nam, @b_leetee, N'Dây nịt nam khóa tự động lịch lãm', 'day-nit-nam-khoa-tu-dong-lich-lam', N'Khóa ray tự động tiện lợi, tăng giảm kích thước dễ dàng.', 'https://placehold.co/600x600?text=Day+Nit+Tu+Dong', 'ACTIVE', GETDATE(), GETDATE()),

-- Nón (27-28)
(@c_non, @b_coolmate, N'Nón lưỡi trai bóng chày Coolmate', 'non-luoi-trai-bong-chay-coolmate', N'Chất kaki thoáng khí, khóa kim loại điều chỉnh size linh hoạt.', 'https://placehold.co/600x600?text=Non+Luoi+Trai', 'ACTIVE', GETDATE(), GETDATE()),
(@c_non, @b_routine, N'Nón bucket vành tròn cá tính Routine', 'non-bucket-vanh-tron-ca-tinh-routine', N'Thiết kế vành tròn trẻ trung, bảo vệ chống nắng hiệu quả.', 'https://placehold.co/600x600?text=Non+Bucket', 'ACTIVE', GETDATE(), GETDATE()),

-- Đồng hồ (29-30)
(@c_dongho, @b_casio, N'Đồng hồ nam Casio Edifice Chronograph', 'dong-ho-nam-casio-edifice-chronograph', N'Mặt kính khoáng cường lực, chống nước 100m, pin 3 năm.', 'https://placehold.co/600x600?text=Casio+Edifice', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dongho, @b_casio, N'Đồng hồ nữ Casio LTP dây kim loại', 'dong-ho-nu-casio-ltp-day-kim-loai', N'Thiết kế nhỏ gọn, mặt số đính đá tinh tế cho phái đẹp.', 'https://placehold.co/600x600?text=Casio+LTP', 'ACTIVE', GETDATE(), GETDATE());

-- 4. BIẾN THỂ SẢN PHẨM (PRODUCT_VARIANTS)
-- Mỗi sản phẩm tạo từ 2 đến 4 biến thể (màu sắc/kích cỡ)

-- Bảng tạm hỗ trợ tạo biến thể
CREATE TABLE #TempVariants (
    product_slug NVARCHAR(300),
    sku NVARCHAR(100),
    barcode NVARCHAR(100),
    color NVARCHAR(50),
    size NVARCHAR(50),
    price DECIMAL(18,2),
    cost_price DECIMAL(18,2),
    weight_gram INT
);

INSERT INTO #TempVariants (product_slug, sku, barcode, color, size, price, cost_price, weight_gram)
VALUES
-- Áo thun Coolmate
('ao-thun-nam-cotton-compact-coolmate', 'TS-CM-BLK-M', '8936001001', N'Đen', 'M', 189000.00, 95000.00, 200),
('ao-thun-nam-cotton-compact-coolmate', 'TS-CM-BLK-L', '8936001002', N'Đen', 'L', 189000.00, 95000.00, 210),
('ao-thun-nam-cotton-compact-coolmate', 'TS-CM-WHT-M', '8936001003', N'Trắng', 'M', 189000.00, 95000.00, 200),
('ao-thun-nam-cotton-compact-coolmate', 'TS-CM-WHT-L', '8936001004', N'Trắng', 'L', 189000.00, 95000.00, 210),

-- Áo thun Routine
('ao-thun-nam-co-tron-basic-routine', 'TS-RT-NVY-M', '8936001005', N'Xanh Navy', 'M', 220000.00, 110000.00, 200),
('ao-thun-nam-co-tron-basic-routine', 'TS-RT-NVY-L', '8936001006', N'Xanh Navy', 'L', 220000.00, 110000.00, 210),
('ao-thun-nam-co-tron-basic-routine', 'TS-RT-GRY-L', '8936001007', N'Xám', 'L', 220000.00, 110000.00, 210),

-- Polo Coolmate
('ao-polo-nam-the-thao-coolmate', 'PO-CM-BLK-M', '8936001008', N'Đen', 'M', 299000.00, 150000.00, 250),
('ao-polo-nam-the-thao-coolmate', 'PO-CM-BLK-L', '8936001009', N'Đen', 'L', 299000.00, 150000.00, 260),
('ao-polo-nam-the-thao-coolmate', 'PO-CM-BLU-L', '8936001010', N'Xanh Dương', 'L', 299000.00, 150000.00, 260),

-- Áo thun nữ Routine
('ao-thun-nu-dang-suong-routine', 'TS-NU-RT-BE-S', '8936001011', N'Be', 'S', 210000.00, 105000.00, 180),
('ao-thun-nu-dang-suong-routine', 'TS-NU-RT-BE-M', '8936001012', N'Be', 'M', 210000.00, 105000.00, 190),
('ao-thun-nu-dang-suong-routine', 'TS-NU-RT-PNK-S', '8936001013', N'Hồng', 'S', 210000.00, 105000.00, 180),

-- Áo croptop nữ
('ao-thun-nu-croptop-nang-dong', 'CP-NU-WHT-S', '8936001014', N'Trắng', 'S', 165000.00, 80000.00, 150),
('ao-thun-nu-croptop-nang-dong', 'CP-NU-BLK-S', '8936001015', N'Đen', 'S', 165000.00, 80000.00, 150),

-- Polo nữ An Phước
('ao-polo-nu-theu-hoa-tiet-an-phuoc', 'PO-AP-RED-M', '8936001016', N'Đỏ Đô', 'M', 450000.00, 220000.00, 230),
('ao-polo-nu-theu-hoa-tiet-an-phuoc', 'PO-AP-WHT-M', '8936001017', N'Trắng', 'M', 450000.00, 220000.00, 230),

-- Quần jean nam Slim Fit
('quan-jean-nam-slim-fit-routine', 'JN-RT-BLU-29', '8936001018', N'Xanh Đậm', '29', 499000.00, 250000.00, 500),
('quan-jean-nam-slim-fit-routine', 'JN-RT-BLU-30', '8936001019', N'Xanh Đậm', '30', 499000.00, 250000.00, 520),
('quan-jean-nam-slim-fit-routine', 'JN-RT-BLK-30', '8936001020', N'Đen Khói', '30', 499000.00, 250000.00, 520),

-- Quần jean nam Straight
('quan-jean-nam-ong-suong-straight', 'JN-ST-LBL-30', '8936001021', N'Xanh Nhạt', '30', 520000.00, 260000.00, 550),
('quan-jean-nam-ong-suong-straight', 'JN-ST-LBL-31', '8936001022', N'Xanh Nhạt', '31', 520000.00, 260000.00, 560),

-- Quần jean Coolmate
('quan-jean-nam-coolmate-copper-denim', 'JN-CM-DRK-30', '8936001023', N'Chàm Đậm', '30', 599000.00, 300000.00, 510),
('quan-jean-nam-coolmate-copper-denim', 'JN-CM-DRK-31', '8936001024', N'Chàm Đậm', '31', 599000.00, 300000.00, 520),

-- Quần jean nữ Skinny
('quan-jean-nu-cap-cao-skinny-routine', 'JN-NU-SKN-26', '8936001025', N'Xanh Đậm', '26', 420000.00, 210000.00, 450),
('quan-jean-nu-cap-cao-skinny-routine', 'JN-NU-SKN-27', '8936001026', N'Xanh Đậm', '27', 420000.00, 210000.00, 460),

-- Quần jean nữ Retro
('quan-jean-nu-ong-loe-retro', 'JN-NU-RET-27', '8936001027', N'Xanh Cổ Điển', '27', 480000.00, 240000.00, 480),
('quan-jean-nu-ong-loe-retro', 'JN-NU-RET-28', '8936001028', N'Xanh Cổ Điển', '28', 480000.00, 240000.00, 490),

-- Quần short jean nữ
('quan-short-jean-nu-tre-trung', 'SH-NU-BLU-S', '8936001029', N'Xanh Nhạt', 'S', 280000.00, 140000.00, 300),
('quan-short-jean-nu-tre-trung', 'SH-NU-BLU-M', '8936001030', N'Xanh Nhạt', 'M', 280000.00, 140000.00, 310),

-- Sneaker Biti's Hunter Street
('giay-sneaker-bitis-hunter-street-nam', 'SN-BT-WHT-40', '8936001031', N'Trắng', '40', 899000.00, 450000.00, 700),
('giay-sneaker-bitis-hunter-street-nam', 'SN-BT-WHT-41', '8936001032', N'Trắng', '41', 899000.00, 450000.00, 720),
('giay-sneaker-bitis-hunter-street-nam', 'SN-BT-BLK-40', '8936001033', N'Đen', '40', 899000.00, 450000.00, 700),

-- Sneaker Biti's Hunter X
('giay-sneaker-bitis-hunter-x-nu', 'SN-BT-PNK-36', '8936001034', N'Hồng Pastel', '36', 999000.00, 500000.00, 650),
('giay-sneaker-bitis-hunter-x-nu', 'SN-BT-PNK-37', '8936001035', N'Hồng Pastel', '37', 999000.00, 500000.00, 670),

-- Giày Canvas
('giay-sneaker-canvas-co-thap-casual', 'SN-CV-BLK-39', '8936001036', N'Đen', '39', 350000.00, 175000.00, 600),
('giay-sneaker-canvas-co-thap-casual', 'SN-CV-BLK-40', '8936001037', N'Đen', '40', 350000.00, 175000.00, 610),

-- Dép quai ngang Biti's
('dep-quai-ngang-eva-bitis', 'DP-BT-BLK-39', '8936001038', N'Đen', '39', 150000.00, 70000.00, 300),
('dep-quai-ngang-eva-bitis', 'DP-BT-BLK-40', '8936001039', N'Đen', '40', 150000.00, 70000.00, 310),

-- Dép xỏ ngón Coolmate
('dep-xo-ngon-di-bien-coolmate', 'DP-CM-NVY-40', '8936001040', N'Xanh Navy', '40', 120000.00, 55000.00, 250),
('dep-xo-ngon-di-bien-coolmate', 'DP-CM-NVY-41', '8936001041', N'Xanh Navy', '41', 120000.00, 55000.00, 260),

-- Sandal quai dán
('dep-sandal-quai-dan-nang-dong', 'SD-BT-GRY-39', '8936001042', N'Xám', '39', 280000.00, 140000.00, 450),
('dep-sandal-quai-dan-nang-dong', 'SD-BT-GRY-40', '8936001043', N'Xám', '40', 280000.00, 140000.00, 460),

-- Balo Coolmate
('ba-lo-laptop-chong-nuoc-coolmate', 'BL-CM-BLK-STD', '8936001044', N'Đen', 'Standard', 499000.00, 250000.00, 800),
('ba-lo-laptop-chong-nuoc-coolmate', 'BL-CM-GRY-STD', '8936001045', N'Xám', 'Standard', 499000.00, 250000.00, 800),

-- Balo mini Lee & Tee
('ba-lo-mini-da-nu-lee-and-tee', 'BL-LT-BRN-STD', '8936001046', N'Nâu Bò', 'Standard', 550000.00, 280000.00, 600),
('ba-lo-mini-da-nu-lee-and-tee', 'BL-LT-BLK-STD', '8936001047', N'Đen', 'Standard', 550000.00, 280000.00, 600),

-- Balo Routine
('ba-lo-du-lich-the-thao-casual-routine', 'BL-RT-BLK-STD', '8936001048', N'Đen', 'Standard', 420000.00, 210000.00, 750),

-- Ví nam đứng An Phước
('vi-da-nam-dung-cao-cap-an-phuoc', 'VI-AP-BRN-STD', '8936001049', N'Nâu Sẫm', 'Standard', 650000.00, 320000.00, 150),
('vi-da-nam-dung-cao-cap-an-phuoc', 'VI-AP-BLK-STD', '8936001050', N'Đen', 'Standard', 650000.00, 320000.00, 150),

-- Ví nam ngang Lee & Tee
('vi-da-nam-dang-ngang-lee-and-tee', 'VI-LT-BRN-STD', '8936001051', N'Nâu Bò', 'Standard', 395000.00, 190000.00, 130),
('vi-da-nam-dang-ngang-lee-and-tee', 'VI-LT-BLK-STD', '8936001052', N'Đen', 'Standard', 395000.00, 190000.00, 130),

-- Ví nữ Lee & Tee
('vi-cam-tay-nu-dai-lee-and-tee', 'VI-LT-RED-STD', '8936001053', N'Đỏ Rượu', 'Standard', 495000.00, 240000.00, 200),
('vi-cam-tay-nu-dai-lee-and-tee', 'VI-LT-BEG-STD', '8936001054', N'Màu Be', 'Standard', 495000.00, 240000.00, 200),

-- Dây nịt khóa kim An Phước
('day-nit-nam-da-bo-khoa-kim-an-phuoc', 'DN-AP-BLK-115', '8936001055', N'Đen', '115cm', 550000.00, 270000.00, 220),
('day-nit-nam-da-bo-khoa-kim-an-phuoc', 'DN-AP-BRN-115', '8936001056', N'Nâu', '115cm', 550000.00, 270000.00, 220),

-- Dây nịt khóa tự động
('day-nit-nam-khoa-tu-dong-lich-lam', 'DN-TD-BLK-120', '8936001057', N'Đen Bạc', '120cm', 380000.00, 190000.00, 230),

-- Nón lưỡi trai Coolmate
('non-luoi-trai-bong-chay-coolmate', 'NN-CM-BLK-FRE', '8936001058', N'Đen', 'Freesize', 149000.00, 70000.00, 120),
('non-luoi-trai-bong-chay-coolmate', 'NN-CM-WHT-FRE', '8936001059', N'Trắng', 'Freesize', 149000.00, 70000.00, 120),

-- Nón bucket Routine
('non-bucket-vanh-tron-ca-tinh-routine', 'NN-RT-BE-FRE', '8936001060', N'Be', 'Freesize', 190000.00, 90000.00, 140),

-- Đồng hồ Casio Edifice
('dong-ho-nam-casio-edifice-chronograph', 'DH-CS-SLV-STD', '8936001061', N'Bạc Mặt Xanh', 'Standard', 3450000.00, 2200000.00, 180),
('dong-ho-nam-casio-edifice-chronograph', 'DH-CS-BLK-STD', '8936001062', N'Đen Thể Thao', 'Standard', 3600000.00, 2300000.00, 180),

-- Đồng hồ Casio nữ LTP
('dong-ho-nu-casio-ltp-day-kim-loai', 'DH-CS-GLD-STD', '8936001063', N'Vàng Hồng', 'Standard', 1850000.00, 1100000.00, 120),
('dong-ho-nu-casio-ltp-day-kim-loai', 'DH-CS-SLV-FEM', '8936001064', N'Bạc Tinh Tế', 'Standard', 1650000.00, 1000000.00, 120);

-- Chèn vào bảng product_variants thật
INSERT INTO product_variants (product_id, sku, barcode, color, size, price, cost_price, weight_gram, status, created_at, updated_at)
SELECT 
    p.id,
    t.sku,
    t.barcode,
    t.color,
    t.size,
    t.price,
    t.cost_price,
    t.weight_gram,
    'ACTIVE',
    GETDATE(),
    GETDATE()
FROM #TempVariants t
INNER JOIN products p ON p.slug = t.product_slug;

DROP TABLE #TempVariants;

-- 5. HÌNH ẢNH SẢN PHẨM (PRODUCT_IMAGES)
INSERT INTO product_images (product_id, variant_id, url, sort_order, is_thumbnail, created_at, updated_at)
SELECT 
    p.id,
    NULL,
    p.thumbnail_url,
    0,
    1,
    GETDATE(),
    GETDATE()
FROM products p;

-- 6. TỒN KHO THEO TỪNG CỬA HÀNG (INVENTORIES)
-- Tạo tồn kho cho tất cả biến thể tại 4 điểm (1 kho tổng, 3 cửa hàng)
-- Có các biến thể tồn = 0 (hết hàng) hoặc tồn < 5 (dưới mức reorder_level) để kiểm thử chức năng cảnh báo
INSERT INTO inventories (variant_id, store_id, quantity_on_hand, reorder_level, version, created_at, updated_at)
SELECT 
    v.id,
    s.id,
    CASE 
        -- Kho tổng Tân Bình: dồi dào hàng hóa
        WHEN s.code = 'WH-TONG' THEN 100
        -- Cửa hàng Q1: một số biến thể hết hàng hoặc tồn cực thấp để test
        WHEN s.code = 'STORE-Q1' AND v.id % 9 = 0 THEN 0  -- Hết hàng
        WHEN s.code = 'STORE-Q1' AND v.id % 5 = 0 THEN 2  -- Tồn thấp cảnh báo
        -- Cửa hàng Q3: một số biến thể khác hết hàng
        WHEN s.code = 'STORE-Q3' AND v.id % 8 = 0 THEN 0  -- Hết hàng
        WHEN s.code = 'STORE-Q3' AND v.id % 6 = 0 THEN 3  -- Tồn thấp
        -- Cửa hàng Bình Thạnh
        WHEN s.code = 'STORE-BT' AND v.id % 7 = 0 THEN 0  -- Hết hàng
        ELSE 12 + (v.id % 15)
    END,
    5,
    0,
    GETDATE(),
    GETDATE()
FROM product_variants v
CROSS JOIN stores s;

