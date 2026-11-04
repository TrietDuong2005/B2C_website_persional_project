# TÀI LIỆU THIẾT KẾ CƠ SỞ DỮ LIỆU v2 (CHUỖI CỬA HÀNG + POS + SHOP ONLINE)

> **Mã tài liệu:** DB_DESIGN_V2  
> **Dự án:** Hệ thống Bán lẻ Chuỗi Cửa hàng Giày Dép "Triết Store" Đa kênh (DA_HDT_QLDA)  
> **Phiên bản:** 2.0  
> **Ngày lập:** 03/11/2026 (Cập nhật 04/11/2026: Ngành hàng Giày dép Triết Store)  
> **RDBMS:** Microsoft SQL Server (Transact-SQL)  
> **Công cụ quản lý:** Flyway Migration (`V1` đến `V30`)  

---

## 1. TỔNG QUAN PHẠM VI CSDL v2

Cơ sở dữ liệu v2 được thiết kế tinh gọn theo mô hình **MoSCoW**, gồm **24 bảng Must Have** (7 bảng cốt lõi kế thừa từ giai đoạn 1 và 17 bảng mới), bổ sung **3 bảng Should Have** và **1 bảng Could Have**, loại bỏ hoàn toàn mô hình thuộc tính động EAV phức tạp của bản nháp v1 để tối ưu hiệu năng và dễ bảo trì.

| Phân loại | Số lượng | Danh sách các bảng |
|---|:---:|---|
| **Đã có (V1–V6)** | 7 bảng | `users`, `roles`, `user_roles`, `user_profiles`, `addresses`, `auth_tokens`, `otp_verifications` |
| **Must Have mới** | 17 bảng | `categories`, `brands`, `products`, `product_variants`, `product_images`, `stores`, `employees`, `inventories`, `inventory_transactions`, `carts`, `cart_items`, `orders`, `order_items`, `order_status_history`, `payments`, `vouchers`, `voucher_usages` |
| **Should Have** | 3 bảng | `pos_shifts`, `stock_transfers`, `stock_transfer_items` |
| **Could Have** | 1 bảng | `reviews` |
| **TỔNG CỘNG** | **28 bảng** | **24 Must + 3 Should + 1 Could** |

---

## 2. GIẢI THÍCH CÁC QUYẾT ĐỊNH THIẾT KẾ QUAN TRỌNG

### 2.1 Vì sao bảng `orders` dùng chung cho cả 2 kênh (Online và POS)?
- **Tránh trùng lặp cấu trúc dữ liệu:** Cả đơn bán tại quầy (POS) và đơn bán trực tuyến (Online) đều có các trường cốt lõi giống nhau: mã đơn hàng, cửa hàng chịu trách nhiệm xuất kho, danh sách món hàng, tổng tiền trước giảm, voucher giảm giá, tổng tiền thanh toán và phương thức thanh toán.
- **Thống kê doanh thu toàn kênh (Omnichannel Reporting):** Giúp truy vấn doanh thu tổng của toàn chuỗi hoặc của từng chi nhánh trong ngày/tháng bằng một câu lệnh `SELECT` đơn giản gom nhóm theo `channel` (`channel = 'ONLINE'` hoặc `channel = 'POS'`), không cần `UNION ALL` qua nhiều bảng rời rạc.
- **Xử lý đặc thù bằng snapshot nullable:** Đối với đơn POS giao nhận ngay tại quầy, các cột thông tin giao hàng (`ship_name`, `ship_phone`, `ship_address`, `ship_latitude`, `ship_longitude`) được phép `NULL`. Đối với đơn Online, các trường này bắt buộc có giá trị để giao hàng.

