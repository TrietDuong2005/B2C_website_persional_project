# PROJECT BRIEF v2 — HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG (WEBSITE + POS + ADMIN)
### Spring Boot MVC + Thymeleaf + SQL Server

> **File này thay thế hoàn toàn bản v1.** Đây là "hợp đồng" giữa chủ dự án và AI: mọi AI làm việc trên repo này phải đọc file này trước, đặc biệt **mục 7, 8, 12, 13**.
>
> `thiet-ke-db-ecommerce-b2c.md` (thiết kế hơn 40 bảng) chỉ còn là **tài liệu tham khảo / hướng mở rộng**. Không dùng nó làm phạm vi. Phạm vi thật nằm ở mục 7 và 8 bên dưới.

---

## 0. THAY ĐỔI SO VỚI V1

| Hạng mục | v1 | v2 |
|---|---|---|
| Phạm vi CSDL | hơn 40 bảng, 13 module | **24 bảng Must + 4 bảng Should/Could** |
| Giao diện | Vue 3 tách riêng | **Thymeleaf + Bootstrap 5, một project duy nhất** |
| Nghiệp vụ | B2C online | **Chuỗi cửa hàng + POS + website online + admin** |
| Thuộc tính sản phẩm | EAV động (attributes/attribute_values) | **Cột `color`, `size` trực tiếp trong `product_variants`** |
| Lộ trình | 10 giai đoạn | **14 ngày** (mục 12) |
| Giữ nguyên | Kiến trúc phân lớp, quy ước đặt tên, `ApiResponse<T>`, Flyway, JWT cho `/api/**`, quy tắc với AI | |

**Đã xong:** giai đoạn 1 (auth + user), gồm migration V1–V6 (`users`, `roles`, `user_roles`, `user_profiles`, `addresses`, `auth_tokens`, `otp_verifications`). **Không sửa các migration này.**

---

## 1. TỔNG QUAN & RÀNG BUỘC

- **Bài toán:** chuỗi cửa hàng thời trang bán lẻ (áo, quần, giày, dép, ba lô, ví, dây nịt, nón, đồng hồ). Có 3 khu giao diện: **Shop** (website bán online), **POS** (bán tại quầy), **Admin** (quản trị chuỗi).
- **Phục vụ 2 môn:** Phát triển phần mềm hướng đối tượng (HDT) và Quản lý dự án phần mềm (QLDA). Một sản phẩm, hai báo cáo.
- **Ràng buộc:** 1 người, 14 ngày, khoảng 4–6 giờ hiệu quả mỗi ngày, 100% code do AI sinh. Người làm phải **đọc hiểu và bảo vệ được** code. Dự án Flutter song song có mức ưu tiên thấp hơn.
- **Actors:**

| Actor | Role trong hệ thống | Dùng khu nào |
|---|---|---|
| Khách hàng | `CUSTOMER` | Shop |
| Thu ngân | `CASHIER` | POS |
| Quản lý cửa hàng | `STORE_MANAGER` | POS, Admin (chỉ dữ liệu cửa hàng mình) |
| Quản trị hệ thống | `ADMIN` | Admin (toàn chuỗi) |

---

## 2. CÔNG NGHỆ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ / Framework | Java 17, Spring Boot 3.x (MVC), Maven |
| CSDL | Microsoft SQL Server, Spring Data JPA (Hibernate) |
| Migration | Flyway (`ddl-auto: validate`) |
| Giao diện | **Thymeleaf** + `thymeleaf-extras-springsecurity6`, Bootstrap 5, Bootstrap Icons, Chart.js (dashboard). Admin/POS dùng 1 template có sẵn (AdminLTE hoặc Tabler). Tất cả nạp qua CDN |
| Bảo mật | Spring Security: **2 SecurityFilterChain** (xem mục 9) |
| Mapping / Validation | MapStruct, Jakarta Validation, Lombok |
| Tài liệu API | springdoc-openapi (Swagger) cho `/api/**` |
| Test | JUnit 5 + Mockito, chỉ cho phần lõi (mục 12, ngày 12) |
| Sơ đồ | PlantUML (`docs/*.puml`) |

---

## 3. KIẾN TRÚC

Luồng chuẩn, **không được bỏ lớp hoặc gọi tắt**:

