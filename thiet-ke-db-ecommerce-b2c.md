# THIẾT KẾ DATABASE — WEBSITE THƯƠNG MẠI ĐIỆN TỬ B2C

> Tài liệu thiết kế mức khái niệm (conceptual/logical design). Chưa bao gồm câu lệnh tạo bảng (DDL). Mục tiêu: hỗ trợ đầy đủ các nghiệp vụ của một sàn/website bán lẻ B2C hoàn chỉnh, đồng thời có khả năng tùy chỉnh cao (product có nhiều biến thể, thuộc tính động, khuyến mãi linh hoạt, đa kênh thanh toán/vận chuyển...).

---

## 1. NGUYÊN TẮC THIẾT KẾ

- **Chuẩn hóa (3NF)** cho phần nghiệp vụ lõi (đơn hàng, thanh toán, kho) để tránh dị thường dữ liệu; **phi chuẩn hóa có kiểm soát** ở phần hiển thị (ví dụ bảng cache giá, snapshot đơn hàng) để tối ưu tốc độ đọc.
- **Soft delete** (`is_deleted`, `deleted_at`) cho các bảng nghiệp vụ quan trọng (product, user, order) thay vì xóa cứng.
- **Audit fields** chuẩn cho mọi bảng: `created_at`, `updated_at`, `created_by`, `updated_by`.
- **EAV có kiểm soát (Attribute-Value)** cho thuộc tính sản phẩm để đạt độ tùy chỉnh cao mà không phải sửa schema khi thêm ngành hàng mới.
- **Snapshot dữ liệu tại thời điểm giao dịch**: đơn hàng phải lưu lại tên/giá/thuộc tính sản phẩm tại thời điểm mua, độc lập với bảng product (vì product có thể đổi giá/xóa sau này).
- **Tách bạch số dư kho (inventory) và giỏ hàng (cart)** để tránh khóa (lock) sai khi nhiều người cùng thao tác.

---

## 2. SƠ ĐỒ CÁC NHÓM MODULE

1. Người dùng & Xác thực (User & Auth)
2. Danh mục & Sản phẩm (Catalog)
3. Giá & Khuyến mãi (Pricing & Promotion)
4. Kho & Tồn kho (Inventory)
5. Giỏ hàng & Wishlist
6. Đơn hàng & Thanh toán (Order & Payment)
7. Vận chuyển (Shipping & Fulfillment)
8. Đánh giá & Hỏi đáp (Review & QnA)
9. Đổi trả/Bảo hành (Return/Refund/Warranty)
10. Khách hàng thân thiết (Loyalty/Membership)
11. Thông báo & CSKH (Notification & Support)
12. Nội dung (CMS/Marketing)
13. Quản trị & Phân quyền (Admin/RBAC)
14. Nhật ký & Phân tích (Audit/Log/Analytics)

---

## 3. CHI TIẾT THIẾT KẾ THEO MODULE

### 3.1 Người dùng & Xác thực

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `users` | Tài khoản gốc (khách hàng, admin, nhân viên đều là user, phân biệt qua role) | id, email, phone, password_hash, status (active/locked/pending), email_verified_at, phone_verified_at | 1-n với addresses, orders, carts |
| `user_profiles` | Thông tin cá nhân mở rộng | user_id, full_name, avatar_url, gender, date_of_birth | 1-1 với users |
| `addresses` | Sổ địa chỉ (giao hàng/thanh toán), 1 user nhiều địa chỉ | id, user_id, recipient_name, phone, province, district, ward, detail_address, is_default, type (shipping/billing) | n-1 với users |
| `roles` | Vai trò hệ thống (customer, admin, staff, warehouse...) | id, code, name | n-n với users qua `user_roles` |
| `user_roles` | Bảng trung gian gán vai trò | user_id, role_id | — |
| `oauth_accounts` | Đăng nhập liên kết Google/Facebook | id, user_id, provider, provider_uid | n-1 với users |
| `auth_tokens` | Refresh token / device session | id, user_id, token_hash, device_info, expires_at, revoked_at | n-1 với users |
| `otp_verifications` | OTP xác thực SĐT/email | id, user_id, code_hash, purpose (register/reset_password), expires_at | n-1 với users |

