# Triết Store — Hệ Thống Bán Lẻ Chuỗi Cửa Hàng Giày Dép Đa Kênh

Hệ thống bán lẻ chuỗi cửa hàng giày dép **"Triết Store"** kết hợp bán hàng tại quầy (POS), đặt hàng trực tuyến (Shop) và quản trị tập trung (Admin), xây dựng trên nền tảng Spring Boot MVC phân lớp theo module nghiệp vụ.

## Công nghệ sử dụng
- **Backend:** Java 17, Spring Boot 3.x (Spring MVC, Spring Data JPA, Spring Security, Validation)
- **Cơ sở dữ liệu & Migration:** Microsoft SQL Server, Flyway (`ddl-auto: validate`, V1–V31)
- **Giao diện:** Thymeleaf, Bootstrap 5, Bootstrap Icons (tập trung theme CSS và tên thương hiệu)
- **Tiện ích & Mapping:** Lombok, MapStruct, springdoc-openapi (Swagger UI)
- **Kiến trúc & Design Patterns:** Strategy (thanh toán, giảm giá, phí ship, kho xuất), Factory (tạo đơn, mã đơn), Observer (trừ/hoàn kho), State (vòng đời đơn hàng)

## Yêu cầu môi trường
- **Java:** JDK 17 trở lên | **Maven:** 3.8 trở lên | **CSDL:** Microsoft SQL Server (cổng `1433`)

## Các bước cài đặt và khởi chạy
1. **Tạo CSDL SQL Server:** Mở SSMS hoặc sqlcmd, tạo database rỗng không chỉ định COLLATE:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```
2. **Cấu hình môi trường:** Sao chép `src/main/resources/application-dev.example.yml` thành `src/main/resources/application-dev.yml` và điền thông tin kết nối SQL Server (xác thực `sa` hoặc Windows Authentication).
3. **Khởi chạy ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```
   Flyway sẽ tự động chạy toàn bộ migration (V1–V31), và `DevDataSeeder` sẽ nạp 5 tài khoản dev mẫu.
   Thư mục lưu trữ tệp tải lên (upload) được cấu hình qua `app.upload.dir` (mặc định `uploads/`, giới hạn `app.upload.max-size-mb=2MB`), tự động tạo khi chạy và được bỏ qua trong git.

## Tài khoản dev thử nghiệm (mật khẩu: `password123`)
| Vai trò | Email | Chi nhánh | Phạm vi / Mục đích |
|---|---|---|---|
| **ADMIN** | `admin@dahdt.com` | Toàn chuỗi | Quản trị toàn hệ thống, danh mục, kho bãi, đơn hàng |
| **STORE_MANAGER** | `manager@dahdt.com` | `STORE-Q1` | Quản lý cửa hàng, nhân viên, tồn kho chi nhánh |
| **CASHIER** | `cashier@dahdt.com` | `STORE-Q1` | Thu ngân POS, bán hàng tại quầy, in hóa đơn |
| **CUSTOMER** | `customer@dahdt.com` | — | Khách hàng mua sắm online, đặt hàng |
| **CUSTOMER** | `customer2@dahdt.com` | — | Khách hàng thứ hai kiểm thử phân quyền đơn hàng |

## Dữ liệu demo cho báo cáo & dashboard
- **Cách kích hoạt:** Đặt `app.demo-data.enabled: true` trong `src/main/resources/application-dev.yml`, khởi chạy ứng dụng một lần (`mvn spring-boot:run`), sau khi hoàn tất nạp thì đặt lại `false`.
- **Dữ liệu được tạo:** Nhập bổ sung tồn kho an toàn; khoảng 25 đơn POS tại quầy (tiền mặt, thẻ, QR); khoảng 20 đơn online của 2 khách dev với đầy đủ 6 trạng thái vòng đời (PENDING, CONFIRMED, PACKING, SHIPPING, COMPLETED, CANCELLED) rải ngẫu nhiên trong 14 ngày gần nhất. Tự động kiểm tra idempotent.