```
Trang web:  Browser → PageController (web/) ─┐
API/AJAX:   Client  → RestController ────────┤→ Service → Repository → Database
                                             │      ↓
                                             │   Entity (chứa luật nghiệp vụ của chính nó)
```

- **Service dùng chung** cho cả trang web và REST. **Chỉ tạo REST endpoint khi trang cần AJAX** (POS tìm sản phẩm, thêm vào giỏ...) hoặc để test bằng Swagger. Đừng viết 2 bộ controller cho mọi chức năng.
- **PageController** mỏng: nhận request, gọi Service, đưa dữ liệu vào `Model`, trả tên template. Không chứa nghiệp vụ.
- **Entity giàu hành vi (rich domain model):** luật nghiệp vụ đặt trong Entity khi nó thuộc về chính đối tượng đó (ví dụ `Inventory.deduct(qty)`, `Order.addItem(...)`, `Voucher.calculateDiscount(subtotal)`). Service điều phối, giao dịch, gọi Repository.
- **Không trả Entity ra ngoài API**: dùng DTO + MapStruct. Với trang Thymeleaf, đưa DTO/ViewModel vào Model, tránh đưa thẳng Entity có quan hệ lazy.
- **Exception:** `GlobalExceptionHandler` cho REST; với trang web, lỗi nghiệp vụ hiển thị qua `RedirectAttributes` (flash message) hoặc trang lỗi chung.

---

## 4. CẤU TRÚC THƯ MỤC

```
src/main/java/com/dahdt/ecommerce/
├── config/                  # SecurityConfig (2 chain), SwaggerConfig, CorsConfig, WebConfig
├── common/                  # exception/, response/ (ApiResponse), util/, BaseEntity
└── modules/
    ├── auth/  user/         # ĐÃ XONG (giai đoạn 1)
    ├── catalog/             # categories, brands, products, product_variants, product_images
    ├── store/               # stores, employees, pos_shifts
    ├── inventory/           # inventories, inventory_transactions, stock_transfers
    ├── cart/                # carts, cart_items
    ├── order/               # orders, order_items, order_status_history
    │   ├── state/           # State pattern (trạng thái đơn)
    │   ├── factory/         # Factory (tạo đơn ONLINE / POS)
    │   └── event/           # Spring Events (Observer)
    ├── payment/             # payments
    │   └── strategy/        # Strategy pattern (cách thanh toán)
    ├── promotion/           # vouchers, voucher_usages
    │   └── strategy/        # Strategy pattern (cách giảm giá)
    ├── report/              # dashboard, thống kê (chỉ đọc)
    └── review/              # reviews (Could)
        (mỗi module có: controller/, web/, service/, repository/, entity/, dto/, mapper/)

src/main/resources/
├── db/migration/            # V1..Vn
├── templates/
│   ├── layout/              # fragment dùng chung (header, sidebar, footer)
│   ├── shop/  admin/  pos/  # 3 khu giao diện
│   └── error/
└── static/                  # css/js tự viết (nếu có)

docs/                        # tài liệu cho 2 báo cáo (*.md, *.puml)
docs/nhat-ky/                # nhật ký theo ngày
```

**Quy ước chỗ đặt:** `controller/` = REST (`XxxController`, đường dẫn `/api/v1/...`); `web/` = trang Thymeleaf (`XxxPageController`). Các controller REST của giai đoạn 1 giữ nguyên.

---

## 5. QUY ƯỚC ĐẶT TÊN & CODE

| Đối tượng | Quy ước | Ví dụ |
|---|---|---|
| Bảng | snake_case, số nhiều | `product_variants` |
| Entity | PascalCase, số ít | `ProductVariant` |
| DTO | hậu tố `RequestDTO` / `ResponseDTO` | `OrderResponseDTO` |
| Service | interface `XxxService` + `XxxServiceImpl` | `OrderService` |
| Repository | `XxxRepository` | `OrderRepository` |
| REST controller | `XxxController`, `/api/v1/...` danh từ số nhiều | `/api/v1/orders` |
| Page controller | `XxxPageController`, URL theo khu: `/shop/...`, `/admin/...`, `/pos/...` | `/admin/products` |
| Template | `templates/<khu>/<module>/<trang>.html`, kebab-case | `admin/catalog/product-list.html` |
| Enum | PascalCase, giá trị UPPER_SNAKE, lưu `@Enumerated(EnumType.STRING)` | `OrderStatus.PENDING` |

