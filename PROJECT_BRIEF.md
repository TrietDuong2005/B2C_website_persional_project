# PROJECT BRIEF — WEBSITE THƯƠNG MẠI ĐIỆN TỬ B2C
### Spring Boot MVC + SQL Server — Tài liệu để AI khởi tạo dự án

> **Cách dùng file này:** đây là bản đặc tả (spec) đầy đủ để dán cho AI coding (Claude Code, GitHub Copilot Chat, Cursor...) trong IntelliJ, giúp AI hiểu rõ kiến trúc, quy ước và phạm vi để sinh code **đồng nhất từ đầu đến cuối dự án** — tránh tình trạng mỗi lần hỏi AI lại code theo 1 kiểu khác nhau. Đặt file này ở gốc repo, tên gợi ý: `PROJECT_BRIEF.md`.
>
> **Tài liệu đi kèm (đặt cùng thư mục `/docs` trong repo):**
> - `thiet-ke-db-ecommerce-b2c.md` — thiết kế đầy đủ 40+ bảng CSDL theo module
> - `tu-dien-du-lieu.md` — từ điển dữ liệu chi tiết (tên trường, kiểu dữ liệu, ràng buộc) cho nhóm bảng lõi
> - `ecommerce_erd.puml` / `ecommerce_erd_core.puml` — sơ đồ ERD
>
> Khi giao việc cho AI, luôn nhắc AI đọc các file trên trước khi sinh code Entity/Migration.

---

## 1. TỔNG QUAN DỰ ÁN

- **Loại dự án:** Website thương mại điện tử B2C (bán lẻ trực tiếp cho người tiêu dùng), ví dụ minh hoạ: shop thời trang.
- **Vai trò người dùng:** Khách hàng (customer), Nhân viên vận hành (staff/warehouse), Quản trị viên (admin) — dùng chung bảng `users`, phân biệt qua `roles`.
- **Phạm vi CSDL:** Bản thiết kế đầy đủ hơn 40 bảng, chia theo 13 module (xem chi tiết trong `thiet-ke-db-ecommerce-b2c.md`): User & Auth, Catalog, Pricing & Promotion, Inventory, Cart & Wishlist, Order & Payment, Shipping, Review & QnA, Return/Refund/Warranty, Loyalty, Notification & Support, CMS/Marketing, Admin/RBAC, Analytics.
- **Mục tiêu học tập:** Người thực hiện (chủ dự án) là sinh viên mới bắt đầu lại với lập trình — **code phải rõ ràng, có comment giải thích, đặt tên nhất quán**, để có thể đọc lại và tự học được sau khi AI sinh xong, không chỉ để "chạy được".

---

## 2. CÔNG NGHỆ SỬ DỤNG (TECH STACK)

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| Ngôn ngữ | Java 17+ (LTS) | |
| Framework | Spring Boot 3.x | Kiến trúc MVC |
| Build tool | Maven | dùng `pom.xml` |
| CSDL | Microsoft SQL Server | driver `mssql-jdbc` |
| ORM | Spring Data JPA (Hibernate) | |
| Quản lý migration CSDL | Flyway (hoặc Liquibase) | **bắt buộc** — không dùng `ddl-auto: update` cho production, để có lịch sử thay đổi schema rõ ràng, dễ học |
| Mapping Entity ↔ DTO | MapStruct | tránh viết converter tay dễ sai sót |
| Validation | Jakarta Bean Validation (`@Valid`, `@NotNull`...) | |
| Bảo mật | Spring Security + JWT | cho đăng nhập/phân quyền |
| Tài liệu API | springdoc-openapi (Swagger UI) | để tự test API bằng giao diện web, không cần Postman ngay từ đầu |
| Log | SLF4J + Logback (có sẵn trong Spring Boot) | |
| Test | JUnit 5 + Mockito | |
| Quản lý mã nguồn | Git, theo Conventional Commits (xem mục 8) | |

**Vì sao chọn các công nghệ này:** đây là bộ công nghệ phổ biến nhất trong hệ sinh thái Spring Boot hiện nay, nhiều tài liệu/tutorial tiếng Việt, dễ tìm câu trả lời khi gặp lỗi — quan trọng với người mới.

---

