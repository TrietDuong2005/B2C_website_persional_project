-- ==============================================================================
-- KỊCH BẢN KIỂM TRA TOÀN DIỆN CƠ SỞ DỮ LIỆU v2 (SQL SERVER)
-- File: docs/kiem-tra-csdl.sql
-- ==============================================================================

USE ecommerce_db;
GO

PRINT '======================================================================';
PRINT '1. KIỂM TRA SỐ LƯỢNG BẢN GHI TRONG CÁC BẢNG MỚI';
PRINT '======================================================================';

SELECT 'categories' AS table_name, COUNT(*) AS total_rows FROM categories
UNION ALL
SELECT 'brands', COUNT(*) FROM brands
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'product_variants', COUNT(*) FROM product_variants
UNION ALL
SELECT 'product_images', COUNT(*) FROM product_images
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'employees', COUNT(*) FROM employees
UNION ALL
SELECT 'inventories', COUNT(*) FROM inventories
UNION ALL
SELECT 'inventory_transactions', COUNT(*) FROM inventory_transactions
UNION ALL
SELECT 'carts', COUNT(*) FROM carts
UNION ALL
SELECT 'cart_items', COUNT(*) FROM cart_items
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL
SELECT 'order_status_history', COUNT(*) FROM order_status_history
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'vouchers', COUNT(*) FROM vouchers
UNION ALL
SELECT 'voucher_usages', COUNT(*) FROM voucher_usages
UNION ALL
SELECT 'pos_shifts', COUNT(*) FROM pos_shifts
UNION ALL
SELECT 'stock_transfers', COUNT(*) FROM stock_transfers
UNION ALL
SELECT 'stock_transfer_items', COUNT(*) FROM stock_transfer_items
UNION ALL
SELECT 'reviews', COUNT(*) FROM reviews;
GO

PRINT '======================================================================';
PRINT '2. KIỂM TRA TỒN KHO CỦA TỪNG CỬA HÀNG VÀ KHO TỔNG';
PRINT '======================================================================';

SELECT 
    s.id AS store_id,
    s.code AS store_code,
    s.name AS store_name,
    s.type AS store_type,
    COUNT(i.id) AS total_variants_managed,
    SUM(i.quantity_on_hand) AS total_quantity_on_hand,
    SUM(CASE WHEN i.quantity_on_hand = 0 THEN 1 ELSE 0 END) AS out_of_stock_count,
    SUM(CASE WHEN i.quantity_on_hand > 0 AND i.quantity_on_hand <= i.reorder_level THEN 1 ELSE 0 END) AS low_stock_warning_count
FROM stores s
LEFT JOIN inventories i ON s.id = i.store_id
GROUP BY s.id, s.code, s.name, s.type
ORDER BY s.id;
GO

PRINT '======================================================================';
PRINT '3. LIỆT KÊ 4 TÀI KHOẢN MẪU, VAI TRÒ VÀ CHI NHÁNH CÔNG TÁC';
PRINT '======================================================================';

SELECT 
    u.id AS user_id,
    u.email,
    p.full_name,
    r.code AS role_code,
    r.name AS role_name,
    u.status AS user_status,
    e.position AS employee_position,
    s.code AS store_code,
    s.name AS store_name
FROM users u
LEFT JOIN user_profiles p ON u.id = p.user_id
INNER JOIN user_roles ur ON u.id = ur.user_id
INNER JOIN roles r ON ur.role_id = r.id
LEFT JOIN employees e ON u.id = e.user_id
LEFT JOIN stores s ON e.store_id = s.id
WHERE u.email IN ('admin@dahdt.com', 'manager@dahdt.com', 'cashier@dahdt.com', 'customer@dahdt.com')
ORDER BY u.id;
GO

PRINT '======================================================================';
PRINT '4. ĐẾM SỐ LƯỢNG SẢN PHẨM THEO TỪNG DANH MỤC LÁ';
PRINT '======================================================================';

SELECT 
    parent.name AS parent_category,
    child.id AS category_id,
    child.name AS leaf_category_name,
    child.slug AS leaf_category_slug,
    COUNT(p.id) AS total_products
FROM categories child
INNER JOIN categories parent ON child.parent_id = parent.id
LEFT JOIN products p ON child.id = p.category_id
GROUP BY parent.name, child.id, child.name, child.slug
ORDER BY parent.name, child.id;
GO

PRINT '======================================================================';
PRINT '5. ĐẾM SỐ LƯỢNG BIẾN THỂ THEO TỪNG DANH MỤC LÁ';
PRINT '======================================================================';

SELECT 
    parent.name AS parent_category,
    child.id AS category_id,
    child.name AS leaf_category_name,
    child.slug AS leaf_category_slug,
    COUNT(v.id) AS total_variants
FROM categories child
INNER JOIN categories parent ON child.parent_id = parent.id
LEFT JOIN products p ON child.id = p.category_id
LEFT JOIN product_variants v ON p.id = v.product_id
GROUP BY parent.name, child.id, child.name, child.slug
ORDER BY parent.name, child.id;
GO


