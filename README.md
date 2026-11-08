# Triết Store — Hệ Thống Bán Lẻ Chuỗi Cửa Hàng Giày Dép Đa Kênh

Hệ thống bán lẻ chuỗi cửa hàng giày dép **"Triết Store"** kết hợp bán hàng tại quầy (POS), đặt hàng trực tuyến (Shop) và quản trị tập trung (Admin), xây dựng theo kiến trúc MVC phân lớp theo module nghiệp vụ.

## Công nghệ sử dụng
- **Backend:** Java 17, Spring Boot 3.x (Spring MVC, Spring Data JPA, Spring Security, Validation)
- **Cơ sở dữ liệu & Migration:** Microsoft SQL Server, Flyway (`ddl-auto: validate`)
- **Giao diện:** Thymeleaf, Bootstrap 5, Bootstrap Icons (tập trung theme CSS và tên thương hiệu)
- **Tiện ích & Mapping:** Lombok, MapStruct, springdoc-openapi (Swagger UI)
- **Build tool:** Maven

## Yêu cầu môi trường
- **Java:** JDK 17 trở lên
- **Maven:** 3.8 trở lên
- **CSDL:** Microsoft SQL Server (đang chạy trên `localhost:1433`)

## Các bước cài đặt và khởi chạy
1. **Tạo CSDL SQL Server:** Mở SSMS hoặc sqlcmd, tạo database rỗng không chỉ định COLLATE:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```
2. **Cấu hình môi trường:** Sao chép file cấu hình mẫu sang file cấu hình cá nhân:
   - Sao chép `src/main/resources/application-dev.example.yml` thành `src/main/resources/application-dev.yml`.
   - Chọn một trong hai cách kết nối CSDL trong `application-dev.yml`:
     - **Cách 1: Tài khoản SQL (sa) — Mặc định:** URL có `databaseName=ecommerce_db;encrypt=true;trustServerCertificate=true`, cấu hình `username: sa` và `password: <mat_khau_sql_server>`.
     - **Cách 2: Windows Authentication (xác thực tích hợp):** URL thêm `integratedSecurity=true` và KHÔNG khai báo `username` hay `password`.
       *Điều kiện theo tài liệu chính thức của Microsoft:* trên Windows cần chép file `mssql-jdbc_auth-<phiên bản>-<kiến trúc>.dll` (đúng với số bit của JVM) vào một thư mục nằm trong biến môi trường `PATH`, hoặc đặt thuộc tính `java.library.path` trỏ tới thư mục chứa nó (`-Djava.library.path=...`); nếu thiếu, ứng dụng sẽ báo lỗi không tìm thấy thư viện xác thực.
3. **Khởi chạy ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```
   Flyway sẽ tự động chạy toàn bộ migration (V1–V30), và `DevDataSeeder` sẽ nạp 5 tài khoản dev mẫu.
   *Lưu ý:* Ở profile dev, tài khoản dev luôn được đặt lại mật khẩu mặc định khi khởi động.

## Tài khoản dev thử nghiệm (mật khẩu: `password123`)
| Vai trò | Email | Chi nhánh | Phạm vi / Mục đích |
|---|---|---|---|
| **ADMIN** | `admin@dahdt.com` | Toàn chuỗi | Quản trị toàn hệ thống, danh mục, kho bãi |
| **STORE_MANAGER** | `manager@dahdt.com` | `STORE-Q1` | Quản lý cửa hàng, nhân viên, tồn kho chi nhánh |
| **CASHIER** | `cashier@dahdt.com` | `STORE-Q1` | Thu ngân POS, bán hàng tại quầy |
| **CUSTOMER** | `customer@dahdt.com` | — | Khách hàng mua sắm online, đặt hàng |
| **CUSTOMER** | `customer2@dahdt.com` | — | Khách hàng thứ hai kiểm thử phân quyền đơn hàng |

