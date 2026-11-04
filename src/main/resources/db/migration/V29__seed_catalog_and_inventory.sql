-- ==============================================================================
-- V29: Dữ liệu mẫu chuỗi bán lẻ giày dép Triết Store
-- Cấu trúc:
--   1. Danh mục 2 cấp: 2 danh mục gốc ("Giày", "Dép") và 8 danh mục con
--   2. 8 Thương hiệu: Nike, Adidas, Converse, Vans, Biti's, Thượng Đình, Crocs, Dr. Martens
--   3. 30 Sản phẩm cha (đối tượng Nam / Nữ / Unisex, đúng 2 sản phẩm HIDDEN để test bộ lọc)
--   4. 120 Biến thể sản phẩm (CROSS JOIN màu và size EU 36-45, SKU chuẩn, barcode 13 số)
--   5. 60 Dòng ảnh sản phẩm (2 ảnh / sản phẩm, dùng /images/placeholder-shoe.svg)
--   6. 480 Dòng tồn kho tại 4 điểm (1 kho tổng, 3 cửa hàng; công thức xác định lặp lại được)
-- KHÔNG seed inventory_transactions (sẽ quản lý ở ngày 4).
-- ==============================================================================

-- 1. DANH MỤC 2 CẤP (GỐC VÀ LÁ)
INSERT INTO categories (parent_id, name, slug, sort_order, is_active, created_at, updated_at)
VALUES 
(NULL, N'Giày', 'giay', 1, 1, GETDATE(), GETDATE()),
(NULL, N'Dép', 'dep', 2, 1, GETDATE(), GETDATE());

DECLARE @cat_giay BIGINT = (SELECT id FROM categories WHERE slug = 'giay');
DECLARE @cat_dep BIGINT  = (SELECT id FROM categories WHERE slug = 'dep');

-- Danh mục lá
INSERT INTO categories (parent_id, name, slug, sort_order, is_active, created_at, updated_at)
VALUES
-- Danh mục con của Giày
(@cat_giay, N'Sneaker', 'sneaker', 1, 1, GETDATE(), GETDATE()),
(@cat_giay, N'Giày da', 'giay-da', 2, 1, GETDATE(), GETDATE()),
(@cat_giay, N'Giày boots', 'giay-boots', 3, 1, GETDATE(), GETDATE()),
(@cat_giay, N'Giày lười', 'giay-luoi', 4, 1, GETDATE(), GETDATE()),
(@cat_giay, N'Giày thể thao', 'giay-the-thao', 5, 1, GETDATE(), GETDATE()),

-- Danh mục con của Dép
(@cat_dep, N'Sandal', 'sandal', 1, 1, GETDATE(), GETDATE()),
(@cat_dep, N'Dép kẹp', 'dep-kep', 2, 1, GETDATE(), GETDATE()),
(@cat_dep, N'Dép slipper', 'dep-slipper', 3, 1, GETDATE(), GETDATE());

-- 2. 8 THƯƠNG HIỆU GIÀY DÉP
INSERT INTO brands (name, slug, is_active, created_at, updated_at)
VALUES 
(N'Nike', 'nike', 1, GETDATE(), GETDATE()),
(N'Adidas', 'adidas', 1, GETDATE(), GETDATE()),
(N'Converse', 'converse', 1, GETDATE(), GETDATE()),
(N'Vans', 'vans', 1, GETDATE(), GETDATE()),
(N'Biti''s', 'bitis', 1, GETDATE(), GETDATE()),
(N'Thượng Đình', 'thuong-dinh', 1, GETDATE(), GETDATE()),
(N'Crocs', 'crocs', 1, GETDATE(), GETDATE()),
(N'Dr. Martens', 'dr-martens', 1, GETDATE(), GETDATE());

