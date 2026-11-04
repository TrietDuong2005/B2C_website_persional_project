# NHẬT KÝ CÔNG VIỆC HÀNG NGÀY
## NGÀY [X] — [DD/MM/2026] — [TÊN MỤC TIÊU CHÍNH TRONG NGÀY]

> **Dự án:** Hệ thống Bán lẻ Chuỗi Cửa hàng Thời trang (Website + POS + Admin)  
> **Người thực hiện:** [Họ và tên sinh viên]  
> **Mã công việc WBS:** [Ví dụ: 1.0 / 2.0 / 3.0...]  
> **Thời gian làm việc:** [Ví dụ: 18:30 – 23:30]  

---

## 1. KẾ HOẠCH DỰ KIẾN TRONG NGÀY (PLANNED TASKS)
- [ ] Công việc dự kiến 1: [Mô tả chi tiết công việc theo WBS]
- [ ] Công việc dự kiến 2: [Mô tả chi tiết công việc theo WBS]
- [ ] Công việc dự kiến 3: [Mô tả chi tiết công việc theo WBS]
- **Số giờ công kế hoạch theo ước lượng PERT ($E$):** `[...h]`

---

## 2. KẾT QUẢ THỰC TẾ ĐẠT ĐƯỢC (ACTUAL DELIVERABLES)
- [ ] Việc đã hoàn thành 1: [Ghi rõ kết quả cụ thể, tên file tạo/sửa]
- [ ] Việc đã hoàn thành 2: [Ghi rõ kết quả cụ thể]
- [ ] Việc chưa hoàn thành / dời sang hôm sau (nếu có): [Nêu rõ lý do]

---

## 3. THỜI GIAN THỰC HIỆN THỰC TẾ (ACTUAL EFFORT)
- **Tổng số giờ làm việc thực tế:** `[...h]` *(Điền số giờ thực tế, tuyệt đối không bịa số liệu)*
- **So sánh với kế hoạch:** `[Thực tế - Kế hoạch = ...h]`
  - *(Ghi chú: Nếu chênh lệch dương là chậm tiến độ, chênh lệch âm là hoàn thành sớm)*

---

## 4. VƯỚNG MẮC & LỖI KỸ THUẬT PHÁT SINH (ISSUES & BUGS)
- **Vấn đề 1:** [Mô tả lỗi hoặc khó khăn kỹ thuật gặp phải (ví dụ: lỗi biên dịch Maven, xung đột Flyway, lỗi logic State pattern...)]
- **Vấn đề 2:** [Khó khăn trong việc phối hợp với trợ lý AI hoặc hiểu ngữ cảnh code cũ]

---

## 5. BIỆN PHÁP XỬ LÝ ĐÃ ÁP DỤNG (SOLUTIONS)
- **Xử lý cho Vấn đề 1:** [Ghi lại cách đã debug, giải pháp kỹ thuật đã áp dụng để vượt qua]
- **Xử lý cho Vấn đề 2:** [Prompt đã điều chỉnh, quy tắc bổ sung hoặc cách tra cứu tài liệu]

---

## 6. BÀI HỌC KINH NGHIỆM RÚT RA (LESSONS LEARNED)
- [Bài học về mặt kỹ thuật lập trình Java/Spring Boot/JPA]
- [Bài học về quy trình làm việc với AI hoặc quản lý thời gian]

---

## 7. NHẬT KÝ TRAO ĐỔI VỚI TRỢ LÝ AI (AI INTERACTION LOG)
- **Model AI sử dụng:** [Ví dụ: Claude 3.5 Sonnet / Gemini 1.5 Pro / GPT-4o...]
- **Prompt chính đã dùng:**
  ```text
  [Dán nội dung prompt quan trọng nhất đã gửi cho AI trong ngày vào đây]
  ```
- **Đánh giá phản hồi của AI:** [AI sinh code chuẩn hay cần nhắc nhở/chỉnh sửa quy ước]

---

## 8. BẰNG CHỨNG KIỂM THỬ & ẢNH CHỤP MÀN HÌNH (EVIDENCE & SCREENSHOTS)
- **Kết quả lệnh build Maven:**
  ```text
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  ```
- **Ảnh chụp màn hình kết quả chức năng:**
  *(Dán link ảnh chụp màn hình giao diện hoặc kết quả Swagger UI vào thư mục docs/nhat-ky/images/ và nhúng vào đây)*
  - `![Ảnh kết quả 1](images/YYYY-MM-DD-result-1.png)`
  - `![Ảnh kết quả 2](images/YYYY-MM-DD-result-2.png)`

---

## 9. ĐỐI CHIẾU TIÊU CHÍ HOÀN THÀNH (DAILY DoD CHECKLIST)
- [ ] Tuân thủ quy ước đặt tên và kiến trúc phân lớp.
- [ ] Chạy `mvn clean compile` thành công có dòng `BUILD SUCCESS`.
- [ ] Ứng dụng khởi động bình thường không lỗi startup exception.
- [ ] Khớp cấu trúc bảng CSDL `ddl-auto: validate`.
- [ ] Đã tự kiểm thử thủ công chức năng hoạt động đúng.
- [ ] Có comment tiếng Việt đầy đủ cho Service và Design Pattern.