- **Response REST** luôn bọc `ApiResponse<T>` (`code`, `message`, `data`).
- **Comment tiếng Việt** cho: mọi Service, mọi hàm nghiệp vụ không tầm thường, mọi class pattern. Mỗi class pattern có comment đầu file nêu rõ nó là pattern nào và vì sao dùng ở đây (phục vụ báo cáo HDT).
- **Tiền** dùng `BigDecimal` / `DECIMAL(18,2)`. **Văn bản tiếng Việt** dùng `NVARCHAR` trong SQL Server. **Thời gian** dùng `DATETIME2`.

---

## 6. CẤU HÌNH

- `application.yml` (chung) + `application-dev.yml` (mật khẩu DB, **không commit**).
- `ddl-auto: validate`, `show-sql: true` trong dev.
- **Flyway:** mỗi thay đổi schema là **một file migration mới** (`V7__...`, `V8__...`). **Tuyệt đối không sửa migration đã chạy** (Flyway sẽ báo lỗi checksum). Nếu đã lỡ sai khi còn ở DB dev: xoá DB `ecommerce_db`, tạo lại rỗng, chạy lại.
- Dữ liệu mẫu: seed bảng danh mục bằng migration SQL; **tài khoản người dùng** seed bằng `DevDataSeeder` (chỉ chạy ở profile `dev`, mật khẩu băm bằng `PasswordEncoder`).

---

## 7. PHẠM VI (MoSCoW)

| Mức | Nội dung |
|---|---|
| **Must** | Đăng nhập + phân quyền 4 role · CRUD danh mục, thương hiệu, sản phẩm, biến thể (màu/size), ảnh (URL) · cửa hàng, nhân viên · **tồn kho theo từng cửa hàng** + lịch sử kho · **Shop:** xem/lọc/tìm sản phẩm, giỏ hàng (khách đã đăng nhập), đặt hàng online (COD + thanh toán giả lập), voucher · **POS:** tìm/quét SKU, lập hóa đơn, áp voucher, thanh toán tiền mặt/thẻ/QR (giả lập), tính tiền thừa, in hóa đơn · **Admin:** quản lý đơn mọi kênh, đổi trạng thái, quản lý voucher, dashboard (doanh thu theo cửa hàng/ngày/kênh, top sản phẩm, tồn kho thấp) |
| **Should** | Ca thu ngân (mở/đóng ca) · chuyển kho giữa cửa hàng · đơn online tự chọn cửa hàng gần nhất còn hàng + phí ship theo khoảng cách (Haversine từ lat/lng) |
| **Could** | Đánh giá sản phẩm · tích điểm · đổi trả |
| **Won't** (chỉ ghi "hướng phát triển" trong báo cáo) | Flash sale, CMS/blog, ticket CSKH, bảo hành, OTP/OAuth, nhập hàng từ NCC, cổng thanh toán/vận chuyển thật, giỏ hàng khách vãng lai, thuộc tính sản phẩm động (EAV) |

**Không được tự thêm tính năng, bảng hay cột ngoài danh sách này nếu chưa hỏi chủ dự án** (chống phình phạm vi).

---

## 8. CƠ SỞ DỮ LIỆU v2

### 8.1 Đã có (V1–V6, giữ nguyên)
`users`, `roles`, `user_roles`, `user_profiles`, `addresses`, `auth_tokens`, `otp_verifications`. Kiểm tra `addresses` đã có `latitude`, `longitude`; nếu thiếu, thêm bằng migration mới.

### 8.2 Bảng mới