-- 3. 30 SẢN PHẨM CHA
DECLARE @b_nike BIGINT    = (SELECT id FROM brands WHERE slug = 'nike');
DECLARE @b_adidas BIGINT  = (SELECT id FROM brands WHERE slug = 'adidas');
DECLARE @b_conv BIGINT    = (SELECT id FROM brands WHERE slug = 'converse');
DECLARE @b_vans BIGINT    = (SELECT id FROM brands WHERE slug = 'vans');
DECLARE @b_bitis BIGINT   = (SELECT id FROM brands WHERE slug = 'bitis');
DECLARE @b_tdinh BIGINT   = (SELECT id FROM brands WHERE slug = 'thuong-dinh');
DECLARE @b_crocs BIGINT   = (SELECT id FROM brands WHERE slug = 'crocs');
DECLARE @b_drma BIGINT    = (SELECT id FROM brands WHERE slug = 'dr-martens');

DECLARE @c_snk BIGINT     = (SELECT id FROM categories WHERE slug = 'sneaker');
DECLARE @c_gda BIGINT     = (SELECT id FROM categories WHERE slug = 'giay-da');
DECLARE @c_gbt BIGINT     = (SELECT id FROM categories WHERE slug = 'giay-boots');
DECLARE @c_glu BIGINT     = (SELECT id FROM categories WHERE slug = 'giay-luoi');
DECLARE @c_gtt BIGINT     = (SELECT id FROM categories WHERE slug = 'giay-the-thao');
DECLARE @c_sdl BIGINT     = (SELECT id FROM categories WHERE slug = 'sandal');
DECLARE @c_dkp BIGINT     = (SELECT id FROM categories WHERE slug = 'dep-kep');
DECLARE @c_dsl BIGINT     = (SELECT id FROM categories WHERE slug = 'dep-slipper');

