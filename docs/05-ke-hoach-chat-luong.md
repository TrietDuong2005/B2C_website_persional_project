# KẾ HOẠCH ĐẢM BẢO CHẤT LƯỢNG PHẦN MỀM (SQA PLAN)
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG THỜI TRANG (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Tài liệu phục vụ:** Báo cáo Quản lý dự án phần mềm & Báo cáo Phát triển phần mềm hướng đối tượng  
> **Ngày ban hành:** 03/11/2026  
> **Mục tiêu:** Thiết lập các tiêu chuẩn chất lượng, quy trình nghiệm thu kỹ thuật và kỷ luật làm việc để đảm bảo sản phẩm đạt chất lượng cao nhất trong thời hạn 14 ngày.  

---

## 1. ĐỊNH NGHĨA "HOÀN THÀNH" (DEFINITION OF DONE - DoD)

Để đảm bảo mỗi ngày làm việc đều tạo ra một lát cắt sản phẩm chạy được và không tích tụ nợ kỹ thuật, dự án áp dụng Định nghĩa Hoàn thành ở 2 cấp độ:

### 1.1 DoD cho Từng Ngày / Lát cắt Chức năng (Daily Slice DoD)
Một gói công việc hàng ngày chỉ được coi là "Hoàn thành" khi thỏa mãn đồng thời tất cả các tiêu chí sau:

1. **Tuân thủ Chuẩn mực Mã nguồn:**
   - Đặt tên bảng, Entity, DTO, Service, Repository, Controller và View đúng 100% quy ước tại Mục 5 của `PROJECT_BRIEF.md`.
   - Có comment tiếng Việt đầy đủ cho mọi Service interface, mọi hàm nghiệp vụ không tầm thường và mọi class hiện thực Design Pattern (giải thích rõ tên pattern, vai trò và lý do dùng để phục vụ báo cáo HDT).
   - Tuyệt đối không trả Entity trực tiếp ra API hoặc đưa trực tiếp ra View Thymeleaf; luôn ánh xạ qua DTO bằng MapStruct.
2. **Biên dịch Thực tế Thành công:**
   - Chạy lệnh `mvn clean compile` trong terminal và kết thúc bằng dòng chữ `BUILD SUCCESS`. Không có cảnh báo nghiêm trọng (fatal warnings).
3. **Khởi động và Tích hợp Cơ sở Dữ liệu Ổn định:**
   - Ứng dụng khởi động bình thường (`mvn spring-boot:run` hoặc chạy qua IDE), không ném bất kỳ ngoại lệ nào lúc nạp ngữ cảnh (`zero startup exceptions`).
   - Cấu hình `spring.jpa.hibernate.ddl-auto: validate` kiểm tra khớp 100% giữa Entity JPA và cấu trúc bảng CSDL do Flyway tạo ra.
4. **Kiểm thử Thủ công Thông suốt:**
   - Chủ dự án đã tự kiểm thử thủ công theo kịch bản chức năng của ngày trên trình duyệt hoặc công cụ Swagger UI (`/swagger-ui/index.html`) và ghi nhận kết quả hoạt động đúng kỳ vọng.
5. **Cập nhật Nhật ký Dự án:**
   - Hoàn thành ghi chép file nhật ký ngày tại `docs/nhat-ky/YYYY-MM-DD.md` (ghi nhận số giờ thực tế, khó khăn, cách xử lý, bài học và ảnh chụp màn hình).

### 1.2 DoD cho Toàn bộ Dự án (Project-level DoD - Tại Mốc M6 Ngày 12)
1. Toàn bộ 24 bảng CSDL Must và các luồng nghiệp vụ tương ứng đã được hiện thực đầy đủ.
2. Bộ Unit Test lõi đạt tỷ lệ vượt qua 100% (`0 failures, 0 errors`).
3. Kịch bản demo tổng thể chạy trơn tru từ A đến Z trên cả 3 phân hệ (Shop -> POS -> Admin).
4. Không có lỗi bảo mật cơ bản (CSRF, IDOR, phân quyền sai vai trò).
5. Hai bản báo cáo học phần HDT và QLDA đã được điền đầy đủ số liệu thực tế, sơ đồ và ảnh chụp minh chứng.

---

## 2. CHIẾN LƯỢC KIỂM THỬ PHẦN MỀM (TESTING STRATEGY)

Trong điều kiện dự án 1 người thực hiện trong 14 ngày, chiến lược kiểm thử áp dụng nguyên lý Pareto: Tập trung kiểm thử tự động vào logic nghiệp vụ lõi (nơi dễ phát sinh sai sót toán học hoặc xung đột trạng thái) và kiểm thử thủ công vào giao diện người dùng và kịch bản demo.

```
       / \
      /   \      Kiểm thử Thủ công (Manual E2E)
     / E2E \     - Duyệt kịch bản Shop Online, POS tại quầy, Admin
    /-------\    --------------------------------------------------
   /  Unit   \   Kiểm thử Tự động (Automated Unit Tests - JUnit 5)
  /   Tests   \  - Tính tiền đơn, Giảm giá Voucher, Trừ/Hoàn kho, State Pattern
 /-------------\
```

### 2.1 Ma trận Phân bổ Kiểm thử

| Hạng mục nghiệp vụ | Phương pháp kiểm thử | Công cụ / Kỹ thuật | Trọng tâm kiểm tra |
|---|:---:|---|---|
| **1. Tính toán tiền đơn hàng** | **Tự động (Unit Test)** | JUnit 5 | `total = subtotal - discount + shipping_fee`, làm tròn `BigDecimal`, không cho tổng âm. |
| **2. Logic giảm giá Voucher** | **Tự động (Unit Test)** | JUnit 5 + Mockito | `PercentDiscountStrategy`, `FixedDiscountStrategy`, chặn khi chưa đạt `min_order_value`, khống chế `max_discount_value`, kiểm tra hạn dùng và số lượt. |
| **3. Trừ kho & Chặn thiếu hàng** | **Tự động (Unit Test)** | JUnit 5 + Mockito | `Inventory.deduct(qty)` ném ngoại lệ khi tồn kho không đủ, kiểm tra số lượng trừ chính xác, ghi sổ cái kho. |
| **4. Chuyển trạng thái đơn hàng** | **Tự động (Unit Test)** | JUnit 5 | Hiện thực State Pattern: kiểm tra chuyển hợp lệ (`PENDING -> CONFIRMED`), chặn các bước chuyển nhảy cóc sai quy trình (`PENDING -> COMPLETED`). |
| **5. Giao diện Shop & Giỏ hàng** | **Thủ công (Manual)** | Trình duyệt Web (Chrome) | Duyệt danh mục, chọn biến thể Màu/Size, thêm giỏ hàng, giao diện hiển thị trên máy tính/mobile. |
| **6. Giao diện Bán hàng POS** | **Thủ công (Manual)** | Trình duyệt Web | Nhập SKU/mã vạch, phím tắt thao tác, tính tiền thừa, hiển thị hóa đơn Print View 80mm. |
| **7. Quản trị Admin & Dashboard** | **Thủ công (Manual)** | Trình duyệt Web | Thao tác đổi trạng thái đơn, lọc báo cáo, hiển thị biểu đồ Chart.js đúng số liệu. |
| **8. Kịch bản Demo End-to-End** | **Thủ công (Manual)** | Kịch bản tổng hợp | Luồng liên hoàn: Khách đặt online -> Kho trừ -> Admin duyệt đơn; Thu ngân bán tại quầy -> Kho trừ -> Admin xem doanh thu. |

---

## 3. TIÊU CHÍ CHẤP NHẬN (ACCEPTANCE CRITERIA - AC) CHO CÁC CHỨC NĂNG MUST

Mỗi chức năng Must được định nghĩa tiêu chí chấp nhận cụ thể theo chuẩn kiểm thử nghiệp vụ:

### AC-01: Xác thực & Phân quyền 4 Vai trò
- **Given:** Người dùng truy cập hệ thống qua URL web hoặc API.
- **When:** Người dùng đăng nhập với thông tin tài khoản hợp lệ.
- **Then:**
  - Khách hàng (`CUSTOMER`) chỉ được vào khu vực `/shop/**`, `/cart`, `/checkout`, `/account/**`.
  - Thu ngân (`CASHIER`) chỉ được vào khu vực `/pos/**`.
  - Quản lý cửa hàng (`STORE_MANAGER`) vào được `/pos/**` và `/admin/**` (nhưng chỉ thấy dữ liệu chi nhánh mình).
  - Quản trị viên (`ADMIN`) có quyền truy cập toàn bộ hệ thống `/admin/**`.
  - Truy cập trái thẩm quyền phải bị chặn với mã lỗi HTTP `403 Forbidden` hoặc chuyển hướng về trang đăng nhập.

### AC-02: Quản lý Danh mục, Thương hiệu, Sản phẩm & Biến thể SKU
- **Given:** Quản trị viên ở màn hình quản lý Catalog.
- **When:** Thêm một Sản phẩm cha và khai báo các Biến thể (ví dụ: Màu Đen - Size L, Màu Trắng - Size M).
- **Then:**
  - Mã SKU của từng biến thể là duy nhất trong toàn hệ thống.
  - Giá bán (`price`) và giá vốn (`cost_price`) lưu đúng kiểu `DECIMAL(18,2)`.
  - Bản ghi `product_variants` được tạo thành công gắn với `product_id`.
  - Khách hàng trên trang Shop có thể chọn màu và size tương ứng để xem đúng giá bán.

### AC-03: Quản lý Tồn kho theo Cửa hàng & Sổ cái Biến động
- **Given:** Một biến thể sản phẩm tồn tại trong hệ thống.
- **When:** Quản trị viên hoặc Quản lý chi nhánh thiết lập số lượng tồn ban đầu hoặc điều chỉnh tồn kho.
- **Then:**
  - Bảng `inventories` ghi nhận cặp duy nhất `(variant_id, store_id)` với số lượng $\ge 0$.
  - Mọi sự thay đổi số lượng bắt buộc phải sinh ra một bản ghi tương ứng trong `inventory_transactions` với đúng loại (`IMPORT`, `SALE`, `CANCEL`, `ADJUST`).
  - Khi hai thao tác cùng cập nhật tồn kho một lúc, cơ chế khóa lạc quan `@Version` phải ngăn chặn việc ghi đè sai dữ liệu.

### AC-04: Phân hệ Shop - Tìm kiếm, Xem chi tiết & Giỏ hàng
- **Given:** Khách hàng đã đăng nhập vào website Shop.
- **When:** Khách hàng tìm kiếm từ khóa, lọc theo danh mục, chọn một biến thể và bấm "Thêm vào giỏ".
- **Then:**
  - Sản phẩm được thêm vào `cart_items` với đúng `variant_id` và số lượng.
  - Đơn giá hiển thị trong giỏ hàng được truy vấn trực tiếp từ bảng `product_variants`, không lấy từ giá gửi lên từ trình duyệt.
  - Nếu số lượng mua vượt quá số lượng tồn kho khả dụng, hệ thống hiển thị thông báo lỗi và không cho thêm.

### AC-05: Shop Online - Đặt hàng Trực tuyến (COD & Voucher)
- **Given:** Khách hàng có sản phẩm trong giỏ hàng và tiến hành checkout.
- **When:** Khách hàng nhập địa chỉ nhận hàng, áp dụng mã voucher hợp lệ và chọn thanh toán COD.
- **Then:**
  - Tiền giảm giá voucher được tính toán chính xác theo công thức của Strategy tương ứng (không vượt quá `max_discount_value`).
  - Đơn hàng được tạo ở trạng thái `PENDING`, kênh `ONLINE`.
  - Snapshot thông tin sản phẩm và địa chỉ được lưu cố định vào `order_items` và `orders`.
  - Sự kiện `OrderPlacedEvent` được kích hoạt lập tức trừ số lượng tồn kho và ghi sổ kho `SALE`.
  - Giỏ hàng của khách được làm trống.

### AC-06: Phân hệ POS - Bán hàng tại quầy, Tính tiền thừa & In hóa đơn
- **Given:** Thu ngân đang mở màn hình POS tại chi nhánh.
- **When:** Thu ngân quét mã SKU/Barcode, áp dụng voucher, chọn thanh toán tiền mặt và nhập tiền khách đưa.
- **Then:**
  - Thông tin mặt hàng hiển thị ngay lập tức với giá chuẩn từ CSDL.
  - Hệ thống tự động tính tiền thừa: `change_amount = cash_received - total_amount`.
  - Khi bấm "Hoàn tất", đơn hàng được tạo với trạng thái `COMPLETED`, kênh `POS`.
  - Tồn kho của cửa hàng tương ứng bị trừ ngay lập tức; giao dịch thanh toán `PAID` được ghi nhận.
  - Cửa sổ in hóa đơn (Print View) kích hoạt hiển thị mẫu hóa đơn bán lẻ rõ ràng.

### AC-07: Admin - Quản lý Đơn hàng Đa kênh & Vòng đời Đơn với State Pattern
- **Given:** Đơn hàng online đang ở trạng thái `PENDING`.
- **When:** Quản trị viên thực hiện chuyển trạng thái tuần tự theo quy trình: `PENDING -> CONFIRMED -> PACKING -> SHIPPING -> COMPLETED`.
- **Then:**
  - Mỗi bước chuyển trạng thái thành công ghi nhận một dòng kiểm toán vào `order_status_history`.
  - Các thao tác chuyển sai quy trình (ví dụ: nhảy từ `PENDING` lên `COMPLETED`) bị chặn và ném ngoại lệ rõ ràng.
  - Nếu đơn hàng bị bấm "Hủy" (`CANCELLED`), hệ thống tự động kích hoạt sự kiện hoàn lại tồn kho và ghi sổ cái kho `CANCEL`.

### AC-08: Admin - Dashboard Thống kê Doanh thu & Cảnh báo Tồn kho
- **Given:** Quản trị viên truy cập màn hình Dashboard.
- **When:** Hệ thống tải dữ liệu tổng hợp.
- **Then:**
  - Hiển thị chính xác tổng doanh thu theo từng chi nhánh, theo từng ngày và theo kênh bán (Online vs POS).
  - Hiển thị danh sách Top 10 sản phẩm có số lượng bán nhiều nhất.
  - Hiển thị danh sách các biến thể có tồn kho $\le$ `reorder_level` kèm tên chi nhánh để quản lý kịp thời bổ sung hàng.

---

## 4. QUY TẮC KIỂM SOÁT THAY ĐỔI PHẠM VI (SCOPE CHANGE CONTROL)

Để ngăn chặn tuyệt đối rủi ro trễ hạn do "phình phạm vi" (Scope Creep), mọi đề xuất thay đổi hoặc bổ sung tính năng trong 14 ngày phải tuân thủ nghiêm ngặt quy trình sau:

```
[Phát sinh ý tưởng / yêu cầu mới]
               │
               ▼
   [Kiểm tra với Mục 7 Brief]
       │                 │
 (Có trong Must?)   (Không có trong Must)
       │                 │
       ▼                 ▼
  [Thực hiện]      [Đánh giá tác động nguồn lực]
                         │
                         ▼
             [Có làm trễ hạn nộp bài?]
                 │               │
               (Có)            (Không & Có phép của PO)
                 │               │
                 ▼               ▼
           [Loại bỏ ngay /   [Xếp vào Should/Could]
            Chuyển vào Won't]
```

1. **Nguyên tắc "Đóng băng Phạm vi Must":** Danh sách 24 bảng CSDL Must và các tính năng Must tại Mục 7 của `PROJECT_BRIEF.md` là bất khả xâm phạm. Tuyệt đối không thêm bất kỳ bảng, cột hay chức năng nào ngoài danh sách này.
2. **Quy tắc Tiếp nhận Ý tưởng Mới:** Mọi ý tưởng phát sinh trong quá trình lập trình (ví dụ: làm thêm tính năng tích điểm, thanh toán VNPay thật, quản lý bảo hành...) tự động được ghi nhận vào danh mục **Mức Won't Have** để trình bày trong phần "Hướng phát triển tương lai" của Báo cáo cuối kỳ.
3. **Quyền Quyết định Duy nhất:** Chỉ có Chủ dự án mới có quyền phê duyệt thay đổi, AI không được tự ý đề xuất mở rộng kiến trúc khi chưa được yêu cầu bằng văn bản.

---

## 5. QUY TẮC BẮT BUỘC KHI LÀM VIỆC VỚI TRỢ LÝ AI

Căn cứ Mục 13 của `PROJECT_BRIEF.md`, các quy tắc sau đây là kỷ luật bắt buộc đối với mọi phiên làm việc với AI:

1. **Không giả định quy ước, phải đọc code thật:** Trước khi sửa hoặc gọi đến file dùng chung (`ApiResponse.java`, `BaseEntity`, `GlobalExceptionHandler`, `SecurityConfig`, các Entity/Service nền tảng), AI bắt buộc phải mở và in nguyên văn nội dung hiện tại của file đó.
2. **Không tự ý đổi quy ước đã có:** Giữ vững cấu trúc thư mục, quy tắc đặt tên (`snake_case` cho DB, `PascalCase` cho Entity, DTO có hậu tố `RequestDTO`/`ResponseDTO`).
3. **Tuyệt đối cấm báo "build thành công" nếu chưa chạy build thật:** Sau khi viết code, AI bắt buộc phải chạy lệnh `mvn clean compile` trong console, hiển thị nguyên văn đoạn log có dòng chữ `BUILD SUCCESS`. Cấm sử dụng các câu khẳng định cảm tính như "tôi tự tin code đúng".
4. **Quy trình khi đổi model AI hoặc mở phiên mới:** Model mới bắt buộc phải đọc lại toàn bộ `PROJECT_BRIEF.md`, tài liệu liên quan trong thư mục `docs/` và mã nguồn hiện có của module đang thực hiện trước khi bắt đầu viết code mới.
5. **Đóng băng các quy ước dùng chung đã chốt:**
   - Cấu trúc `ApiResponse.success(T data, String message)`, `ApiResponse.success(String message)` và `ApiResponse.error(String message, int code)`.
   - Thứ tự plugin trong `pom.xml`: `lombok` $\rightarrow$ `mapstruct-processor` $\rightarrow$ `lombok-mapstruct-binding` (binding luôn đứng cuối cùng).
6. **Không sửa migration đã tồn tại:** Mọi thay đổi về cấu trúc bảng phải tạo file migration mới theo thứ tự số hiệu tăng dần (`V7__...`, `V8__...`). Tuyệt đối không can thiệp vào các migration đã áp dụng vào DB.
7. **Không tự thêm tính năng/bảng/cột ngoài phạm vi:** Tuyệt đối không sinh thêm các bảng phụ ngoài danh mục 24 bảng Must và các bảng Should/Could được cho phép.
8. **Quy trình nghiệm thu lát cắt:** Mỗi lát cắt công việc phải kết thúc bằng: Chạy lệnh build thật + Khởi động app thành công + Hướng dẫn chủ dự án các bước kiểm thử thủ công cụ thể. Không thực hiện lệnh `git commit` trừ khi chủ dự án yêu cầu rõ ràng.
9. **Không bịa đặt số liệu thực tế:** Các số liệu về thời gian thực tế, kết quả kiểm thử thực tế và nhật ký ngày phải để trống để người làm trực tiếp điền số liệu thật.
10. **Minh bạch hóa kết quả sinh mã:** Sau mỗi lần sinh code, AI phải lập danh sách liệt kê tất cả các file đã tạo hoặc chỉnh sửa, kèm theo đúng một dòng giải thích ngắn gọn bằng tiếng Việt dễ hiểu cho từng file.

