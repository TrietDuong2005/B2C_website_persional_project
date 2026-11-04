# NHẬT KÝ THAY ĐỔI DỰ ÁN (PROJECT CHANGE LOG)
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG THỜI TRANG (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Tài liệu phục vụ:** Báo cáo Quản lý dự án phần mềm & Báo cáo Phát triển phần mềm hướng đối tượng  
> **Quy trình áp dụng:** Quy trình kiểm soát thay đổi tích hợp (Integrated Change Control - PMBOK)  
> **Ngày lập:** 03/11/2026 (Cập nhật liên tục trong suốt 14 ngày dự án)  
> **Người quản lý thay đổi:** Chủ dự án (Project Owner)  

---

## 1. QUY TRÌNH KIỂM SOÁT THAY ĐỔI (CHANGE CONTROL PROCESS)

Để đảm bảo dự án kiểm soát chặt chẽ phạm vi (Zero Scope Creep), bảo vệ đường găng và không bị vỡ kế hoạch trong 14 ngày, mọi thay đổi so với kế hoạch cơ sở (Baseline) đều phải tuân thủ nghiêm ngặt quy trình 6 bước:

```
[1. Đề xuất CR] ──> [2. Phân tích Tác động] ──> [3. Đánh giá & Phê duyệt]
                             │                            │
                             ▼                            ▼
[6. Đóng CR & Log] <── [5. Nghiệm thu & DoD] <── [4. Cập nhật Baseline & Thực thi]
```

1. **Bước 1 - Tiếp nhận Yêu cầu Thay đổi (Change Request - CR):** Ghi nhận mã hiệu, ngày phát sinh, nguồn gốc đề xuất và mô tả chi tiết nội dung thay đổi.
2. **Bước 2 - Phân tích Tác động Toàn diện (Impact Analysis):**
   - *Phạm vi (Scope):* Tăng/giảm bao nhiêu bảng, bao nhiêu màn hình hoặc API? Có ảnh hưởng đến 24 bảng Must không?
   - *Lịch trình (Schedule):* Ảnh hưởng thế nào đến đường găng (Critical Path) và mốc Code Freeze N12?
   - *Công sức & Chi phí (Effort/Cost):* Tăng thêm hay tiết kiệm bao nhiêu giờ công? Có phát sinh giờ công lãng phí (Sunk Cost)?
   - *Rủi ro (Risks):* Có làm tăng điểm số của rủi ro hiện có hoặc làm nảy sinh rủi ro mới trong Sổ rủi ro không?
3. **Bước 3 - Đánh giá & Phê duyệt:** Chủ dự án xem xét và phê duyệt hoặc bác bỏ yêu cầu thay đổi.
4. **Bước 4 - Cập nhật Tài liệu Cơ sở & Triển khai:** Đồng bộ hóa thay đổi vào `PROJECT_BRIEF.md`, WBS, Sổ rủi ro và các sơ đồ kiến trúc trước khi sinh mã.
5. **Bước 5 - Kiểm tra & Nghiệm thu (Verification & DoD):** Chạy `mvn clean compile` đạt `BUILD SUCCESS` và đáp ứng tiêu chí DoD tương ứng.
6. **Bước 6 - Đóng Yêu cầu & Cập nhật Nhật ký:** Cập nhật trạng thái cuối cùng vào bảng Nhật ký thay đổi này.

---

## 2. BẢNG NHẬT KÝ THAY ĐỔI CHI TIẾT (CHANGE LOG TABLE)

| Mã CR | Ngày | Nội dung thay đổi | Lý do thay đổi | Ảnh hưởng tới phạm vi / lịch trình / rủi ro / chi phí | Người duyệt | Trạng thái |
|:---:|:---:|---|---|---|:---:|:---:|
| **CR-01** | 03/11/2026 | Chuyển đổi mô hình quản lý thuộc tính sản phẩm từ **thuộc tính động EAV** (`attributes`, `attribute_values`, `product_attribute_values`) sang **lưu trực tiếp cột `color` và `size`** trong bảng `product_variants`. | Rút ngắn thời gian phát triển; đơn giản hóa logic lọc/tìm kiếm sản phẩm trên Shop; giảm tối đa độ phức tạp của màn hình thêm/sửa sản phẩm Admin trong thời hạn 14 ngày. | - **Phạm vi:** Giảm từ hơn 40 bảng (v1) xuống 24 bảng Must + 4 Should/Could; loại bỏ hoàn toàn các bảng EAV.<br>- **Lịch trình & Mã nguồn:** Mã nguồn catalog đã sinh ở giai đoạn 2 cũ bị loại bỏ; xóa bỏ các migration nháp cũ.<br>- **Giờ công bỏ đi (Sunk Cost):** `[... giờ]` *(để trống cho người thực hiện tự điền số liệu thực tế)*.<br>- **Rủi ro:** Phát sinh rủi ro RSK-16 (xung đột code cũ với schema mới); đã ban hành Quy tắc 11 & 12 tại Mục 13 brief để cô lập và lên kế hoạch xóa code cũ ở Ngày 3A. | Chủ dự án | **ĐÃ PHÊ DUYỆT & ĐÃ ÁP DỤNG** |
| **CR-02** | 03/11/2026 | 1. Chuyển đổi bài toán từ **B2C trực tuyến thuần túy** sang **Hệ thống Chuỗi cửa hàng bán lẻ kết hợp POS tại quầy và Shop Online**.<br>2. Thay thế giao diện **Single Page App Vue 3 tách riêng** bằng **Thymeleaf + Bootstrap 5 tích hợp trực tiếp trong Spring Boot MVC** (gom thành 1 repo duy nhất). | 1. Đáp ứng yêu cầu bài toán thực tế của học phần Quản lý dự án phần mềm (quản lý chuỗi chi nhánh và điểm bán lẻ POS tại quầy).<br>2. Tối ưu nguồn lực 1 người làm: gộp 2 môn học (HDT và QLDA) vào chung 1 sản phẩm phần mềm, viết 2 báo cáo riêng biệt.<br>3. Giảm chi phí tích hợp, loại bỏ cấu hình CORS phức tạp và rủi ro vận hành 2 repository độc lập. | - **Phạm vi:** Bổ sung các bảng nghiệp vụ: `stores`, `employees`, `pos_shifts`, kênh POS trong `orders`, sổ cái kho `inventory_transactions`.<br>- **Kiến trúc:** Chuyển sang kiến trúc Monolith tinh gọn 1 repo; thiết lập 2 SecurityFilterChain (Stateless JWT cho `/api/**` và Form/Session cho Web).<br>- **Lịch trình:** Tái cấu trúc toàn bộ kế hoạch từ 10 giai đoạn mở rộng sang lộ trình 14 ngày làm việc tập trung.<br>- **Rủi ro:** Loại bỏ rủi ro lệch pha API giữa Frontend/Backend; tăng nhẹ rủi ro cấu hình Spring Security 2 chain (RSK-01, RSK-03). | Chủ dự án & Giảng viên | **ĐÃ PHÊ DUYỆT & ĐÃ ÁP DỤNG** |
| **CR-03** | 04/11/2026 | Thu hẹp ngành hàng kinh doanh sang **giày dép**, đặt tên thương hiệu chuỗi **"Triết Store"**, tập trung cấu hình thương hiệu và theme tại một nơi (`app.brand.name`, `theme.css`). | 1. Thống nhất đề tài cho 2 môn học (HDT và QLDA).<br>2. Dữ liệu mẫu tinh gọn, dễ kiểm soát (30 sản phẩm cha, 120 biến thể).<br>3. Mô hình thuộc tính 2 trục `color` (màu sắc) và `size` (kích cỡ số EU 36–45) hoàn toàn phù hợp và phản ánh tự nhiên ngành hàng giày dép. | - **Phạm vi:** Không đổi schema CSDL (giữ nguyên cấu trúc 24 bảng Must + 4 Should/Could), không thêm/bớt tính năng hay nghiệp vụ.<br>- **Dữ liệu mẫu:** Viết lại toàn bộ seed V28 (tên cửa hàng Triết Store), V29 (danh mục 2 cấp Giày/Dép, 8 thương hiệu, 30 sản phẩm, 120 biến thể, 60 ảnh, 480 dòng tồn kho), V30 (voucher).<br>- **Tài liệu:** Cập nhật bối cảnh và thuật ngữ trong `01`, `02`, `06`, `README.md`.<br>- **Giờ công thực tế:** `[... giờ]` *(để trống cho người thực hiện tự điền số liệu thực tế)*. | Chủ dự án | **ĐÃ PHÊ DUYỆT & ĐÃ ÁP DỤNG** |

---

## 3. PHÂN TÍCH CHI TIẾT TÁC ĐỘNG CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 3.1 Phân tích Thay đổi CR-01: Bỏ mô hình EAV, chuyển sang cột cố định `color`/`size`
- **Bối cảnh phát sinh:** Bản thiết kế v1 ban đầu hướng tới hệ thống thương mại điện tử tổng quát nên sử dụng mô hình EAV (Entity - Attribute - Value) với các bảng `attributes`, `attribute_values`, `product_attribute_values`. Mô hình này cho phép tạo thuộc tính tùy ý cho từng danh mục sản phẩm.
- **Vấn đề gặp phải:**
  1. Trong hệ thống thời trang chuỗi, các thuộc tính thương mại cốt lõi luôn xoay quanh **Màu sắc** và **Kích cỡ**. Việc duy trì mô hình EAV biến các câu truy vấn lọc sản phẩm thành chuỗi lệnh `JOIN` nhiều tầng rất nặng nề đối với SQL Server.
  2. Giao diện quản trị tạo mới sản phẩm biến thể theo EAV đòi hỏi xử lý JavaScript động phức tạp, vượt quá năng lực thời gian trong khung 14 ngày của 1 lập trình viên.
- **Giải pháp và Tác động:**
  - Chuyển `color` và `size` thành 2 cột dữ liệu trực tiếp trong bảng `product_variants`.
  - Quyết định này giúp loại bỏ 3 bảng trung gian, đơn giản hóa biểu mẫu Thymeleaf tạo biến thể ở Ngày 3B, và đảm bảo truy vấn SQL lấy tồn kho theo biến thể chạy với tốc độ cao nhất.
  - **Bài học kinh nghiệm quản lý dự án:** Cần dứt khoát loại bỏ mã nguồn thử nghiệm cũ không còn tương thích (RSK-16) để tránh gây nhiễu cho AI trong các phiên sinh mã kế tiếp.

### 3.2 Phân tích Thay đổi CR-02: Gộp 2 môn học, chuyển sang Chuỗi + POS và dùng Thymeleaf Monolith
- **Bối cảnh phát sinh:** Ban đầu dự án được định hình là một ứng dụng bán hàng B2C trực tuyến với Backend Spring Boot và Frontend Vue 3 độc lập. Tuy nhiên, bài toán môn Quản lý dự án phần mềm yêu cầu mô hình kinh doanh có quy mô chuỗi chi nhánh và điểm bán hàng vật lý (POS).
- **Vấn đề gặp phải:**
  1. Việc phát triển song song 2 đồ án độc lập cho 2 môn học HDT và QLDA cùng lúc với dự án Flutter khác sẽ gây quá tải nghiêm trọng nguồn lực (vượt quá 5 giờ/ngày).
  2. Việc duy trì 2 repository riêng biệt (Spring Boot API + Vue 3 SPA) đòi hỏi cấu hình máy chủ web kép, quản lý trạng thái token trên trình duyệt phức tạp và tăng gấp đôi công sức kiểm thử.
- **Giải pháp và Tác động:**
  - Quyết định sáp nhập: Xây dựng **một sản phẩm phần mềm duy nhất** phục vụ trọn vẹn cả 2 môn học; viết 2 bản báo cáo với trọng tâm khác nhau:
    - Báo cáo HDT tập trung sâu vào Kiến trúc phân lớp, Domain Model giàu nghiệp vụ và 4 Mẫu thiết kế hướng đối tượng (Strategy, State, Factory, Observer).
    - Báo cáo QLDA tập trung vào WBS, ước lượng PERT, CPM, Sổ rủi ro thực tế, Nhật ký thay đổi và Kiểm soát nợ kỹ thuật.
  - Chuyển toàn bộ giao diện sang Thymeleaf kết hợp Bootstrap 5: Gom toàn bộ dự án vào một repository duy nhất. Điều này giúp loại bỏ hoàn toàn các lỗi liên quan đến CORS, đồng bộ trạng thái đăng nhập bằng Session tiện lợi và rút ngắn đáng kể thời gian đóng gói bàn giao.

### 3.3 Phân tích Thay đổi CR-03: Thu hẹp ngành hàng sang giày dép và thương hiệu "Triết Store"
- **Bối cảnh phát sinh:** Đề tài ban đầu sử dụng danh mục "Thời trang tổng hợp" (gồm cả quần, áo, váy, ba lô, đồng hồ, nón...), dẫn đến việc dữ liệu mẫu bị dàn trải, khó kiểm soát chất lượng dữ liệu và khó chuẩn hóa bộ thuộc tính biến thể (size số của quần jean khác size chữ của áo, khác size phụ kiện).
- **Lý do thay đổi:**
  1. Thống nhất đề tài thực tế cho 2 môn học, xây dựng thương hiệu bán lẻ cụ thể mang tên **"Triết Store"**.
  2. Ngành hàng giày dép có đặc thù phân loại kích cỡ cực kỳ chuẩn mực theo bảng size EU (hai chữ số: 36, 37, ..., 45), giúp sắp xếp và hiển thị nhất quán trên cả quầy POS lẫn Shop Online.
  3. Dữ liệu mẫu gọn gàng với 30 sản phẩm cha và 120 biến thể phân bổ đều trên 8 danh mục lá (Sneaker, Giày da, Giày boots, Giày lười, Giày thể thao, Sandal, Dép kẹp, Dép slipper), giúp quá trình demo và kiểm thử tự động đạt độ tin cậy cao.
- **Giải pháp và Tác động:**
  - Giữ nguyên 100% cấu trúc lược đồ CSDL v2 (V1–V27 không thay đổi); chỉ viết lại các file migration dữ liệu mẫu V28, V29, V30 (ngoại lệ có chủ ý đã được phê duyệt).
  - Tách bạch cấu hình thương hiệu (`app.brand.name: "Triết Store"`) đưa vào qua `@ControllerAdvice` và bảng màu theme tập trung trong `theme.css`.
  - Không phát sinh nợ kỹ thuật hay làm biến động đường găng của dự án.

---

## 4. MA TRẬN THEO DÕI THAY ĐỔI VỚI CÁC TÀI LIỆU DỰ ÁN (TRACEABILITY MATRIX)

| Mã CR | Tài liệu cập nhật chính | Mã WBS liên quan | Mã Rủi ro liên quan | Trạng thái đồng bộ tài liệu |
|:---:|---|:---:|:---:|:---:|
| **CR-01** | `PROJECT_BRIEF.md` (Mục 0, 7, 8)<br>`docs/01-mo-ta-bai-toan.md` (Mục 3)<br>`docs/06-thiet-ke-csdl-v2.md` | WBS 2.1, 2.2, 3.2 | RSK-16 | **Đã đồng bộ 100%** |
| **CR-02** | `PROJECT_BRIEF.md` (Mục 0, 1, 2, 8, 9, 14)<br>`docs/01-mo-ta-bai-toan.md`<br>`docs/02-use-case.md`<br>`docs/03-wbs-va-ke-hoach.md` | WBS 3.1, 4.0, 8.0, 9.0 | RSK-01, RSK-03, RSK-14, RSK-15 | **Đã đồng bộ 100%** |
| **CR-03** | `PROJECT_BRIEF.md` (Mục 1, 15)<br>`docs/01-mo-ta-bai-toan.md`<br>`docs/02-use-case.md`<br>`docs/06-thiet-ke-csdl-v2.md`<br>`README.md`<br>`docs/kiem-tra-csdl.sql` | WBS 3.3 (Ngày 3C) | RSK-02 | **Đã đồng bộ 100%** |