### 2.2 Vì sao sử dụng Snapshot trong `order_items` và thông tin giao hàng?
- **Bảo toàn lịch sử giao dịch (Historical Immutability):** Giá bán (`price`), tên sản phẩm, mã SKU, màu sắc và kích cỡ có thể thay đổi trong tương lai, hoặc sản phẩm có thể bị xóa khỏi hệ thống. Nếu `order_items` chỉ lưu khóa ngoại trỏ tới `product_variants` mà không sao chép lại giá bán và thông tin sản phẩm, hóa đơn mua trong quá khứ sẽ bị sai lệch giá trị hoặc mất thông tin hiển thị.
- **Tương tự với địa chỉ nhận hàng:** Khách hàng có thể sửa hoặc xóa địa chỉ trong sổ địa chỉ cá nhân (`addresses`). Việc snapshot họ tên, số điện thoại, địa chỉ nhận hàng ngay trên bảng `orders` đảm bảo đơn hàng đã đặt giữ nguyên vẹn thông tin giao dịch ban đầu.

### 2.3 Vì sao bảng `inventories` có cột `version`?
- **Kiểm soát đồng thời bằng Khóa lạc quan (Optimistic Locking):** Trong môi trường bán hàng đa kênh, tình trạng nhiều giao dịch cùng truy cập vào một mặt hàng là rất phổ biến (ví dụ: khách đặt online đôi giày cuối cùng trong khi thu ngân tại cửa hàng cũng đang quét mã đôi giày đó).
- **Cơ chế hoạt động:** Cột `version` (kiểu `BIGINT`) kết hợp với annotation `@Version` của JPA. Khi thực hiện trừ tồn kho, Hibernate sinh câu lệnh:
  ```sql
  UPDATE inventories 
  SET quantity_on_hand = quantity_on_hand - ?, version = version + 1 
  WHERE id = ? AND version = ?
  ```
  Nếu có giao dịch khác đã cập nhật trước, số dòng ảnh hưởng là 0, JPA lập tức ném ra `OptimisticLockException`. Hệ thống sẽ chặn bán vượt tồn mà không cần khóa chết bảng bằng khóa bi quan (`Pessimistic Lock`), giúp hệ thống đạt thông lượng cao và phản hồi nhanh.

### 2.4 Vì sao bỏ mô hình EAV (Entity-Attribute-Value) để đưa `color`, `size` trực tiếp vào `product_variants`?
- Hệ thống tập trung vào ngành hàng giày dép bán lẻ chuỗi "Triết Store". Tất cả sản phẩm giày dép đều xoay quanh 2 trục phân loại biến thể cốt lõi là **Màu sắc (`color`)** và **Kích cỡ (`size` EU hai chữ số từ 36 đến 45)**.
- Mô hình EAV động (phải nối qua 4-5 bảng trung gian `attributes`, `attribute_values`, `variant_attribute_values`) gây phức tạp hóa truy vấn SQL, giảm tốc độ tìm kiếm tại quầy POS và làm khó việc bảo vệ đồ án đối với sinh viên. Cố định 2 cột `color` và `size` giúp truy vấn nhanh, trực quan và dễ hiểu.

---

## 3. TỪ ĐIỂN DỮ LIỆU CÁC BẢNG MỚI (DATA DICTIONARY)

### 3.1 Bảng `categories` (Danh mục sản phẩm cây cha - con)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh danh mục |
| `parent_id` | `BIGINT` | Yes | `NULL` | FK trỏ đến `categories(id)` | Khóa ngoại danh mục cha (cây đệ quy) |
| `name` | `NVARCHAR(100)` | No | | | Tên danh mục (ví dụ: Giày, Dép, Sneaker, Sandal) |
| `slug` | `NVARCHAR(150)` | No | | `UNIQUE` | Đường dẫn thân thiện SEO |
| `sort_order` | `INT` | No | `0` | | Thứ tự sắp xếp hiển thị |
| `is_active` | `BIT` | No | `1` | | Trạng thái hiển thị (1: Hoạt động, 0: Khóa) |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật cuối |

