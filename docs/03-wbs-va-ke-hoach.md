# CẤU TRÚC PHÂN RÃ CÔNG VIỆC (WBS) VÀ KẾ HOẠCH TIẾN ĐỘ
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG THỜI TRANG (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Tài liệu phục vụ:** Báo cáo Quản lý dự án phần mềm & Báo cáo Phát triển phần mềm hướng đối tượng  
> **Thời gian thực hiện:** 03/11/2026 – 16/11/2026 (Hạn nộp bài: 17/11/2026 — GIẢ ĐỊNH, chưa có thông báo chính thức, ước lượng theo lịch thi các môn khác)  
> **Nguồn lực khả dụng:** 01 người × 14 ngày × 5.0h = 70.0 giờ hiệu quả (không có ngày bận)  
> **Sơ đồ Gantt đính kèm:** [`docs/03-gantt.puml`](file:///c:/Users/Admin/Desktop/workSpace/4_1/DA_HDT/docs/03-gantt.puml)  

---

## 1. CẤU TRÚC PHÂN RÃ CÔNG VIỆC (WBS 3 CẤP)

Cấu trúc WBS bám sát lộ trình 14 ngày ở Mục 12 của `PROJECT_BRIEF.md`:

```
1.0 KHỞI ĐỘNG, ĐẶC TẢ YÊU CẦU & KẾ HOẠCH DỰ ÁN (NGÀY 1)
├── 1.1 Phân tích Nghiệp vụ & Xác lập Phạm vi
│   ├── 1.1.1 Biên soạn mô tả bài toán, bối cảnh chuỗi thời trang & từ điển thuật ngữ
│   └── 1.1.2 Xác lập ma trận phân hạng phạm vi MoSCoW, giả định & ràng buộc
├── 1.2 Mô hình hóa Chức năng (Use Case)
│   ├── 1.2.1 Xây dựng sơ đồ Use Case tổng quát bằng PlantUML
│   └── 1.2.2 Viết đặc tả chi tiết 4 Use Case trọng tâm (POS, Shop, Đổi trạng thái, Tồn kho)
└── 1.3 Quản lý Dự án & Kiểm soát Rủi ro
    ├── 1.3.1 Lập WBS 3 cấp, ước lượng công sức PERT 3 điểm & biểu đồ Gantt
    ├── 1.3.2 Lập Sổ đăng ký rủi ro (Risk Register) bao gồm các sự cố thực tế với AI
    └── 1.3.3 Thiết lập Kế hoạch chất lượng (DoD, Testing Strategy, Scope Control)

2.0 THIẾT KẾ CƠ SỞ DỮ LIỆU, MIGRATION & DỮ LIỆU MẪU (NGÀY 2)
├── 2.1 Thiết kế Lược đồ CSDL v2 & Domain Model
│   ├── 2.1.1 Thiết kế ERD v2 (24 bảng Must + Should/Could) & chuẩn hóa quan hệ
│   └── 2.1.2 Phác thảo sơ đồ lớp thực thể (Rich Domain Model & BaseEntity)
└── 2.2 Hiện thực hóa Database Migration & Seed
    ├── 2.2.1 Viết các tập lệnh Flyway Migration bổ sung (từ V14/V15)
    ├── 2.2.2 Xây dựng DevDataSeeder nạp người dùng mẫu 4 role và danh mục nền
    └── 2.2.3 Kiểm thử thực thi Flyway và đối soát cấu hình ddl-auto: validate

3.0 KIẾN TRÚC LÕI, BẢO MẬT 2 CHAIN & QUẢN LÝ CATALOG (NGÀY 3)
├── 3.1 Cấu hình Bảo mật Spring Security
│   ├── 3.1.1 Thiết lập 2 SecurityFilterChain (Stateless JWT cho API & Session cho Web)
│   └── 3.1.2 Tích hợp phân quyền 4 vai trò & xử lý ngoại lệ truy cập
└── 3.2 Giao diện Quản trị Cơ sở & Quản lý Catalog Sản phẩm
    ├── 3.2.1 Tích hợp layout Admin chuẩn (Bootstrap 5, Thymeleaf fragments)
    └── 3.2.2 Triển khai CRUD Danh mục (cha-con), Thương hiệu, Sản phẩm cha, Biến thể SKU

4.0 QUẢN LÝ CHI NHÁNH CỬA HÀNG, NHÂN SỰ & TỒN KHO ĐA ĐIỂM (NGÀY 4)
├── 4.1 Quản trị Chi nhánh Cửa hàng & Nhân sự
│   ├── 4.1.1 Quản lý danh sách Cửa hàng (địa chỉ, tọa độ GPS, loại hình)
│   └── 4.1.2 Quản lý Nhân viên và phân bổ tài khoản vào từng chi nhánh
└── 4.2 Quản trị Tồn kho & Sổ cái Biến động
    ├── 4.2.1 Quản lý số lượng tồn kho theo chi nhánh kết hợp khóa lạc quan @Version
    └── 4.2.2 Xây dựng sổ cái kho inventory_transactions (append-only)

5.0 PHÂN HỆ CỬA HÀNG TRỰC TUYẾN (SHOP ONLINE) & GIỎ HÀNG (NGÀY 5)
├── 5.1 Giao diện Trưng bày Sản phẩm Khách hàng
│   ├── 5.1.1 Xây dựng trang chủ, danh sách sản phẩm, bộ lọc danh mục và tìm kiếm
│   └── 5.1.2 Trang chi tiết sản phẩm, xem ảnh gallery, chọn biến thể Màu/Size
└── 5.2 Xử lý Giỏ hàng Trực tuyến
    ├── 5.2.1 Xây dựng giỏ hàng cho tài khoản đã đăng nhập (carts, cart_items)
    └── 5.2.2 Đồng bộ giá từ CSDL và kiểm tra số lượng tồn kho khả dụng

6.0 ĐẶT HÀNG TRỰC TUYẾN, THANH TOÁN, VOUCHER & TRỪ KHO (NGÀY 6)
├── 6.1 Màn hình Checkout & Chiến lược Khuyến mãi
│   ├── 6.1.1 Xây dựng màn hình Checkout, chọn địa chỉ nhận hàng
│   ├── 6.1.2 Áp dụng Strategy Pattern cho tính giảm giá Voucher (Percent/Fixed)
│   └── 6.1.3 Áp dụng Strategy Pattern cho phương thức thanh toán (COD, QR/E-Wallet giả lập)
└── 6.2 Xử lý Giao dịch Đặt hàng & Bắn sự kiện Trừ kho
    ├── 6.2.1 Sử dụng OrderFactory khởi tạo đơn PENDING và lưu snapshot sản phẩm
    └── 6.2.2 Ứng dụng Observer Pattern (OrderPlacedEvent) trừ kho và ghi sổ cái tự động

7.0 ĐÁNH GIÁ GIỮA KỲ, TINH GỌN NỢ KỸ THUẬT & GẮN NHÃN v0.5 (NGÀY 7)
├── 7.1 Đánh giá Tiến độ & Ra quyết định Phạm vi
│   ├── 7.1.1 Đối soát bảng theo dõi kế hoạch vs thực tế sau 6 ngày đầu
│   └── 7.1.2 Đánh giá nguy cơ chậm tiến độ và kích hoạt Cut-list (nếu cần)
└── 7.2 Tinh gọn Kỹ thuật & Kiểm thử Tích hợp
    ├── 7.2.1 Rà soát mã nguồn, sửa lỗi N+1 Hibernate, dọn dẹp nợ kỹ thuật
    └── 7.2.2 Kiểm thử thông suốt luồng Shop Online và tạo Git Tag v0.5

8.0 ĐIỂM BÁN HÀNG TẠI QUẦY (POS) - GIAO DIỆN & TÌM KIẾM SKU (NGÀY 8)
├── 8.1 Giao diện Bán hàng POS Chuyên dụng
│   ├── 8.1.1 Thiết kế giao diện POS bán lẻ tối ưu thao tác chuột và phím tắt
│   └── 8.1.2 Tích hợp tìm kiếm nhanh sản phẩm qua từ khóa hoặc quét mã vạch SKU/Barcode
└── 8.2 Giỏ hàng Hóa đơn tại Quầy
    └── 8.2.1 Quản lý danh sách hàng hóa trong hóa đơn quầy, đổi số lượng tức thì

9.0 HOÀN THIỆN POS, THANH TOÁN, IN HÓA ĐƠN & CA THU NGÂN (NGÀY 9)
├── 9.1 Xử lý Thanh toán & Hoàn tất Hóa đơn POS
│   ├── 9.1.1 Tích hợp tính tiền thừa (tiền mặt), xác nhận thanh toán thẻ/QR
│   ├── 9.1.2 Trừ kho chi nhánh trực tiếp, lưu đơn COMPLETED và in hóa đơn (Print View 80mm)
└── 9.2 Quản lý Ca Thu ngân (Mức Should)
    └── 9.2.1 Triển khai mở ca (khai báo tiền đầu ca), chốt ca và đối soát tiền mặt

10.0 QUẢN TRỊ ĐƠN HÀNG ĐA KÊNH & VÒNG ĐỜI ĐƠN HÀNG (NGÀY 10)
├── 10.1 Quản lý Đơn hàng Toàn kênh (Admin)
│   └── 10.1.1 Màn hình quản trị danh sách đơn hàng lọc theo kênh Online/POS, chi nhánh
└── 10.2 Quản lý Vòng đời Đơn hàng với State Pattern
    ├── 10.2.1 Hiện thực hóa State Pattern cho luồng PENDING -> CONFIRMED -> PACKING -> SHIPPING -> COMPLETED
    └── 10.2.2 Xử lý Hủy đơn (CANCELLED) và kích hoạt Observer Pattern tự động hoàn kho

11.0 BÁO CÁO THỐNG KÊ & DASHBOARD PHÂN TÍCH (NGÀY 11)
├── 11.1 Thống kê Doanh thu & Tồn kho
│   ├── 11.1.1 Viết truy vấn tổng hợp doanh thu theo chi nhánh, theo kênh bán và mốc thời gian
│   └── 11.1.2 Thống kê Top 10 sản phẩm bán chạy và danh sách cảnh báo tồn kho thấp
└── 11.2 Trực quan hóa Số liệu trên Dashboard
    └── 11.2.1 Tích hợp Chart.js hiển thị biểu đồ và phân quyền hiển thị theo vai trò

12.0 ĐÓNG BĂNG MÃ NGUỒN, KIỂM THỬ LÕI & DỮ LIỆU DEMO (NGÀY 12)
├── 12.1 Đóng băng Mã nguồn & Unit Test Lõi
│   ├── 12.1.1 Viết bộ kiểm thử Unit Test (JUnit 5 + Mockito) cho tính tiền, voucher, trừ kho, State
│   └── 12.1.2 Kiểm tra toàn diện tiêu chí DoD và chốt mã nguồn (Tag Git v1.0)
└── 12.2 Kiểm thử End-to-End & Chuẩn bị Kịch bản Demo
    ├── 12.2.1 Nạp bộ dữ liệu mẫu demo sạch, đa dạng sản phẩm thời trang
    └── 12.2.2 Chạy thử nghiệm trơn tru toàn bộ kịch bản bán hàng đa kênh

13.0 HOÀN THIỆN 2 BÁO CÁO HỌC PHẦN (HDT & QLDA) (NGÀY 13)
├── 13.1 Soạn thảo Báo cáo Phát triển Phần mềm Hướng đối tượng
│   └── 13.1.1 Trình bày kiến trúc phân lớp, Class Diagram, giải trình 4 Design Patterns
└── 13.2 Soạn thảo Báo cáo Quản lý Dự án Phần mềm
    └── 13.2.1 Tổng hợp WBS, PERT, số liệu theo dõi tiến độ thực tế, sổ rủi ro và đánh giá QA

14.0 DỰ PHÒNG, DIỄN TẬP DEMO & BÀN GIAO NỘP BÀI (NGÀY 14)
├── 14.1 Diễn tập Báo cáo & Kiểm tra Dự phòng
│   ├── 14.1.1 Diễn tập demo và trình bày theo thời gian quy định
│   └── 14.1.2 Kiểm tra kịch bản dự phòng mất mạng Internet (tải sẵn CSS/JS CDN về máy)
└── 14.2 Đóng gói Sản phẩm & Bàn giao
    └── 14.2.1 Đóng gói mã nguồn, tài liệu, slide thuyết trình và nộp bài chính thức (Milestone M7, hạn nộp 17/11 là GIẢ ĐỊNH)
```

---

## 2. ƯỚC LƯỢNG CÔNG SỨC 3 ĐIỂM (PERT)

Ước lượng công sức được tính toán dựa trên kỹ thuật **PERT (Program Evaluation and Review Technique)** với công thức:

$$E = \frac{O + 4M + P}{6}$$

Trong đó:
- $O$ (Optimistic): Thời gian ước lượng lạc quan nhất khi mọi việc diễn ra hoàn hảo không gặp sự cố kỹ thuật.
- $M$ (Most Likely): Thời gian ước lượng khả dĩ nhất trong điều kiện làm việc bình thường.
- $P$ (Pessimistic): Thời gian ước lượng bi quan nhất khi gặp sự cố kỹ thuật, xung đột thư viện hoặc lỗi AI.
- $E$ (Expected Duration): Giá trị kỳ vọng công sức hoàn thành.
- $\sigma = \frac{P - O}{6}$: Độ lệch chuẩn (mức độ biến động rủi ro của công việc).

### Bảng Ước lượng Chi tiết 14 Gói Công việc Chính:

| Mã WBS | Gói công việc chính (Theo ngày) | $O$ (h) | $M$ (h) | $P$ (h) | $E$ (h) | $\sigma$ (h) | Ghi chú rủi ro chính |
|:---:|---|:---:|:---:|:---:|:---:|:---:|---|
| **1.0** | Đặc tả bài toán, Use Case & Kế hoạch (Ngày 1) | 4.0 | 5.0 | 6.5 | **5.08** | 0.42 | Thống nhất phạm vi với brief v2 |
| **2.0** | CSDL, Flyway Migration & Seed (Ngày 2) | 3.5 | 4.5 | 7.0 | **4.75** | 0.58 | Xung đột checksum Flyway, seed user |
| **3.0** | Security 2 Chain & Quản lý Catalog (Ngày 3) | 4.0 | 5.0 | 7.5 | **5.25** | 0.58 | Cấu hình 2 SecurityFilterChain phức tạp |
| **4.0** | Cửa hàng, Nhân sự & Tồn kho đa điểm (Ngày 4) | 3.5 | 4.5 | 6.5 | **4.67** | 0.50 | Xử lý khóa lạc quan @Version |
| **5.0** | Shop Online: Trưng bày & Giỏ hàng (Ngày 5) | 3.5 | 4.5 | 6.5 | **4.67** | 0.50 | Xử lý chọn biến thể động Màu/Size |
| **6.0** | Checkout, Voucher, Trừ kho Observer (Ngày 6) | 4.0 | 5.0 | 7.5 | **5.25** | 0.58 | Tích hợp Strategy & Spring Event |
| **7.0** | Đánh giá Giữa kỳ, Nợ kỹ thuật, Tag v0.5 (Ngày 7) | 3.0 | 4.0 | 5.5 | **4.08** | 0.42 | Rà soát lỗi N+1, quyết định Cut-list |
| **8.0** | Bán hàng POS: Giao diện & Tra cứu SKU (Ngày 8) | 3.5 | 4.5 | 6.5 | **4.67** | 0.50 | Tối ưu trải nghiệm thao tác phím tắt quầy |
| **9.0** | Hoàn thiện POS, In hóa đơn & Ca thu ngân (Ngày 9)| 4.0 | 5.0 | 7.0 | **5.17** | 0.50 | In hóa đơn 80mm & mở/chốt ca |
| **10.0**| Quản trị Đơn hàng & State Pattern (Ngày 10) | 4.0 | 5.0 | 7.0 | **5.17** | 0.50 | Logic chuyển đổi trạng thái nghiêm ngặt |
| **11.0**| Báo cáo Thống kê & Dashboard Chart.js (Ngày 11)| 3.5 | 4.5 | 6.5 | **4.67** | 0.50 | Truy vấn nhóm phức tạp & hiển thị biểu đồ|
| **12.0**| Đóng băng mã nguồn, Unit Test & Demo Data (Ngày 12)| 4.0 | 5.0 | 7.5 | **5.25** | 0.58 | Viết JUnit Mockito & chuẩn bị dữ liệu |
| **13.0**| Hoàn thiện 2 Báo cáo HDT & QLDA (Ngày 13) | 4.0 | 5.0 | 6.5 | **5.08** | 0.42 | Vẽ biểu đồ lớp & tổng hợp bảng biểu |
| **14.0**| Diễn tập Demo, Dự phòng & Đóng gói (Ngày 14) | 3.0 | 4.0 | 5.0 | **4.00** | 0.33 | Diễn tập thuyết trình & nộp bài M7 |
| **TỔNG**| **TOÀN DỰ ÁN (14 NGÀY)** | **51.5**| **65.5**| **93.0**| **67.75**| **1.85** | *(Độ lệch chuẩn tổng $\sqrt{\sum \sigma_i^2}$)* |

---

## 3. ĐỐI CHIẾU CÔNG SỨC ƯỚC LƯỢNG VỚI NGUỒN LỰC KHẢ DỤNG

### 3.1 Phân tích Cân đối Nguồn lực (Resource Balance Analysis)
- **Tổng số ngày làm việc quy định:** 14 ngày (áp dụng trục ngày tương đối **N1 – N14**; theo mốc nộp bài giả định 17/11/2026, lịch tương ứng từ 03/11/2026 đến 16/11/2026).
- **Lịch bận & định mức làm việc:**
  - Định mức làm việc tiêu chuẩn: **5.0 giờ/ngày** cho toàn bộ 14 ngày.
  - Ngày bận thi / ôn thi môn khác: **Không có** (làm việc đủ 14 ngày liên tục theo định mức chuẩn).
- **Tổng quỹ thời gian khả dụng thực tế:**
  $$\text{Tổng giờ khả dụng} = 14 \text{ ngày} \times 5.0 \text{ giờ} = 70.0 \text{ giờ}$$
- **Tổng công sức kỳ vọng theo PERT ($E$):**
  $$\text{Tổng } E = 67.75 \text{ giờ}$$
- **Chênh lệch giữa Tổng quỹ giờ khả dụng và Tổng ước lượng PERT ($E$):**
  $$\Delta_E = \text{Tổng khả dụng} - \text{Tổng } E = 70.0 - 67.75 = +2.25 \text{ giờ (DƯƠNG)}$$

> [!NOTE] Đánh giá Quỹ thời gian Dự phòng ở Kịch bản Kỳ vọng ($E$)
> Với tổng quỹ thời gian khả dụng là 70.0 giờ và tổng công sức kỳ vọng PERT là 67.75 giờ, dự án có một vùng đệm an toàn tự nhiên $\Delta_E = +2.25$ giờ ($\approx 2$ giờ 15 phút). Vùng đệm dương này cho phép chủ dự án chủ động hấp thu các biến động nhỏ trong quá trình thực thi mà không làm chậm tiến độ chung.

### 3.2 Phân tích Kịch bản Rủi ro Bi quan & Hệ quả đối với Cut-list (Pessimistic Scenario & Cut-list Trigger)
- **Tổng công sức theo ước lượng bi quan ($P$):**
  $$\text{Tổng } P = 93.0 \text{ giờ}$$
- **Chênh lệch giữa Tổng quỹ giờ khả dụng và Tổng ước lượng PERT bi quan ($P$):**
  $$\Delta_P = \text{Tổng khả dụng} - \text{Tổng } P = 70.0 - 93.0 = -23.0 \text{ giờ (ÂM)}$$

- **Hệ quả đối với Cut-list:**
  - Khi kịch bản bi quan xảy ra trên diện rộng, dự án sẽ thiếu hụt **$-23.0$ giờ** (tương đương chậm khoảng 4.6 ngày làm việc so với quỹ thời gian 70.0 giờ).
  - Để chủ động phòng ngừa nguy cơ chậm tiến độ trên đường găng, chiến lược cắt giảm phạm vi (**Cut-list**) được thiết lập với ngưỡng kích hoạt rõ ràng:
    - Tại **Mốc Ngày 7 (09/11/2026 - M4)**: Sau khi đối soát 6 ngày đầu tiên, nếu tổng số giờ thực tế tích lũy vượt kế hoạch từ **3.0 giờ trở lên** (chậm từ 3 giờ), ban quản lý dự án sẽ **kích hoạt Cut-list** (bắt đầu bằng **Ưu tiên cắt 1**: loại bỏ toàn bộ mức Should gồm định tuyến đơn Haversine và chuyển kho `stock_transfers`).
    - Việc kích hoạt Cut-list sẽ tiết kiệm ngay ước tính **10.0 – 12.0 giờ công**, đưa tổng công sức kỳ vọng dự án từ 67.75 giờ về mức an toàn $\approx 56.0 - 58.0$ giờ ($< 70.0$ giờ khả dụng), nới rộng vùng đệm an toàn lên $+12.0$ đến $+14.0$ giờ để bảo toàn 100% các tính năng Must và mốc nộp bài.

---

## 4. TIẾN ĐỘ 14 NGÀY DẠNG BẢNG & CÁC MỐC QUAN TRỌNG (MILESTONES)

| Ngày | Ngày DL | Nội dung công việc chính | Kết quả đầu ra (Deliverable) | Giờ KH ($E$) | Mốc quan trọng (Milestone) |
|:---:|:---:|---|---|:---:|:---:|
| **N1** | 03/11 | Chốt phạm vi, Use Case, WBS, Rủi ro, Kế hoạch chất lượng | Bộ tài liệu `docs/01..05` hoàn chỉnh | 5.08 | **M1: Chốt tài liệu phân tích & KH** |
| **N2** | 04/11 | Thiết kế ERD v2, Migration CSDL, DevDataSeeder | Flyway chạy thành công, app boot không lỗi | 4.75 | **M2: Xong nền tảng CSDL & Seed** |
| **N3** | 05/11 | Cấu hình Security 2 chain, layout Admin, CRUD Catalog | Đăng nhập Admin, thêm sản phẩm có biến thể | 5.25 | |
| **N4** | 06/11 | Quản lý Chi nhánh, Nhân viên, Tồn kho & Sổ kho | Admin quản lý tồn chi nhánh, xem sổ cái kho | 4.67 | |
| **N5** | 07/11 | Giao diện Shop: Trang chủ, danh mục, lọc, chi tiết, Giỏ hàng | Khách xem và thêm được sản phẩm vào giỏ | 4.67 | |
| **N6** | 08/11 | Checkout Online, Strategy Thanh toán & Voucher, Trừ kho | Đặt hàng COD thành công, kho trừ tự động | 5.25 | **M3: Xong Shop Online & COD** |
| **N7** | 09/11 | Đánh giá giữa kỳ, đo lường chênh lệch, trả nợ kỹ thuật | Báo cáo tiến độ N7, Tag Git `v0.5` | 4.08 | **M4: Mốc kiểm soát Ngày 7 (Tag v0.5)** |
| **N8** | 10/11 | Giao diện POS bán hàng quầy, tìm kiếm/quét mã SKU | Giao diện POS tìm kiếm và lên giỏ hàng quầy | 4.67 | |
| **N9** | 11/11 | Hoàn thiện POS, thanh toán tiền mặt/thẻ, in bill, mở/chốt ca | Bán và in hóa đơn tại quầy, kho trừ đúng | 5.17 | **M5: Xong POS & In hóa đơn** |
| **N10**| 12/11 | Quản trị đơn đa kênh, State Pattern vòng đời đơn, hủy hoàn kho| Admin duyệt chuyển trạng thái đơn, hủy hoàn kho | 5.17 | |
| **N11**| 13/11 | Dashboard thống kê doanh thu, kênh, chi nhánh, Chart.js | Dashboard hiển thị đúng số liệu mẫu | 4.67 | |
| **N12**| 14/11 | Đóng băng mã nguồn, Unit Test lõi, chuẩn bị data demo | Bộ test pass 100%, kịch bản demo trơn tru | 5.25 | **M6: Code Freeze (Tag v1.0)** |
| **N13**| 15/11 | Soạn thảo hoàn thiện 2 báo cáo chi tiết HDT và QLDA | Bản thảo hoàn chỉnh của 2 báo cáo | 5.08 | |
| **N14**| 16/11 | Diễn tập thuyết trình, kiểm tra dự phòng, đóng gói nộp | Bộ slide, video/kịch bản demo, repo hoàn thiện | 4.00 | |
| **-**  | **17/11**| **HẠN CHÓT NỘP BÀI (GIẢ ĐỊNH)** | **Nộp toàn bộ sản phẩm và báo cáo** | **-** | **M7: Nộp bài chính thức (Hạn giả định)** |

> [!IMPORTANT] Quy tắc Điều hành Tiến độ theo Ngày Tương đối (N1–N14) & Hạn Nộp Giả Định
> 1. **Ngày nộp 17/11/2026 là GIẢ ĐỊNH:** Chưa có thông báo chính thức từ nhà trường/khoa, ước lượng dựa theo khung lịch thi các học phần khác.
> 2. **Lịch trình dùng ngày tương đối N1–N14:** Toàn bộ công việc được quản lý theo trục ngày tương đối (N1 đến N14). Khi có thông báo hạn nộp chính thức, chỉ cần tịnh tiến ánh xạ ngày dương lịch tương ứng mà không làm xáo trộn cấu trúc WBS.
> 3. **Quy tắc Code Freeze:** Mốc đóng băng mã nguồn (Code Freeze) luôn được ấn định bằng **Hạn nộp chính thức trừ đi 3 ngày** (tương ứng ngày **N12**). Sau mốc này, tuyệt đối không viết thêm code hay sửa tính năng, chỉ tập trung kiểm thử và viết báo cáo.
> 4. **Trường hợp hạn nộp thật MUỘN hơn 17/11/2026:** **Tuyệt đối KHÔNG mở rộng phạm vi (Zero Scope Creep)**, không thêm tính năng mới. Toàn bộ phần thời gian dôi dư được dùng làm vùng đệm (buffer) tăng cường kiểm thử, diễn tập demo và hoàn thiện 2 bản báo cáo học phần.
> 5. **Trường hợp hạn nộp thật SỚM hơn 17/11/2026:** **Kích hoạt Cut-list sớm hơn tương ứng** (cắt toàn bộ Should/Could) để co ngắn tiến độ, bảo toàn 100% phạm vi 24 bảng Must và đảm bảo mốc Code Freeze trước hạn nộp 3 ngày.

---

## 5. ĐƯỜNG GĂNG (CRITICAL PATH) VÀ CHIẾN LƯỢC CUT-LIST

### 5.1 Xác định Đường Găng (Critical Path Method - CPM)
Đường găng là chuỗi các công việc phụ thuộc lẫn nhau có tổng thời lượng dài nhất và có độ trễ bằng 0 ($\text{Float} = 0$). Bất kỳ sự chậm trễ nào trên đường găng đều làm chậm thời điểm hoàn thành toàn bộ dự án:

$$\text{Critical Path} = \mathbf{1.0 \rightarrow 2.0 \rightarrow 3.0 \rightarrow 4.0 \rightarrow 6.0 \rightarrow 8.0 \rightarrow 9.0 \rightarrow 10.0 \rightarrow 12.0 \rightarrow 13.0 \rightarrow 14.0 \rightarrow M7 \text{ (Hạn giả định)}}$$

- **Lý do găng:**
  - Không thể viết Migration (2.0) nếu chưa chốt Lược đồ CSDL và Phạm vi (1.0).
  - Không thể làm nghiệp vụ Catalog & Tồn kho (3.0, 4.0) nếu chưa có CSDL chuẩn (2.0).
  - Không thể thực hiện Đặt hàng (6.0) và Bán hàng POS (8.0, 9.0) nếu thiếu tầng Tồn kho (4.0).
  - Không thể quản trị Vòng đời đơn (10.0) nếu chưa có dữ liệu đơn hàng phát sinh từ Shop và POS.
  - Không thể đóng băng mã nguồn (12.0) và viết báo cáo cuối kỳ (13.0) nếu lõi chức năng chưa ổn định.

### 5.2 Chiến lược Cắt giảm Phạm vi (Cut-list)
Theo quy định tại Mục 12 của `PROJECT_BRIEF.md`, nếu tại mốc Ngày 7 (M4) hoặc bất kỳ thời điểm nào dự án bị chậm so với đường găng, việc cắt giảm phạm vi sẽ được thực thi tuần tự theo thứ tự ưu tiên sau:

1. **Ưu tiên cắt 1:** Toàn bộ các chức năng thuộc mức **Should**:
   - Thuật toán định tuyến đơn online tự động chọn cửa hàng gần nhất theo khoảng cách Haversine.
   - Tính năng điều chuyển hàng hóa giữa các cửa hàng (`stock_transfers`).
2. **Ưu tiên cắt 2:** Chức năng **Quản lý ca thu ngân (`pos_shifts`)**:
   - Thu ngân đăng nhập bán hàng trực tiếp mà không cần thao tác Mở ca/Đóng ca và kiểm kê tiền mặt.
3. **Ưu tiên cắt 3:** **Biểu đồ động trên Dashboard**:
   - Giữ lại các bảng số liệu thống kê doanh thu và tồn kho cơ bản, tạm thời không tích hợp biểu đồ Chart.js phức tạp.
4. **Ưu tiên cắt 4:** Toàn bộ các chức năng thuộc mức **Could**:
   - Đánh giá sản phẩm (`reviews`), tích điểm thành viên, đổi trả hàng.

> [!IMPORTANT] Cam kết Bất khả xâm phạm
> **Các tính năng thuộc mức Must Have tuyệt đối KHÔNG ĐƯỢC PHÉP CẮT GIẢM.** Dù trong bất kỳ tình huống nào, các chức năng Must phải được bảo toàn 100% để đảm bảo yêu cầu tối thiểu của cả 2 môn học.

---

## 6. CẤU TRÚC NGUỒN LỰC VÀ CHI PHÍ DỰ ÁN

Dự án áp dụng mô hình phát triển phần mềm tinh gọn do một cá nhân chủ trì, chi phí được hạch toán minh bạch dựa trên công sức thực tế và chi phí công cụ, không bịa đặt các số liệu thương mại không có thật:

### 6.1 Nguồn lực Nhân sự
- **Số lượng:** 01 người (Chủ dự án kiêm Lập trình viên, Tester, BA, PM).
- **Tổng thời lượng quy đổi:** 70.0 giờ làm việc hiệu quả (14 ngày × 5.0h).
- **Định mức chi phí công lao động (Quy ước nội bộ):**
  - Mức quy đổi công lao động học tập/nghiên cứu: **100.000 VNĐ / giờ công**.
  - Tổng chi phí nhân công quy đổi:
    $$\text{Chi phí nhân lực} = 70.0 \text{ giờ} \times 100.000 \text{ VNĐ/giờ} = 7.000.000 \text{ VNĐ}$$

### 6.2 Chi phí Công cụ & Hạ tầng Kỹ thuật

| STT | Hạng mục công cụ | Phiên bản sử dụng | Mục đích | Chi phí thực tế |
|:---:|---|---|---|:---:|
| 1 | **Visual Studio Code / IntelliJ IDEA** | Community Edition | Môi trường lập trình (IDE) | **0 VNĐ** (Miễn phí) |
| 2 | **Microsoft SQL Server 2022** | Developer Edition | Hệ quản trị CSDL quan hệ | **0 VNĐ** (Bản quyền dev) |
| 3 | **Git & GitHub** | GitHub Free Tier | Quản lý mã nguồn & phân nhánh | **0 VNĐ** (Miễn phí) |
| 4 | **Thư viện Giao diện (Bootstrap 5, Chart.js)** | Phiên bản mới nhất qua CDN | Giao diện người dùng & biểu đồ | **0 VNĐ** (Mã nguồn mở) |
| 5 | **Công cụ Trợ lý AI (AI Coding Assistant)** | Gói dịch vụ sẵn có | Hỗ trợ sinh mã nguồn theo quy chuẩn | **0 VNĐ** (Tận dụng gói có sẵn) |
| 6 | **Công cụ Vẽ sơ đồ (PlantUML)** | Tiện ích mở rộng VS Code | Thiết kế sơ đồ Use Case, Gantt | **0 VNĐ** (Mã nguồn mở) |
| **TỔNG** | **CHI PHÍ CÔNG CỤ & BẢN QUYỀN** | | | **0 VNĐ** |

### 6.3 Tổng Chi phí Dự án
- **Chi phí tiền mặt thực tế phải chi trả:** **0 VNĐ** (khai thác 100% công cụ miễn phí/mã nguồn mở).
- **Tổng giá trị đầu tư quy đổi (Total Investment Value):** **7.000.000 VNĐ** (tương đương 70.0 giờ công chuyên môn).

