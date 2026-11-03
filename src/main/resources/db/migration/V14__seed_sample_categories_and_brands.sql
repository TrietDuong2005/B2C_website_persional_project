-- V14: Dữ liệu mẫu cho Categories và Brands để test

-- Thêm 3 Categories
INSERT INTO categories (name, slug, sort_order) VALUES (N'Áo thun nam', 'ao-thun-nam', 1);
INSERT INTO categories (name, slug, sort_order) VALUES (N'Quần jean', 'quan-jean', 2);
INSERT INTO categories (name, slug, sort_order) VALUES (N'Giày sneaker', 'giay-sneaker', 3);

-- Thêm 2 Brands
INSERT INTO brands (name, slug) VALUES (N'Nike', 'nike');
INSERT INTO brands (name, slug) VALUES (N'Adidas', 'adidas');

-- Thêm 2 Attributes mẫu
INSERT INTO attributes (name, input_type) VALUES (N'Màu sắc', 'color');
INSERT INTO attributes (name, input_type) VALUES (N'Kích cỡ', 'select');