### 3.2 Bảng `brands` (Thương hiệu sản phẩm)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh thương hiệu |
| `name` | `NVARCHAR(100)` | No | | | Tên thương hiệu (Nike, Adidas, Converse, Vans, Biti's...) |
| `slug` | `NVARCHAR(150)` | No | | `UNIQUE` | Đường dẫn URL thân thiện |
| `is_active` | `BIT` | No | `1` | | Trạng thái hoạt động |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.3 Bảng `products` (Sản phẩm cha)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh sản phẩm cha |
| `category_id` | `BIGINT` | No | | FK trỏ đến `categories(id)` | Thuộc danh mục nào |
| `brand_id` | `BIGINT` | Yes | `NULL` | FK trỏ đến `brands(id)` | Thuộc thương hiệu nào |
| `name` | `NVARCHAR(255)` | No | | | Tên sản phẩm mẫu |
| `slug` | `NVARCHAR(300)` | No | | `UNIQUE` | Slug định danh duy nhất trên URL |
| `description` | `NVARCHAR(MAX)`| Yes | `NULL` | | Mô tả chi tiết chất liệu, kiểu dáng |
| `thumbnail_url`| `NVARCHAR(500)`| Yes | `NULL` | | Ảnh đại diện sản phẩm |
| `status` | `NVARCHAR(20)` | No | `'ACTIVE'` | `CHECK in ('ACTIVE', 'HIDDEN')` | Trạng thái bày bán |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.4 Bảng `product_variants` (Biến thể sản phẩm - đơn vị bán thực tế)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh biến thể |
| `product_id` | `BIGINT` | No | | FK trỏ đến `products(id)` | Thuộc sản phẩm cha nào |
| `sku` | `NVARCHAR(100)` | No | | `UNIQUE`, INDEX | Mã quản lý kho duy nhất (SKU) |
| `barcode` | `NVARCHAR(100)` | Yes | `NULL` | Filtered `UNIQUE` Index | Mã vạch để quét tại quầy POS |
| `color` | `NVARCHAR(50)` | No | | | Màu sắc (Đen, Trắng, Nâu, Xám...) |
| `size` | `NVARCHAR(50)` | No | | | Kích cỡ size EU (36, 37, 38, 39, 40, 41, 42, 43, 44, 45...) |
| `price` | `DECIMAL(18,2)` | No | | `CHECK (price >= 0)` | Giá bán niêm yết |
| `cost_price` | `DECIMAL(18,2)` | No | `0.00` | `CHECK (cost_price >= 0)` | Giá vốn phục vụ tính lợi nhuận |
| `weight_gram` | `INT` | No | `0` | `CHECK (weight_gram >= 0)`| Cân nặng đóng gói (gram) để tính cước |
| `status` | `NVARCHAR(20)` | No | `'ACTIVE'` | `CHECK in ('ACTIVE', 'INACTIVE')` | Trạng thái kinh doanh biến thể |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.5 Bảng `product_images` (Bộ sưu tập ảnh sản phẩm)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh ảnh |
| `product_id` | `BIGINT` | No | | FK trỏ đến `products(id)` | Ảnh của sản phẩm nào |
| `variant_id` | `BIGINT` | Yes | `NULL` | FK trỏ đến `product_variants(id)` | Ảnh gắn riêng cho biến thể (màu) |
| `url` | `NVARCHAR(500)` | No | | | Đường dẫn URL hình ảnh |
| `sort_order` | `INT` | No | `0` | | Thứ tự hiển thị trong gallery |
| `is_thumbnail`| `BIT` | No | `0` | | Có phải ảnh thumbnail không |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.6 Bảng `stores` (Cửa hàng bán lẻ và kho bãi)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã định danh chi nhánh |
| `code` | `NVARCHAR(50)` | No | | `UNIQUE`, INDEX | Mã chi nhánh (STORE-Q1, WH-TONG...) |
| `name` | `NVARCHAR(255)` | No | | | Tên cửa hàng hoặc kho |
| `type` | `NVARCHAR(20)` | No | `'STORE'` | `CHECK in ('STORE', 'WAREHOUSE')` | Loại điểm: Bán lẻ hay Kho tổng |
| `province` | `NVARCHAR(100)` | No | | | Tỉnh/Thành phố |
| `district` | `NVARCHAR(100)` | No | | | Quận/Huyện |
| `ward` | `NVARCHAR(100)` | No | | | Phường/Xã |
| `detail_address`| `NVARCHAR(500)`| No | | | Số nhà, tên đường chi tiết |
| `latitude` | `DECIMAL(10,7)` | Yes | `NULL` | | Tọa độ vĩ độ (GIS GPS) |
| `longitude` | `DECIMAL(10,7)` | Yes | `NULL` | | Tọa độ kinh độ (GIS GPS) |
| `phone` | `NVARCHAR(20)` | Yes | `NULL` | | Số điện thoại liên hệ chi nhánh |
| `status` | `NVARCHAR(20)` | No | `'ACTIVE'` | `CHECK in ('ACTIVE', 'INACTIVE')` | Trạng thái hoạt động |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.7 Bảng `employees` (Hồ sơ nhân viên chuỗi)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã nhân viên |
| `user_id` | `BIGINT` | No | | `UNIQUE`, FK trỏ `users(id)` | Tài khoản người dùng tương ứng |
| `store_id` | `BIGINT` | No | | FK trỏ `stores(id)`, INDEX | Cửa hàng nhân viên công tác |
| `position` | `NVARCHAR(20)` | No | `'STAFF'` | `CHECK in ('MANAGER', 'CASHIER', 'STAFF')` | Vị trí công tác |
| `status` | `NVARCHAR(20)` | No | `'ACTIVE'` | `CHECK in ('ACTIVE', 'INACTIVE')` | Trạng thái làm việc |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.8 Bảng `inventories` (Tồn kho theo cửa hàng)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã bản ghi tồn kho |
| `variant_id` | `BIGINT` | No | | FK trỏ `product_variants(id)` | Biến thể sản phẩm |
| `store_id` | `BIGINT` | No | | FK trỏ `stores(id)`, INDEX | Cửa hàng / Kho |
| `quantity_on_hand`| `INT` | No | `0` | `CHECK (quantity_on_hand >= 0)` | Số lượng tồn kho thực tế khả dụng |
| `reorder_level`| `INT` | No | `5` | `CHECK (reorder_level >= 0)` | Ngưỡng báo động sắp hết hàng |
| `version` | `BIGINT` | No | `0` | | Phiên bản phục vụ Optimistic Locking |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |
> **Ràng buộc duy nhất:** `UNIQUE (variant_id, store_id)` đảm bảo mỗi biến thể chỉ có đúng 1 dòng tồn kho tại mỗi cửa hàng.

### 3.9 Bảng `inventory_transactions` (Sổ cái giao dịch kho - Append Only)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã giao dịch kho |
| `variant_id` | `BIGINT` | No | | FK trỏ `product_variants(id)` | Biến thể biến động |
| `store_id` | `BIGINT` | No | | FK trỏ `stores(id)` | Tại chi nhánh nào |
| `type` | `NVARCHAR(30)` | No | | `CHECK (type IN ('IMPORT', 'SALE', 'CANCEL', 'TRANSFER_IN', 'TRANSFER_OUT', 'ADJUST'))` | Loại nghiệp vụ kho |
| `quantity_change`| `INT` | No | | | Số lượng biến thiên (+ tăng, - giảm) |
| `quantity_after` | `INT` | No | | `CHECK (quantity_after >= 0)` | Tồn kho ngay sau khi giao dịch |
| `reference_type` | `NVARCHAR(50)`| Yes | `NULL` | | Loại chứng từ tham chiếu (`ORDER`, `TRANSFER`...) |
| `reference_id` | `BIGINT` | Yes | `NULL` | | Khóa tham chiếu chứng từ gốc |
| `note` | `NVARCHAR(500)`| Yes | `NULL` | | Ghi chú lý do xuất nhập |
| `created_by` | `BIGINT` | Yes | `NULL` | FK trỏ `users(id)` | Nhân viên thực hiện |
| `created_at` | `DATETIME2` | No | `GETDATE()` | INDEX | Thời điểm phát sinh giao dịch |

### 3.10 Bảng `carts` và `cart_items` (Giỏ hàng khách đã đăng nhập)
- **`carts`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `user_id` (`BIGINT`, NOT NULL, FK trỏ `users(id)`)
  - `status` (`NVARCHAR(20)`, NOT NULL DEFAULT `'ACTIVE'`, `CHECK in ('ACTIVE', 'ABANDONED', 'CONVERTED')`)
  - `created_at`, `updated_at` (`DATETIME2`)
- **`cart_items`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `cart_id` (`BIGINT`, NOT NULL, FK trỏ `carts(id)` ON DELETE CASCADE)
  - `variant_id` (`BIGINT`, NOT NULL, FK trỏ `product_variants(id)`)
  - `quantity` (`INT`, NOT NULL DEFAULT 1, `CHECK (quantity > 0)`)
  - `UNIQUE (cart_id, variant_id)`
  - *Lưu ý:* Giá bán không lưu trong giỏ, truy vấn trực tiếp từ bảng biến thể.

### 3.11 Bảng `orders` (Đơn hàng đa kênh)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Khóa chính đơn hàng |
| `order_code` | `NVARCHAR(50)` | No | | `UNIQUE`, INDEX | Mã đơn hàng (ví dụ: `OD-20261103-0001`) |
| `channel` | `NVARCHAR(20)` | No | | `CHECK in ('ONLINE', 'POS')` | Kênh bán hàng |
| `store_id` | `BIGINT` | No | | FK trỏ `stores(id)`, INDEX | Cửa hàng tiếp nhận / xuất kho |
| `customer_id` | `BIGINT` | Yes | `NULL` | FK trỏ `users(id)`, INDEX | Tài khoản khách (null nếu khách lẻ POS) |
| `cashier_id` | `BIGINT` | Yes | `NULL` | FK trỏ `users(id)` | Thu ngân tạo đơn (null nếu đơn online) |
| `status` | `NVARCHAR(30)` | No | | `CHECK in ('PENDING', 'CONFIRMED', 'PACKING', 'SHIPPING', 'COMPLETED', 'CANCELLED')` | Trạng thái vòng đời đơn |
| `subtotal` | `DECIMAL(18,2)` | No | `0.00` | `CHECK (subtotal >= 0)` | Tiền hàng trước chiết khấu |
| `discount_total`| `DECIMAL(18,2)`| No | `0.00` | `CHECK (discount_total >= 0)`| Tổng tiền giảm giá (voucher) |
| `shipping_fee` | `DECIMAL(18,2)` | No | `0.00` | `CHECK (shipping_fee >= 0)` | Phí vận chuyển (0đ với đơn POS) |
| `total_amount` | `DECIMAL(18,2)` | No | `0.00` | `CHECK (total_amount >= 0)` | Tổng tiền khách phải thanh toán |
| `voucher_code` | `NVARCHAR(50)` | Yes | `NULL` | | Mã voucher đã áp dụng |
| `ship_name` | `NVARCHAR(255)` | Yes | `NULL` | | Snapshot: Tên người nhận (Online) |
| `ship_phone` | `NVARCHAR(20)` | Yes | `NULL` | | Snapshot: SĐT người nhận |
| `ship_address` | `NVARCHAR(500)` | Yes | `NULL` | | Snapshot: Địa chỉ nhận hàng |
| `ship_latitude`| `DECIMAL(10,7)` | Yes | `NULL` | | Snapshot: Vĩ độ nhận hàng |
| `ship_longitude`| `DECIMAL(10,7)`| Yes | `NULL` | | Snapshot: Kinh độ nhận hàng |
| `note` | `NVARCHAR(500)` | Yes | `NULL` | | Ghi chú đơn hàng của khách |
| `placed_at` | `DATETIME2` | No | `GETDATE()` | INDEX | Thời điểm đặt hàng |
| `completed_at` | `DATETIME2` | Yes | `NULL` | | Thời điểm giao/hoàn tất đơn |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.12 Bảng `order_items` (Chi tiết đơn hàng - Snapshot)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|
| `id` | `BIGINT` | No | PK, `IDENTITY(1,1)` | Khóa chính dòng đơn |
| `order_id` | `BIGINT` | No | FK trỏ `orders(id)`, INDEX | Thuộc đơn hàng nào |
| `variant_id` | `BIGINT` | Yes | FK trỏ `product_variants(id)` | Mã biến thể sản phẩm |
| `product_name` | `NVARCHAR(255)` | No | | Snapshot: Tên sản phẩm tại thời điểm mua |
| `sku` | `NVARCHAR(100)` | No | | Snapshot: Mã SKU |
| `color` | `NVARCHAR(50)` | No | | Snapshot: Màu sắc |
| `size` | `NVARCHAR(50)` | No | | Snapshot: Kích thước |
| `unit_price` | `DECIMAL(18,2)` | No | `CHECK (unit_price >= 0)` | Snapshot: Đơn giá bán tại thời điểm mua |
| `quantity` | `INT` | No | `CHECK (quantity > 0)` | Số lượng mua |
| `line_total` | `DECIMAL(18,2)` | No | `CHECK (line_total >= 0)` | Thành tiền dòng = unit_price × quantity |
| `created_at` | `DATETIME2` | No | `GETDATE()` | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | Thời điểm cập nhật |

### 3.13 Bảng `order_status_history` (Lịch sử chuyển trạng thái đơn hàng)
- `id` (PK, `BIGINT IDENTITY`)
- `order_id` (`BIGINT`, NOT NULL, FK trỏ `orders(id)`, INDEX)
- `from_status` (`NVARCHAR(30)`, NULL - null khi đơn vừa tạo)
- `to_status` (`NVARCHAR(30)`, NOT NULL - trạng thái đích)
- `changed_by` (`BIGINT`, NULL, FK trỏ `users(id)` - nhân viên thực hiện)
- `note` (`NVARCHAR(500)`, NULL - lý do chuyển/hủy)
- `changed_at` (`DATETIME2`, NOT NULL DEFAULT `GETDATE()`)

### 3.14 Bảng `payments` (Thanh toán đa phương thức)
| Tên cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc / Khóa | Mô tả |
|---|---|:---:|---|---|---|
| `id` | `BIGINT` | No | `IDENTITY(1,1)` | PK | Mã giao dịch thanh toán |
| `order_id` | `BIGINT` | No | | FK trỏ `orders(id)`, INDEX | Đơn hàng thanh toán |
| `method` | `NVARCHAR(30)` | No | | `CHECK (method IN ('CASH', 'CARD', 'QR', 'COD', 'EWALLET_MOCK'))` | Phương thức thanh toán (Strategy) |
| `amount` | `DECIMAL(18,2)` | No | | `CHECK (amount >= 0)` | Số tiền thanh toán |
| `status` | `NVARCHAR(20)` | No | `'PENDING'` | `CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))` | Trạng thái thanh toán |
| `transaction_ref`| `NVARCHAR(100)`| Yes | `NULL` | | Mã giao dịch ngân hàng/cổng thanh toán |
| `cash_received` | `DECIMAL(18,2)` | Yes | `NULL` | | Tiền mặt khách đưa (cho POS) |
| `change_amount` | `DECIMAL(18,2)` | Yes | `NULL` | | Tiền thừa trả lại khách (cho POS) |
| `paid_at` | `DATETIME2` | Yes | `NULL` | | Thời điểm thanh toán thành công |
| `created_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm tạo |
| `updated_at` | `DATETIME2` | No | `GETDATE()` | | Thời điểm cập nhật |

### 3.15 Bảng `vouchers` và `voucher_usages` (Khuyến mãi giảm giá)
- **`vouchers`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `code` (`NVARCHAR(50)`, NOT NULL, `UNIQUE`, INDEX)
  - `name` (`NVARCHAR(255)`, NOT NULL)
  - `discount_type` (`NVARCHAR(20)`, NOT NULL, `CHECK in ('PERCENT', 'FIXED')`)
  - `discount_value` (`DECIMAL(18,2)`, NOT NULL, `CHECK >= 0`)
  - `min_order_value` (`DECIMAL(18,2)`, NOT NULL DEFAULT 0.00)
  - `max_discount_value` (`DECIMAL(18,2)`, NULL)
  - `usage_limit` (`INT`, NULL)
  - `used_count` (`INT`, NOT NULL DEFAULT 0)
  - `start_at`, `end_at` (`DATETIME2`, NOT NULL)
  - `status` (`NVARCHAR(20)`, NOT NULL DEFAULT `'ACTIVE'`, `CHECK in ('ACTIVE', 'INACTIVE')`)
- **`voucher_usages`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `voucher_id` (`BIGINT`, NOT NULL, FK trỏ `vouchers(id)`)
  - `order_id` (`BIGINT`, NOT NULL, FK trỏ `orders(id)`)
  - `user_id` (`BIGINT`, NULL, FK trỏ `users(id)`)
  - `used_at` (`DATETIME2`, NOT NULL DEFAULT `GETDATE()`)

### 3.16 Bảng `pos_shifts` (Should Have - Ca làm việc thu ngân)
- `id` (PK, `BIGINT IDENTITY`)
- `store_id` (`BIGINT`, NOT NULL, FK trỏ `stores(id)`)
- `cashier_id` (`BIGINT`, NOT NULL, FK trỏ `users(id)`)
- `opened_at` (`DATETIME2`, NOT NULL DEFAULT `GETDATE()`)
- `closed_at` (`DATETIME2`, NULL)
- `opening_cash` (`DECIMAL(18,2)`, NOT NULL DEFAULT 0.00, `CHECK >= 0`)
- `closing_cash` (`DECIMAL(18,2)`, NULL)
- `expected_cash` (`DECIMAL(18,2)`, NULL)
- `status` (`NVARCHAR(20)`, NOT NULL DEFAULT `'OPEN'`, `CHECK in ('OPEN', 'CLOSED')`)

### 3.17 Bảng `stock_transfers` & `stock_transfer_items` (Should Have - Chuyển kho)
- **`stock_transfers`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `code` (`NVARCHAR(50)`, NOT NULL, `UNIQUE`)
  - `from_store_id` (`BIGINT`, NOT NULL, FK trỏ `stores(id)`)
  - `to_store_id` (`BIGINT`, NOT NULL, FK trỏ `stores(id)`)
  - `status` (`NVARCHAR(20)`, NOT NULL DEFAULT `'DRAFT'`, `CHECK in ('DRAFT', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED')`)
  - `note` (`NVARCHAR(500)`, NULL)
  - `created_by` (`BIGINT`, NULL, FK trỏ `users(id)`)
- **`stock_transfer_items`:**
  - `id` (PK, `BIGINT IDENTITY`)
  - `transfer_id` (`BIGINT`, NOT NULL, FK trỏ `stock_transfers(id)` ON DELETE CASCADE)
  - `variant_id` (`BIGINT`, NOT NULL, FK trỏ `product_variants(id)`)
  - `quantity` (`INT`, NOT NULL, `CHECK > 0`)
  - `UNIQUE (transfer_id, variant_id)`

### 3.18 Bảng `reviews` (Could Have - Đánh giá sản phẩm)
- `id` (PK, `BIGINT IDENTITY`)
- `order_item_id` (`BIGINT`, NOT NULL, `UNIQUE`, FK trỏ `order_items(id)`)
- `user_id` (`BIGINT`, NOT NULL, FK trỏ `users(id)`)
- `product_id` (`BIGINT`, NOT NULL, FK trỏ `products(id)`)
- `rating` (`INT`, NOT NULL, `CHECK (rating >= 1 AND rating <= 5)`)
- `comment` (`NVARCHAR(1000)`, NULL)
- `status` (`NVARCHAR(20)`, NOT NULL DEFAULT `'APPROVED'`, `CHECK in ('PENDING', 'APPROVED', 'HIDDEN')`)

---

## 4. BẢNG TỔNG HỢP KHÓA NGOẠI (FOREIGN KEYS SUMMARY)

| Bảng nguồn | Cột khóa ngoại | Bảng tham chiếu | Hành vi xóa (ON DELETE) | Mục đích nghiệp vụ |
|---|---|---|---|---|
| `categories` | `parent_id` | `categories(id)` | NO ACTION | Cây phân cấp danh mục cha - con |
| `products` | `category_id` | `categories(id)` | NO ACTION | Phân loại sản phẩm vào danh mục |
| `products` | `brand_id` | `brands(id)` | NO ACTION | Gắn sản phẩm với thương hiệu cung cấp |
| `product_variants` | `product_id` | `products(id)` | NO ACTION | Biến thể thuộc sản phẩm cha |
| `product_images` | `product_id` | `products(id)` | NO ACTION | Ảnh hiển thị của sản phẩm |
| `product_images` | `variant_id` | `product_variants(id)` | NO ACTION | Ảnh riêng cho biến thể màu sắc |
| `employees` | `user_id` | `users(id)` | NO ACTION | Hồ sơ nhân viên ứng với tài khoản |
| `employees` | `store_id` | `stores(id)` | NO ACTION | Cửa hàng công tác của nhân viên |
| `inventories` | `variant_id` | `product_variants(id)` | NO ACTION | Theo dõi tồn kho của từng biến thể |
| `inventories` | `store_id` | `stores(id)` | NO ACTION | Điểm lưu kho (cửa hàng/kho tổng) |
| `inventory_transactions` | `variant_id` | `product_variants(id)` | NO ACTION | Biến thể phát sinh biến động kho |
| `inventory_transactions` | `store_id` | `stores(id)` | NO ACTION | Cửa hàng phát sinh giao dịch kho |
| `inventory_transactions` | `created_by` | `users(id)` | NO ACTION | Nhân viên tạo phiếu xuất/nhập/điều chỉnh |
| `carts` | `user_id` | `users(id)` | NO ACTION | Chủ nhân giỏ hàng trực tuyến |
| `cart_items` | `cart_id` | `carts(id)` | CASCADE | Danh sách hàng trong giỏ (xóa theo giỏ) |
| `cart_items` | `variant_id` | `product_variants(id)` | NO ACTION | Biến thể được đưa vào giỏ |
| `orders` | `store_id` | `stores(id)` | NO ACTION | Chi nhánh xử lý đơn hàng / xuất bán |
| `orders` | `customer_id` | `users(id)` | NO ACTION | Tài khoản khách mua hàng (Online) |
| `orders` | `cashier_id` | `users(id)` | NO ACTION | Thu ngân lên đơn tại quầy (POS) |
| `order_items` | `order_id` | `orders(id)` | NO ACTION | Dòng chi tiết thuộc đơn hàng |
| `order_items` | `variant_id` | `product_variants(id)` | SET NULL / NO ACTION | Tham chiếu gốc đến biến thể sản phẩm |
| `order_status_history` | `order_id` | `orders(id)` | NO ACTION | Đơn hàng được đổi trạng thái |
| `order_status_history` | `changed_by` | `users(id)` | NO ACTION | Nhân viên thực hiện chuyển trạng thái |
| `payments` | `order_id` | `orders(id)` | NO ACTION | Đơn hàng được thanh toán |
| `voucher_usages` | `voucher_id` | `vouchers(id)` | NO ACTION | Voucher được sử dụng |
| `voucher_usages` | `order_id` | `orders(id)` | NO ACTION | Đơn hàng áp dụng voucher |
| `voucher_usages` | `user_id` | `users(id)` | NO ACTION | Khách hàng đã áp dụng mã |
| `pos_shifts` | `store_id` | `stores(id)` | NO ACTION | Cửa hàng mở ca thu ngân |
| `pos_shifts` | `cashier_id` | `users(id)` | NO ACTION | Thu ngân trực ca |
| `stock_transfers` | `from_store_id` | `stores(id)` | NO ACTION | Cửa hàng xuất chuyển hàng |
| `stock_transfers` | `to_store_id` | `stores(id)` | NO ACTION | Cửa hàng nhập nhận hàng |
| `stock_transfers` | `created_by` | `users(id)` | NO ACTION | Người tạo lệnh điều chuyển |
| `stock_transfer_items` | `transfer_id` | `stock_transfers(id)` | CASCADE | Món hàng chuyển theo phiếu |
| `stock_transfer_items` | `variant_id` | `product_variants(id)` | NO ACTION | Biến thể được điều chuyển |
| `reviews` | `order_item_id` | `order_items(id)` | NO ACTION | Đánh giá gắn với sản phẩm đã mua |
| `reviews` | `user_id` | `users(id)` | NO ACTION | Khách hàng viết đánh giá |
| `reviews` | `product_id` | `products(id)` | NO ACTION | Sản phẩm được đánh giá |

