# E-Commerce B2C — Spring Boot MVC + SQL Server

## Mô tả

Website thương mại điện tử B2C (bán lẻ trực tiếp cho người tiêu dùng), xây dựng bằng **Spring Boot 3.x** với kiến trúc MVC phân lớp theo module nghiệp vụ. Hệ thống hỗ trợ đầy đủ các chức năng: quản lý sản phẩm, giỏ hàng, đặt hàng, thanh toán, vận chuyển, đánh giá, khuyến mãi, chăm sóc khách hàng và quản trị phân quyền.

## Tech Stack

- **Java 17** + **Spring Boot 3.x** (MVC)
- **SQL Server** (cơ sở dữ liệu)
- **Spring Data JPA** (ORM/Hibernate)
- **Flyway** (quản lý migration CSDL)
- **Spring Security + JWT** (bảo mật, phân quyền)
- **MapStruct** (chuyển đổi Entity ↔ DTO)
- **springdoc-openapi** (Swagger UI — tài liệu API)
- **Maven** (build tool)

## Yêu cầu trước khi chạy

1. **Java 17+** — tải tại [Adoptium](https://adoptium.net/)
2. **Maven 3.8+** — tải tại [Maven](https://maven.apache.org/download.cgi) (hoặc dùng IntelliJ có sẵn)
3. **SQL Server** — cài đặt và đảm bảo đang chạy trên `localhost:1433`
4. **Tạo database**: mở SQL Server Management Studio (SSMS), chạy lệnh:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```
5. **Cấu hình kết nối DB**: mở file `src/main/resources/application-dev.yml`, đổi `password` thành mật khẩu SQL Server của bạn.

## Cách chạy

```bash
# Build project (kiểm tra không có lỗi)
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Sau khi chạy, truy cập:
- **Swagger UI** (test API): [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs** (JSON): [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Tài khoản dev (mẫu thử nghiệm)

Dữ liệu tài khoản được tự động khởi tạo khi chạy profile `dev` qua `DevDataSeeder` (mật khẩu đã được mã hóa bằng `BCryptPasswordEncoder`):

| Vai trò | Email | Mật khẩu | Chi nhánh gắn kèm | Mục đích sử dụng |
|---|---|---|---|---|
| **ADMIN** | `admin@dahdt.com` | `password123` | Toàn hệ thống | Quản trị chuỗi, danh mục, kho bãi toàn quốc |
| **STORE_MANAGER** | `manager@dahdt.com` | `password123` | `STORE-Q1` (Nguyễn Trãi - Q1) | Quản lý cửa hàng, duyệt đơn, xem tồn kho chi nhánh |
| **CASHIER** | `cashier@dahdt.com` | `password123` | `STORE-Q1` (Nguyễn Trãi - Q1) | Đăng nhập POS, quét mã bán hàng tại quầy |
| **CUSTOMER** | `customer@dahdt.com` | `password123` | — | Đăng nhập Website Shop, mua hàng, áp voucher |

## Cấu trúc dự án

Dự án tổ chức theo **module nghiệp vụ** (feature-based):

```
src/main/java/com/dahdt/ecommerce/
├── config/          # Cấu hình: Security, Swagger, CORS...
├── common/          # Code dùng chung: exception handler, API response, tiện ích
└── modules/         # Từng module nghiệp vụ (auth, catalog, order...)
    └── <module>/
        ├── controller/   # Nhận request, trả response
        ├── service/      # Logic nghiệp vụ
        ├── repository/   # Truy vấn CSDL
        ├── entity/       # Ánh xạ bảng CSDL
        ├── dto/          # Dữ liệu vào/ra API
        └── mapper/       # Chuyển đổi Entity ↔ DTO
```

## Tài liệu tham khảo

- `PROJECT_BRIEF.md` — đặc tả kiến trúc và quy ước dự án
- `thiet-ke-db-ecommerce-b2c.md` — thiết kế CSDL 40+ bảng theo module