## 3. KIẾN TRÚC PHÂN LỚP (LAYERED ARCHITECTURE)

Mỗi chức năng đi qua đúng 5 lớp theo thứ tự sau, **không được bỏ qua lớp hoặc gọi tắt** (ví dụ Controller không được gọi thẳng Repository):

```
Controller  →  Service (interface)  →  ServiceImpl  →  Repository  →  Database
     ↑                                        ↓
   DTO (request/response)              Entity (bảng CSDL)
     ↑                                        ↓
              Mapper (MapStruct) chuyển đổi qua lại
```

- **Controller**: chỉ nhận request, validate đầu vào, gọi Service, trả response. Không chứa logic nghiệp vụ.
- **Service (interface) + ServiceImpl**: chứa toàn bộ logic nghiệp vụ (vd: kiểm tra tồn kho trước khi đặt hàng).
- **Repository**: interface kế thừa `JpaRepository`, chỉ chứa truy vấn dữ liệu.
- **Entity**: ánh xạ 1-1 với bảng CSDL (theo đúng file thiết kế CSDL).
- **DTO**: dữ liệu ra/vào API, **không bao giờ trả thẳng Entity ra ngoài API** (tránh lộ dữ liệu nhạy cảm như `password_hash`, tránh vòng lặp JSON khi Entity có quan hệ 2 chiều).
- **Mapper**: dùng MapStruct để chuyển Entity ↔ DTO, tránh code tay lặp lại.
- **Exception**: dùng `@ControllerAdvice` + `@ExceptionHandler` tập trung 1 chỗ (`GlobalExceptionHandler`), không try-catch rải rác từng Controller.

---

## 4. CẤU TRÚC THƯ MỤC (PACKAGE STRUCTURE)

Tổ chức theo **module nghiệp vụ** (feature-based), không tổ chức theo layer toàn cục — lý do: khi dự án lớn (40+ bảng), tổ chức theo layer (1 package `controller` chứa 40 file) sẽ rất khó tìm, còn tổ chức theo module giúp mở từng module độc lập, dễ học từng phần:

```
src/main/java/com/<tencongty>/ecommerce/
│
├── config/                    # Cấu hình chung: SecurityConfig, SwaggerConfig, CorsConfig...
├── common/                    # Dùng chung toàn hệ thống
│   ├── exception/              # GlobalExceptionHandler, các custom Exception
│   ├── response/               # ApiResponse<T> - wrapper chuẩn cho mọi API
│   └── util/                   # Hàm tiện ích chung
│
├── modules/
│   ├── auth/                   # Đăng ký, đăng nhập, JWT, phân quyền
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── mapper/
│   ├── user/                   # user_profiles, addresses
│   ├── catalog/                # categories, brands, products, product_variants, attributes...
│   ├── inventory/               # warehouses, inventories, purchase_orders
│   ├── promotion/               # promotions, vouchers, flash_sales
│   ├── cart/                    # carts, cart_items, wishlists
│   ├── order/                   # orders, order_items, order_status_history
│   ├── payment/                 # payments, payment_methods, refunds
│   ├── shipping/                # shipments, shipping_carriers, shipping_zones
│   ├── review/                  # reviews, product_questions
│   ├── returnwarranty/          # return_requests, warranties
│   ├── loyalty/                 # loyalty_points, membership_tiers
│   ├── notification/            # notifications, support_tickets
│   └── cms/                     # banners, cms_pages, blog_posts
│
└── EcommerceApplication.java   # Class main
```

**Quy tắc:** mỗi module là 1 lát cắt dọc đầy đủ 5 lớp — muốn hiểu chức năng "đặt hàng" hoạt động thế nào, chỉ cần mở đúng thư mục `modules/order/`, không phải nhảy qua lại nhiều package rời rạc.

---

## 5. QUY ƯỚC ĐẶT TÊN & CODING CONVENTION

**Bắt buộc AI tuân thủ tuyệt đối các quy ước sau trên toàn bộ dự án (không được đổi cách đặt tên giữa chừng):**