| Mức | Bảng | Cột chính | Ghi chú |
|---|---|---|---|
| Must | `categories` | id, parent_id, name, slug, sort_order, is_active | cây cha–con |
| Must | `brands` | id, name, slug, is_active | |
| Must | `products` | id, category_id, brand_id, name, slug, description, thumbnail_url, status (ACTIVE/HIDDEN) | sản phẩm "cha" |
| Must | `product_variants` | id, product_id, sku (unique), barcode (unique, null), color, size, price, cost_price, weight_gram, status | đơn vị thực sự bán và có tồn kho |
| Must | `product_images` | id, product_id, variant_id (null), url, sort_order, is_thumbnail | |
| Must | `stores` | id, code (unique), name, type (STORE/WAREHOUSE), province, district, ward, detail_address, latitude, longitude, phone, status | thay cho `warehouses` |
| Must | `employees` | id, user_id (unique), store_id, position (MANAGER/CASHIER/STAFF), status | gắn user với cửa hàng |
| Must | `inventories` | id, variant_id, store_id, quantity_on_hand, reorder_level, **version** | unique (variant_id, store_id); `version` cho khóa lạc quan |
| Must | `inventory_transactions` | id, variant_id, store_id, type (IMPORT/SALE/CANCEL/TRANSFER_IN/TRANSFER_OUT/ADJUST), quantity_change (±), quantity_after, reference_type, reference_id, note, created_by, created_at | sổ cái kho, chỉ thêm, không sửa |
| Must | `carts` | id, user_id, status | giỏ hàng cho khách đã đăng nhập |
| Must | `cart_items` | id, cart_id, variant_id, quantity | unique (cart_id, variant_id); **giá lấy từ DB, không lưu** |
| Must | `orders` | id, order_code (unique), **channel (ONLINE/POS)**, store_id, customer_id (null), cashier_id (null), status, subtotal, discount_total, shipping_fee, total_amount, voucher_code, ship_name, ship_phone, ship_address, ship_latitude, ship_longitude, note, placed_at, completed_at | dùng chung cho 2 kênh; thông tin giao hàng là snapshot (null với POS) |
| Must | `order_items` | id, order_id, variant_id, product_name, sku, color, size, unit_price, quantity, line_total | snapshot tại thời điểm mua |
| Must | `order_status_history` | id, order_id, from_status, to_status, changed_by, note, changed_at | |
| Must | `payments` | id, order_id, method (CASH/CARD/QR/COD/EWALLET_MOCK), amount, status (PENDING/PAID/FAILED/REFUNDED), transaction_ref, cash_received, change_amount, paid_at | thay cho `payment_methods` |
| Must | `vouchers` | id, code (unique), name, discount_type (PERCENT/FIXED), discount_value, min_order_value, max_discount_value, usage_limit, used_count, start_at, end_at, status | |
| Must | `voucher_usages` | id, voucher_id, order_id, user_id (null), used_at | |
| Should | `pos_shifts` | id, store_id, cashier_id, opened_at, closed_at, opening_cash, closing_cash, expected_cash, status | |
| Should | `stock_transfers` | id, code, from_store_id, to_store_id, status (DRAFT/IN_TRANSIT/RECEIVED/CANCELLED), created_by, created_at | |
| Should | `stock_transfer_items` | id, transfer_id, variant_id, quantity | |
| Could | `reviews` | id, order_item_id (unique), user_id, product_id, rating (1–5), comment, status | |

Tổng: **7 (đã có) + 17 Must mới = 24 bảng Must**, cộng 3 Should và 1 Could.

### 8.3 Nguyên tắc
- Khóa chính `BIGINT IDENTITY(1,1)`. Mọi bảng mới có `created_at`, `updated_at` giống `BaseEntity` hiện có (đọc `V1` và `BaseEntity` để giữ cùng phong cách).
- Có đầy đủ `FOREIGN KEY`, `NOT NULL`, `UNIQUE`, `CHECK` cho giá trị enum, index cho cột tra cứu thường xuyên (`sku`, `order_code`, `store_id`, `placed_at`).
- Tiếng Việt dùng `NVARCHAR`.

---

## 9. PHÂN QUYỀN & BẢO MẬT

Hai `SecurityFilterChain` trong `SecurityConfig`:

1. **Chain `/api/**`:** stateless, JWT (đã làm ở giai đoạn 1). `/api/v1/auth/**` và Swagger cho phép public.
2. **Chain còn lại (trang web):** đăng nhập bằng form + **session**, CSRF bật, trang đăng nhập riêng.

| Khu | URL | Role được vào |
|---|---|---|
| Shop | `/`, `/shop/**` (xem), `/cart`, `/checkout`, `/account/**` | xem: ai cũng được; giỏ/đặt hàng/tài khoản: `CUSTOMER` |
| POS | `/pos/**` | `CASHIER`, `STORE_MANAGER`, `ADMIN` |
| Admin | `/admin/**` | `ADMIN`, `STORE_MANAGER` (quản lý chỉ thấy dữ liệu cửa hàng mình) |

Role mới cần thêm bằng migration: `STORE_MANAGER`, `CASHIER` (đã có `CUSTOMER`, `ADMIN`).

---

## 10. QUY TẮC NGHIỆP VỤ LÕI