## Các URL chính theo từng khu
- **Khu Shop (Khách hàng):**
  - Trang chủ: [http://localhost:8080/](http://localhost:8080/)
  - Danh sách & lọc sản phẩm: [http://localhost:8080/shop/products](http://localhost:8080/shop/products)
  - Giỏ hàng: [http://localhost:8080/cart](http://localhost:8080/cart)
  - Thanh toán (Checkout): [http://localhost:8080/checkout](http://localhost:8080/checkout)
  - Lịch sử đơn hàng: [http://localhost:8080/account/orders](http://localhost:8080/account/orders)
- **Khu Quản trị (Admin):**
  - Dashboard báo cáo: [http://localhost:8080/admin](http://localhost:8080/admin)
  - Danh mục sản phẩm: [http://localhost:8080/admin/categories](http://localhost:8080/admin/categories)
  - Thương hiệu: [http://localhost:8080/admin/brands](http://localhost:8080/admin/brands)
  - Sản phẩm & biến thể: [http://localhost:8080/admin/products](http://localhost:8080/admin/products)
  - Tồn kho & Sổ cái kho: [http://localhost:8080/admin/inventory](http://localhost:8080/admin/inventory)
  - Quản lý đơn hàng đa kênh: [http://localhost:8080/admin/orders](http://localhost:8080/admin/orders)
  - Cửa hàng & Nhân viên: [http://localhost:8080/admin/stores](http://localhost:8080/admin/stores)
- **Khu Bán hàng tại quầy (POS):**
  - Màn hình bán hàng POS: [http://localhost:8080/pos](http://localhost:8080/pos)
  - Đơn bán gần đây: [http://localhost:8080/pos/orders](http://localhost:8080/pos/orders)
- **Tài liệu API (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Đăng nhập form:** [http://localhost:8080/login](http://localhost:8080/login)

## Danh sách chức năng đã hoàn thành
- **Bảo mật & Phân quyền:** 2 SecurityFilterChain (Session cho Web + JWT cho API), phân quyền 4 vai trò rõ ràng.
- **Danh mục & Sản phẩm:** CRUD danh mục dạng cây, thương hiệu, sản phẩm giày dép cha, biến thể màu/size EU.
- **Hình ảnh sản phẩm (CR-06):** Tải ảnh trực tiếp từ máy tính (tối đa 2MB, định dạng png/jpg/jpeg/webp) hoặc URL; gắn ảnh theo biến thể cụ thể, theo màu sắc hoặc chung cho sản phẩm; đổi ảnh động theo màu trên Shop.
- **Quản trị kho & Chi nhánh:** Quản lý điểm bán và nhân sự, tồn kho chi nhánh với khóa lạc quan (`@Version`), sổ cái biến động kho.
- **Shop trực tuyến:** Trang chủ, xem/tìm kiếm/lọc sản phẩm, giỏ hàng, áp mã voucher, checkout, lịch sử và chi tiết đơn hàng.
- **Quản lý đơn hàng đa kênh:** Theo dõi đơn Shop và POS, chuyển trạng thái theo State Pattern (PENDING → CONFIRMED → PACKING → SHIPPING → COMPLETED).
- **Hủy đơn & Hoàn kho:** Khách hủy đơn PENDING, Admin hủy đơn hợp lệ; tự động hoàn lại tồn kho chi nhánh qua Observer Pattern.
- **Bán hàng tại quầy (POS):** Tra cứu nhanh SKU/barcode, giỏ hàng quầy, áp voucher, tính tiền thừa, thanh toán trọn gói 1 transaction, in hóa đơn 80mm.
- **Báo cáo & Dashboard:** Thống kê doanh thu theo ngày/chi nhánh/kênh bán, top sản phẩm bán chạy, cảnh báo tồn kho thấp dạng bảng số.
- **Dữ liệu demo:** Bộ tạo dữ liệu đa kênh phong phú phục vụ kiểm thử và báo cáo (`DemoDataSeeder`).

## Kiểm thử (Tests)
Hệ thống có **56 lớp kiểm thử** với tổng cộng **253 phương thức test** (đếm từ mã nguồn, gồm 250 `@Test` và 3 `@ParameterizedTest`), bao gồm các test tích hợp `@SpringBootTest` trên CSDL SQL Server thật và các test dựng trang hoàn chỉnh bằng MockMvc + Thymeleaf.
> **Lưu ý:** Lệnh `mvn test` yêu cầu CSDL SQL Server đang chạy và kết nối được cấu hình trong `application-dev.yml`.
```bash
mvn test
```