| Đối tượng | Quy ước | Ví dụ |
|---|---|---|
| Tên bảng CSDL | snake_case, số nhiều | `product_variants`, `order_items` |
| Tên class Entity | PascalCase, số ít | `ProductVariant`, `OrderItem` |
| Tên biến/thuộc tính Java | camelCase | `productName`, `totalAmount` |
| Tên DTO | Hậu tố rõ mục đích | `ProductRequestDTO`, `ProductResponseDTO` |
| Tên Controller | Hậu tố `Controller` | `ProductController` |
| Tên Service interface | Hậu tố `Service` | `ProductService` |
| Tên Service implement | Hậu tố `ServiceImpl` | `ProductServiceImpl` |
| Tên Repository | Hậu tố `Repository` | `ProductRepository` |
| Endpoint API | REST chuẩn, danh từ số nhiều, kebab-case nếu nhiều từ | `/api/v1/product-variants` |
| HTTP method | GET (đọc), POST (tạo), PUT (sửa toàn bộ), PATCH (sửa 1 phần), DELETE (xoá) | |
| Response trả về | Luôn bọc trong `ApiResponse<T>` gồm `code`, `message`, `data` | xem mẫu bên dưới |

**Mẫu response chuẩn (bắt buộc áp dụng cho mọi API):**
```json
{
  "code": 200,
  "message": "Lấy danh sách sản phẩm thành công",
  "data": { ... }
}
```

**Comment code:** mỗi class Service, mỗi hàm xử lý nghiệp vụ phức tạp (vd: tính phí ship, trừ tồn kho khi đặt hàng) **bắt buộc có comment tiếng Việt giải thích logic**, vì mục tiêu là để người mới đọc lại hiểu được, không chỉ để chạy đúng.

---

## 6. CẤU HÌNH KẾT NỐI SQL SERVER

Yêu cầu AI tạo sẵn 2 file cấu hình theo profile (để không lẫn cấu hình máy cá nhân với môi trường khác sau này):

- `application.yml` (cấu hình chung, không chứa thông tin nhạy cảm)
- `application-dev.yml` (thông tin kết nối DB thật, **thêm vào `.gitignore`**, không commit lên Git)

Thông tin cần có trong `application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=ecommerce_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: <mật khẩu của bạn>
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: validate   # KHÔNG dùng "update" — schema phải được tạo qua Flyway migration
    show-sql: true         # bật để người mới nhìn thấy câu SQL thật Hibernate sinh ra, phục vụ học
  flyway:
    enabled: true
    locations: classpath:db/migration
```

**Vì sao bắt buộc dùng Flyway thay vì để Hibernate tự tạo bảng (`ddl-auto: update`):** khi dự án có 40+ bảng với nhiều ràng buộc khoá ngoại phức tạp, để Hibernate tự sinh bảng dễ sai lệch so với thiết kế đã duyệt và **không có lịch sử thay đổi** — mỗi lần đổi schema, bạn viết 1 file migration mới (`V1__create_users_table.sql`, `V2__create_products_table.sql`...), giúp bạn **đọc lại được từng bước schema được xây dựng như thế nào**, rất tốt cho việc học.

---

## 7. LỘ TRÌNH XÂY DỰNG (BẮT BUỘC LÀM THEO THỨ TỰ)

Không yêu cầu AI code toàn bộ 40+ bảng cùng lúc trong 1 lần — chia theo giai đoạn (mỗi giai đoạn AI sinh code xong, bạn chạy thử, hiểu rồi mới sang giai đoạn tiếp theo):

| Giai đoạn | Module | Kết quả cần đạt |
|---|---|---|
| 1 | `auth`, `user` | Đăng ký, đăng nhập trả JWT, xem/sửa thông tin cá nhân, quản lý địa chỉ |
| 2 | `catalog` | CRUD danh mục, thương hiệu, sản phẩm, biến thể, thuộc tính; API xem danh sách/lọc/chi tiết sản phẩm |
| 3 | `inventory` | Quản lý kho, nhập hàng, trừ/cộng tồn kho |
| 4 | `cart` | Thêm/sửa/xoá giỏ hàng, đồng bộ giỏ khi đăng nhập |
| 5 | `order` | Đặt hàng từ giỏ hàng, snapshot dữ liệu, cập nhật trạng thái đơn |
| 6 | `payment` | Tích hợp thanh toán (COD trước, sau đó thêm cổng thanh toán online) |
| 7 | `shipping` | Tính phí ship, tạo vận đơn, tra cứu trạng thái giao hàng |
| 8 | `review` | Đánh giá sản phẩm sau khi mua |
| 9 | `promotion` | Voucher, khuyến mãi, flash sale |
| 10 | Các module còn lại | `returnwarranty`, `loyalty`, `notification`, `cms`, `admin` — làm sau khi đã quen luồng chính |