1. **Giá luôn lấy từ DB** tại thời điểm tạo đơn, không bao giờ tin giá do client gửi lên.
2. **Trừ kho ngay khi tạo đơn** (cả ONLINE và POS), ghi một dòng `inventory_transactions`. Hủy đơn thì hoàn kho và ghi dòng `CANCEL`. Dùng `@Version` trên `inventories` (khóa lạc quan) và `@Transactional` để chống bán vượt tồn khi 2 thao tác cùng lúc.
3. **Không đủ tồn thì chặn** (báo lỗi nghiệp vụ rõ ràng), không cho tồn kho âm.
4. **Đơn POS:** tạo và thanh toán trong một thao tác, kết thúc ở trạng thái `COMPLETED`. Trừ kho của cửa hàng mà thu ngân đang làm.
5. **Đơn ONLINE:** vòng đời `PENDING → CONFIRMED → PACKING → SHIPPING → COMPLETED`; có thể `CANCELLED` từ `PENDING`, `CONFIRMED`, `PACKING`. Mọi lần đổi trạng thái ghi `order_status_history`. Chuyển trạng thái sai thứ tự phải bị chặn.
6. **Voucher:** kiểm tra còn hạn, còn lượt, đủ giá trị tối thiểu; giảm tối đa `max_discount_value`; ghi `voucher_usages` và tăng `used_count` trong cùng giao dịch với đơn hàng.
7. **Snapshot:** `order_items` và thông tin giao hàng trong `orders` lưu giá trị tại thời điểm mua.
8. **Tính tiền:** `total_amount = subtotal − discount_total + shipping_fee`. Mọi phép tính tiền dùng `BigDecimal`, làm tròn thống nhất.

---

## 11. DESIGN PATTERN BẮT BUỘC (cho báo cáo HDT)

| Pattern | Dùng ở đâu | Vị trí |
|---|---|---|
| **Strategy** | Cách thanh toán (`CashPayment`, `CardPayment`, `QrPayment`, `CodPayment`) và cách giảm giá (`PercentDiscount`, `FixedDiscount`) | `payment/strategy/`, `promotion/strategy/` |
| **State** | Vòng đời đơn hàng: mỗi trạng thái là một class quyết định chuyển được sang trạng thái nào | `order/state/` |
| **Factory** | Tạo đơn ONLINE và đơn POS với mặc định khác nhau (kênh, trạng thái đầu, có/không thông tin giao hàng) | `order/factory/` |
| **Observer** | Spring Events: `OrderPlacedEvent` → listener trừ kho + ghi sổ kho; `OrderCancelledEvent` → listener hoàn kho. Listener chạy đồng bộ, trong cùng transaction | `order/event/` |

Mỗi class pattern có comment đầu file nêu pattern, vai trò, lý do dùng. Cũng cần thể hiện **đóng gói, kế thừa (`BaseEntity`), đa hình (interface Strategy/State)** trong sơ đồ lớp.

---

## 12. LỘ TRÌNH 14 NGÀY

Mỗi ngày kết thúc bằng: **build thật thành công, app chạy được, tự test, commit**. Ngày nào không đạt thì không sang ngày sau (trừ khi dùng cut-list bên dưới).