INSERT INTO products (category_id, brand_id, name, slug, description, thumbnail_url, status, created_at, updated_at)
VALUES
-- 1. Sneaker (4 sản phẩm)
(@c_snk, @b_nike, N'Giày Sneaker Nike Air Force 1 Nam', 'giay-sneaker-nike-air-force-1-nam', 
 N'Thiết kế kinh điển với đệm khí Nike Air êm ái, chất da bền đẹp. Phù hợp phong cách đường phố năng động hàng ngày.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_snk, @b_adidas, N'Giày Sneaker Adidas Stan Smith Unisex', 'giay-sneaker-adidas-stan-smith-unisex', 
 N'Biểu tượng tối giản với thân giày da mềm Primegreen thân thiện môi trường. Dễ dàng phối cùng mọi trang phục thường nhật.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_snk, @b_conv, N'Giày Sneaker Converse Chuck 70 Unisex', 'giay-sneaker-converse-chuck-70-unisex', 
 N'Chất vải Canvas dày dặn 12oz, đệm lót OrthoLite cải tiến êm chân tối đa. Phong cách vintage vượt thời gian.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_snk, @b_vans, N'Giày Sneaker Vans Old Skool Unisex', 'giay-sneaker-vans-old-skool-unisex', 
 N'Sọc lượn sóng Jazz Stripe đặc trưng kết hợp da lộn và vải canvas bền bỉ. Đế cao su bánh quế bám đường chống trơn trượt.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 2. Giày da (4 sản phẩm)
(@c_gda, @b_bitis, N'Giày da Oxford Biti''s Nam', 'giay-da-oxford-bitis-nam', 
 N'Chất liệu da bò thật cao cấp xử lý bóng mờ lịch lãm. Thiết kế đóng nẹp chuẩn mực cho các sự kiện trang trọng và công sở.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gda, @b_drma, N'Giày da Derby Dr. Martens 1461 Nam', 'giay-da-derby-dr-martens-1461-nam', 
 N'Giày da 3 lỗ xỏ kinh điển với đường chỉ vàng viền đế nổi bật. Da Smooth cao cấp chống mài mòn và đệm khí AirWair đàn hồi.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gda, @b_bitis, N'Giày da Brogue Cổ Điển Nam', 'giay-da-brogue-co-dien-nam', 
 N'Họa tiết đục lỗ Wingtip tinh xảo mang phong cách quý ông cổ điển. Phù hợp phối cùng vest và quần âu sang trọng.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gda, @b_bitis, N'Giày da Cao Gót Nữ Công Sở', 'giay-da-cao-got-nu-cong-so', 
 N'Gót nhọn thanh mảnh cao 5cm vững chãi, chất da mềm êm ái nâng niu bàn chân. Tôn vinh nét duyên dáng của phái đẹp.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 3. Giày boots (4 sản phẩm)
(@c_gbt, @b_drma, N'Giày Boots Dr. Martens 1460 8 Lỗ Unisex', 'giay-boots-dr-martens-1460-8-lo-unisex', 
 N'Đôi boots 8 lỗ huyền thoại gắn liền với văn hóa cá tính và nổi loạn. Cấu trúc đế Goodyear welted siêu bền chịu lực hoàn hảo.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gbt, @b_drma, N'Giày Chelsea Boots Da Nam', 'giay-chelsea-boots-da-nam', 
 N'Thiết kế cổ chun co giãn tiện lợi không cần buộc dây, phom dáng thon gọn. Sản phẩm ẩn tạm thời để kiểm thử bộ lọc.', 
 '/images/placeholder-shoe.svg', 'HIDDEN', GETDATE(), GETDATE()), -- [HIDDEN 1]
(@c_gbt, @b_drma, N'Giày Combat Boots Khóa Kéo Nữ', 'giay-combat-boots-khoa-keo-nu', 
 N'Đế hầm hố tôn chiều cao cùng khóa kéo bên hông tháo xỏ nhanh chóng. Phong cách năng động, thời thượng cho bạn nữ.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gbt, @b_vans, N'Giày Boots Cổ Lửng Da Bò Nữ', 'giay-boots-co-lung-da-bo-nu', 
 N'Chất da bò sáp tự nhiên bụi bặm, cổ lửng ôm vừa cổ chân giữ ấm tốt. Lý tưởng cho các chuyến dạo phố mùa đông.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 4. Giày lười (4 sản phẩm)
(@c_glu, @b_bitis, N'Giày lười Loafer Penny Nam', 'giay-luoi-loafer-penny-nam', 
 N'Đai da vắt ngang thân giày tinh tế, chất da bóng nhẹ dễ lau chùi. Phù hợp phong cách smart-casual hiện đại.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_glu, @b_vans, N'Giày lười Slip-on Vans Classic Unisex', 'giay-luoi-slip-on-vans-classic-unisex', 
 N'Mẫu giày không dây kinh điển tiện dụng tuyệt đối, chỉ cần xỏ chân là đi. Lớp đệm cổ giày êm ái chống cọ xát gót.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_glu, @b_bitis, N'Giày lười Moccasin Biti''s Nam', 'giay-luoi-moccasin-bitis-nam', 
 N'Đế đúc hạt cao su chống trượt dẻo dai uốn cong linh hoạt. Trọng lượng nhẹ tạo cảm giác êm như bước đi trên thảm.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_glu, @b_bitis, N'Giày lười Nữ Đế Bằng Thời Trang', 'giay-luoi-nu-de-bang-thoi-trang', 
 N'Phom dáng ôm chân thon gọn, chất liệu êm ái thoáng khí suốt ngày dài. Lựa chọn số một cho ngày làm việc bận rộn.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 5. Giày thể thao (4 sản phẩm)
(@c_gtt, @b_nike, N'Giày thể thao Chạy Bộ Nike Pegasus Nam', 'giay-the-thao-chay-bo-nike-pegasus-nam', 
 N'Công nghệ bọt React foam phản hồi năng lượng tối đa, lưới thoáng khí siêu nhẹ. Trợ thủ đắc lực cho những cự ly chạy bền.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gtt, @b_adidas, N'Giày thể thao Adidas Ultraboost Nữ', 'giay-the-thao-adidas-ultraboost-nu', 
 N'Đế giữa Boost hoàn trả năng lượng êm ái bậc nhất kết hợp vải dệt Primeknit ôm chân. Phong cách thời trang thể thao cao cấp.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gtt, @b_tdinh, N'Giày thể thao Thượng Đình Vải Nam', 'giay-the-thao-thuong-dinh-vai-nam', 
 N'Mẫu giày vải quốc dân bền bỉ, đế cao su bám sân xuất sắc. Thích hợp chạy bộ, đá bóng phong trào và tập luyện thể dục.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_gtt, @b_tdinh, N'Giày thể thao Cầu Lông Thượng Đình Unisex', 'giay-the-thao-cau-long-thuong-dinh-unisex', 
 N'Đế kếp ma sát cao chuyên dụng cho sân cầu lông và bóng chuyền trong nhà. Bảo vệ khớp cổ chân khi di chuyển chuyển hướng gấp.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 6. Sandal (4 sản phẩm)
(@c_sdl, @b_bitis, N'Sandal Dã Ngoại Biti''s Hunter Nam', 'sandal-da-ngoai-bitis-hunter-nam', 
 N'Quai dù kháng nước nhanh khô, đế LiteFLEX êm nhẹ bám địa hình tốt. Đồng hành hoàn hảo cho các chuyến trekking dã ngoại.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_sdl, @b_crocs, N'Sandal Quai Dán Crocs Unisex', 'sandal-quai-dan-crocs-unisex', 
 N'Chất liệu mút tế bào kín Croslite đúc nguyên khối chống nước và kháng khuẩn. Dây quai dán điều chỉnh độ ôm chân linh hoạt.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_sdl, @b_bitis, N'Sandal Quai Mảnh Nữ Dạo Phố', 'sandal-quai-manh-nu-dao-pho', 
 N'Thiết kế quai thanh lịch nữ tính, đế đệm mút mềm tạo bước đi uyển chuyển. Rất đẹp khi kết hợp cùng váy xòe mùa hè.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_sdl, @b_bitis, N'Sandal Da Quai Chéo Nam', 'sandal-da-quai-cheo-nam', 
 N'Quai da bò thật bản rộng bền chắc, phong cách phóng khoáng và đĩnh đạc. Thích hợp đi làm hàng ngày hoặc cà phê cuối tuần.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 7. Dép kẹp (3 sản phẩm)
(@c_dkp, @b_bitis, N'Dép kẹp Xỏ Ngón Biti''s Nam', 'dep-kep-xo-ngon-bitis-nam', 
 N'Chất liệu mút EVA siêu nhẹ, quai dù mềm không gây đau kẽ ngón chân. Sản phẩm ẩn tạm thời để kiểm thử bộ lọc.', 
 '/images/placeholder-shoe.svg', 'HIDDEN', GETDATE(), GETDATE()), -- [HIDDEN 2]
(@c_dkp, @b_tdinh, N'Dép kẹp Đi Biển Thượng Đình Unisex', 'dep-kep-di-bien-thuong-dinh-unisex', 
 N'Đế cao su dẻo rãnh ngang chống trượt nước, quai nhựa mềm chịu nhiệt tốt. Bền bỉ và tiện ích cho các chuyến du lịch biển.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dkp, @b_bitis, N'Dép kẹp Thời Trang Nữ Đế Xốp', 'dep-kep-thoi-trang-nu-de-xop', 
 N'Đế xốp nhiều lớp êm ái nâng chiều cao nhẹ nhàng, hoa văn nữ tính duyên dáng. Phù hợp dạo phố và sinh hoạt gia đình.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),

-- 8. Dép slipper (3 sản phẩm)
(@c_dsl, @b_crocs, N'Dép Slipper Crocs Classic Clog Unisex', 'dep-slipper-crocs-classic-clog-unisex', 
 N'Dép sục bít mũi kinh điển với các lỗ thoáng khí gắn phụ kiện jibbitz độc đáo. Quai xoay gót linh hoạt chuyển đổi giữa dép và sandal.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dsl, @b_adidas, N'Dép Slipper Quai Ngang Adidas Adilette Nam', 'dep-slipper-quai-ngang-adidas-adilette-nam', 
 N'Thiết kế quai ngang bản to 3 sọc thể thao nổi tiếng, lòng dép ôm vòm chân giảm mỏi. Chống nước tiện lợi mang sau khi tập luyện.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE()),
(@c_dsl, @b_bitis, N'Dép Slipper Bánh Mì Nữ Đế Đúc', 'dep-slipper-banh-mi-nu-de-duc', 
 N'Phom dáng bánh mì đang được giới trẻ ưa chuộng, đế đúc EVA mềm như bông gòn. Cực kỳ êm chân và dễ phối đồ.', 
 '/images/placeholder-shoe.svg', 'ACTIVE', GETDATE(), GETDATE());

-- 4. BIẾN THỂ SẢN PHẨM (PRODUCT_VARIANTS)
-- Dùng CROSS JOIN (VALUES ...) để sinh gọn gàng chính xác 4 biến thể (2 màu x 2 size EU) cho mỗi sản phẩm.
-- Tổng số biến thể = 30 x 4 = 120 biến thể (<= 130).
-- SKU duy nhất chuẩn {LOẠI}-{HÃNG}-{MẪU}-{MÀU}-{SIZE}.
-- Barcode duy nhất 13 chữ số chuẩn GS1.

CREATE TABLE #TempVariants (
    product_slug NVARCHAR(255) COLLATE DATABASE_DEFAULT,
    sku NVARCHAR(100) COLLATE DATABASE_DEFAULT,
    barcode NVARCHAR(100) COLLATE DATABASE_DEFAULT,
    color NVARCHAR(50) COLLATE DATABASE_DEFAULT,
    size NVARCHAR(50) COLLATE DATABASE_DEFAULT,
    price DECIMAL(18,2),
    cost_price DECIMAL(18,2),
    weight_gram INT
);

-- Bảng cấu hình 30 sản phẩm với mã quy ước và giá / cân nặng
CREATE TABLE #ProductSeedMeta (
    slug NVARCHAR(255) COLLATE DATABASE_DEFAULT,
    type_code NVARCHAR(10) COLLATE DATABASE_DEFAULT,
    brand_code NVARCHAR(10) COLLATE DATABASE_DEFAULT,
    model_code NVARCHAR(10) COLLATE DATABASE_DEFAULT,
    price DECIMAL(18,2),
    cost_price DECIMAL(18,2),
    weight_gram INT,
    color1_code NVARCHAR(10) COLLATE DATABASE_DEFAULT, color1_name NVARCHAR(50) COLLATE DATABASE_DEFAULT,
    color2_code NVARCHAR(10) COLLATE DATABASE_DEFAULT, color2_name NVARCHAR(50) COLLATE DATABASE_DEFAULT,
    size1 NVARCHAR(10) COLLATE DATABASE_DEFAULT,
    size2 NVARCHAR(10) COLLATE DATABASE_DEFAULT
);

INSERT INTO #ProductSeedMeta VALUES
-- Sneaker (1.2 - 3.5M, 600-1100g)
('giay-sneaker-nike-air-force-1-nam', 'SNK', 'NIKE', 'AF1', 2650000.00, 1590000.00, 850, 'TRANG', N'Trắng', 'DEN', N'Đen', '40', '41'),
('giay-sneaker-adidas-stan-smith-unisex', 'SNK', 'ADID', 'STAN', 2150000.00, 1290000.00, 800, 'TRANG', N'Trắng', 'XANH', N'Xanh Lá', '39', '40'),
('giay-sneaker-converse-chuck-70-unisex', 'SNK', 'CONV', 'C70', 1850000.00, 1100000.00, 780, 'DEN', N'Đen', 'KEM', N'Kem', '38', '39'),
('giay-sneaker-vans-old-skool-unisex', 'SNK', 'VANS', 'OSK', 1650000.00, 990000.00, 790, 'DEN', N'Đen', 'DO', N'Đỏ', '39', '40'),

-- Giày da (0.9 - 2.5M, 600-1100g)
('giay-da-oxford-bitis-nam', 'GDA', 'BTI', 'OXF', 1250000.00, 750000.00, 900, 'DEN', N'Đen', 'NAU', N'Nâu', '40', '41'),
('giay-da-derby-dr-martens-1461-nam', 'GDA', 'DRMA', '1461', 2450000.00, 1470000.00, 980, 'DEN', N'Đen', 'RUOU', N'Đỏ Rượu', '41', '42'),
('giay-da-brogue-co-dien-nam', 'GDA', 'BTI', 'BRO', 1350000.00, 810000.00, 920, 'NAU', N'Nâu', 'DEN', N'Đen', '40', '41'),
('giay-da-cao-got-nu-cong-so', 'GDA', 'BTI', 'HEEL', 950000.00, 570000.00, 650, 'DEN', N'Đen', 'BE', N'Be', '36', '37'),

-- Giày boots (1.5 - 4.0M, 600-1100g)
('giay-boots-dr-martens-1460-8-lo-unisex', 'GBT', 'DRMA', '1460', 3650000.00, 2190000.00, 1050, 'DEN', N'Đen', 'NAU', N'Nâu Đất', '39', '40'),
('giay-chelsea-boots-da-nam', 'GBT', 'DRMA', 'CHEL', 2850000.00, 1710000.00, 990, 'DEN', N'Đen', 'NAU', N'Nâu', '41', '42'),
('giay-combat-boots-khoa-keo-nu', 'GBT', 'DRMA', 'COMB', 2650000.00, 1590000.00, 880, 'DEN', N'Đen', 'TRANG', N'Trắng', '36', '37'),
('giay-boots-co-lung-da-bo-nu', 'GBT', 'VANS', 'MID', 1950000.00, 1170000.00, 820, 'BE', N'Be', 'DEN', N'Đen', '37', '38'),

-- Giày lười (0.8 - 2.0M, 600-1100g)
('giay-luoi-loafer-penny-nam', 'GLU', 'BTI', 'PENN', 1250000.00, 750000.00, 750, 'DEN', N'Đen', 'NAU', N'Nâu', '40', '41'),
('giay-luoi-slip-on-vans-classic-unisex', 'GLU', 'VANS', 'SLIP', 1450000.00, 870000.00, 720, 'CARO', N'Caro', 'DEN', N'Đen', '39', '40'),
('giay-luoi-moccasin-bitis-nam', 'GLU', 'BTI', 'MOCC', 980000.00, 580000.00, 700, 'XANH', N'Xanh Navy', 'NAU', N'Nâu', '41', '42'),
('giay-luoi-nu-de-bang-thoi-trang', 'GLU', 'BTI', 'FLAT', 850000.00, 510000.00, 600, 'KEM', N'Kem', 'DEN', N'Đen', '36', '37'),

-- Giày thể thao (1.0 - 3.5M, 600-1100g)
('giay-the-thao-chay-bo-nike-pegasus-nam', 'GTT', 'NIKE', 'PEG', 3150000.00, 1890000.00, 720, 'DEN', N'Đen', 'XANH', N'Xanh Dương', '41', '42'),
('giay-the-thao-adidas-ultraboost-nu', 'GTT', 'ADID', 'ULTR', 3450000.00, 2070000.00, 680, 'TRANG', N'Trắng', 'HONG', N'Hồng Pastel', '36', '37'),
('giay-the-thao-thuong-dinh-vai-nam', 'GTT', 'TDIN', 'VAI', 1050000.00, 630000.00, 650, 'TRANG', N'Trắng Kẻ Xanh', 'DEN', N'Đen', '40', '41'),
('giay-the-thao-cau-long-thuong-dinh-unisex', 'GTT', 'TDIN', 'BADM', 1150000.00, 690000.00, 660, 'TRANG', N'Trắng Đỏ', 'XANH', N'Xanh Chuối', '39', '40'),

-- Sandal (0.3 - 0.9M, 200-450g)
('sandal-da-ngoai-bitis-hunter-nam', 'SDL', 'BTI', 'HUNT', 480000.00, 280000.00, 380, 'DEN', N'Đen', 'XAM', N'Xám Rêu', '40', '41'),
('sandal-quai-dan-crocs-unisex', 'SDL', 'CROC', 'STRAP', 790000.00, 470000.00, 320, 'DEN', N'Đen', 'KEM', N'Kem', '39', '40'),
('sandal-quai-manh-nu-dao-pho', 'SDL', 'BTI', 'SLIM', 360000.00, 210000.00, 250, 'BE', N'Be', 'TRANG', N'Trắng', '36', '37'),
('sandal-da-quai-cheo-nam', 'SDL', 'BTI', 'CROS', 520000.00, 310000.00, 400, 'NAU', N'Nâu', 'DEN', N'Đen', '41', '42'),

-- Dép kẹp (0.1 - 0.35M, 200-450g)
('dep-kep-xo-ngon-bitis-nam', 'DKP', 'BTI', 'FLIP', 160000.00, 95000.00, 220, 'DEN', N'Đen', 'XANH', N'Xanh Rêu', '40', '41'),
('dep-kep-di-bien-thuong-dinh-unisex', 'DKP', 'TDIN', 'SEAS', 120000.00, 70000.00, 200, 'DO', N'Đỏ', 'XANH', N'Xanh Dương', '38', '39'),
('dep-kep-thoi-trang-nu-de-xop', 'DKP', 'BTI', 'FOAM', 220000.00, 130000.00, 210, 'HONG', N'Hồng', 'BE', N'Màu Be', '36', '37'),

-- Dép slipper (0.2 - 0.6M, 200-450g)
('dep-slipper-crocs-classic-clog-unisex', 'DSL', 'CROC', 'CLOG', 590000.00, 350000.00, 310, 'TRANG', N'Trắng', 'DEN', N'Đen', '39', '40'),
('dep-slipper-quai-ngang-adidas-adilette-nam', 'DSL', 'ADID', 'ADIL', 490000.00, 290000.00, 280, 'DEN', N'Đen 3 Sọc', 'XANH', N'Xanh Navy', '41', '42'),
('dep-slipper-banh-mi-nu-de-duc', 'DSL', 'BTI', 'BRED', 280000.00, 165000.00, 290, 'KEM', N'Kem Sữa', 'HONG', N'Hồng Phấn', '36', '37');

-- Tạo biến thể bằng CROSS JOIN (VALUES ...)
INSERT INTO #TempVariants (product_slug, sku, barcode, color, size, price, cost_price, weight_gram)
SELECT 
    m.slug,
    m.type_code + '-' + m.brand_code + '-' + m.model_code + '-' + c.color_code + '-' + s.size_val AS sku,
    NULL, -- sẽ gán barcode 13 số tự động
    c.color_name,
    s.size_val,
    m.price,
    m.cost_price,
    m.weight_gram
FROM #ProductSeedMeta m
CROSS JOIN (
    VALUES (1), (2)
) AS color_idx(idx)
CROSS JOIN (
    VALUES (1), (2)
) AS size_idx(idx)
CROSS APPLY (
    SELECT 
        CASE WHEN color_idx.idx = 1 THEN m.color1_code ELSE m.color2_code END AS color_code,
        CASE WHEN color_idx.idx = 1 THEN m.color1_name ELSE m.color2_name END AS color_name
) c
CROSS APPLY (
    SELECT 
        CASE WHEN size_idx.idx = 1 THEN m.size1 ELSE m.size2 END AS size_val
) s;

DROP TABLE #ProductSeedMeta;

-- Đánh số Barcode 13 chữ số duy nhất cho từng biến thể
;WITH NumberedVariants AS (
    SELECT 
        product_slug, sku, color, size, price, cost_price, weight_gram,
        '89350000' + RIGHT('00000' + CAST(ROW_NUMBER() OVER (ORDER BY product_slug, sku) AS VARCHAR(5)), 5) AS gen_barcode
    FROM #TempVariants
)
INSERT INTO product_variants (product_id, sku, barcode, color, size, price, cost_price, weight_gram, status, created_at, updated_at)
SELECT 
    p.id,
    nv.sku,
    nv.gen_barcode,
    nv.color,
    nv.size,
    nv.price,
    nv.cost_price,
    nv.weight_gram,
    'ACTIVE',
    GETDATE(),
    GETDATE()
FROM NumberedVariants nv
INNER JOIN products p ON p.slug = nv.product_slug;

DROP TABLE #TempVariants;

-- 5. HÌNH ẢNH SẢN PHẨM (PRODUCT_IMAGES)
-- Mỗi sản phẩm 2 ảnh mẫu /images/placeholder-shoe.svg (1 ảnh thumbnail sort_order = 0, 1 ảnh gallery sort_order = 1)
-- Tổng cộng = 30 x 2 = 60 dòng
INSERT INTO product_images (product_id, variant_id, url, sort_order, is_thumbnail, created_at, updated_at)
SELECT 
    p.id,
    NULL,
    '/images/placeholder-shoe.svg',
    0,
    1,
    GETDATE(),
    GETDATE()
FROM products p
UNION ALL
SELECT 
    p.id,
    NULL,
    '/images/placeholder-shoe.svg',
    1,
    0,
    GETDATE(),
    GETDATE()
FROM products p;

-- 6. TỒN KHO THEO TỪNG CỬA HÀNG (INVENTORIES)
-- Mọi biến thể x mọi cửa hàng/kho: 120 biến thể x 4 điểm = 480 dòng.
-- Kho tổng Tân Bình (WH-TONG): 40 đến 120 đôi dồi dào.
-- Cửa hàng bán lẻ (STORE-Q1, STORE-Q3, STORE-BT): từ 0 đến 25 đôi.
-- reorder_level = 5.
-- Ít nhất 6 dòng bằng 0 (hết hàng) và ít nhất 8 dòng <= 5 (sắp hết) để kiểm thử bộ lọc cảnh báo.
-- Sử dụng công thức xác định lặp lại được: ABS(CHECKSUM(v.sku, s.code)).
INSERT INTO inventories (variant_id, store_id, quantity_on_hand, reorder_level, version, created_at, updated_at)
SELECT 
    v.id,
    s.id,
    CASE 
        -- Kho tổng: từ 40 đến 120
        WHEN s.code = 'WH-TONG' THEN 40 + (ABS(CHECKSUM(v.sku, s.code)) % 81)
        -- 3 Cửa hàng bán lẻ: từ 0 đến 25
        ELSE 
            CASE 
                -- Tồn bằng 0 (hết hàng) - đảm bảo >= 6 dòng
                WHEN ABS(CHECKSUM(v.sku, s.code)) % 13 = 0 THEN 0
                -- Tồn chạm ngưỡng cảnh báo (<= 5) - đảm bảo >= 8 dòng
                WHEN ABS(CHECKSUM(v.sku, s.code)) % 11 = 0 THEN 2
                WHEN ABS(CHECKSUM(v.sku, s.code)) % 7  = 0 THEN 4
                -- Tồn thông thường từ 6 đến 25
                ELSE 6 + (ABS(CHECKSUM(v.sku, s.code)) % 20)
            END
    END,
    5,
    0,
    GETDATE(),
    GETDATE()
FROM product_variants v
CROSS JOIN stores s;