**Yêu cầu AI:** khi bắt đầu 1 giai đoạn mới, luôn tóm tắt lại các bảng liên quan, luồng dữ liệu đi qua các lớp như thế nào trước khi sinh code — giống cách giải thích ở tài liệu `giai-thich-db-shop-thoi-trang.md`.

---

## 8. QUY ƯỚC GIT

- Nhánh chính: `main` (chỉ merge code đã chạy được).
- Nhánh phát triển theo module: `feature/auth`, `feature/catalog`, `feature/order`...
- Commit message theo Conventional Commits: `feat: thêm API đăng ký tài khoản`, `fix: sửa lỗi tính sai phí ship`, `docs: cập nhật README`.
- Mỗi giai đoạn ở mục 7 tương ứng 1 Pull Request riêng để dễ đọc lại lịch sử phát triển.

---

## 9. YÊU CẦU KIỂM THỬ TỐI THIỂU

- Mỗi Service quan trọng (đặt hàng, trừ kho, tính phí ship, áp voucher) cần có ít nhất 1 Unit Test bằng JUnit + Mockito.
- Dùng Swagger UI (`/swagger-ui.html`) để tự test API bằng tay sau khi API được sinh ra, không cần cài Postman ngay.

---

## 10. YÊU CẦU BỔ SUNG VỀ TÍNH MỞ RỘNG (THEO CÁC TRAO ĐỔI TRƯỚC)

- Bảng `addresses` và `warehouses` cần có sẵn cột `latitude`, `longitude` để sau này tích hợp tính phí ship theo khoảng cách địa lý (GIS) — xem lại phần trao đổi trước về Hibernate Spatial/PostGIS nếu muốn nâng cấp (lưu ý: SQL Server cũng hỗ trợ kiểu dữ liệu không gian `geography`, có thể dùng thay vì 2 cột số nếu muốn làm chuẩn hơn).
- Mô hình sản phẩm bắt buộc theo đúng nguyên tắc SPU/SKU (`products` tách khỏi `product_variants`) và hệ thuộc tính động (`attributes`/`attribute_values`) đã thống nhất — **không để AI tự ý gộp lại thành 1 bảng phẳng có cột `color`, `size` cứng**.
- `order_items` bắt buộc lưu snapshot (`product_name_snapshot`, `variant_attributes_snapshot`, `unit_price`) — không được chỉ lưu `variant_id` rồi join sang bảng sản phẩm để lấy giá hiện tại.

---

## 11. CÂU LỆNH/PROMPT MẪU ĐỂ GIAO VIỆC CHO AI TỪNG GIAI ĐOẠN

Bạn có thể copy mẫu dưới đây, chỉ đổi tên module, để giao việc rõ ràng từng giai đoạn thay vì nói chung chung "code hết cho tôi":

```
Đọc file PROJECT_BRIEF.md và docs/thiet-ke-db-ecommerce-b2c.md trước khi làm.
Nhiệm vụ: xây dựng module "catalog" (giai đoạn 2 trong lộ trình) gồm các bảng:
categories, brands, products, product_variants, attributes, attribute_values,
variant_attribute_values, product_images.
Yêu cầu: đủ 5 lớp (Entity, Repository, DTO, Mapper, Service, Controller),
migration Flyway tương ứng, tuân thủ đúng quy ước đặt tên ở mục 5 của PROJECT_BRIEF.md.
Sau khi xong, giải thích ngắn gọn từng file vừa tạo dùng để làm gì.
```

---

## 12. GHI CHÚ CUỐI

Tài liệu này là "hợp đồng" giữa bạn và AI trong suốt dự án — mỗi lần mở phiên làm việc mới với AI, hãy dán lại (hoặc trỏ tới) file này để AI không code lệch quy ước so với các phần đã làm trước đó.