| Ngày | Làm gì | Đạt khi |
|---|---|---|
| 1 | Chốt phạm vi, use case, WBS + lịch, risk register, kế hoạch chất lượng (chỉ tài liệu, không code) | `docs/01..05` đã đọc và chỉnh bởi chủ dự án |
| 2 | ERD v2, toàn bộ migration + seed, class diagram nháp | App chạy, Flyway áp dụng hết migration, dữ liệu mẫu có đủ |
| 3 | **Lát mẫu:** Security 2 chain + layout chung + admin CRUD danh mục/thương hiệu/sản phẩm/biến thể | Đăng nhập admin, thêm 1 sản phẩm có biến thể từ giao diện |
| 4 | Cửa hàng, nhân viên, tồn kho theo cửa hàng, sổ kho | Admin xem/chỉnh tồn từng cửa hàng, có lịch sử kho |
| 5 | Shop: trang chủ, danh sách/lọc/tìm/chi tiết sản phẩm, giỏ hàng | Khách thêm sản phẩm vào giỏ |
| 6 | Checkout online, thanh toán (Strategy), voucher, trừ kho (Observer) | Đặt đơn COD thành công, kho giảm đúng |
| 7 | **Mốc kiểm tra:** so kế hoạch–thực tế, trả nợ kỹ thuật, tag `v0.5` | Báo cáo tiến độ + quyết định cắt/giữ Should |
| 8–9 | POS: tìm/quét SKU, hóa đơn, tiền thừa, in hóa đơn (+ ca thu ngân nếu kịp) | Bán 1 đơn tại quầy, in được hóa đơn, kho trừ đúng |
| 10 | Admin quản lý đơn mọi kênh, đổi trạng thái (State), lịch sử | Đi hết vòng đời 1 đơn online |
| 11 | Dashboard: doanh thu theo cửa hàng/ngày/kênh, top sản phẩm, tồn kho thấp | Số liệu khớp dữ liệu mẫu |
| 12 | **Code freeze:** test end-to-end, unit test lõi, chuẩn bị dữ liệu demo | Kịch bản demo chạy trọn vẹn |
| 13 | Hoàn thiện 2 báo cáo (số liệu thật, ảnh chụp, sơ đồ) | Hai bản báo cáo hoàn chỉnh |
| 14 | Dự phòng, tập demo, nộp | |

**Cut-list (cắt theo thứ tự nếu trễ ở ngày 7):** (1) toàn bộ mức Should, (2) `pos_shifts`, (3) biểu đồ dashboard (chỉ giữ bảng số), (4) mức Could. **Mức Must không cắt.**

---

## 13. QUY TẮC BẮT BUỘC VỚI AI

1. **Không giả định quy ước, phải đọc code thật.** Trước khi sửa hoặc gọi đến file dùng chung (`ApiResponse.java`, `BaseEntity`, `GlobalExceptionHandler`, `SecurityConfig`, Entity/Service đã có), mở và in nguyên văn nội dung hiện tại của file đó.
2. **Không tự ý đổi quy ước đã có.** Thấy quy ước cũ có vấn đề thì hỏi chủ dự án trước.
3. **Cấm báo "hoàn thành"/"build thành công" nếu chưa chạy build thật.** Sau khi sửa code: chạy `mvn clean compile` trong terminal, dán nguyên văn output, chỉ được xác nhận khi có dòng `BUILD SUCCESS`. Cấm các câu như "tôi tự tin code đúng".
4. **Sau khi đổi model hoặc mở phiên mới:** đọc lại toàn bộ `PROJECT_BRIEF.md`, tài liệu liên quan trong `docs/` và code hiện có của module đang làm trước khi viết thêm.
5. **Quy ước dùng chung đã chốt (không đổi):**
   - `ApiResponse.success(T data, String message)`; `ApiResponse.success(String message)` khi không có data; `ApiResponse.error(String message, int code)`.
   - Thứ tự `annotationProcessorPaths` trong `pom.xml`: lombok → mapstruct-processor → lombok-mapstruct-binding (binding đứng cuối).
6. **Không sửa migration đã tồn tại.** Mọi thay đổi schema là migration mới.
7. **Không thêm tính năng/bảng/cột ngoài mục 7–8** nếu chưa hỏi.
8. **Mỗi lát cắt kết thúc bằng:** build thật + chạy app + tự test các luồng chính + hướng dẫn chủ dự án test thủ công từng bước. Không commit trừ khi được yêu cầu.
9. **Không bịa số liệu thật** trong tài liệu (thời gian thực tế, kết quả test...). Phần "thực tế" để trống cho chủ dự án điền.
10. **Sau mỗi lần sinh code:** liệt kê file đã tạo/sửa, mỗi file một dòng giải thích bằng tiếng Việt đơn giản (chủ dự án là người mới).

---

## 14. GIT, TÀI LIỆU, NHẬT KÝ

- Nhánh làm việc: `develop`; `main` chỉ nhận code đã chạy được. Commit cuối mỗi ngày, theo Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`).
- Tag: `v0.5` (ngày 7), `v1.0` (ngày 12).
- Nhật ký: mỗi ngày một file `docs/nhat-ky/YYYY-MM-DD.md`, ghi việc dự định, việc làm được, **giờ thực tế**, vướng mắc, cách xử lý, kèm ảnh chụp. Dùng làm số liệu cho báo cáo QLDA.
- Lưu lại các prompt đã dùng vào `docs/prompts/` (bằng chứng quy trình làm việc).
