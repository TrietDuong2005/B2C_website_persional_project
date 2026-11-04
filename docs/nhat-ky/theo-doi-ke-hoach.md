# BẢNG THEO DÕI TIẾN ĐỘ THỰC TẾ SO VỚI KẾ HOẠCH
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG THỜI TRANG (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Thời gian:** 03/11/2026 – 16/11/2026 (Hạn nộp bài: 17/11/2026 — GIẢ ĐỊNH, chưa có thông báo chính thức, ước lượng theo lịch thi các môn khác)  
> **Nguyên tắc quản lý tiến độ & cập nhật nhật ký:**  
> - **Quy tắc ngày tương đối N1–N14:** Toàn bộ tiến độ neo theo chuỗi ngày tương đối. Khi có hạn nộp thật chính thức, chỉ tịnh tiến ngày dương lịch tương ứng mà không làm đổi cấu trúc WBS.  
> - **Quy tắc Code Freeze:** Mốc đóng băng mã nguồn luôn được ấn định bằng **Hạn nộp trừ đi 3 ngày** (tương ứng ngày **N12**).  
> - **Nếu hạn nộp thật MUỘN hơn 17/11:** **Tuyệt đối KHÔNG thêm phạm vi (Zero Scope Creep)**, không thêm tính năng; toàn bộ thời gian dư dùng làm đệm kiểm thử và hoàn thiện 2 bản báo cáo.  
> - **Nếu hạn nộp thật SỚM hơn 17/11:** Kích hoạt ngay lập tức danh mục **Cut-list** sớm hơn để bảo toàn 100% phạm vi 24 bảng Must và mốc đóng băng mã nguồn.  
> - **Cột "Kế hoạch (giờ)":** Điền sẵn theo giá trị kỳ vọng ước lượng PERT ($E$) từ tài liệu `docs/03-wbs-va-ke-hoach.md` (Tổng $E = 67.75$ giờ).  
> - **Quỹ thời gian & Lịch làm việc:** Toàn bộ 14 ngày làm việc đều đặn theo định mức chuẩn 5.0 giờ/ngày (không có ngày bận). Tổng quỹ thời gian khả dụng là **70.0 giờ** ($14 \times 5\text{h}$), tạo vùng đệm an toàn tự nhiên $\Delta_E = +2.25$ giờ so với tổng kế hoạch $E = 67.75$ giờ.  
> - **Các cột "Thực tế (giờ)", "Chênh lệch", "Đạt DoD?", "Ghi chú":** **GIỮ NGUYÊN HOÀN TOÀN TRỐNG** để người thực hiện tự điền số liệu thật sau mỗi ngày làm việc. **TUYỆT ĐỐI KHÔNG BỊA ĐẶT SỐ LIỆU THỰC TẾ.**  

---

## 1. BẢNG THEO DÕI CHI TIẾT 14 NGÀY

| Ngày | Ngày DL | Nội dung công việc theo kế hoạch WBS | Sản phẩm bàn giao (Deliverable) | Kế hoạch (h) | Thực tế (h) | Chênh lệch (h) | Đạt DoD? | Ghi chú & Bài học phát sinh |
|:---:|:---:|---|---|:---:|:---:|:---:|:---:|---|
| **N1** | 03/11/2026 | Khởi động, Use Case, WBS, Rủi ro, Kế hoạch chất lượng | Bộ tài liệu `docs/01..05` hoàn chỉnh | **5.08** | | | | *(Chốt M1 tài liệu)* |
| **N2** | 04/11/2026 | Thiết kế ERD v2, Migration CSDL, DevDataSeeder | Flyway chạy thành công, app boot không lỗi | **4.75** | | | | *(Chốt M2 CSDL)* |
| **N3** | 05/11/2026 | Security 2 chain, layout Admin, CRUD Catalog | Đăng nhập Admin, thêm sản phẩm biến thể | **5.25** | | | | |
| **N4** | 06/11/2026 | Quản lý Chi nhánh, Nhân sự, Tồn kho & Sổ kho | Admin quản lý tồn kho chi nhánh, xem sổ cái | **4.67** | | | | |
| **N5** | 07/11/2026 | Giao diện Shop: Trưng bày, lọc, chi tiết, Giỏ hàng | Khách xem và thêm được sản phẩm vào giỏ | **4.67** | | | | |
| **N6** | 08/11/2026 | Checkout Online, Strategy Thanh toán & Voucher, Trừ kho | Đặt hàng COD thành công, kho trừ tự động | **5.25** | | | | *(Chốt M3 Shop Online)* |
| **N7** | 09/11/2026 | Đánh giá giữa kỳ, kiểm tra nợ kỹ thuật, gắn nhãn v0.5 | Báo cáo tiến độ N7, Tag Git `v0.5` | **4.08** | | | | *(Chốt M4 Mốc Ngày 7)* |
| **N8** | 10/11/2026 | Giao diện POS bán hàng quầy, tìm kiếm/quét mã SKU | Giao diện POS tìm kiếm và lên giỏ quầy | **4.67** | | | | |
| **N9** | 11/11/2026 | Hoàn thiện POS, thanh toán tiền mặt/thẻ, in bill, mở/chốt ca | Bán và in hóa đơn tại quầy, kho trừ đúng | **5.17** | | | | *(Chốt M5 POS & Hóa đơn)* |
| **N10**| 12/11/2026 | Quản trị đơn đa kênh, State Pattern vòng đời đơn, hủy hoàn kho| Admin duyệt đổi trạng thái đơn, hủy hoàn kho | **5.17** | | | | |
| **N11**| 13/11/2026 | Dashboard thống kê doanh thu, kênh, chi nhánh, Chart.js | Dashboard hiển thị đúng số liệu mẫu | **4.67** | | | | |
| **N12**| 14/11/2026 | Đóng băng mã nguồn, Unit Test lõi, chuẩn bị data demo | Bộ test pass 100%, kịch bản demo trơn tru | **5.25** | | | | *(Chốt M6 Code Freeze)* |
| **N13**| 15/11/2026 | Soạn thảo hoàn thiện 2 báo cáo chi tiết HDT và QLDA | Bản thảo hoàn chỉnh của 2 báo cáo | **5.08** | | | | |
| **N14**| 16/11/2026 | Diễn tập thuyết trình, kiểm tra dự phòng, đóng gói nộp | Bộ slide, video/kịch bản demo, repo hoàn thiện | **4.00** | | | | |
| **-**  | **17/11/2026**| **HẠN CHÓT NỘP BÀI (GIẢ ĐỊNH)** | **Nộp toàn bộ sản phẩm và báo cáo** | **-** | | | | **M7: Nộp bài chính thức (Hạn giả định)** |
| **TỔNG**| | **TỔNG CỘNG 14 NGÀY** | | **67.75** | | | | |

---

## 2. HƯỚNG DẪN CÁCH TÍNH TOÁN VÀ ĐIỀN THÔNG TIN

1. **Công thức tính Cột "Chênh lệch (h)":**
   $$\text{Chênh lệch} = \text{Thực tế (h)} - \text{Kế hoạch (h)}$$
   - Giá trị **dương ($> 0$)**: Tiêu tốn nhiều thời gian hơn dự kiến $\rightarrow$ **Chậm tiến độ**.
   - Giá trị **âm ($< 0$)**: Hoàn thành nhanh hơn dự kiến $\rightarrow$ **Vượt tiến độ**.
   - Giá trị **bằng $0$**: Hoàn thành chuẩn xác theo kỳ vọng PERT.

2. **Quy định Cột "Đạt DoD?":**
   - Điền **"ĐẠT"** nếu đáp ứng đủ 6 tiêu chí của Daily DoD trong tài liệu `docs/05-ke-hoach-chat-luong.md`.
   - Điền **"CHƯA"** nếu có tiêu chí chưa xong (ví dụ: app chưa build được hoặc thiếu comment); ghi rõ lý do vào cột Ghi chú và hoàn thành bù trong ca làm việc tiếp theo.

---

## 3. TỔNG HỢP VÀ PHÂN TÍCH PHƯƠNG SAI TIẾN ĐỘ (DÙNG CHO BÁO CÁO QLDA)

*(Phần này sinh viên để trống và tổng hợp số liệu sau khi hoàn thành xong 14 ngày làm việc)*

- **Tổng quỹ thời gian khả dụng thực tế:** `70.0 giờ` (14 ngày × 5.0h)
- **Tổng số giờ kế hoạch theo PERT (PV - Planned Value):** `67.75 giờ` (Vùng đệm dự phòng cơ sở $\Delta_E = +2.25$ giờ)
- **Tổng số giờ thực tế đã dùng (AC - Actual Cost):** `[... giờ]`
- **Phương sai chi phí công sức ($CV = PV - AC$):** `[... giờ]`
- **Chỉ số hiệu năng sử dụng thời gian ($CPI = PV / AC$):** `[...]`
- **Đánh giá chung về kiểm soát tiến độ:**
  - `[Nhận xét về mức độ bám sát kế hoạch, nguyên nhân dẫn đến chênh lệch giữa các ngày]`
  - `[Đánh giá hiệu quả của việc chốt mốc kiểm soát Ngày 7 và quyết định Cut-list (nếu có)]`