**Chức năng hỗ trợ:** đăng ký/đăng nhập (email, SĐT, OAuth), quên mật khẩu, xác thực 2 lớp, quản lý nhiều địa chỉ giao hàng, quản lý phiên đăng nhập đa thiết bị, phân quyền nhiều vai trò cho 1 tài khoản (vd nhân viên vừa là admin vừa là khách).

---

### 3.2 Danh mục & Sản phẩm (Catalog) — trọng tâm độ tùy chỉnh

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `categories` | Danh mục dạng cây (self-reference) | id, parent_id, name, slug, image_url, sort_order | tự tham chiếu; n-n với products |
| `brands` | Thương hiệu | id, name, slug, logo_url | 1-n với products |
| `products` | Sản phẩm "cha" (spu - Standard Product Unit) | id, name, slug, brand_id, description, status (draft/active/hidden), product_type (simple/variant), meta_title, meta_description | 1-n với product_variants |
| `product_categories` | Gán sản phẩm vào nhiều danh mục | product_id, category_id | bảng trung gian n-n |
| `attributes` | Định nghĩa thuộc tính (Màu sắc, Size, RAM...) dùng chung toàn hệ thống | id, name, input_type (select/color/text) | 1-n với attribute_values |
| `attribute_values` | Giá trị cụ thể của thuộc tính (Đỏ, Xanh, XL, 8GB) | id, attribute_id, value, extra_data (vd mã màu hex) | n-1 với attributes |
| `category_attributes` | Thuộc tính nào áp dụng cho danh mục nào (để form nhập liệu động) | category_id, attribute_id, is_required, is_variant_defining | bảng trung gian |
| `product_variants` | Biến thể bán được (sku) — đây là đơn vị thực sự có giá/tồn kho | id, product_id, sku, price, compare_at_price, cost_price, weight, dimensions, barcode, status | 1-n từ products; 1-n với inventory, order_items |
| `variant_attribute_values` | Biến thể này ứng với tổ hợp giá trị thuộc tính nào (vd SKU-01 = Đỏ + XL) | variant_id, attribute_value_id | bảng trung gian n-n |
| `product_images` | Ảnh sản phẩm/biến thể | id, product_id, variant_id (nullable), url, sort_order, is_thumbnail | n-1 với products/variants |
| `product_specifications` | Thông số kỹ thuật dạng tự do (key-value hiển thị, không dùng để lọc) | id, product_id, spec_group, spec_key, spec_value, sort_order | n-1 với products |
| `product_tags` | Gắn nhãn tự do (bestseller, new, sale...) | product_id, tag_id | n-n |
| `tags` | Danh sách nhãn | id, name, slug | — |

**Vì sao tách `products` và `product_variants`:** một sản phẩm áo có thể có 10 biến thể (5 màu × 2 size), mỗi biến thể có SKU/giá/tồn kho riêng nhưng chia sẻ chung mô tả, ảnh đại diện, danh mục. Đây là mô hình chuẩn của Shopee/Tiki/Lazada, cho phép **thêm ngành hàng mới mà không sửa schema** (chỉ cần thêm attribute mới).

**Chức năng hỗ trợ:** danh mục đa cấp, sản phẩm nhiều biến thể (màu/size/dung lượng...), bộ lọc sản phẩm động theo thuộc tính, SEO từng sản phẩm, gắn nhãn khuyến mãi/mới, quản lý ảnh theo từng biến thể.

---

