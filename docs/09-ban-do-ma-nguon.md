# 09. BẢN ĐỒ MÃ NGUỒN HỆ THỐNG "TRIẾT STORE"

> **Tài liệu tham chiếu mã nguồn và kiến trúc hệ thống.**  
> Mọi thông tin trong tài liệu này được đối chiếu trực tiếp 100% từ mã nguồn Java (`src/main/java`), tệp cấu hình (`application.yml`, `application-dev.yml`), kịch bản CSDL Flyway (`src/main/resources/db/migration`), giao diện Thymeleaf (`src/main/resources/templates`) và mã kiểm thử (`src/test/java`).

---

## MỤC LỤC
1. [Cấu trúc gói & module](#1-cấu-trúc-gói--module)
2. [Bảng đường dẫn (URLs & Endpoints)](#2-bảng-đường-dẫn-urls--endpoints)
   - [2.1. Khu Shop (Khách hàng)](#21-khu-shop-khách-hàng)
   - [2.2. Khu Quản trị (ADMIN, STORE_MANAGER)](#22-khu-quản-trị-admin-store_manager)
   - [2.3. Khu POS (CASHIER, STORE_MANAGER, ADMIN)](#23-khu-pos-cashier-store_manager-admin)
   - [2.4. Khu API Công khai & REST API (Stateless JWT)](#24-khu-api-công-khai--rest-api-stateless-jwt)
   - [2.5. Trang Đăng nhập, Trang Lỗi & Phục vụ tệp tải lên (Web Public)](#25-trang-đăng-nhập-trang-lỗi--phục-vụ-tệp-tải-lên-web-public)
3. [Bản đồ Design Pattern](#3-bản-đồ-design-pattern)
   - [3.1. Strategy Pattern](#31-strategy-pattern)
   - [3.2. Factory Pattern](#32-factory-pattern)
   - [3.3. Observer Pattern (Spring Events)](#33-observer-pattern-spring-events)
   - [3.4. State Pattern (Vòng đời đơn hàng)](#34-state-pattern-vòng-đời-đơn-hàng)
4. [Danh sách Thực thể (Entity) & Bảng CSDL](#4-danh-sách-thực-thể-entity--bảng-csdl)
   - [4.1. Chi tiết 23 Thực thể JPA](#41-chi-tiết-23-thực-thể-jpa)
   - [4.2. Bảng tổng hợp 28 bảng trong Flyway Migration (V1–V31)](#42-bảng-tổng-hợp-28-bảng-trong-flyway-migration-v1v31)
5. [Danh sách Service & Quy tắc nghiệp vụ lõi](#5-danh-sách-service--quy-tắc-nghiệp-vụ-lõi)
   - [5.1. Danh sách Service và các phương thức chính](#51-danh-sách-service-và-các-phương-thức-chính)
   - [5.2. Các quy tắc nghiệp vụ nổi bật](#52-các-quy-tắc-nghiệp-vụ-nổi-bật)
6. [Thuộc tính cấu hình hệ thống (tiền tố app.*)](#6-thuộc-tính-cấu-hình-hệ-thống-tiền-tố-app)
7. [Thống kê kiểm thử (Tests - Đếm từ mã nguồn)](#7-thống-kê-kiểm-thử-tests---đếm-từ-mã-nguồn)
   - [7.1. Bảng số lượng lớp test và phương thức kiểm thử theo module](#71-bảng-số-lượng-lớp-test-và-phương-thức-kiểm-thử-theo-module)
   - [7.2. Danh sách Test tích hợp trên SQL Server thật](#72-danh-sách-test-tích-hợp-trên-sql-server-thật)
   - [7.3. Danh sách Test dựng trang thật (Thymeleaf Rendering Integration Test)](#73-danh-sách-test-dựng-trang-thật-thymeleaf-rendering-integration-test)
8. [Các hạn chế đã biết](#8-các-hạn-chế-đã-biết)
9. [Danh sách TODO, FIXME & Báo cáo các lớp không được gọi trực tiếp](#9-danh-sách-todo-fixme--báo-cáo-các-lớp-không-được-gọi-trực-tiếp)

---

## 1. CẤU TRÚC GÓI & MODULE

Toàn bộ mã nguồn Java của dự án đặt dưới gói `com.dahdt.ecommerce`, phân chia theo kiến trúc MVC kết hợp thiết kế theo module nghiệp vụ (Modular Monolith):

| Gói / Module | Số lớp Java | Mô tả chức năng |
|---|:---:|---|
| `com.dahdt.ecommerce` (Gốc) | 1 | Lớp khởi động ứng dụng Spring Boot (`EcommerceApplication.java`). |
| `common` | 12 | Các thành phần dùng chung: BaseEntity (audit thời gian), Exception nghiệp vụ, ApiResponse chuẩn, tiện ích MoneyUtil, RedirectUtil, SlugUtil, xử lý trang lỗi và Global Model Attributes. |
| `config` | 10 | Cấu hình Security (2 SecurityFilterChain), Cấu hình tài nguyên tải lên WebMvcConfig, Swagger OpenAPI, DevDataSeeder, DemoDataPlanner, DemoDataSeeder và các thành phần bảo mật JWT (`config.security`). |
| `modules/auth` | 9 | Xác thực JWT cho REST API, xác thực Form Login web, đăng ký người dùng mới và quản lý mã phiên. |
| `modules/cart` | 12 | Quản lý giỏ hàng trực tuyến của khách hàng: thêm biến thể, đổi số lượng, xóa dòng sản phẩm, kiểm tra tồn kho kho tổng. |
| `modules/catalog` | 52 | Quản lý danh mục 2 cấp, thương hiệu, sản phẩm giày dép cha, biến thể màu sắc/kích cỡ EU, hình ảnh (tải tệp lên, ảnh theo màu sắc qua FileStorageService và ProductImageResolver); hiển thị danh sách, tìm kiếm, lọc và chi tiết sản phẩm. |
| `modules/cms` | 0 | Module khung quản trị nội dung / bài viết (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/inventory` | 19 | Quản lý tồn kho theo từng chi nhánh có khóa lạc quan (`@Version`), sổ cái giao dịch kho (Audit log), xác định nguồn cung ứng kho tổng (`fulfillment`). |
| `modules/loyalty` | 0 | Module khung tích điểm khách hàng thân thiết (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/notification`| 0 | Module khung gửi thông báo khách hàng (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/order` | 42 | Đặt hàng trực tuyến, xem lịch sử đơn của khách, quản lý đơn hàng đa kênh trên Admin, Factory tạo đơn, Observer trừ/hoàn kho, State máy trạng thái và Strategy phí vận chuyển. |
| `modules/payment` | 11 | Quản lý phương thức và giao dịch thanh toán: Strategy Pattern cho tiền mặt, thẻ, mã QR, COD và ví điện tử giả lập. |
| `modules/pos` | 16 | Phân hệ bán hàng tại quầy POS: tra cứu biến thể/mã SKU/mã vạch, giỏ hàng quầy, thanh toán trọn gói 1 giao dịch và xuất phiếu hóa đơn in nhiệt 80mm. |
| `modules/promotion` | 10 | Quản lý voucher khuyến mại: kiểm tra điều kiện áp dụng, Strategy Pattern cho giảm giá theo phần trăm và giảm giá cố định. |
| `modules/report` | 21 | Báo cáo thống kê và Dashboard quản trị dạng bảng số: doanh thu theo ngày, theo chi nhánh, theo kênh bán, top sản phẩm bán chạy và cảnh báo tồn kho thấp. |
| `modules/returnwarranty` | 0 | Module khung đổi trả & bảo hành sản phẩm (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/review` | 0 | Module khung đánh giá & bình luận sản phẩm (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/shipping` | 0 | Module khung liên kết đơn vị vận chuyển bên thứ ba (hiện chỉ có `.gitkeep`, chưa triển khai). |
| `modules/store` | 24 | Quản lý danh sách chi nhánh (cửa hàng/kho bãi), hồ sơ nhân sự theo chi nhánh, kiểm soát phạm vi dữ liệu chi nhánh (`StoreScopeService`) và điều hướng trang POS. |
| `modules/user` | 18 | Quản lý thông tin tài khoản người dùng, 4 vai trò hệ thống (ADMIN, STORE_MANAGER, CASHIER, CUSTOMER) và sổ địa chỉ giao hàng. |
| **TỔNG CỘNG** | **257** | **Toàn bộ hệ thống có 257 tệp mã nguồn Java.** |

---

## 2. BẢNG ĐƯỜNG DẪN (URLs & ENDPOINTS)

Bảo mật được cấu hình tập trung trong `SecurityConfig` qua 2 `SecurityFilterChain`:
- **Chain 1 (`/api/**`):** Cơ chế Stateless + JWT. Chỉ có `/api/v1/auth/**` là công khai (`permitAll`), các endpoint còn lại bắt buộc có JWT Bearer Token (`authenticated`).
- **Chain 2 (Web MVC):** Cơ chế Session + Form Login, CSRF bật. Phân quyền truy cập dựa trên vai trò Spring Security (`ROLE_*`) và chú thích `@PreAuthorize`.

### 2.1. Khu Shop (Khách hàng)
Áp dụng cho khách hàng duyệt sản phẩm và mua sắm trực tuyến.

| Phương thức | Đường dẫn URL | Lớp Controller | Vai trò được phép (SecurityConfig / PreAuthorize) | Mô tả nghiệp vụ |
|---|---|---|---|---|
| `GET` | `/` | `ShopHomePageController` | Công khai (`permitAll`) | Trang chủ Shop: hiển thị banner, danh mục tiêu biểu và 8 sản phẩm mới nhất. |
| `GET` | `/shop/products` | `ShopProductPageController` | Công khai (`permitAll`) | Danh sách sản phẩm dạng lưới kèm tìm kiếm từ khóa, bộ lọc đa điều kiện và phân trang 12 sản phẩm/trang. |
| `GET` | `/shop/products/{slug}` | `ShopProductPageController` | Công khai (`permitAll`) | Xem chi tiết sản phẩm theo slug: chọn màu/size, hiển thị giá và tồn khả dụng tại kho tổng, bộ sưu tập ảnh chuyển theo màu. |
| `GET` | `/cart` | `ShopCartPageController` | `CUSTOMER` | Xem bảng giỏ hàng cá nhân: danh sách dòng sản phẩm, số lượng, đơn giá, thành tiền và tạm tính. |
| `POST` | `/cart/add` | `ShopCartPageController` | `CUSTOMER` | Thêm một biến thể sản phẩm vào giỏ hàng hoặc cộng dồn số lượng nếu đã có. |
| `POST` | `/cart/update` | `ShopCartPageController` | `CUSTOMER` | Cập nhật số lượng của một dòng sản phẩm trong giỏ hàng (kiểm tra quyền sở hữu). |
| `POST` | `/cart/remove` | `ShopCartPageController` | `CUSTOMER` | Xóa một dòng sản phẩm khỏi giỏ hàng (kiểm tra quyền sở hữu). |
| `GET` | `/checkout` | `ShopOrderPageController` | `CUSTOMER` | Hiển thị màn hình đặt hàng, tự động điền địa chỉ mặc định và phương thức thanh toán. |
| `POST` | `/checkout/preview` | `ShopOrderPageController` | `CUSTOMER` | Tính toán lại chi phí khi khách hàng áp dụng hoặc gỡ bỏ mã giảm giá voucher. |
| `POST` | `/checkout/place` | `ShopOrderPageController` | `CUSTOMER` | Thực hiện tạo đơn hàng trực tuyến, áp dụng trừ kho tự động và xóa giỏ hàng. |
| `GET` | `/checkout/success/{code}` | `ShopOrderPageController` | `CUSTOMER` | Màn hình thông báo đặt hàng thành công kèm tóm tắt mã đơn và thông tin giao hàng. |
| `GET` | `/account/orders` | `ShopOrderPageController` | `CUSTOMER` | Danh sách lịch sử các đơn hàng trực tuyến do khách hàng hiện tại đã đặt (phân trang). |
| `GET` | `/account/orders/{code}` | `ShopOrderPageController` | `CUSTOMER` | Xem chi tiết một đơn hàng của khách theo mã đơn (chặn truy cập nếu không phải chủ đơn). |
| `POST` | `/account/orders/{code}/cancel` | `ShopOrderPageController` | `CUSTOMER` | Khách hàng tự hủy đơn hàng trực tuyến của mình khi đơn đang ở trạng thái `PENDING`. |

### 2.2. Khu Quản trị (ADMIN, STORE_MANAGER)
Phục vụ quản lý chuỗi, danh mục, kho bãi, nhân sự và đơn hàng toàn hệ thống.

| Phương thức | Đường dẫn URL | Lớp Controller | Vai trò được phép (SecurityConfig / PreAuthorize) | Mô tả nghiệp vụ |
|---|---|---|---|---|
| `GET` | `/admin` | `AdminDashboardPageController` | `ADMIN`, `STORE_MANAGER` | Màn hình tổng quan Dashboard báo cáo doanh thu, chỉ số kinh doanh và bảng số liệu. |
| `GET` | `/admin/` | `AdminDashboardPageController` | `ADMIN`, `STORE_MANAGER` | Alias đường dẫn trang Dashboard quản trị. |
| `GET` | `/admin/dashboard` | `AdminDashboardPageController` | `ADMIN`, `STORE_MANAGER` | Alias đường dẫn trang Dashboard quản trị. |
| `GET` | `/admin/brands` | `AdminBrandPageController` | `ADMIN`, `STORE_MANAGER` | Danh sách thương hiệu sản phẩm có tìm kiếm, phân trang và form thêm/sửa. |
| `POST` | `/admin/brands/save` | `AdminBrandPageController` | `ADMIN`, `STORE_MANAGER` | Lưu thông tin thương hiệu (tạo mới hoặc cập nhật thông tin). |
| `POST` | `/admin/brands/{id}/toggle-status` | `AdminBrandPageController` | `ADMIN`, `STORE_MANAGER` | Bật hoặc tắt trạng thái kích hoạt hoạt động của thương hiệu. |
| `POST` | `/admin/brands/{id}/delete` | `AdminBrandPageController` | `ADMIN`, `STORE_MANAGER` | Xóa mềm thương hiệu bằng cách chuyển trạng thái kích hoạt sang ẩn. |
| `GET` | `/admin/categories` | `AdminCategoryPageController` | `ADMIN`, `STORE_MANAGER` | Danh sách danh mục sản phẩm dạng cây cha-con 2 cấp và form thêm/sửa. |
| `POST` | `/admin/categories/save` | `AdminCategoryPageController` | `ADMIN`, `STORE_MANAGER` | Lưu thông tin danh mục sản phẩm với slug tự sinh duy nhất. |
| `POST` | `/admin/categories/{id}/toggle-status` | `AdminCategoryPageController` | `ADMIN`, `STORE_MANAGER` | Bật hoặc tắt trạng thái hiển thị của danh mục sản phẩm. |
| `POST` | `/admin/categories/{id}/delete` | `AdminCategoryPageController` | `ADMIN`, `STORE_MANAGER` | Xóa mềm danh mục (chặn nếu danh mục còn danh mục con hoặc sản phẩm gắn kèm). |
| `GET` | `/admin/products` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Danh sách sản phẩm cha có tìm kiếm, bộ lọc danh mục/thương hiệu/trạng thái và phân trang. |
| `GET` | `/admin/products/new` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Form tạo mới sản phẩm cha (tên, danh mục, thương hiệu, mô tả, ảnh đại diện). |
| `GET` | `/admin/products/{id}/edit` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Form chỉnh sửa thông tin cơ bản của sản phẩm cha. |
| `POST` | `/admin/products/save` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Lưu thông tin sản phẩm cha (tạo mới hoặc cập nhật). |
| `POST` | `/admin/products/{id}/toggle-status` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Bật/tắt trạng thái hiển thị của sản phẩm cha (ACTIVE <-> HIDDEN). |
| `POST` | `/admin/products/{id}/delete` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Xóa mềm sản phẩm cha bằng cách chuyển trạng thái sang HIDDEN. |
| `GET` | `/admin/products/{id}` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Trang xem chi tiết sản phẩm cha: quản lý danh sách biến thể (màu/size) và bộ sưu tập ảnh (gắn theo biến thể, theo màu sắc hoặc chung). |
| `POST` | `/admin/products/{id}/variants/save` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Tạo mới hoặc cập nhật thông tin biến thể sản phẩm (SKU, giá bán, giá vốn, màu, size). |
| `POST` | `/admin/products/{id}/variants/{variantId}/toggle-status` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Bật hoặc tắt trạng thái kinh doanh của biến thể (ACTIVE <-> INACTIVE). |
| `POST` | `/admin/products/{id}/variants/{variantId}/delete` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Ngưng kinh doanh biến thể sản phẩm (chuyển trạng thái sang INACTIVE). |
| `POST` | `/admin/products/{id}/images/save` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Thêm ảnh sản phẩm: hỗ trợ tải tệp ảnh từ máy tính (tối đa 2MB, png/jpg/jpeg/webp) hoặc nhập URL; gắn ảnh cho biến thể cụ thể, cho màu sắc (`color:`) hoặc chung cho sản phẩm. |
| `POST` | `/admin/products/{id}/images/{imageId}/delete` | `AdminProductPageController` | `ADMIN`, `STORE_MANAGER` | Xóa bản ghi hình ảnh của sản phẩm và tự động xóa tệp đĩa vật lý tương ứng nếu lưu trong uploads. |
| `GET` | `/admin/inventory` | `AdminInventoryPageController` | `ADMIN`, `STORE_MANAGER` | Bảng quản lý tồn kho theo chi nhánh (ADMIN chọn mọi kho, STORE_MANAGER cố định kho mình). |
| `POST` | `/admin/inventory/import` | `AdminInventoryPageController` | `ADMIN`, `STORE_MANAGER` | Nhập thêm số lượng hàng hóa vào chi nhánh (giao dịch IMPORT) và ghi sổ cái kho. |
| `POST` | `/admin/inventory/adjust` | `AdminInventoryPageController` | `ADMIN`, `STORE_MANAGER` | Điều chỉnh tồn kho thực tế sau khi kiểm kê (giao dịch ADJUST) và ghi sổ cái kho. |
| `POST` | `/admin/inventory/reorder-level` | `AdminInventoryPageController` | `ADMIN`, `STORE_MANAGER` | Thiết lập ngưỡng cảnh báo tồn kho tối thiểu (`reorder_level`) cho từng biến thể. |
| `GET` | `/admin/inventory/ledger` | `AdminInventoryPageController` | `ADMIN`, `STORE_MANAGER` | Tra cứu sổ cái biến động kho (Audit Log) theo chi nhánh, loại giao dịch và thời gian. |
| `GET` | `/admin/orders` | `AdminOrderPageController` | `ADMIN`, `STORE_MANAGER` | Danh sách đơn hàng toàn hệ thống đa kênh (lọc theo chi nhánh, kênh, trạng thái). |
| `GET` | `/admin/orders/{id}` | `AdminOrderPageController` | `ADMIN`, `STORE_MANAGER` | Xem chi tiết đơn hàng: snapshot sản phẩm, thông tin khách/kho, tiền thanh toán, lịch sử trạng thái. |
| `POST` | `/admin/orders/{id}/status` | `AdminOrderPageController` | `ADMIN`, `STORE_MANAGER` *(chỉ ADMIN thao tác đơn Online)* | Chuyển trạng thái đơn hàng sang nấc tiếp theo hợp lệ theo State Pattern. |
| `POST` | `/admin/orders/{id}/cancel` | `AdminOrderPageController` | `ADMIN`, `STORE_MANAGER` *(chỉ ADMIN thao tác đơn Online)* | Hủy đơn hàng và tự động kích hoạt hoàn kho qua Observer Pattern. |
| `GET` | `/admin/stores` | `AdminStorePageController` | `@PreAuthorize("hasRole('ADMIN')")` | Danh sách chi nhánh cửa hàng và kho bãi có tìm kiếm, phân trang và trạng thái. |
| `POST` | `/admin/stores/save` | `AdminStorePageController` | `@PreAuthorize("hasRole('ADMIN')")` | Tạo mới hoặc cập nhật thông tin chi nhánh (kiểm tra mã duy nhất và tọa độ GPS). |
| `POST` | `/admin/stores/{id}/toggle-status` | `AdminStorePageController` | `@PreAuthorize("hasRole('ADMIN')")` | Ngưng hoạt động hoặc kích hoạt lại chi nhánh cửa hàng. |
| `GET` | `/admin/stores/{storeId}/employees` | `AdminEmployeePageController` | `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")` | Danh sách hồ sơ nhân viên đang làm việc tại chi nhánh. |
| `POST` | `/admin/stores/{storeId}/employees/add` | `AdminEmployeePageController` | `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")` | Thêm nhân viên vào chi nhánh từ email người dùng có sẵn và gán vai trò hệ thống. |
| `POST` | `/admin/stores/{storeId}/employees/{id}/update` | `AdminEmployeePageController` | `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")` | Cập nhật chức vụ hoặc điều chuyển chi nhánh cho nhân viên, tự đồng bộ Role. |
| `POST` | `/admin/stores/{storeId}/employees/{id}/toggle-status` | `AdminEmployeePageController` | `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")` | Khóa hoặc mở khóa trạng thái làm việc của nhân viên. |

### 2.3. Khu POS (CASHIER, STORE_MANAGER, ADMIN)
Phục vụ thu ngân và nhân viên bán lẻ trực tiếp tại quầy cửa hàng.

| Phương thức | Đường dẫn URL | Lớp Controller | Vai trò được phép (SecurityConfig / PreAuthorize) | Mô tả nghiệp vụ |
|---|---|---|---|---|
| `GET` | `/pos` | `PosPageController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | Giao diện bán hàng tại quầy POS: tìm sản phẩm, giỏ hàng quầy, thanh toán tiền mặt/thẻ/QR. |
| `GET` | `/pos/receipts/{orderCode}` | `PosPageController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | Xem và in phiếu hóa đơn bán lẻ khổ 80mm (kiểm tra nhân viên chỉ xem hóa đơn kho mình). |
| `GET` | `/pos/orders` | `PosPageController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | Danh sách các đơn bán hàng POS vừa hoàn tất gần đây tại chi nhánh đang đăng nhập. |
| `GET` | `/pos/api/products` | `PosApiController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | AJAX API: Tra cứu biến thể bán tại quầy theo mã SKU, barcode hoặc tên, kèm kiểm tra tồn kho quầy. |
| `POST` | `/pos/api/preview` | `PosApiController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | AJAX API: Xem trước hóa đơn quầy, áp dụng mã voucher và tính số tiền giảm giá. |
| `POST` | `/pos/api/checkout` | `PosApiController` | `CASHIER`, `STORE_MANAGER`, `ADMIN` | AJAX API: Thanh toán trọn gói trong 1 transaction: tạo đơn COMPLETED, thanh toán, trừ kho, tính tiền thừa. |

### 2.4. Khu API Công khai & REST API (Stateless JWT)
Phân hệ REST API stateless dành cho client di động hoặc tích hợp ngoài, xác thực qua JWT Bearer.

| Phương thức | Đường dẫn URL | Lớp Controller | Vai trò được phép (SecurityConfig / PreAuthorize) | Mô tả nghiệp vụ |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | `AuthController` | Công khai (`permitAll`) | Đăng ký tài khoản khách hàng mới, lưu mật khẩu băm BCrypt và cấp vai trò `ROLE_CUSTOMER`. |
| `POST` | `/api/v1/auth/login` | `AuthController` | Công khai (`permitAll`) | Đăng nhập bằng email/password, trả về Access Token (15 phút) và Refresh Token (7 ngày). |
| `GET` | `/api/v1/addresses` | `AddressController` | Đã xác thực (`authenticated` - JWT Bearer) | Lấy danh sách sổ địa chỉ nhận hàng của người dùng hiện tại. |
| `POST` | `/api/v1/addresses` | `AddressController` | Đã xác thực (`authenticated` - JWT Bearer) | Thêm mới một địa chỉ giao hàng vào sổ địa chỉ cá nhân. |
| `PUT` | `/api/v1/addresses/{id}` | `AddressController` | Đã xác thực (`authenticated` - JWT Bearer) | Cập nhật thông tin địa chỉ giao hàng (kiểm tra quyền sở hữu). |
| `DELETE` | `/api/v1/addresses/{id}` | `AddressController` | Đã xác thực (`authenticated` - JWT Bearer) | Xóa một địa chỉ khỏi sổ địa chỉ cá nhân (kiểm tra quyền sở hữu). |
| `GET` | `/api/v1/users/me` | `UserController` | Đã xác thực (`authenticated` - JWT Bearer) | Lấy thông tin hồ sơ người dùng cá nhân của tài khoản đang đăng nhập. |
| `GET` | `/v3/api-docs` | `SwaggerConfig` (Springdoc) | Công khai (`permitAll`) | Tài liệu định dạng JSON OpenAPI v3 phục vụ kiểm thử và sinh mã client. |
| `GET` | `/swagger-ui.html` | `SwaggerConfig` (Springdoc) | Công khai (`permitAll`) | Giao diện Swagger UI tương tác trực tiếp với các endpoint `/api/**`. |
| `GET` | `/swagger-ui/**` | `SwaggerConfig` (Springdoc) | Công khai (`permitAll`) | Các tài nguyên giao diện tĩnh hỗ trợ trang Swagger UI. |

### 2.5. Trang Đăng nhập, Trang Lỗi & Phục vụ tệp tải lên (Web Public)

| Phương thức | Đường dẫn URL | Lớp Controller / Cấu hình | Vai trò được phép (SecurityConfig / PreAuthorize) | Mô tả nghiệp vụ |
|---|---|---|---|---|
| `GET` | `/login` | `AuthPageController` | Công khai (`permitAll`) | Giao diện form đăng nhập web riêng; tự động chuyển hướng theo vai trò nếu đã đăng nhập. |
| `GET` | `/uploads/**` | `WebMvcConfig` / ResourceHandler | Công khai (`permitAll` - chỉ GET) | Phục vụ trực tiếp tệp hình ảnh sản phẩm đã tải lên từ thư mục đĩa vật lý `uploads/`. |
| `GET` | `/error/403` | `ErrorPageController` | Công khai (`permitAll`) | Trang thông báo lỗi 403 Forbidden thân thiện khi truy cập vượt quyền hạn. |
| `GET` | `/error/404` | `ErrorPageController` | Công khai (`permitAll`) | Trang thông báo lỗi 404 Not Found thân thiện khi không tìm thấy trang hoặc tài nguyên. |
| `GET` | `/error/500` | `ErrorPageController` | Công khai (`permitAll`) | Trang thông báo lỗi 500 Internal Server Error thân thiện khi máy chủ gặp sự cố. |
| `ALL` | `/error` | `ErrorPageController` | Công khai (`permitAll`) | Bộ điều hướng mã lỗi toàn cục tự động chuyển đến template 403/404/500 tương ứng. |

---

## 3. BẢN ĐỒ DESIGN PATTERN

Dự án cài đặt đầy đủ 4 nhóm Design Pattern bắt buộc theo đúng chuẩn kiến trúc hướng đối tượng (OOP):

### 3.1. Strategy Pattern
Tách rời các thuật toán xử lý nghiệp vụ có thể thay đổi độc lập mà không ảnh hưởng đến mã nguồn gọi đến.

#### A. Chiến lược thanh toán (Payment Strategy)
- **Interface:** `PaymentStrategy`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/payment/strategy/PaymentStrategy.java`  
  - *Phương thức chính:* `getPaymentMethod()`, `pay(PaymentContext context)`
- **Các lớp cài đặt cụ thể (Concrete Strategies):**
  1. `CashPaymentStrategy` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/CashPaymentStrategy.java`): Xử lý thanh toán tiền mặt tại quầy POS, kiểm tra tiền khách đưa và tính tiền thừa trả khách.
  2. `CardPaymentStrategy` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/CardPaymentStrategy.java`): Giả lập quẹt thẻ ngân hàng POS, sinh mã chuẩn chi `CARD-...` và chuyển trạng thái `PAID`.
  3. `QrPaymentStrategy` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/QrPaymentStrategy.java`): Giả lập quét mã QR động tại quầy POS, sinh mã giao dịch `QR-...` và chuyển trạng thái `PAID`.
  4. `CodPaymentStrategy` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/CodPaymentStrategy.java`): Xử lý thanh toán khi nhận hàng (COD) cho đơn Shop Online, lưu trạng thái thanh toán `PENDING`.
  5. `EWalletMockPaymentStrategy` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/EWalletMockPaymentStrategy.java`): Giả lập ví điện tử trực tuyến, sinh mã tham chiếu ví `EW-MOCK-...` và ghi nhận `PAID`.
- **Registry quản lý:** `PaymentStrategyRegistry` (`src/main/java/com/dahdt/ecommerce/modules/payment/strategy/PaymentStrategyRegistry.java`).
- **Nơi sử dụng:**
  - `OrderServiceImpl.java` (gọi khi khách thanh toán đặt đơn hàng trực tuyến).
  - `PosSaleServiceImpl.java` (gọi khi thu ngân xử lý thanh toán hóa đơn quầy POS).

#### B. Chiến lược giảm giá voucher (Discount Strategy)
- **Interface:** `DiscountStrategy`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/promotion/strategy/DiscountStrategy.java`  
  - *Phương thức chính:* `getDiscountType()`, `calculateDiscount(BigDecimal subtotal, BigDecimal discountValue, BigDecimal maxDiscountValue)`
- **Các lớp cài đặt cụ thể (Concrete Strategies):**
  1. `PercentDiscountStrategy` (`src/main/java/com/dahdt/ecommerce/modules/promotion/strategy/PercentDiscountStrategy.java`): Giảm theo tỷ lệ phần trăm (%), chặn không vượt quá mức giảm trần `maxDiscountValue` nếu có cấu hình.
  2. `FixedDiscountStrategy` (`src/main/java/com/dahdt/ecommerce/modules/promotion/strategy/FixedDiscountStrategy.java`): Giảm một số tiền cố định (VNĐ), chặn không giảm vượt quá tổng giá trị đơn hàng.
- **Registry quản lý:** `DiscountStrategyRegistry` (`src/main/java/com/dahdt/ecommerce/modules/promotion/strategy/DiscountStrategyRegistry.java`).
- **Nơi sử dụng:**
  - `Voucher.java` (Entity ủy thác tính số tiền giảm giá qua Strategy).
  - `OrderServiceImpl.java` & `PosSaleServiceImpl.java` (kiểm tra tính hợp lệ và áp dụng voucher).

#### C. Chiến lược tính phí vận chuyển (Shipping Fee Strategy)
- **Interface:** `ShippingFeeCalculator`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/order/shipping/ShippingFeeCalculator.java`  
  - *Phương thức chính:* `calculateShippingFee(BigDecimal subtotalAfterDiscount)`
- **Lớp cài đặt cụ thể:**
  1. `FlatRateShippingFeeCalculator` (`src/main/java/com/dahdt/ecommerce/modules/order/shipping/FlatRateShippingFeeCalculator.java`): Tính phí ship đồng giá tiêu chuẩn (mặc định 30.000 đ) và miễn phí vận chuyển cho đơn hàng đạt ngưỡng cấu hình (mặc định từ 1.000.000 đ).
- **Nơi sử dụng:**
  - `OrderServiceImpl.java` (gọi trong hàm xem trước `previewOrder` và tạo đơn `placeOrder`).

#### D. Chiến lược chọn nguồn xuất hàng (Fulfillment Source Strategy)
- **Interface:** `FulfillmentSourceResolver`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/inventory/fulfillment/FulfillmentSourceResolver.java`  
  - *Phương thức chính:* `resolveDefaultSource()`
- **Lớp cài đặt cụ thể:**
  1. `MainWarehouseFulfillmentResolver` (`src/main/java/com/dahdt/ecommerce/modules/inventory/fulfillment/MainWarehouseFulfillmentResolver.java`): Mặc định chọn kho tổng (chi nhánh có `type = WAREHOUSE` và đang `ACTIVE` với ID nhỏ nhất) làm nguồn cung ứng cho đơn hàng online.
- **Nơi sử dụng:**
  - `OnlineAvailabilityServiceImpl.java` (tính tồn khả dụng cho Shop).
  - `OrderServiceImpl.java` (gán chi nhánh xuất kho cho đơn hàng online).

---

### 3.2. Factory Pattern
Đóng gói logic tạo lập các đối tượng phức tạp nhằm đảm bảo dữ liệu toàn vẹn và nhất quán.

#### A. Factory khởi tạo đơn hàng (Order Creation)
- **Lớp cài đặt:** `OrderFactory`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/order/factory/OrderFactory.java`
- **Phương thức khởi tạo:**
  - `createOnlineOrder(User customer, Store sourceStore, PlaceOrderCommand cmd, ...)`: Khởi tạo đơn Online kênh `ONLINE`, trạng thái đầu `PENDING`, lưu snapshot người nhận/địa chỉ giao hàng và danh sách sản phẩm snapshot.
  - `createPosOrder(Store store, User cashier, PosCheckoutRequestDTO req, ...)`: Khởi tạo đơn tại quầy kênh `POS`, trạng thái hoàn tất `COMPLETED`, không lưu địa chỉ giao hàng, gắn thu ngân và cửa hàng cụ thể.
- **Nơi sử dụng:**
  - `OrderServiceImpl.java` (tạo đơn hàng online).
  - `PosSaleServiceImpl.java` (tạo đơn bán lẻ tại quầy POS).

#### B. Bộ sinh mã đơn hàng (Order Code Factory)
- **Lớp cài đặt:** `OrderCodeGenerator`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/order/factory/OrderCodeGenerator.java`
- **Quy tắc sinh mã:**
  - Đơn Online: Tiền tố `DH` + `yyyyMMdd` + 6 ký tự ngẫu nhiên (Ví dụ: `DH20261111ABC123`).
  - Đơn POS: Tiền tố `POS-` + mã cửa hàng + `-` + `yyyyMMdd` + 4 ký tự ngẫu nhiên (Ví dụ: `POS-STORE-Q1-20261111AB12`).
- **Nơi sử dụng:**
  - `OrderFactory.java` (sinh mã đơn duy nhất trước khi lưu bản ghi đơn hàng).

---

### 3.3. Observer Pattern (Spring Events)
Phát các sự kiện nghiệp vụ bất đồng bộ hoặc tách rời trách nhiệm xử lý giữa phân hệ Đơn hàng và phân hệ Kho bãi, đảm bảo tính đóng gói và an toàn giao dịch.

#### A. Các sự kiện (Events)
1. `OrderPlacedEvent` (`src/main/java/com/dahdt/ecommerce/modules/order/event/OrderPlacedEvent.java`): Bắn ra ngay sau khi đơn hàng được tạo thành công (cả Online lẫn POS).
2. `OrderCancelledEvent` (`src/main/java/com/dahdt/ecommerce/modules/order/event/OrderCancelledEvent.java`): Bắn ra khi đơn hàng bị hủy bỏ (bởi Admin hoặc khách hàng).

#### B. Các bộ lắng nghe (Listeners / Observers)
1. `OrderPlacedListener` (`src/main/java/com/dahdt/ecommerce/modules/order/event/OrderPlacedListener.java`):  
   - *Hành vi:* Lắng nghe `OrderPlacedEvent`, duyệt qua các mặt hàng trong đơn và gọi `InventoryService.deductStock(...)` để trừ số lượng tồn kho của chi nhánh xuất hàng, đồng thời ghi một dòng giao dịch loại `SALE` vào sổ cái kho. Chạy đồng bộ trong cùng giao dịch tạo đơn.
2. `OrderCancelledListener` (`src/main/java/com/dahdt/ecommerce/modules/order/event/OrderCancelledListener.java`):  
   - *Hành vi:* Lắng nghe `OrderCancelledEvent`, duyệt qua các mặt hàng trong đơn và gọi `InventoryService.revertStock(...)` để hoàn lại số lượng tồn kho cho chi nhánh, đồng thời ghi một dòng giao dịch loại `CANCEL` vào sổ cái kho. Chạy đồng bộ trong cùng giao dịch hủy đơn.
- **Nơi phát sự kiện:**
  - `OrderServiceImpl.java` (bắn `OrderPlacedEvent` khi khách đặt đơn).
  - `PosSaleServiceImpl.java` (bắn `OrderPlacedEvent` khi thu ngân thanh toán đơn POS).
  - `OrderStatusServiceImpl.java` (bắn `OrderCancelledEvent` khi đơn hàng chuyển sang `CANCELLED`).

---

### 3.4. State Pattern (Vòng đời đơn hàng)
Mỗi trạng thái của đơn hàng là một đối tượng độc lập đóng gói quy tắc chuyển đổi trạng thái hợp lệ, ngăn chặn việc nhảy cóc trạng thái hoặc hủy đơn sai quy định.

```
PENDING ──> CONFIRMED ──> PACKING ──> SHIPPING ──> COMPLETED
   │            │           │
   └─── CANCELLED <─────────┘
```

- **Interface trạng thái:** `OrderState`  
  - *Đường dẫn:* `src/main/java/com/dahdt/ecommerce/modules/order/state/OrderState.java`  
  - *Phương thức chính:* `getOrderStatus()`, `getValidNextStatuses()`, `canTransitionTo(OrderStatus)`, `canCancel()`
- **Các lớp trạng thái cụ thể (Concrete States):**
  1. `PendingState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/PendingState.java`): Chờ xác nhận. Chuyển được sang `CONFIRMED` hoặc `CANCELLED`. Cho phép hủy.
  2. `ConfirmedState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/ConfirmedState.java`): Đã xác nhận. Chuyển được sang `PACKING` hoặc `CANCELLED`. Cho phép hủy.
  3. `PackingState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/PackingState.java`): Đang đóng gói. Chuyển được sang `SHIPPING` hoặc `CANCELLED`. Cho phép hủy.
  4. `ShippingState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/ShippingState.java`): Đang giao hàng. Chỉ chuyển được sang `COMPLETED`. **Tuyệt đối không cho phép hủy.**
  5. `CompletedState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/CompletedState.java`): Đã hoàn tất. Trạng thái kết thúc, không chuyển tiếp, không cho phép hủy.
  6. `CancelledState` (`src/main/java/com/dahdt/ecommerce/modules/order/state/CancelledState.java`): Đã hủy. Trạng thái kết thúc, không chuyển tiếp, không cho phép hủy lại (chống hủy 2 lần).
- **Bộ chuyển đổi trạng thái (State Factory):** `OrderStateFactory` (`src/main/java/com/dahdt/ecommerce/modules/order/state/OrderStateFactory.java`).
- **Nơi sử dụng:**
  - `OrderStatusServiceImpl.java` (kiểm tra hợp lệ trước khi đổi trạng thái hoặc hủy đơn).
  - `AdminOrderPageController.java` (lấy danh sách trạng thái kế tiếp và kiểm tra quyền hủy để render nút bấm trên giao diện chi tiết đơn).

---

## 4. DANH SÁCH THỰC THỂ (ENTITY) & BẢNG CSDL

Hệ thống có **23 lớp `@Entity`** tương ứng với các bảng nghiệp vụ chính. Tất cả kế thừa từ `BaseEntity` (tự động có `createdAt`, `updatedAt`).

### 4.1. Chi tiết 23 Thực thể JPA

| Thực thể (Entity) | Bảng CSDL | Các trường chính | Quan hệ thực thể | Đường dẫn tệp nguồn |
|---|---|---|---|---|
| `User` | `users` | id, email, phone, password, status, emailVerifiedAt, phoneVerifiedAt, deletedAt | `@OneToOne` profile, `@ManyToMany` roles (bảng `user_roles`) | `modules/user/entity/User.java` |
| `Role` | `roles` | id, code, name, description | `@ManyToMany` users | `modules/user/entity/Role.java` |
| `UserProfile` | `user_profiles` | id, fullName, avatarUrl, gender, dateOfBirth | `@OneToOne` user | `modules/user/entity/UserProfile.java` |
| `Address` | `addresses` | id, recipientName, phone, province, district, ward, detailAddress, latitude, longitude, isDefault | `@ManyToOne` user | `modules/user/entity/Address.java` |
| `AuthToken` | `auth_tokens` | id, tokenHash, deviceInfo, expiresAt, revokedAt | `@ManyToOne` user | `modules/auth/entity/AuthToken.java` |
| `OtpVerification` | `otp_verifications` | id, codeHash, purpose, expiresAt | `@ManyToOne` user | `modules/auth/entity/OtpVerification.java` |
| `Brand` | `brands` | id, name, slug, description, isActive | 1-N products | `modules/catalog/entity/Brand.java` |
| `Category` | `categories` | id, name, slug, sortOrder, isActive | `@ManyToOne` parent (Category) | `modules/catalog/entity/Category.java` |
| `Product` | `products` | id, name, slug, description, thumbnailUrl, status | `@ManyToOne` category, `@ManyToOne` brand, 1-N variants | `modules/catalog/entity/Product.java` |
| `ProductVariant` | `product_variants` | id, sku, barcode, color, size, price, costPrice, weightGram, status | `@ManyToOne` product | `modules/catalog/entity/ProductVariant.java` |
| `ProductImage` | `product_images` | id, color, url, sortOrder, isThumbnail | `@ManyToOne` product, `@ManyToOne` variant | `modules/catalog/entity/ProductImage.java` |
| `Store` | `stores` | id, code, name, type (STORE/WAREHOUSE), province, district, ward, detailAddress, latitude, longitude, phone, status | 1-N inventories, 1-N employees | `modules/store/entity/Store.java` |
| `Employee` | `employees` | id, position (MANAGER/CASHIER/STAFF), status | `@OneToOne` user, `@ManyToOne` store | `modules/store/entity/Employee.java` |
| `Inventory` | `inventories` | id, quantityOnHand, reorderLevel, version (`@Version`) | `@ManyToOne` variant, `@ManyToOne` store | `modules/inventory/entity/Inventory.java` |
| `InventoryTransaction` | `inventory_transactions` | id, type, quantityChange, quantityAfter, referenceType, referenceId, note | `@ManyToOne` variant, `@ManyToOne` store, `@ManyToOne` createdBy (User) | `modules/inventory/entity/InventoryTransaction.java` |
| `Cart` | `carts` | id, status (ACTIVE/CHECKED_OUT/ABANDONED) | `@ManyToOne` user, 1-N items | `modules/cart/entity/Cart.java` |
| `CartItem` | `cart_items` | id, quantity | `@ManyToOne` cart, `@ManyToOne` variant | `modules/cart/entity/CartItem.java` |
| `Order` | `orders` | id, orderCode, channel (ONLINE/POS), status, subtotal, discountTotal, shippingFee, totalAmount, voucherCode, shipName, shipPhone, shipAddress, shipLatitude, shipLongitude, note, placedAt, completedAt | `@ManyToOne` store, `@ManyToOne` customer (User), `@ManyToOne` cashier (User), 1-N items, 1-N history | `modules/order/entity/Order.java` |
| `OrderItem` | `order_items` | id, productName, sku, color, size, unitPrice, quantity, lineTotal | `@ManyToOne` order, `@ManyToOne` variant | `modules/order/entity/OrderItem.java` |
| `OrderStatusHistory` | `order_status_history` | id, fromStatus, toStatus, note, changedAt | `@ManyToOne` order, `@ManyToOne` changedBy (User) | `modules/order/entity/OrderStatusHistory.java` |
| `Payment` | `payments` | id, method, amount, status, transactionRef, cashReceived, changeAmount, paidAt | `@ManyToOne` order | `modules/payment/entity/Payment.java` |
| `Voucher` | `vouchers` | id, code, name, discountType, discountValue, minOrderValue, maxDiscountValue, usageLimit, usedCount, startAt, endAt, status | 1-N voucherUsages | `modules/promotion/entity/Voucher.java` |
| `VoucherUsage` | `voucher_usages` | id, usedAt | `@ManyToOne` voucher, `@ManyToOne` order, `@ManyToOne` user | `modules/promotion/entity/VoucherUsage.java` |

### 4.2. Bảng tổng hợp 28 bảng trong Flyway Migration (V1–V31)

Các tệp migration từ `V1` đến `V31` quản lý tổng cộng **28 bảng CSDL**:

| STT | Tên bảng CSDL | Tệp Migration Flyway | Mức độ quy định | Trạng thái mã nguồn Java |
|:---:|---|---|:---:|---|
| 1 | `users` | `V1__create_users_table.sql` | Must (Giai đoạn 1) | Thực thể `User.java` |
| 2 | `roles` | `V2__create_roles_and_user_roles_table.sql` | Must (Giai đoạn 1) | Thực thể `Role.java` |
| 3 | `user_roles` | `V2__create_roles_and_user_roles_table.sql` | Must (Giai đoạn 1) | Bảng nối Many-to-Many trong `User.java` |
| 4 | `user_profiles` | `V3__create_user_profiles_table.sql` | Must (Giai đoạn 1) | Thực thể `UserProfile.java` |
| 5 | `addresses` | `V4__create_addresses_table.sql` | Must (Giai đoạn 1) | Thực thể `Address.java` |
| 6 | `auth_tokens` | `V5__create_auth_tokens_and_otp_table.sql` | Must (Giai đoạn 1) | Thực thể `AuthToken.java` |
| 7 | `otp_verifications` | `V5__create_auth_tokens_and_otp_table.sql` | Must (Giai đoạn 1) | Thực thể `OtpVerification.java` |
| 8 | `categories` | `V8__create_categories_table.sql` | Must v2 | Thực thể `Category.java` |
| 9 | `brands` | `V9__create_brands_table.sql` | Must v2 | Thực thể `Brand.java` |
| 10 | `products` | `V10__create_products_table.sql` | Must v2 | Thực thể `Product.java` |
| 11 | `product_variants` | `V11__create_product_variants_table.sql` | Must v2 | Thực thể `ProductVariant.java` |
| 12 | `product_images` | `V12__create_product_images_table.sql` *(V31 thêm `color`)* | Must v2 | Thực thể `ProductImage.java` |
| 13 | `stores` | `V13__create_stores_table.sql` | Must v2 | Thực thể `Store.java` |
| 14 | `employees` | `V14__create_employees_table.sql` | Must v2 | Thực thể `Employee.java` |
| 15 | `inventories` | `V15__create_inventories_table.sql` | Must v2 | Thực thể `Inventory.java` |
| 16 | `inventory_transactions` | `V16__create_inventory_transactions_table.sql` | Must v2 | Thực thể `InventoryTransaction.java` |
| 17 | `carts` | `V17__create_carts_table.sql` | Must v2 | Thực thể `Cart.java` |
| 18 | `cart_items` | `V18__create_cart_items_table.sql` | Must v2 | Thực thể `CartItem.java` |
| 19 | `orders` | `V19__create_orders_table.sql` | Must v2 | Thực thể `Order.java` |
| 20 | `order_items` | `V20__create_order_items_table.sql` | Must v2 | Thực thể `OrderItem.java` |
| 21 | `order_status_history` | `V21__create_order_status_history_table.sql` | Must v2 | Thực thể `OrderStatusHistory.java` |
| 22 | `payments` | `V22__create_payments_table.sql` | Must v2 | Thực thể `Payment.java` |
| 23 | `vouchers` | `V23__create_vouchers_table.sql` | Must v2 | Thực thể `Voucher.java` |
| 24 | `voucher_usages` | `V24__create_voucher_usages_table.sql` | Must v2 | Thực thể `VoucherUsage.java` |
| 25 | `pos_shifts` | `V25__create_pos_shifts_table.sql` | Should | Đã có schema bảng; chưa có Entity/Service (thuộc danh sách cắt giảm) |
| 26 | `stock_transfers` | `V26__create_stock_transfers_and_items_table.sql` | Should | Đã có schema bảng; chưa có Entity/Service (thuộc danh sách cắt giảm) |
| 27 | `stock_transfer_items` | `V26__create_stock_transfers_and_items_table.sql` | Should | Đã có schema bảng; chưa có Entity/Service (thuộc danh sách cắt giảm) |
| 28 | `reviews` | `V27__create_reviews_table.sql` | Could | Đã có schema bảng; chưa có Entity/Service (thuộc danh sách cắt giảm) |

> **Tổng kết:** Đạt 100% 24 bảng Must quy định trong Project Brief v2 (gồm 7 bảng giai đoạn 1 và 17 bảng mới), cộng thêm 4 bảng mức Should/Could đã tạo sẵn schema. Tệp `V31__add_color_to_product_images_table.sql` bổ sung cột `color NVARCHAR(50) NULL` vào bảng `product_images`.

---

## 5. DANH SÁCH SERVICE & QUY TẮC NGHIỆP VỤ LÕI

### 5.1. Danh sách Service và các phương thức chính

1. **`AuthService`** (`modules/auth/service/AuthService.java`)
   - `register(RegisterRequestDTO)`: Đăng ký tài khoản khách hàng mới, mã hóa mật khẩu bằng BCrypt và gán vai trò `ROLE_CUSTOMER`.
   - `login(LoginRequestDTO)`: Xác thực thông tin đăng nhập, sinh cặp Access Token (15 phút) và Refresh Token (7 ngày).

2. **`CartService`** (`modules/cart/service/CartService.java` - cài đặt `CartServiceImpl.java`)
   - `getCart(Long userId)`: Lấy toàn bộ thông tin giỏ hàng của người dùng kèm danh sách dòng sản phẩm và tổng tiền tạm tính.
   - `addItem(Long userId, AddToCartRequestDTO)`: Thêm biến thể sản phẩm vào giỏ hoặc cộng dồn số lượng nếu đã có, kiểm tra tồn kho tại kho tổng.
   - `updateItemQuantity(Long userId, Long cartItemId, int quantity)`: Điều chỉnh số lượng dòng sản phẩm trong giỏ và kiểm tra quyền sở hữu giỏ.
   - `removeItem(Long userId, Long cartItemId)`: Xóa một dòng sản phẩm khỏi giỏ hàng sau khi xác thực quyền sở hữu.
   - `clearCart(Long userId)`: Xóa sạch toàn bộ giỏ hàng của người dùng sau khi đặt hàng thành công.
   - `countActiveCartItems(Long userId)`: Đếm tổng số lượng món hàng trong giỏ của người dùng để hiển thị huy hiệu trên navbar.

3. **`BrandService`** (`modules/catalog/service/BrandService.java` - cài đặt `BrandServiceImpl.java`)
   - `getBrands(String keyword, Pageable)`: Lấy danh sách thương hiệu có phân trang và tìm kiếm theo tên hoặc slug.
   - `getAllActiveBrands()`: Lấy danh sách toàn bộ thương hiệu đang kích hoạt để đổ dữ liệu vào combobox chọn thương hiệu.
   - `getBrandById(Long id)`: Lấy thông tin chi tiết một thương hiệu theo ID.
   - `createBrand(BrandRequestDTO)`: Tạo mới thương hiệu với slug tự động sinh duy nhất.
   - `updateBrand(Long id, BrandRequestDTO)`: Cập nhật thông tin thương hiệu.
   - `toggleBrandStatus(Long id)`: Đảo trạng thái kích hoạt (`isActive`) của thương hiệu.
   - `deleteBrand(Long id)`: Xóa mềm thương hiệu bằng cách đặt `isActive = false`.

4. **`CategoryService`** (`modules/catalog/service/CategoryService.java` - cài đặt `CategoryServiceImpl.java`)
   - `getAllCategories()`: Lấy toàn bộ danh mục sản phẩm dạng phẳng để quản lý.
   - `getCategoryTree()`: Lấy cấu trúc cây danh mục 2 cấp (danh mục gốc kèm danh mục con) phục vụ menu và bộ lọc.
   - `getActiveCategories()`: Lấy danh sách các danh mục đang kích hoạt để gán cho sản phẩm hoặc chọn danh mục cha.
   - `getCategoryById(Long id)`: Lấy thông tin chi tiết một danh mục theo ID.
   - `createCategory(CategoryRequestDTO)`: Tạo mới danh mục với slug tự sinh duy nhất.
   - `updateCategory(Long id, CategoryRequestDTO)`: Cập nhật thông tin danh mục (tên, danh mục cha, thứ tự sắp xếp, trạng thái).
   - `toggleCategoryStatus(Long id)`: Bật/tắt trạng thái hiển thị của danh mục sản phẩm.
   - `deleteCategory(Long id)`: Xóa mềm danh mục (`isActive = false`), chặn nếu còn danh mục con hoặc sản phẩm liên kết.

5. **`ProductService`** (`modules/catalog/service/ProductService.java` - cài đặt `ProductServiceImpl.java`)
   - `getProducts(String keyword, Long categoryId, Long brandId, ProductStatus status, Pageable)`: Lấy danh sách sản phẩm quản trị có phân trang và lọc đa điều kiện.
   - `getProductById(Long id)`: Lấy tóm tắt thông tin sản phẩm cha theo ID.
   - `getProductDetail(Long id)`: Lấy chi tiết toàn diện sản phẩm kèm danh sách biến thể và hình ảnh.
   - `createProduct(ProductRequestDTO)`: Tạo mới sản phẩm cha với slug tự sinh duy nhất.
   - `updateProduct(Long id, ProductRequestDTO)`: Cập nhật thông tin cơ bản của sản phẩm cha.
   - `toggleProductStatus(Long id)`: Bật/tắt trạng thái hiển thị của sản phẩm (ACTIVE <-> HIDDEN).
   - `deleteProduct(Long id)`: Xóa mềm sản phẩm bằng cách chuyển trạng thái sang HIDDEN.

6. **`ProductVariantService`** (`modules/catalog/service/ProductVariantService.java` - cài đặt `ProductVariantServiceImpl.java`)
   - `getVariantsByProductId(Long productId)`: Lấy danh sách tất cả biến thể của một sản phẩm cha.
   - `getVariantById(Long variantId)`: Lấy chi tiết một biến thể sản phẩm theo ID.
   - `createVariant(ProductVariantRequestDTO)`: Tạo mới biến thể với mã SKU duy nhất và kiểm tra thuộc tính trùng lặp.
   - `updateVariant(Long variantId, ProductVariantRequestDTO)`: Cập nhật giá bán, giá vốn, màu sắc, kích cỡ và mã vạch của biến thể.
   - `toggleVariantStatus(Long variantId)`: Đảo trạng thái kinh doanh của biến thể (ACTIVE <-> INACTIVE).
   - `deleteVariant(Long variantId)`: Ngưng kinh doanh biến thể bằng cách chuyển trạng thái sang INACTIVE.
   - `addProductImage(ProductImageRequestDTO)`: Thêm ảnh cho sản phẩm hoặc biến thể qua tệp tải lên máy tính hoặc qua URL; gắn ảnh cho biến thể cụ thể, cho màu sắc hoặc chung cho sản phẩm.
   - `deleteProductImage(Long imageId)`: Xóa bản ghi hình ảnh sản phẩm và xóa tệp vật lý tương ứng nếu lưu trong thư mục upload.

7. **`FileStorageService`** (`modules/catalog/service/FileStorageService.java` - cài đặt `FileStorageServiceImpl.java`)
   - `storeProductImage(MultipartFile file)`: Kiểm tra tính hợp lệ và an toàn của tệp ảnh tải lên (đuôi mở rộng .png/.jpg/.jpeg/.webp, MIME type hình ảnh, dung lượng <= 2MB), sinh tên tệp UUID duy nhất và lưu trữ vào thư mục `uploads/products/`.
   - `deleteFile(String fileUrl)`: Xóa tệp ảnh vật lý trên đĩa nếu nằm trong thư mục uploads, bỏ qua an toàn với đường dẫn ảnh tĩnh `/images/...` hoặc URL ngoài.

8. **`ProductImageResolver`** (`modules/catalog/service/ProductImageResolver.java` - `@Component`)
   - `resolveImageUrl(ProductVariant variant)`: Phân giải hình ảnh cho biến thể sản phẩm theo đúng thứ tự ưu tiên 4 bước: (1) ảnh gắn đúng variant_id; (2) ảnh gắn đúng màu sắc (color); (3) ảnh chung sản phẩm hoặc thumbnail cha; (4) ảnh placeholder mặc định `/images/placeholder-shoe.svg`.

9. **`ShopCatalogService`** (`modules/catalog/service/ShopCatalogService.java` - cài đặt `ShopCatalogServiceImpl.java`)
   - `listProducts(ShopProductFilterDTO filter, Pageable)`: Tìm kiếm, lọc đa tiêu chí (danh mục, thương hiệu, giá, màu, size, tồn kho) và phân trang sản phẩm trên Shop.
   - `getProductDetail(String slug)`: Lấy chi tiết sản phẩm theo slug, kèm biến thể màu/size, bộ sưu tập ảnh gom nhóm theo màu và số lượng tồn khả dụng tại kho tổng.
   - `latestProducts(int limit)`: Lấy danh sách sản phẩm mới nhất đang kinh doanh để hiển thị ở trang chủ Shop.
   - `getFilterData()`: Lấy dữ liệu danh mục, thương hiệu, màu sắc, kích cỡ để dựng bộ lọc trên trang Shop.

10. **`InventoryService`** (`modules/inventory/service/InventoryService.java` - cài đặt `InventoryServiceImpl.java`)
    - `getStoreInventory(Long storeId, String keyword, Long categoryId, String stockStatus, Pageable)`: Lấy bảng tồn kho của một chi nhánh có tìm kiếm, lọc và phân trang.
    - `importStock(InventoryImportRequestDTO, User currentUser)`: Nhập thêm hàng vào chi nhánh (giao dịch `IMPORT`), cộng tồn và ghi sổ cái kho.
    - `adjustStock(InventoryAdjustRequestDTO, User currentUser)`: Điều chỉnh tồn kho thực tế sau khi kiểm kê (giao dịch `ADJUST`), ghi sổ cái chênh lệch.
    - `getLedgerEntries(Long storeId, Long variantId, InventoryTransactionType, LocalDateTime from, LocalDateTime to, Pageable)`: Truy vấn lịch sử sổ cái biến động kho theo nhiều tiêu chí lọc và khoảng thời gian.
    - `deductStock(Long storeId, Long variantId, int quantity, String refType, Long refId, User currentUser)`: Trừ tồn kho chi nhánh khi phát sinh bán hàng, áp dụng khóa lạc quan `@Version` chống bán vượt tồn và ghi sổ cái.
    - `revertStock(Long storeId, Long variantId, int quantity, String refType, Long refId, User currentUser)`: Hoàn lại tồn kho chi nhánh khi đơn hàng bị hủy và ghi sổ cái với loại giao dịch `CANCEL`.
    - `countLowStockVariants(Long storeId, int threshold)`: Đếm số lượng biến thể có mức tồn thấp hơn hoặc bằng ngưỡng cảnh báo.

11. **`OnlineAvailabilityService`** (`modules/inventory/fulfillment/OnlineAvailabilityService.java` - cài đặt `OnlineAvailabilityServiceImpl.java`)
    - `getAvailableStock(Long variantId)`: Lấy số lượng tồn kho khả dụng của một biến thể tại kho tổng cung ứng trực tuyến.
    - `getAvailableStockMap(List<Long> variantIds)`: Lấy bảng ánh xạ số lượng tồn kho khả dụng cho danh sách biến thể để tối ưu truy vấn hàng loạt.
    - `hasSufficientStock(Long variantId, int requestedQty)`: Kiểm tra kho tổng có đủ số lượng đáp ứng cho giỏ hàng hoặc đơn hàng online hay không.

12. **`OrderService`** (`modules/order/service/OrderService.java` - cài đặt `OrderServiceImpl.java`)
    - `previewOrder(Long userId, PlaceOrderCommand)`: Tính toán tạm tính, kiểm tra và áp dụng voucher, tính phí vận chuyển và tổng tiền đơn hàng trực tuyến trước khi đặt.
    - `placeOrder(Long userId, PlaceOrderCommand)`: Tạo đơn hàng trực tuyến qua OrderFactory, áp dụng Strategy thanh toán/giảm giá, phát sự kiện `OrderPlacedEvent` để trừ kho và xóa giỏ hàng trong cùng giao dịch.

13. **`OrderQueryService`** (`modules/order/service/OrderQueryService.java` - cài đặt `OrderQueryServiceImpl.java`)
    - `getCustomerOrders(Long userId, Pageable)`: Lấy danh sách lịch sử đơn hàng của khách hàng theo phân trang.
    - `getCustomerOrderDetail(Long userId, String orderCode)`: Lấy chi tiết đơn hàng của khách hàng theo mã đơn, kiểm tra chặt chẽ quyền sở hữu của khách.

14. **`OrderStatusService`** (`modules/order/service/OrderStatusService.java` - cài đặt `OrderStatusServiceImpl.java`)
    - `getOrdersForAdmin(Long storeId, OrderChannel, OrderStatus, String keyword, Pageable)`: Truy vấn danh sách đơn hàng toàn hệ thống có lọc theo chi nhánh (`StoreScopeService`), kênh bán, trạng thái và từ khóa.
    - `getOrderDetailForAdmin(Long orderId)`: Lấy thông tin chi tiết đầy đủ của đơn hàng, lịch sử trạng thái, sản phẩm snapshot và thông tin thanh toán cho quản trị viên.
    - `changeStatus(Long orderId, OrderStatus targetStatus, Long actorId, String note)`: Chuyển trạng thái đơn hàng theo máy trạng thái (State Pattern), ghi lịch sử biến động và phát sự kiện hoàn kho nếu hủy đơn.
    - `cancelOrder(Long orderId, Long actorId, String reason)`: Hủy đơn hàng và kích hoạt hoàn kho tự động, ngăn chặn hủy lặp lại nếu đơn đã ở trạng thái CANCELLED.

15. **`PosCatalogService`** (`modules/pos/service/PosCatalogService.java` - cài đặt `PosCatalogServiceImpl.java`)
    - `searchVariantsForPos(Long storeId, String keyword, int limit)`: Tìm kiếm biến thể sản phẩm bán tại quầy theo mã SKU, mã vạch hoặc tên, kèm kiểm tra tồn kho tại cửa hàng hiện tại.
    - `getVariantForPos(Long storeId, Long variantId)`: Lấy thông tin chi tiết một biến thể kèm số lượng tồn kho tại quầy.

16. **`PosSaleService`** (`modules/pos/service/PosSaleService.java` - cài đặt `PosSaleServiceImpl.java`)
    - `previewSale(Long storeId, PosSalePreviewRequestDTO)`: Tính toán nhanh giá trị giỏ hàng tại quầy POS, áp dụng mã voucher và tính số tiền giảm giá trước khi thanh toán.
    - `checkout(Long storeId, User cashier, PosCheckoutRequestDTO)`: Thực hiện bán hàng và thanh toán trọn gói tại quầy POS trong một giao dịch: tạo đơn qua Factory, thanh toán bằng Strategy, hoàn tất COMPLETED và trừ kho ngay lập tức.

17. **`PosReceiptService`** (`modules/pos/service/PosReceiptService.java` - cài đặt `PosReceiptServiceImpl.java`)
    - `getReceiptData(String orderCode, User currentUser)`: Lấy dữ liệu chi tiết hóa đơn bán lẻ định dạng in nhiệt 80mm, kiểm tra nhân viên chỉ được xem hóa đơn thuộc chi nhánh của mình.
    - `getRecentPosOrders(Long storeId, Pageable)`: Lấy danh sách các đơn hàng POS vừa thanh toán gần đây tại chi nhánh.

18. **`ReportService`** (`modules/report/service/ReportService.java` - cài đặt `ReportServiceImpl.java`)
    - `getDashboardData(Long storeId, Integer daysRange)`: Tổng hợp các chỉ số kinh doanh toàn diện (tổng doanh thu, số đơn hoàn tất, giá trị trung bình đơn, doanh thu theo chi nhánh, doanh thu theo kênh bán, bảng doanh thu theo ngày, top sản phẩm bán chạy, danh sách tồn kho thấp và đơn chờ xử lý).

19. **`EmployeeService`** (`modules/store/service/EmployeeService.java` - cài đặt `EmployeeServiceImpl.java`)
    - `getEmployeesByStore(Long storeId, Pageable)`: Lấy danh sách nhân viên của một chi nhánh có phân trang.
    - `getAllEmployeesByStore(Long storeId)`: Lấy toàn bộ danh sách nhân viên đang làm việc tại chi nhánh.
    - `getEmployeeById(Long id)`: Lấy thông tin chi tiết một nhân viên theo ID.
    - `addEmployee(Long storeId, EmployeeCreateRequestDTO)`: Thêm nhân viên mới vào chi nhánh từ tài khoản email đã có, đồng thời cấp Role tương ứng (CASHIER hoặc STORE_MANAGER).
    - `updateEmployee(Long id, EmployeeUpdateRequestDTO)`: Cập nhật vị trí chức vụ hoặc điều chuyển chi nhánh cho nhân viên, tự động đồng bộ vai trò hệ thống.
    - `lockEmployee(Long id)`: Khóa nhân viên (ngưng hoạt động).
    - `unlockEmployee(Long id)`: Mở khóa hoạt động cho nhân viên.

20. **`StoreScopeService`** (`modules/store/service/StoreScopeService.java` - cài đặt `StoreScopeServiceImpl.java`)
    - `getCurrentUser()`: Lấy thực thể User của người dùng đang đăng nhập trong SecurityContext.
    - `getCurrentEmployee()`: Lấy hồ sơ Employee gắn với tài khoản đang đăng nhập.
    - `isCurrentAdmin()`: Kiểm tra người dùng hiện tại có vai trò `ROLE_ADMIN` hay không.
    - `isCurrentStoreManager()`: Kiểm tra người dùng hiện tại có vai trò `ROLE_STORE_MANAGER` hay không.
    - `getAssignedStoreId()`: Lấy ID cửa hàng được phân công cho nhân viên/quản lý hiện tại (trả về null nếu là ADMIN toàn quyền).
    - `getAssignedStore()`: Lấy thực thể cửa hàng được phân công cho nhân viên/quản lý hiện tại.
    - `resolveStoreId(Long requestedStoreId)`: Giải quyết ID cửa hàng hợp lệ: STORE_MANAGER bị cố định cửa hàng mình phụ trách (bị chặn nếu chọn ID khác), ADMIN được tự do chọn chi nhánh.

21. **`StoreService`** (`modules/store/service/StoreService.java` - cài đặt `StoreServiceImpl.java`)
    - `getStores(String keyword, Pageable)`: Lấy danh sách chi nhánh và kho bãi có tìm kiếm và phân trang.
    - `getStoreById(Long id)`: Lấy chi tiết thông tin chi nhánh theo ID.
    - `getStoreByCode(String code)`: Lấy thông tin chi nhánh theo mã code định danh.
    - `getActiveStores()`: Lấy toàn bộ danh sách các chi nhánh đang hoạt động phục vụ dropdown lựa chọn.
    - `createStore(StoreRequestDTO)`: Tạo mới chi nhánh hoặc kho bãi, kiểm tra mã code duy nhất và tọa độ GPS hợp lệ.
    - `updateStore(Long id, StoreRequestDTO)`: Cập nhật thông tin chi nhánh (tên, địa chỉ, số điện thoại, tọa độ, trạng thái).
    - `deactivateStore(Long id)`: Ngưng hoạt động chi nhánh (xóa mềm).
    - `activateStore(Long id)`: Kích hoạt lại chi nhánh ngưng hoạt động.

22. **`AddressService`** (`modules/user/service/AddressService.java`)
    - `getMyAddresses(Long userId)`: Lấy danh sách sổ địa chỉ nhận hàng của người dùng.
    - `createAddress(Long userId, AddressRequestDTO)`: Thêm địa chỉ nhận hàng mới cho người dùng.
    - `updateAddress(Long userId, Long addressId, AddressRequestDTO)`: Cập nhật thông tin địa chỉ nhận hàng.
    - `deleteAddress(Long userId, Long addressId)`: Xóa một địa chỉ khỏi sổ địa chỉ người dùng.

23. **`UserService`** (`modules/user/service/UserService.java`)
    - `getUserProfile(Long userId)`: Lấy thông tin hồ sơ cá nhân của người dùng.
    - `updateUserProfile(Long userId, UserProfileRequestDTO)`: Cập nhật thông tin cá nhân của người dùng.

24. **`CustomUserDetailsService`** (`config/security/CustomUserDetailsService.java`)
    - `loadUserByUsername(String username)`: Tải thông tin người dùng từ CSDL theo email để Spring Security xác thực phiên đăng nhập hoặc cấp quyền.

---

### 5.2. Các quy tắc nghiệp vụ nổi bật

1. **Làm tròn và so sánh tiền tệ (`MoneyUtil`):**
   - Mọi phép tính tiền sử dụng kiểu `BigDecimal`.
   - Tiền VNĐ được làm tròn về số nguyên đồng (scale 0) theo phương thức `RoundingMode.HALF_UP` qua hàm `MoneyUtil.roundMoney(...)`.
   - CSDL lưu trữ cột tiền dưới dạng `DECIMAL(18,2)`. Khi so sánh số tiền trong mã nguồn, **luôn dùng `compareTo(...) == 0`**, tuyệt đối không dùng `equals(...)` để tránh lỗi so sánh sai khác về scale (`100.00` vs `100`).
   - Định dạng hiển thị chuẩn: `1.290.000 đ` (sử dụng dấu chấm phân cách hàng nghìn, không có phần thập phân).

2. **Trừ kho tức thời và Khóa lạc quan (`@Version`):**
   - Kho hàng được trừ ngay khi tạo đơn thành công (cả đơn Online lẫn đơn POS tại quầy), không giữ chỗ ảo.
   - Thực thể `Inventory` sử dụng chú thích `@Version` trên trường `version` để thực thi cơ chế khóa lạc quan (Optimistic Locking). Khi xảy ra tranh chấp cập nhật đồng thời (hai khách cùng mua một sản phẩm tồn cuối), giao dịch đến sau sẽ bị ném `ObjectOptimisticLockingFailureException` và rollback an toàn, đảm bảo **không bao giờ xảy ra tồn kho âm**.
   - Mọi biến động kho đều ghi nhận một dòng không thể sửa đổi vào bảng `inventory_transactions` (Sổ cái kho).

3. **Quy tắc kiểm tra và áp dụng Voucher:**
   - Mã voucher phải đang ở trạng thái kích hoạt (`status = ACTIVE`).
   - Thời gian hiện tại phải nằm trong khoảng hiệu lực: `startAt <= now <= endAt`.
   - Lượt sử dụng còn lại: `usedCount < usageLimit`.
   - Giá trị tạm tính của đơn hàng phải đạt mức tối thiểu: `subtotal >= minOrderValue`.
   - Khi áp dụng thành công, hệ thống tăng `usedCount` lên 1 và ghi nhận bản ghi vào bảng `voucher_usages` trong cùng giao dịch với đơn hàng.

4. **Chống hủy hai lần và bảo vệ trạng thái đơn hàng (Idempotency & State Integrity):**
   - Đơn hàng chỉ có thể hủy khi đang ở các trạng thái: `PENDING`, `CONFIRMED`, `PACKING` (đối với Admin); hoặc chỉ duy nhất `PENDING` (đối với khách hàng tự hủy trên Shop).
   - Khi đơn đã ở trạng thái `CANCELLED` hoặc `COMPLETED`, máy trạng thái `OrderState` sẽ từ chối mọi yêu cầu chuyển trạng thái hoặc hủy lặp lại, ném ngoại lệ nghiệp vụ `BadRequestException`.
   - Khách hàng chỉ được phép hủy đơn hàng của chính tài khoản mình (`userId` khớp `customer_id`), ngăn chặn việc hủy chéo đơn hàng của người khác.

5. **Quy tắc quản lý tệp và gán ảnh theo màu sắc (CR-06):**
   - Giới hạn dung lượng tệp ảnh tải lên tối đa là 2MB (`app.upload.max-size-mb: 2`), kiểm tra nghiêm ngặt định dạng tệp thông qua phần mở rộng (`.png`, `.jpg`, `.jpeg`, `.webp`) và MIME type (`image/png`, `image/jpeg`, `image/webp`).
   - Mỗi hình ảnh chỉ được gắn vào một trong ba cấp đối tượng: (1) biến thể cụ thể (`variant_id`); (2) một màu sắc của sản phẩm (`color`); hoặc (3) dùng chung cho toàn bộ sản phẩm (cả hai đều NULL).
   - Khi hiển thị, `ProductImageResolver` áp dụng thuật toán 4 bước ưu tiên để luôn hiển thị đúng ảnh của màu hoặc biến thể mà khách hàng đang chọn trên trang Shop.

---

## 6. THUỘC TÍNH CẤU HÌNH HỆ THỐNG (TIỀN TỐ APP.*)

Các thuộc tính cấu hình tự định nghĩa của dự án được tập trung dưới tiền tố `app.*` trong `application.yml` và `application-dev.yml`:

| Thuộc tính cấu hình | Giá trị mặc định | Giá trị trong `dev` | Ý nghĩa nghiệp vụ |
|---|:---:|:---:|---|
| `app.brand.name` | `"Triết Store"` | `"Triết Store"` | Tên thương hiệu hiển thị tập trung trên toàn bộ giao diện Shop, POS, Admin và tiêu đề trang. |
| `app.dev-hints` | `false` | `true` | Bật hoặc tắt bảng gợi ý tài khoản mẫu trên màn hình đăng nhập web. |
| `app.demo-data.enabled` | `false` | `false` *(đổi true khi cần)* | Cờ kích hoạt nạp bộ dữ liệu demo (đơn hàng đa kênh, tồn kho) phục vụ báo cáo và dashboard. Tự động idempotent. |
| `app.shipping.flat-fee` | `30000` | `30000` | Mức phí vận chuyển tiêu chuẩn cố định cho đơn hàng trực tuyến (30.000 VNĐ). |
| `app.shipping.free-threshold` | `1000000` | `1000000` | Ngưỡng giá trị đơn hàng được miễn phí vận chuyển (đơn từ 1.000.000 VNĐ có phí ship = 0 đ). |
| `app.inventory.low-stock-threshold` | `5` | `5` | Ngưỡng số lượng tồn kho tối thiểu (đôi) để kích hoạt cảnh báo hàng sắp hết trên Dashboard. |
| `app.dashboard.low-stock-max-rows` | `20` | `20` | Số dòng tối đa hiển thị trong bảng cảnh báo tồn kho thấp trên trang Dashboard quản trị. |
| `app.dashboard.top-products-limit` | `10` | `10` | Số lượng sản phẩm bán chạy nhất hiển thị trên Dashboard. |
| `app.dashboard.pending-orders-limit` | `5` | `5` | Số lượng đơn hàng trực tuyến chờ xử lý mới nhất hiển thị trên Dashboard. |
| `app.dashboard.default-days-range` | `30` | `30` | Khoảng thời gian mặc định (số ngày gần nhất) để thống kê doanh thu và báo cáo. |
| `app.upload.dir` | `"uploads"` | `"uploads"` | Đường dẫn thư mục gốc trên máy chủ dùng để lưu trữ các tệp tải lên (hình ảnh sản phẩm). |
| `app.upload.max-size-mb` | `2` | `2` | Dung lượng tối đa cho phép của mỗi tệp hình ảnh tải lên (đơn vị: MB). |
| `app.jwt.secret` | — | `MyDevSecretKeyForJWTSigning...` | Khóa bí mật dùng để ký và giải mã mã thông báo JWT (bắt buộc tối thiểu 256 bits). |
| `app.jwt.access-token-expiration` | `900000` | `900000` | Thời gian sống của Access Token JWT (15 phút = 900.000 mili-giây). |
| `app.jwt.refresh-token-expiration` | `604800000` | `604800000` | Thời gian sống của Refresh Token JWT (7 ngày = 604.800.000 mili-giây). |

---

## 7. THỐNG KÊ KIỂM THỬ (TESTS - ĐẾM TỪ MÃ NGUỒN)

> **Ghi chú quan trọng:** Số liệu dưới đây được **đếm trực tiếp từ mã nguồn kiểm thử** (`src/test/java`), tuyệt đối không chạy lệnh kiểm thử `mvn test` trong quá trình lập tài liệu này.

### 7.1. Bảng số lượng lớp test và phương thức kiểm thử theo module

- **Tổng số lớp kiểm thử:** **56 lớp test**  
- **Tổng số phương thức kiểm thử:** **253 phương thức** (gồm 250 phương thức `@Test` thông thường và 3 phương thức `@ParameterizedTest` kiểm thử tham số hóa).

| Phân hệ / Module | Số lớp Test | Số phương thức Test | Danh sách các tệp kiểm thử chi tiết |
|---|:---:|:---:|---|
| `root` | 1 | 1 | `EcommerceApplicationTests.java` (1 test) |
| `common` | 2 | 13 | `RedirectUtilTest.java` (8 tests), `SlugUtilTest.java` (5 tests) |
| `config` | 3 | 12 | `DemoDataSeederIntegrationTest.java` (2 tests), `DemoDataSeederTest.java` (5 tests), `DevDataSeederTest.java` (5 tests) |
| `modules/cart` | 2 | 11 | `CartTest.java` (4 tests), `CartServiceTest.java` (7 tests) |
| `modules/catalog` | 11 | 47 | `AdminProductDetailPageRenderIntegrationTest.java` (1 test), `AdminProductImageUploadColorSecurityMockMvcTest.java` (3 tests), `AdminProductImageUploadMockMvcTest.java` (8 tests), `CategoryServiceTest.java` (4 tests), `FileStorageServiceTest.java` (10 tests), `ProductDetailPageColorRenderIntegrationTest.java` (2 tests), `ProductImageRepositoryColorIntegrationTest.java` (1 test), `ProductImageResolverTest.java` (6 tests), `ProductServiceTest.java` (5 tests), `ShopCatalogServiceIntegrationTest.java` (4 tests), `ShopCatalogServiceTest.java` (3 tests) |
| `modules/inventory` | 5 | 18 | `InventoryTest.java` (7 tests), `InventoryTransactionTypeTest.java` (4 tests), `MainWarehouseFulfillmentResolverTest.java` (2 tests), `InventoryServiceTest.java` (4 tests), `InventoryLedgerPageRenderingIntegrationTest.java` (1 test) |
| `modules/order` | 16 | 70 | `OrderLifecycleIntegrationTest.java` (5 tests), `OrderPlacementIntegrationTest.java` (1 test), `CheckoutFormValidationTest.java` (6 tests), `OrderEntityTest.java` (3 tests), `OrderCancelledListenerTest.java` (2 tests), `OrderPlacedListenerTest.java` (3 tests), `OrderFactoryTest.java` (3 tests), `OrderQueryServiceTest.java` (3 tests), `OrderServiceTest.java` (7 tests), `OrderStatusServiceTest.java` (9 tests), `FlatRateShippingFeeCalculatorTest.java` (4 tests), `OrderStateFactoryTest.java` (2 tests), `OrderStateTest.java` (6 tests), `AdminOrderPageControllerTest.java` (4 tests), `OrderDisplayHelperTest.java` (5 tests), `OrderTemplateRenderingIntegrationTest.java` (7 tests) |
| `modules/payment` | 1 | 10 | `PaymentStrategyTest.java` (10 tests) |
| `modules/pos` | 5 | 27 | `PosSaleIntegrationTest.java` (2 tests), `PosApiControllerTest.java` (8 tests), `PosCatalogServiceTest.java` (4 tests), `PosReceiptServiceTest.java` (6 tests), `PosSaleServiceTest.java` (7 tests) |
| `modules/promotion` | 2 | 12 | `VoucherTest.java` (6 tests), `DiscountStrategyTest.java` (6 tests) |
| `modules/report` | 4 | 17 | `ReportServiceImplTest.java` (5 tests), `ReportServiceIntegrationTest.java` (3 tests), `AdminDashboardPageControllerTest.java` (4 tests), `AdminDashboardPageIntegrationTest.java` (5 tests) |
| `modules/store` | 2 | 8 | `EmployeeServiceTest.java` (4 tests), `StoreScopeServiceTest.java` (4 tests) |
| `modules/user` | 2 | 7 | `AddressMapperTest.java` (2 tests), `RoleDisplayHelperTest.java` (5 tests) |
| **TỔNG CỘNG** | **56** | **253** | **56 lớp test, 253 phương thức kiểm thử** |

---

### 7.2. Danh sách Test tích hợp trên SQL Server thật
Các lớp kiểm thử sử dụng `@SpringBootTest` kết nối trực tiếp CSDL SQL Server thật (`ecommerce_db`) nhằm kiểm tra trọn vẹn luồng nghiệp vụ, giao dịch và dữ liệu:

1. `com/dahdt/ecommerce/EcommerceApplicationTests.java`: Smoke test nạp toàn bộ Spring Context và kiểm tra kết nối CSDL SQL Server.
2. `com/dahdt/ecommerce/config/DemoDataSeederIntegrationTest.java`: Kiểm tra cơ chế nạp dữ liệu demo mẫu của `DemoDataSeeder` trên CSDL thật, đảm bảo tính idempotent không sinh trùng đơn hàng.
3. `com/dahdt/ecommerce/modules/catalog/repository/ProductImageRepositoryColorIntegrationTest.java`: Kiểm tra truy vấn hình ảnh theo màu sắc và phân nhóm hình ảnh biến thể trên bảng `product_images` CSDL thật.
4. `com/dahdt/ecommerce/modules/catalog/service/ShopCatalogServiceIntegrationTest.java`: Kiểm tra tra cứu, tìm kiếm, lọc đa tiêu chí và phân trang sản phẩm Shop trực tiếp trên các bảng CSDL thật.
5. `com/dahdt/ecommerce/modules/catalog/web/AdminProductDetailPageRenderIntegrationTest.java`: Kiểm tra kết nối CSDL và phân quyền quản trị khi dựng trang chi tiết sản phẩm và quản lý danh sách ảnh.
6. `com/dahdt/ecommerce/modules/catalog/web/AdminProductImageUploadColorSecurityMockMvcTest.java`: Kiểm tra xác thực phân quyền và lưu dữ liệu ảnh biến thể theo màu vào CSDL thật.
7. `com/dahdt/ecommerce/modules/catalog/web/ProductDetailPageColorRenderIntegrationTest.java`: Kiểm tra nạp dữ liệu thật và kết xuất bộ sưu tập ảnh theo màu trên giao diện chi tiết sản phẩm Shop.
8. `com/dahdt/ecommerce/modules/inventory/web/InventoryLedgerPageRenderingIntegrationTest.java`: Dựng trang Sổ cái kho (`/admin/inventory/ledger`) với dữ liệu thật, kiểm tra phân quyền truy cập và render danh sách lịch sử biến động kho.
9. `com/dahdt/ecommerce/modules/order/OrderPlacementIntegrationTest.java`: Kiểm tra trọn vẹn luồng tạo đơn hàng online, áp dụng voucher, tính toán tiền và phát sự kiện trừ kho trên CSDL thật.
10. `com/dahdt/ecommerce/modules/order/OrderLifecycleIntegrationTest.java`: Kiểm thử toàn diện chu trình sống của đơn hàng: chuyển đổi qua các trạng thái (PENDING -> CONFIRMED -> PACKING -> SHIPPING -> COMPLETED), hủy đơn và tự động hoàn kho.
11. `com/dahdt/ecommerce/modules/order/web/OrderTemplateRenderingIntegrationTest.java`: Dựng 5 trang đơn hàng (chi tiết đơn admin, danh sách đơn admin, checkout, thông báo thành công, lịch sử và chi tiết đơn khách) kết nối CSDL thật.
12. `com/dahdt/ecommerce/modules/pos/PosSaleIntegrationTest.java`: Kiểm tra luồng bán hàng tại quầy POS hoàn tất trong một giao dịch duy nhất trên CSDL thật (tạo đơn COMPLETED, thanh toán, trừ kho chi nhánh).
13. `com/dahdt/ecommerce/modules/report/service/ReportServiceIntegrationTest.java`: Kiểm tra truy vấn tổng hợp báo cáo doanh thu, top sản phẩm và thống kê đơn hàng từ các bảng dữ liệu thật.
14. `com/dahdt/ecommerce/modules/report/web/AdminDashboardPageIntegrationTest.java`: Kiểm tra dựng toàn bộ Dashboard quản trị với dữ liệu tổng hợp trực tiếp từ CSDL thật.

---

### 7.3. Danh sách Test dựng trang thật (Thymeleaf Rendering Integration Test)
Các lớp kiểm thử sử dụng `@SpringBootTest` kết hợp `@AutoConfigureMockMvc` và phân quyền Spring Security đầy đủ nhằm kết xuất toàn bộ cây DOM của template Thymeleaf thật, giúp phát hiện triệt để các lỗi truy cập thuộc tính null, sai kiểu hoặc thiếu DTO (tránh hoàn toàn các hạn chế của `standaloneSetup`):

1. `com/dahdt/ecommerce/modules/catalog/web/AdminProductDetailPageRenderIntegrationTest.java`:  
   - Dựng thật trang quản trị chi tiết sản phẩm (`admin/catalog/product-detail.html`), kiểm tra hiển thị bảng biến thể, danh sách ảnh gom theo màu, modal tải tệp ảnh lên và CSRF token.
2. `com/dahdt/ecommerce/modules/catalog/web/ProductDetailPageColorRenderIntegrationTest.java`:  
   - Dựng thật trang chi tiết sản phẩm phía khách hàng (`shop/product-detail.html`), kiểm tra bộ chọn màu sắc, hiển thị ảnh theo màu, thumbnail và hành vi đổi ảnh theo biến thể được chọn.
3. `com/dahdt/ecommerce/modules/inventory/web/InventoryLedgerPageRenderingIntegrationTest.java`:  
   - Dựng trang Sổ cái kho (`admin/inventory/ledger-list.html`), kiểm tra phân quyền truy cập và render danh sách lịch sử biến động kho.
4. `com/dahdt/ecommerce/modules/order/web/OrderTemplateRenderingIntegrationTest.java`:  
   - Dựng trang chi tiết đơn hàng quản trị (`admin/order/order-detail.html`) cho cả đơn Online và POS ở tất cả các trạng thái vòng đời.
   - Dựng trang chi tiết đơn hàng phía khách hàng (`shop/order/order-detail.html`), trang lịch sử đơn (`shop/order/order-history.html`), trang checkout (`shop/order/checkout.html`) và trang đặt hàng thành công (`shop/order/checkout-success.html`).
5. `com/dahdt/ecommerce/modules/report/web/AdminDashboardPageIntegrationTest.java`:  
   - Dựng trang Dashboard quản trị (`admin/report/dashboard.html`), kiểm tra nạp dữ liệu động cho các thẻ chỉ số, bảng doanh thu theo chi nhánh, doanh thu theo kênh bán, bảng top sản phẩm và bảng cảnh báo tồn kho thấp.

---

## 8. CÁC HẠN CHẾ ĐÃ BIẾT

Dựa trên phân tích mã nguồn thực tế và tài liệu dự án, hệ thống hiện có các giới hạn kỹ thuật và phạm vi sau:

1. **Thanh toán trực tuyến & POS chỉ ở mức giả lập:** Các hình thức thanh toán quẹt thẻ, mã QR và ví điện tử (`CardPaymentStrategy`, `QrPaymentStrategy`, `EWalletMockPaymentStrategy`) sinh mã giao dịch giả định dạng `CARD-...`, `QR-...`, `EW-MOCK-...` và ghi nhận trạng thái `PAID`, chưa tích hợp kết nối mạng thực tế với cổng thanh toán (như VNPay, MoMo, ZaloPay, VietQR hay máy POS quẹt thẻ của ngân hàng).
2. **Chưa tích hợp xuất hóa đơn điện tử pháp lý:** Phân hệ POS chỉ hỗ trợ tạo dữ liệu và kết xuất giao diện in phiếu hóa đơn bán lẻ 80mm nội bộ, chưa kết nối giải pháp hóa đơn điện tử có mã của cơ quan thuế (như VNPT Invoice, Viettel S-Invoice, MISA meInvoice).
3. **Chưa hỗ trợ quy trình đổi trả hàng & hoàn tiền sau bán:** Hệ thống hiện chỉ hỗ trợ hủy đơn trước khi giao hàng (trạng thái `PENDING`, `CONFIRMED`, `PACKING`); chưa có nghiệp vụ đổi hàng, trả hàng và hoàn tiền sau khi đơn hàng đã hoàn tất (`COMPLETED`). Module `returnwarranty` hiện là khung rỗng.
4. **Phí vận chuyển tính theo biểu phí phẳng cố định:** Biểu phí giao hàng (`FlatRateShippingFeeCalculator`) áp dụng đồng giá 30.000 đ và miễn phí cho đơn từ 1.000.000 đ; chưa tính cước động theo khoảng cách GPS (Haversine từ chi nhánh đến tọa độ khách) và chưa tích hợp API hãng vận chuyển (GHN, GHTK, Viettel Post).
5. **Kênh Online cố định xuất hàng từ Kho tổng:** Đơn hàng đặt trên website Shop luôn được điều phối xuất từ Kho tổng (`MainWarehouseFulfillmentResolver`), chưa hỗ trợ thuật toán tự động định tuyến xuất hàng từ cửa hàng gần nhất còn hàng.
6. **Bắt buộc đăng nhập để mua hàng (Chưa hỗ trợ Giỏ hàng khách vãng lai):** Khách hàng phải đăng nhập tài khoản có vai trò `CUSTOMER` để thêm sản phẩm vào giỏ và tiến hành thanh toán (chưa có Guest Cart dựa trên Cookie/Session vãng lai).
7. **Đánh giá và bình luận sản phẩm chưa cài đặt giao diện:** Bảng CSDL `reviews` đã được tạo từ migration `V27`, nhưng tầng mã Java chưa triển khai Entity, Service hay Controller tương ứng (thuộc mức Could được bảo lưu).
8. **Chưa hỗ trợ mở/đóng ca thu ngân POS:** Bảng `pos_shifts` đã tạo ở `V25`, nhưng chưa xây dựng giao diện và nghiệp vụ mở/chốt ca thu ngân (thuộc danh sách cắt giảm Should).
9. **Chưa hỗ trợ điều chuyển hàng giữa các kho:** Hai bảng `stock_transfers` và `stock_transfer_items` đã tạo ở `V26`, nhưng chưa có phân hệ giao diện thực hiện lệnh chuyển kho (thuộc danh sách cắt giảm Should).
10. **Chưa tích hợp gửi OTP thật qua SMS/Email:** Bảng `otp_verifications` đã tạo từ giai đoạn 1 nhưng chưa nối dịch vụ viễn thông SMS Gateway hoặc máy chủ SMTP gửi mail.

---

## 9. DANH SÁCH TODO, FIXME & BÁO CÁO CÁC LỚP KHÔNG ĐƯỢC GỌI TRỰC TIẾP

### 9.1. Danh sách chú thích TODO / FIXME / XXX
- **Kết quả quét toàn bộ mã nguồn:** **0 kết quả.**  
- Trong toàn bộ thư mục `src/main` và `src/test`, **không có bất kỳ dòng chú thích nào chứa từ khóa `TODO`, `FIXME` hay `XXX`**. Toàn bộ các chức năng đã cam kết đều được cài đặt hoàn chỉnh.

### 9.2. Báo cáo các lớp không thấy được import hoặc gọi trực tiếp trong mã Java
Qua phân tích phụ thuộc tĩnh, phát hiện một số lớp không xuất hiện trong các câu lệnh `import` của các lớp Java khác. Đây là đặc tính kiến trúc bình thường của Spring Boot Framework, được phân loại cụ thể như sau:

1. **Các Controller và Configuration được nạp tự động bởi Spring Framework (Component Scanning):**
   - *Web & REST Controllers:* `AddressController`, `AdminBrandPageController`, `AdminCategoryPageController`, `AdminEmployeePageController`, `AdminInventoryPageController`, `AdminProductPageController`, `AdminStorePageController`, `AuthController`, `AuthPageController`, `ErrorPageController`, `PosPageController`, `UserController`.  
     *Lý do:* Là các điểm tiếp nhận request đầu vào (Entry points) của ứng dụng, được Spring MVC tự động quét qua `@Controller` / `@RestController` và đăng ký vào bộ điều phối `DispatcherServlet`.
   - *Cấu hình hệ thống:* `SecurityConfig`, `WebMvcConfig`, `SwaggerConfig`.  
     *Lý do:* Chứa chú thích `@Configuration`, được Spring Boot nạp tự động khi khởi động ngữ cảnh ứng dụng (ApplicationContext).
   - *Controller Advice dùng chung:* `GlobalModelAttributes`, `ShopModelAttributes`.  
     *Lý do:* Khai báo `@ControllerAdvice`, Spring MVC tự động áp dụng vào tất cả các PageController để cung cấp dữ liệu toàn cục cho template.

2. **Các lớp triển khai Service (`*ServiceImpl`) được tiêm phụ thuộc qua Interface:**
   - `BrandServiceImpl` (triển khai `BrandService`).
   - `FileStorageServiceImpl` (triển khai `FileStorageService`).
   - `OnlineAvailabilityServiceImpl` (triển khai `OnlineAvailabilityService`).
   - `StoreServiceImpl` (triển khai `StoreService`).  
     *Lý do:* Tuân thủ nguyên lý Dependency Inversion (SOLID), các Controller và Service khác chỉ phụ thuộc và inject Interface, không gọi trực tiếp tên lớp `*ServiceImpl`.

3. **Các Entity và Repository chưa có mã nghiệp vụ gọi trực tiếp:**
   - `OtpVerification` (`modules/auth/entity/OtpVerification.java`): Bảng xác thực OTP từ giai đoạn 1, hiện chưa có OTP Service trong luồng mua hàng.
   - `OrderItemRepository` (`modules/order/repository/OrderItemRepository.java`): Interface repository mở rộng `JpaRepository`, nhưng nghiệp vụ lưu và truy vấn `OrderItem` được thực hiện qua quan hệ tầng thác `CascadeType.ALL` trên thực thể `Order` (`order.getItems()`).
   - `UserProfileRepository` (`modules/user/repository/UserProfileRepository.java`): Thao tác với hồ sơ người dùng hiện được thực hiện thông qua liên kết `@OneToOne` từ `User` (`user.getProfile()`) hoặc `UserRepository`.

*(Báo cáo giữ nguyên hiện trạng mã nguồn, tuyệt đối không xóa bất kỳ lớp nào).*