## Các URL chính theo từng khu
- **Khu Shop (Khách hàng):**
  - Trang chủ: [http://localhost:8080/](http://localhost:8080/)
  - Danh sách & lọc sản phẩm: [http://localhost:8080/shop/products](http://localhost:8080/shop/products)
  - Giỏ hàng: [http://localhost:8080/cart](http://localhost:8080/cart)
  - Thanh toán (Checkout): [http://localhost:8080/checkout](http://localhost:8080/checkout)
  - Lịch sử đơn hàng: [http://localhost:8080/account/orders](http://localhost:8080/account/orders)
- **Khu Quản trị (Admin):**
  - Dashboard: [http://localhost:8080/admin](http://localhost:8080/admin)
  - Danh mục: [http://localhost:8080/admin/categories](http://localhost:8080/admin/categories)
  - Thương hiệu: [http://localhost:8080/admin/brands](http://localhost:8080/admin/brands)
  - Sản phẩm: [http://localhost:8080/admin/products](http://localhost:8080/admin/products)
  - Tồn kho & Sổ kho: [http://localhost:8080/admin/inventory](http://localhost:8080/admin/inventory)
  - Cửa hàng & Nhân viên: [http://localhost:8080/admin/stores](http://localhost:8080/admin/stores)
- **Khu Bán hàng tại quầy (POS):**
  - Màn hình POS (dự kiến hoàn thiện Ngày 8–9): [http://localhost:8080/pos](http://localhost:8080/pos)
- **Tài liệu API (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Đăng nhập:** [http://localhost:8080/login](http://localhost:8080/login)

## Cách chạy kiểm thử (Tests)
> **Lưu ý:** Lệnh `mvn test` sử dụng trực tiếp cơ sở dữ liệu môi trường dev (`application-dev.yml`), do đó **Microsoft SQL Server phải đang chạy** và database `ecommerce_db` đã được cấu hình trước khi chạy test.
```bash
mvn test
```

## Cấu trúc thư mục rút gọn
```
src/main/java/com/dahdt/ecommerce/
├── config/                  # Cấu hình Security (2 chain), Swagger, DevDataSeeder
├── common/                  # Exception, BaseEntity, RedirectUtil, ControllerAdvice chung
└── modules/
    ├── auth/ & user/        # Xác thực JWT/Session, quản lý người dùng, địa chỉ
    ├── catalog/             # Danh mục, thương hiệu, sản phẩm giày dép, biến thể size/màu
    ├── store/               # Cửa hàng chi nhánh, nhân viên, phạm vi quản lý
    ├── inventory/           # Quản lý tồn kho theo chi nhánh, sổ cái biến động kho
    ├── cart/                # Giỏ hàng online của khách hàng
    ├── order/               # Đơn hàng trực tuyến, Factory tạo đơn, Observer trừ/hoàn kho
    ├── payment/             # Chiến lược thanh toán (Strategy: COD, Tiền mặt, Thẻ, QR)
    ├── promotion/           # Mã giảm giá (Strategy: Phần trăm, Cố định), kiểm tra điều kiện
    └── report/              # Thống kê báo cáo, dashboard doanh thu
src/main/resources/
├── db/migration/            # Flyway migrations (V1 đến V30)
└── templates/               # Giao diện Thymeleaf (shop/, admin/, pos/, layout/)
```

## Trạng thái chức năng
- **Đã hoàn thành (Ngày 1–6, Mốc v0.5):**
  - Xác thực 2 chain (Session cho Web + JWT cho API), phân quyền 4 vai trò.
  - CRUD danh mục 2 cấp, thương hiệu, sản phẩm giày dép kèm biến thể màu/size EU.
  - Quản lý cửa hàng, nhân viên, tồn kho chi nhánh có khóa lạc quan (`@Version`) và sổ cái kho.
  - Shop online: trang chủ, xem/tìm kiếm/lọc sản phẩm, giỏ hàng, checkout, áp mã voucher.
  - Đặt hàng online với Design Pattern: Factory (tạo đơn), Strategy (thanh toán & giảm giá), Observer (trừ kho tự động qua Spring Events).
  - Tối ưu hiệu năng nạp dữ liệu Shop và chuẩn hóa bảo mật Open Redirect.
- **Đang triển khai tiếp theo (Ngày 8–14):**
  - Khu POS bán hàng tại quầy: tìm/quét mã SKU, giỏ hàng quầy, tính tiền thừa, in hóa đơn.
  - Quản lý đơn hàng tập trung đa kênh trên trang Admin (đổi trạng thái với State Pattern).
  - Báo cáo thống kê, dashboard doanh thu theo chi nhánh và kênh bán.