### 3.3 Giá & Khuyến mãi

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `price_history` | Lịch sử thay đổi giá theo variant | id, variant_id, old_price, new_price, changed_at | n-1 với product_variants |
| `promotions` | Chương trình khuyến mãi (giảm giá theo %, số tiền, mua X tặng Y) | id, name, type, discount_type, discount_value, start_at, end_at, status | 1-n với promotion_products |
| `promotion_products` | Sản phẩm/biến thể áp dụng khuyến mãi | promotion_id, variant_id (hoặc product_id/category_id để áp dụng hàng loạt) | n-n |
| `vouchers` | Mã giảm giá (coupon) | id, code, discount_type, discount_value, min_order_value, max_discount_value, usage_limit, usage_limit_per_user, start_at, end_at | 1-n với voucher_usages |
| `voucher_usages` | Lịch sử dùng mã | id, voucher_id, user_id, order_id, used_at | n-1 với vouchers, orders |
| `flash_sales` | Sự kiện flash sale theo khung giờ | id, name, start_at, end_at | 1-n với flash_sale_items |
| `flash_sale_items` | Sản phẩm trong flash sale kèm giá sốc + giới hạn số lượng | flash_sale_id, variant_id, flash_price, quantity_limit, sold_count | n-1 hai chiều |

**Chức năng hỗ trợ:** giảm giá theo sản phẩm/danh mục/toàn đơn, mã voucher có điều kiện (đơn tối thiểu, giới hạn lượt dùng/user), flash sale đếm ngược, lịch sử giá để hiển thị "giá trước/sau".

---

### 3.4 Kho & Tồn kho

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `warehouses` | Kho hàng (có thể nhiều kho/chi nhánh) | id, name, address, is_default | 1-n với inventories |
| `inventories` | Tồn kho theo từng variant tại từng kho | id, variant_id, warehouse_id, quantity_on_hand, quantity_reserved | n-1 với product_variants, warehouses |
| `inventory_transactions` | Nhật ký nhập/xuất/điều chỉnh kho | id, variant_id, warehouse_id, type (import/export/adjust/return), quantity, reference_type, reference_id, note | n-1 với inventories |
| `suppliers` | Nhà cung cấp | id, name, contact_info | 1-n với purchase_orders |
| `purchase_orders` | Đơn nhập hàng từ NCC | id, supplier_id, warehouse_id, status, total_amount | 1-n với purchase_order_items |
| `purchase_order_items` | Chi tiết mặt hàng nhập | purchase_order_id, variant_id, quantity, unit_cost | n-1 |

**Điểm quan trọng:** `quantity_reserved` dùng để giữ hàng khi khách đã đặt nhưng chưa thanh toán xong (tránh oversell); tồn khả dụng = `quantity_on_hand - quantity_reserved`.

**Chức năng hỗ trợ:** quản lý đa kho, nhập hàng từ NCC, cảnh báo hết hàng, đối soát kho, ngăn bán vượt tồn kho khi nhiều người mua cùng lúc.

---

### 3.5 Giỏ hàng & Wishlist

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `carts` | Giỏ hàng (theo user hoặc theo session của khách vãng lai) | id, user_id (nullable), session_id, status | 1-n với cart_items |
| `cart_items` | Sản phẩm trong giỏ | id, cart_id, variant_id, quantity, price_snapshot | n-1 với carts, variants |
| `wishlists` | Danh sách yêu thích | id, user_id, variant_id, created_at | n-n giữa users và variants |
| `recently_viewed` | Sản phẩm đã xem gần đây | id, user_id/session_id, variant_id, viewed_at | n-n |

**Chức năng hỗ trợ:** giỏ hàng cho cả khách chưa đăng nhập, lưu wishlist, gợi ý "sản phẩm đã xem", đồng bộ giỏ hàng khi khách đăng nhập lại.

---

### 3.6 Đơn hàng & Thanh toán

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `orders` | Đơn hàng | id, order_code, user_id, status (pending/confirmed/shipping/completed/cancelled), subtotal, discount_total, shipping_fee, total_amount, shipping_address_id, note, placed_at | 1-n với order_items, payments, shipments |
| `order_items` | Chi tiết sản phẩm trong đơn — **snapshot** dữ liệu tại thời điểm mua | id, order_id, variant_id, product_name_snapshot, variant_attributes_snapshot, unit_price, quantity, total_price | n-1 |
| `order_status_history` | Lịch sử chuyển trạng thái đơn hàng | id, order_id, from_status, to_status, changed_by, changed_at, note | n-1 với orders |
| `payment_methods` | Danh mục phương thức thanh toán (COD, VNPay, Momo, ZaloPay, thẻ...) | id, code, name, is_active | 1-n với payments |
| `payments` | Giao dịch thanh toán của đơn hàng | id, order_id, payment_method_id, amount, status (pending/success/failed/refunded), transaction_ref, paid_at | n-1 với orders |
| `refunds` | Hoàn tiền | id, payment_id, amount, reason, status, processed_at | n-1 với payments |

**Chức năng hỗ trợ:** đa phương thức thanh toán, thanh toán từng phần/toàn phần, theo dõi trạng thái đơn realtime, lịch sử thay đổi trạng thái phục vụ khiếu nại, hoàn tiền một phần hoặc toàn phần, đơn hàng vẫn hiển thị đúng thông tin sản phẩm dù sản phẩm gốc đã đổi/xóa (nhờ snapshot).

---

### 3.7 Vận chuyển (Shipping & Fulfillment)

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `shipping_carriers` | Đơn vị vận chuyển (GHN, GHTK, Viettel Post, tự vận hành...) | id, name, code | 1-n với shipments |
| `shipping_zones` | Khu vực tính phí ship | id, name, provinces | 1-n với shipping_rates |
| `shipping_rates` | Bảng giá ship theo khu vực/khối lượng | id, zone_id, min_weight, max_weight, fee | n-1 |
| `shipments` | Vận đơn gắn với order | id, order_id, carrier_id, tracking_code, status, shipped_at, delivered_at | 1-1/1-n với orders |
| `shipment_tracking_logs` | Lịch sử trạng thái vận đơn (đồng bộ từ API hãng vận chuyển) | id, shipment_id, status, location, logged_at | n-1 với shipments |

**Chức năng hỗ trợ:** tính phí ship động theo khu vực/khối lượng, tích hợp nhiều đơn vị vận chuyển, tra cứu hành trình đơn hàng.

---

### 3.8 Đánh giá & Hỏi đáp

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `reviews` | Đánh giá sản phẩm (chỉ cho phép nếu đã mua) | id, order_item_id, user_id, product_id, rating, comment, status (pending/approved/rejected) | n-1 với order_items, products |
| `review_images` | Ảnh/video đính kèm đánh giá | id, review_id, url | n-1 với reviews |
| `review_replies` | Phản hồi của shop cho đánh giá | id, review_id, replied_by, content | n-1 với reviews |
| `product_questions` | Hỏi đáp sản phẩm (Q&A) | id, product_id, user_id, question | n-1 với products |
| `product_answers` | Trả lời câu hỏi | id, question_id, user_id, answer | n-1 với product_questions |

**Chức năng hỗ trợ:** đánh giá kèm ảnh/video, kiểm duyệt review, shop phản hồi đánh giá, hỏi đáp công khai trên trang sản phẩm.

---

### 3.9 Đổi trả / Bảo hành

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `return_requests` | Yêu cầu đổi/trả hàng | id, order_id, user_id, reason, status, requested_at | n-1 với orders |
| `return_items` | Sản phẩm cụ thể được trả | id, return_request_id, order_item_id, quantity, condition_note | n-1 |
| `warranties` | Thông tin bảo hành theo sản phẩm/đơn hàng | id, order_item_id, warranty_period_months, expires_at | n-1 với order_items |
| `warranty_claims` | Yêu cầu bảo hành | id, warranty_id, issue_description, status | n-1 với warranties |

**Chức năng hỗ trợ:** quy trình đổi trả có kiểm soát trạng thái, quản lý thời hạn bảo hành, xử lý yêu cầu bảo lãnh.

---

### 3.10 Khách hàng thân thiết (Loyalty)

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `membership_tiers` | Hạng thành viên (Bạc/Vàng/Kim cương) | id, name, min_spending, benefits_description | 1-n với users |
| `loyalty_points` | Số dư điểm tích lũy | id, user_id, points_balance | 1-1 với users |
| `loyalty_point_transactions` | Lịch sử cộng/trừ điểm | id, user_id, order_id, points, type (earn/redeem/expire), created_at | n-1 |

**Chức năng hỗ trợ:** tích điểm theo giá trị đơn hàng, đổi điểm lấy voucher, phân hạng thành viên theo tổng chi tiêu.

---

### 3.11 Thông báo & Chăm sóc khách hàng

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `notifications` | Thông báo hệ thống gửi cho user | id, user_id, type, title, content, is_read, created_at | n-1 với users |
| `support_tickets` | Ticket hỗ trợ khách hàng | id, user_id, order_id (nullable), subject, status, priority | n-1 với users |
| `support_ticket_messages` | Hội thoại trong ticket | id, ticket_id, sender_id, message, attachments, created_at | n-1 với support_tickets |

**Chức năng hỗ trợ:** thông báo trạng thái đơn/khuyến mãi, hệ thống ticket CSKH có hội thoại nhiều lượt.

---

### 3.12 Nội dung & Marketing (CMS)

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `banners` | Banner trang chủ/khu vực | id, image_url, link_url, position, start_at, end_at, sort_order | — |
| `cms_pages` | Trang tĩnh (giới thiệu, chính sách, FAQ) | id, slug, title, content, status | — |
| `blog_posts` | Bài viết blog/tin tức | id, title, slug, content, author_id, published_at | n-1 với users |
| `email_campaigns` | Chiến dịch email marketing | id, subject, content, target_segment, sent_at | — |

**Chức năng hỗ trợ:** quản lý banner theo thời gian hiển thị, trang nội dung tĩnh, blog SEO, email marketing theo phân khúc khách hàng.

---

### 3.13 Quản trị & Phân quyền

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `permissions` | Danh sách quyền hạn chi tiết (product.create, order.refund...) | id, code, description | n-n với roles |
| `role_permissions` | Gán quyền cho vai trò | role_id, permission_id | bảng trung gian |
| `admin_activity_logs` | Nhật ký thao tác của admin/nhân viên | id, admin_id, action, target_table, target_id, old_value, new_value, created_at | n-1 với users |

**Chức năng hỗ trợ:** phân quyền chi tiết theo hành động (RBAC), truy vết mọi thao tác chỉnh sửa dữ liệu của quản trị viên.

---

### 3.14 Nhật ký & Phân tích

| Bảng | Mục đích | Trường chính | Quan hệ |
|---|---|---|---|
| `product_view_logs` | Log lượt xem sản phẩm (phục vụ gợi ý, thống kê) | id, product_id, user_id/session_id, viewed_at | — |
| `search_logs` | Log từ khóa tìm kiếm | id, keyword, user_id, result_count, searched_at | — |
| `login_logs` | Log đăng nhập | id, user_id, ip_address, device, logged_at, status | n-1 với users |

**Chức năng hỗ trợ:** gợi ý sản phẩm liên quan/cá nhân hóa, thống kê từ khóa hot, phát hiện đăng nhập bất thường.

---

## 4. SƠ ĐỒ QUAN HỆ TỔNG QUÁT (mức khái niệm)

```
users ──< addresses
users ──< orders ──< order_items >── product_variants
users ──< carts ──< cart_items >── product_variants
users ──< wishlists >── product_variants
users ──< reviews
users ──< support_tickets ──< support_ticket_messages

products ──< product_variants ──< inventories >── warehouses
products ──< product_images
products >──< categories (qua product_categories)
products >──< attributes (qua category_attributes, định hướng nhập liệu)
product_variants >──< attribute_values (qua variant_attribute_values)

orders ──< payments ──< refunds
orders ──< shipments ──< shipment_tracking_logs
orders ──< order_status_history
orders ──< return_requests ──< return_items

promotions >──< product_variants/products/categories
vouchers ──< voucher_usages >── orders

roles >──< permissions (qua role_permissions)
users >──< roles (qua user_roles)
```

---

## 5. DANH SÁCH CHỨC NĂNG ĐƯỢC HỖ TRỢ TỪ DB NÀY

**Phía khách hàng:**
- Đăng ký/đăng nhập (email, SĐT, OAuth), quản lý hồ sơ, nhiều địa chỉ giao hàng
- Duyệt danh mục đa cấp, lọc sản phẩm theo thuộc tính động (màu, size, giá, thương hiệu...)
- Xem chi tiết sản phẩm với nhiều biến thể, ảnh riêng theo biến thể
- Giỏ hàng (kể cả khách vãng lai), wishlist, sản phẩm đã xem
- Áp mã giảm giá, tham gia flash sale
- Đặt hàng, chọn phương thức thanh toán, theo dõi trạng thái đơn & vận đơn realtime
- Đánh giá sản phẩm kèm ảnh, hỏi đáp công khai
- Yêu cầu đổi trả, tra cứu bảo hành
- Tích điểm thành viên, đổi điểm lấy ưu đãi
- Nhận thông báo, gửi yêu cầu hỗ trợ (ticket)

**Phía quản trị (Admin/Vận hành):**
- Quản lý danh mục/sản phẩm/biến thể/thuộc tính không giới hạn ngành hàng
- Quản lý giá, lịch sử giá, chương trình khuyến mãi/voucher/flash sale
- Quản lý kho đa chi nhánh, nhập hàng từ NCC, cảnh báo tồn kho thấp
- Xử lý đơn hàng theo quy trình trạng thái, đối soát thanh toán, xử lý hoàn tiền
- Quản lý vận chuyển, đối tác giao hàng, bảng giá ship
- Duyệt đánh giá, phản hồi khách hàng
- Quản lý đổi trả/bảo hành
- Phân quyền chi tiết theo vai trò, truy vết thao tác admin
- Quản lý nội dung (banner, blog, trang tĩnh), chiến dịch email
- Xem báo cáo/thống kê dựa trên log hành vi người dùng

**Khả năng mở rộng (đúng yêu cầu "tùy chỉnh cao"):**
- Thêm ngành hàng mới (điện thoại, thời trang, thực phẩm...) chỉ cần thêm `attribute` + `attribute_value` mới, không cần sửa schema
- Thêm phương thức thanh toán/vận chuyển mới chỉ cần thêm dòng dữ liệu trong `payment_methods`/`shipping_carriers`
- Thêm loại khuyến mãi mới bằng cách mở rộng `discount_type` mà không phá vỡ cấu trúc bảng hiện có
- Multi-warehouse sẵn sàng cho mô hình nhiều chi nhánh/kho vùng miền

---

## 6. GHI CHÚ TRIỂN KHAI (khi bước sang giai đoạn code)

- Nên dùng `UUID` hoặc `BIGINT auto-increment` cho khóa chính tùy theo yêu cầu bảo mật/khả năng mở rộng.
- Cân nhắc index cho: `products.slug`, `orders.order_code`, `product_variants.sku`, các cột `foreign key`, và cột dùng để lọc/tìm kiếm thường xuyên.
- Với dữ liệu tìm kiếm sản phẩm tần suất cao, có thể đồng bộ sang Elasticsearch/Meilisearch thay vì query trực tiếp DB quan hệ.
- Với thuộc tính động (EAV), cần cân nhắc giữa tính linh hoạt và hiệu năng — có thể kết hợp thêm cột JSON (`variant_attributes_snapshot`) để tăng tốc đọc.
