# SỔ ĐĂNG KÝ RỦI RO DỰ ÁN (RISK REGISTER)
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG THỜI TRANG (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Tài liệu phục vụ:** Báo cáo Quản lý dự án phần mềm & Báo cáo Phát triển phần mềm hướng đối tượng  
> **Ngày lập:** 03/11/2026 (Cập nhật liên tục trong 14 ngày)  
> **Người quản lý rủi ro:** Chủ dự án (Project Owner / Lead Dev)  

---

## 1. PHƯƠNG PHÁP ĐÁNH GIÁ RỦI RO

Hệ thống quản lý rủi ro áp dụng ma trận định tính $3 \times 3$ theo chuẩn quản lý dự án:

### 1.1 Thang đo Xác suất (Probability - P)
- **Thấp (1):** Ít có khả năng xảy ra ($< 20\%$).
- **Trung bình (2):** Có thể xảy ra trong quá trình phát triển ($20\% - 50\%$).
- **Cao (3):** Khả năng xảy ra rất lớn hoặc chắc chắn xảy ra nếu không kiểm soát ($> 50\%$).

### 1.2 Thang đo Mức độ Ảnh hưởng (Impact - I)
- **Thấp (1):** Gây trễ dưới 2 giờ làm việc, ảnh hưởng nhỏ đến giao diện hoặc tài liệu, không ảnh hưởng logic lõi.
- **Trung bình (2):** Gây trễ 2 – 5 giờ làm việc, ảnh hưởng đến 1 chức năng hoặc gây lỗi biên dịch tạm thời.
- **Cao (3):** Gây hỏng hệ thống, mất dữ liệu, phá vỡ kiến trúc, trễ hạn nộp bài hoặc vi phạm yêu cầu học phần.

### 1.3 Ma trận Điểm số Rủi ro ($\text{Score} = P \times I$)
- **1 – 2 (Màu xanh):** Rủi ro thấp — Tiếp tục theo dõi thông thường.
- **3 – 4 (Màu vàng):** Rủi ro trung bình — Cần biện pháp phòng ngừa và kế hoạch dự phòng chủ động.
- **6 – 9 (Màu đỏ):** Rủi ro cao / Nghiêm trọng — Bắt buộc có hành động ngăn chặn tức thì và theo dõi sát hàng ngày.

---

## 2. BẢNG SỔ ĐĂNG KÝ RỦI RO CHI TIẾT (HIỆN CÓ 18 RỦI RO)

| Mã | Mô tả rủi ro | Nguyên nhân gốc rễ | P | I | Điểm ($P \times I$) | Biện pháp phòng ngừa (Proactive) | Kế hoạch xử lý / Kế hoạch dự phòng (Contingency) | Người chịu trách nhiệm | Trạng thái |
|:---:|---|---|:---:|:---:|:---:|---|---|:---:|:---:|
| **RSK-01** | **Đổi model AI giữa chừng làm lệch quy ước code** | Chuyển đổi giữa các model AI khác nhau khiến model mới mất ngữ cảnh, tự sinh code theo quy ước mới (đổi `ApiResponse`, đảo thứ tự mapper). | **Cao (3)** | **Cao (3)** | **9 (Đỏ)** | Thiết lập ngay file `PROJECT_BRIEF.md` v2 làm "hợp đồng bắt buộc". Yêu cầu AI đọc lại brief và in nguyên văn file dùng chung trước khi viết code. | **Đã áp dụng:** Ban hành ngay brief v2, đóng băng quy ước dùng chung ở mục 5 & 13; buộc AI đọc lại tài liệu trước mỗi phiên làm việc mới. | Chủ dự án | **ĐÃ XẢY RA** |
| **RSK-02** | **AI báo "build thành công" khi chưa build thật** | Trợ lý AI có xu hướng dự đoán ngôn từ khẳng định "tôi tự tin code đúng, đã build thành công" mà không chạy terminal kiểm tra thực tế. | **Cao (3)** | **Cao (3)** | **9 (Đỏ)** | Đưa vào quy tắc kỷ luật bắt buộc tại mục 13: Cấm báo thành công nếu chưa chạy lệnh kiểm thử thật trong console. | **Đã áp dụng:** Thiết lập quy trình: Sau mỗi lần sinh code, bắt buộc chạy `mvn clean compile` trong terminal, paste nguyên văn dòng `BUILD SUCCESS` mới được bàn giao. | Chủ dự án | **ĐÃ XẢY RA** |
| **RSK-03** | **IDE lệch pha với Maven sau khi chạy `mvn clean`** | Sau khi `mvn clean`, thư mục target chứa mã tự sinh của MapStruct và Lombok bị xóa khiến IDE (VS Code / IntelliJ) báo đỏ hàng loạt. | **Cao (3)** | **TB (2)** | **6 (Đỏ)** | Thiết lập thứ tự plugin trong `pom.xml`: Lombok -> MapStruct processor -> Lombok-binding. Không vội vàng sửa code khi thấy IDE báo đỏ giả. | **Đã áp dụng:** Chạy ngay `mvn compile` để Maven sinh lại mã nguồn trung gian, sau đó reload project trong IDE (Clean Java Language Server Workspace). | Chủ dự án | **ĐÃ XẢY RA** |
| **RSK-04** | **Phạm vi phình to (Scope Creep)** | Tâm lý muốn bổ sung thêm tính năng phong phú hoặc AI tự ý sinh thêm các bảng/cột ngoài brief (flash sale, EAV, thanh toán thật). | **Cao (3)** | **Cao (3)** | **9 (Đỏ)** | Chốt cứng ma trận MoSCoW tại mục 7 brief; quy định rõ nguyên tắc "Zero Scope Creep": cấm tự thêm bảng/cột nếu chưa hỏi chủ dự án. | Nếu phát sinh tính năng mới, lập tức chuyển vào danh mục Won't (hướng phát triển tương lai trong báo cáo), kiên quyết bảo vệ phạm vi 24 bảng Must. | Chủ dự án | Chưa xảy ra |
| **RSK-05** | **Dự án Flutter song song chiếm dụng thời gian** | Ràng buộc làm song song học phần di động dẫn đến nguy cơ không đảm bảo đủ 5 giờ làm việc hiệu quả mỗi ngày cho đồ án này. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Lập lịch biểu làm việc cố định (ví dụ: dành khung giờ tối 18h30 - 23h30 chỉ tập trung cho dự án này); ưu tiên tuyệt đối đồ án này trước. | Kích hoạt Cut-list tại mốc Ngày 7: cắt giảm toàn bộ mức Should (định tuyến đơn Haversine, ca thu ngân) để giải phóng 10 - 12 giờ công. | Chủ dự án | Chưa xảy ra |
| **RSK-06** | **Hết quota API hoặc gặp rate-limit công cụ AI** | Sử dụng AI liên tục với cường độ cao trong ngày dẫn đến vượt hạn mức yêu cầu/phút (rate limit) hoặc cạn kiệt token/quota. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Tinh gọn prompt, chỉ gửi kèm các file thực sự cần thiết; tránh gửi toàn bộ mã nguồn thừa thãi vào cửa sổ ngữ cảnh (context). | Chuẩn bị sẵn 2 tài khoản/môi trường AI dự phòng (chuyển đổi fallback giữa các model tương đương); lưu trữ prompt vào `docs/prompts/`. | Chủ dự án | Chưa xảy ra |
| **RSK-07** | **Lỗi migration Flyway do lệch mã băm Checksum** | Vô tình sửa trực tiếp nội dung các file migration cũ (`V1..V6` hoặc các file đã chạy vào DB) khiến Flyway chặn khởi động ứng dụng. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Nghiêm cấm sửa file migration đã áp dụng; mọi thay đổi lược đồ phải tạo file migration số hiệu mới (`V7`, `V8`...). | Nếu đang ở môi trường DB dev cá nhân và trót sửa file cũ: Chạy script xóa DB `ecommerce_db`, tạo lại DB rỗng và để Flyway chạy lại từ đầu. | Chủ dự án | Chưa xảy ra |
| **RSK-08** | **Bán vượt tồn kho khi 2 giao dịch đồng thời (Overselling)**| Hai khách hàng online cùng đặt mua hoặc quầy POS cùng bán 1 sản phẩm còn tồn kho = 1 tại cùng một mili-giây. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Sử dụng cột `version` (`@Version` trong JPA) trên bảng `inventories` để kích hoạt cơ chế khóa lạc quan (Optimistic Locking). | Bọc nghiệp vụ tạo đơn trong `@Transactional`; khi xảy ra `OptimisticLockException`, rollback giao dịch và báo lỗi rõ ràng cho người dùng. | Chủ dự án | Chưa xảy ra |
| **RSK-09** | **Mất kết nối mạng Internet khi demo báo cáo (CDN phụ thuộc)** | Giao diện sử dụng Bootstrap 5, Icon và Chart.js tải từ CDN; nếu phòng thi mất mạng hoặc mạng yếu, giao diện sẽ vỡ nát. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Kiểm tra kết nối mạng 3G/4G dự phòng trước buổi thuyết trình; giữ cache trình duyệt không bị xóa. | Tại Ngày 14 (Dự phòng), tải toàn bộ file CSS/JS thư viện CDN về lưu cục bộ trong `static/vendor/` và tạo switch dự phòng trong file HTML layout. | Chủ dự án | Chưa xảy ra |
| **RSK-10** | **Lỗi dữ liệu mẫu demo (Dirty/Inconsistent Demo Data)** | Sau nhiều lần test thử, dữ liệu demo bị xáo trộn (sản phẩm test bị mua hết tồn, tài khoản demo bị khóa mật khẩu). | **TB (2)** | **TB (2)** | **4 (Vàng)** | Tách biệt script seed dữ liệu sạch riêng biệt qua `DevDataSeeder` và migration SQL chuyên biệt. | Viết sẵn lệnh SQL hoặc endpoint `/api/dev/reset-demo-data` để reset toàn bộ dữ liệu demo về trạng thái ban đầu sạch sẽ chỉ trong 5 giây trước khi vào phòng chấm. | Chủ dự án | Chưa xảy ra |
| **RSK-11** | **Xung đột Lazy Loading trong Thymeleaf View** | Controller đưa trực tiếp Entity có quan hệ lười biếng (`FetchType.LAZY`) ra View Thymeleaf, gây ra lỗi `LazyInitializationException`. | **Cao (3)** | **TB (2)** | **6 (Đỏ)** | Tuân thủ nghiêm ngặt quy tắc kiến trúc tại mục 3 brief: Tuyệt đối không đưa Entity ra View/API; luôn chuyển đổi sang DTO/ViewModel bằng MapStruct. | Dùng truy vấn `JOIN FETCH` trong Repository khi cần nạp dữ liệu quan hệ, hoặc cấu hình `open-in-view: false` để phát hiện lỗi ngay từ tầng Service. | Chủ dự án | Chưa xảy ra |
| **RSK-12** | **Sai lệch số tiền do dùng kiểu dữ liệu số thực** | Dùng kiểu `float` hoặc `double` trong Java để tính tiền dẫn đến lỗi làm tròn dấu phẩy động (floating-point precision error). | **Thấp (1)** | **Cao (3)** | **3 (Vàng)** | Quy định bắt buộc tại mục 5 brief: Mọi thuộc tính tiền tệ trong Java phải dùng `BigDecimal` và trong SQL Server dùng `DECIMAL(18,2)`. | Rà soát toàn bộ Entity và DTO liên quan đến tiền tệ (`Order`, `Payment`, `Voucher`); thiết lập chế độ làm tròn `RoundingMode.HALF_UP`. | Chủ dự án | Chưa xảy ra |
| **RSK-13** | **Trễ hạn nộp do mất cân đối giữa viết code và soạn báo cáo**| Sa đà vào việc trau chuốt tính năng và fix bug đến tận ngày cuối, không còn đủ thời gian soạn thảo 2 bản báo cáo HDT và QLDA. | **TB (2)** | **Cao (3)** | **6 (Đỏ)** | Thực hiện nghiêm ngặt quy định **Code Freeze tại Ngày 12 (luôn bằng hạn nộp trừ 3 ngày; theo hạn nộp giả định 17/11 là ngày 14/11)**; dành trọn vẹn 2 ngày cuối (Ngày 13 & 14) chỉ tập trung hoàn thiện báo cáo và slide. | Nếu Ngày 11 còn tồn đọng lỗi nhỏ không nghiêm trọng: Tạm hoãn sửa lỗi, khóa toàn bộ code để viết báo cáo theo đúng cấu trúc mẫu. | Chủ dự án | Chưa xảy ra |
| **RSK-14** | **Hạn nộp chưa được xác nhận chính thức** | Khoa/Trường chưa công bố lịch nộp bài và bảo vệ chính thức; mốc 17/11/2026 hiện là GIẢ ĐỊNH dựa theo lịch thi các môn khác. | **Cao (3)** | **Cao (3)** | **9 (Đỏ)** | Lập kế hoạch theo ngày tương đối N1–N14; quy định mốc Code Freeze = hạn nộp trừ 3 ngày (N12). Theo dõi sát thông báo đào tạo. | Nếu hạn thật muộn hơn: KHÔNG mở rộng phạm vi (Zero Scope Creep), lấy thời gian dư làm đệm kiểm thử và hoàn thiện 2 báo cáo. Nếu hạn thật sớm hơn: kích hoạt Cut-list sớm hơn tương ứng. | Chủ dự án | Chưa xảy ra |
| **RSK-15** | **Lịch thi / ôn thi các môn khác chiếm giờ làm** | Trùng lịch thi hoặc ôn tập các môn học khác trong học kỳ làm giảm số giờ làm việc hiệu quả trong ngày. | **TB (2)** | **TB (2)** | **4 (Vàng)** | Theo dõi sát lịch thi từ nhà trường; duy trì định mức 5.0h/ngày và tận dụng vùng đệm an toàn ($\Delta_E = +2.25$h) ở các ngày đầu để dự phòng biến động. | Nếu phát sinh lịch thi/ôn thi đột xuất hoặc tại Mốc Ngày 7 bị trễ từ 3.0h trở lên: kích hoạt Cut-list theo thứ tự ưu tiên (bỏ các tính năng Should: định tuyến Haversine, stock_transfers) để tiết kiệm 10–12h. | Chủ dự án | Chưa xảy ra |
| **RSK-16** | **Dùng prompt / tài liệu lỗi thời gây ra code thừa và xung đột** | Sau khi chốt phạm vi v2 (bỏ mô hình EAV), prompt catalog cũ vẫn được chạy và sinh ra code Attribute/AttributeValue không còn khớp schema v2. | **Cao (3)** | **Cao (3)** | **9 (Đỏ)** | Ban hành Quy tắc 11 và 12 tại Mục 13 của `PROJECT_BRIEF.md`: schema v2 và brief luôn thắng; cấm `ddl-auto: none/update`; rà soát kỹ prompt trước khi giao AI. | **Đã xử lý & kế hoạch:** Ở Ngày 2 đã xóa migration nháp cũ, đặt tạm `ddl-auto: none` để app boot được; tại Ngày 3A bắt buộc xóa toàn bộ code catalog cũ (`Attribute`, `AttributeValue`), khôi phục `ddl-auto: validate`. | Chủ dự án | **ĐÃ XẢY RA** |
| **RSK-17** | **Khác collation giữa database và tempdb gây lỗi script seed V29** | CSDL `ecommerce_db` và hệ thống `tempdb` khác collation (cột bảng tạm `#...` tự động nhận collation của `tempdb`, cột bảng thật nhận collation của DB) dẫn đến xung đột khi so sánh/ghép chuỗi trong SQL Server lúc chạy seed dữ liệu mẫu V29. Phát hiện nhờ test `contextLoads`. | **Cao (3)** | **TB (2)** | **6 (Đỏ)** | Tạo database không chỉ định COLLATE (`CREATE DATABASE ecommerce_db;`); trong mọi script SQL dùng bảng tạm (`#...`), luôn khai báo mọi cột chữ kèm `COLLATE DATABASE_DEFAULT` (Quy tắc 13 tại Mục 13 `PROJECT_BRIEF.md`). | **Đã áp dụng:** Sửa file migration V29 khai báo `COLLATE DATABASE_DEFAULT` cho tất cả các cột chữ trong bảng tạm `#temp_variants`; tạo DB không chỉ định COLLATE và chạy kiểm thử `contextLoads` pass 100%. | Chủ dự án | **ĐÃ XẢY RA** |
| **RSK-18** | **Lệnh Maven (`mvn test`) và chạy ứng dụng bị treo trong cửa sổ trợ lý AI** | Môi trường terminal con hoặc cơ chế sandbox của công cụ AI gặp lỗi deadlock/block I/O khi chạy kiểm thử `mvn test` hoặc chạy ứng dụng Spring Boot (bị treo nhiều phút không kết thúc), trong khi chạy tay trực tiếp trong terminal bên ngoài thì hoàn tất sau khoảng 35 giây. | **Cao (3)** | **TB (2)** | **6 (Đỏ)** | Ban hành Quy tắc 3 tại Mục 13 `PROJECT_BRIEF.md`: Trợ lý AI chỉ chạy lệnh kiểm tra biên dịch `mvn clean compile`; nghiêm cấm AI tự chạy `mvn test` và chạy ứng dụng trong cửa sổ AI. Đặt timeout ngắt lệnh sau 3 phút nếu không kết thúc. | **Đã áp dụng:** Chủ dự án tự mở terminal độc lập để chạy `mvn test` và khởi chạy ứng dụng; AI chỉ nhận kết quả thông báo và log nghiệm thu từ chủ dự án. | Chủ dự án | **ĐÃ XẢY RA** |

---

## 3. PHÂN TÍCH VÀ ĐÁNH GIÁ CÁC SỰ CỐ THỰC TẾ ĐÃ XẢY RA

Trong quá trình khởi động và thực hiện các ngày đầu (giai đoạn 1 & 2, đến ngày 3C) của dự án, 6 rủi ro kỹ thuật đã thực sự phát sinh trong thực tế (RSK-01, RSK-02, RSK-03, RSK-16, RSK-17, RSK-18). Việc ghi nhận và phân tích sâu các sự cố này mang lại giá trị thực tiễn rất cao cho báo cáo Quản lý dự án phần mềm (QLDA):

### 3.1 Sự cố 1: Đổi model AI làm lệch quy ước kiến trúc (RSK-01)
- **Diễn biến thực tế:** Khi chuyển từ phiên bản model này sang model khác giữa các phiên làm việc, model AI mới không được tiếp cận đầy đủ các quy ước ngầm định trước đó. Kết quả là AI tự ý tạo lớp phản hồi khác thay vì dùng `ApiResponse<T>`, tự đổi cấu trúc DTO và đảo lộn thứ tự annotation processor.
- **Biện pháp đã xử lý triệt để:** 
  1. Soạn thảo tài liệu `PROJECT_BRIEF.md` phiên bản 2.0 đóng vai trò như một bản đặc tả ràng buộc kỹ thuật tối cao.
  2. Đưa vào Mục 13 quy tắc bắt buộc: Trước khi sinh mã mới, AI phải đọc brief và in nguyên văn mã nguồn của các lớp nền tảng (`BaseEntity`, `ApiResponse`).

### 3.2 Sự cố 2: AI báo "hoàn thành/build thành công" khi chưa chạy build thật (RSK-02)
- **Diễn biến thực tế:** Trợ lý AI sau khi sinh code đã lập tức thông báo *"Tôi đã tạo xong chức năng, code hoàn toàn chính xác và đã build thành công"*. Tuy nhiên khi chủ dự án chạy thử lệnh Maven, trình biên dịch báo lỗi thiếu thư viện và sai kiểu dữ liệu MapStruct.
- **Biện pháp đã xử lý triệt để:**
  1. Ban hành quy tắc kỷ luật nghiêm ngặt: AI không được phép đưa ra bất kỳ kết luận nào về sự thành công của mã nguồn nếu chưa thực thi lệnh `mvn clean compile` trong terminal.
  2. Mọi thông báo bàn giao bắt buộc phải đính kèm nguyên văn đoạn log có dòng chữ `BUILD SUCCESS` từ Maven.

### 3.3 Sự cố 3: Lệch pha giữa IDE và Maven sau `mvn clean` (RSK-03)
- **Diễn biến thực tế:** Sau khi thực hiện lệnh dọn dẹp `mvn clean`, toàn bộ các file `.class` và mã nguồn mapper tự sinh bởi MapStruct trong thư mục `target/generated-sources` bị xóa sạch. Giao diện IDE hiển thị hàng loạt gạch chân đỏ báo lỗi không tìm thấy bean mapper, gây hoang mang và lãng phí thời gian debug nhầm chỗ.
- **Biện pháp đã xử lý triệt để:**
  1. Chuẩn hóa quy trình thao tác: Sau mỗi lần chạy `mvn clean`, bắt buộc phải chạy kèm lệnh `mvn compile`.
  2. Thực hiện lệnh đồng bộ lại Language Server của IDE để nạp lại đường dẫn classpath chính xác.

### 3.4 Sự cố 4: Dùng prompt catalog cũ gây sinh mã thừa và xung đột schema (RSK-16)
- **Diễn biến thực tế:** Sau khi chốt phạm vi v2 (loại bỏ mô hình thuộc tính động EAV phức tạp, chuyển sang lưu trực tiếp cột `color` và `size` trong `product_variants`), prompt catalog cũ của giai đoạn 2 vẫn được đưa vào thực thi. Hệ quả là AI đã sinh ra các lớp thực thể (`Attribute`, `AttributeValue`), Repository và Service theo mô hình EAV cũ không còn bảng tương ứng trong CSDL v2. Khi khởi động với chế độ kiểm tra nghiêm ngặt `ddl-auto: validate`, Hibernate báo lỗi không tìm thấy bảng. Tại Ngày 2, để kiểm tra các file Flyway migration mới (V7–V30), AI đã phải xóa các file migration nháp cũ và tạm thời chuyển cấu hình sang `ddl-auto: none` trong `application-dev.yml` để ứng dụng có thể khởi động.
- **Biện pháp đã xử lý & Kế hoạch khắc phục triệt để:**
  1. Bổ sung Quy tắc 11 và 12 vào Mục 13 của `PROJECT_BRIEF.md`:
     - *Quy tắc 11:* Cấm đặt `ddl-auto: none` hoặc `update`. Nếu tạm thời phải đổi để chẩn đoán lỗi, phải ghi nhật ký và hoàn trả về `validate` trước khi kết thúc ngày làm việc.
     - *Quy tắc 12:* Khi code cũ mâu thuẫn với brief hoặc schema hiện tại, brief và schema v2 luôn thắng. Tuyệt đối không "vá" code cũ cho khớp mà phải cô lập và loại bỏ.
  2. Lên kế hoạch xử lý dứt điểm trong phiên Ngày 3A: Xóa bỏ hoàn toàn mã nguồn catalog cũ liên quan đến EAV, cấu hình trả về `spring.jpa.hibernate.ddl-auto: validate`, và kiểm tra biên dịch bằng lệnh `mvn clean compile` đạt `BUILD SUCCESS`.

### 3.5 Sự cố 5: Khác collation giữa database và tempdb gây lỗi script seed V29 (RSK-17)
- **Diễn biến thực tế:** Khi thực hiện nạp dữ liệu mẫu giày dép (CR-03 ở Ngày 3C) qua migration V29, script SQL sử dụng bảng tạm `#temp_variants` để sinh 120 biến thể sản phẩm. Do máy chủ SQL Server cục bộ cài đặt mặc định collation của `tempdb` khác với collation của cơ sở dữ liệu `ecommerce_db`, câu lệnh ghép dữ liệu từ `#temp_variants` sang bảng `products` và `product_variants` bị SQL Server chặn với lỗi xung đột collation giữa các cột chuỗi ký tự (`NVARCHAR`) (cột bảng tạm theo collation của `tempdb`, cột bảng thật theo collation của DB). Lỗi này được phát hiện chính xác nhờ test `contextLoads` khi chạy kiểm thử ứng dụng Spring Boot.
- **Biện pháp đã xử lý triệt để:**
  1. Ban hành Quy tắc 13 tại Mục 13 của `PROJECT_BRIEF.md`:
     - Khởi tạo database bằng lệnh chuẩn không chỉ định COLLATE: `CREATE DATABASE ecommerce_db;`.
     - Mọi script SQL có sử dụng bảng tạm (`#...`) bắt buộc phải khai báo tường minh mệnh đề `COLLATE DATABASE_DEFAULT` cho tất cả các cột kiểu chuỗi ký tự (`VARCHAR`, `NVARCHAR`).
  2. Cập nhật script `V29__seed_catalog_and_inventory.sql`, thêm `COLLATE DATABASE_DEFAULT` cho các cột chuỗi của `#temp_variants`. Tạo lại cơ sở dữ liệu dev rỗng và chạy Flyway áp dụng thành công toàn bộ migration V1–V30; kiểm thử `contextLoads` đạt kết quả pass.

### 3.6 Sự cố 6: Lệnh Maven (`mvn test`) chạy trong cửa sổ AI bị treo (RSK-18)
- **Diễn biến thực tế:** Trong ngày 04/11, trợ lý AI thực hiện lệnh chạy kiểm thử `mvn test` và lệnh khởi chạy ứng dụng bên trong cửa sổ terminal của công cụ AI. Cả hai lần tiến trình đều bị treo (hang) vô thời hạn trong nhiều phút mà không trả về kết quả hay luồng xuất dữ liệu (console output), gây ách tắc phiên làm việc. Ngược lại, khi chủ dự án mở terminal độc lập của hệ điều hành (PowerShell/CMD) và chạy tay cùng câu lệnh `mvn test`, toàn bộ 15/15 test cases hoàn tất thành công (`BUILD SUCCESS`) chỉ sau khoảng 35 giây.
- **Biện pháp đã xử lý triệt để:**
  1. Cập nhật và siết chặt Quy tắc 3 tại Mục 13 của `PROJECT_BRIEF.md`:
     - Phân định rõ thẩm quyền thực thi: AI chỉ được phép chạy lệnh biên dịch `mvn clean compile` trong sandbox để xác nhận tính hợp lệ của cú pháp; **`mvn test` và chạy ứng dụng do chính chủ dự án tự chạy trong terminal của mình**.
     - Thiết lập cơ chế ngắt: Nếu bất kỳ lệnh nào do AI thực thi không kết thúc sau 3 phút, AI bắt buộc phải chủ động dừng lại và báo cáo chủ dự án, tuyệt đối không thử lại lặp đi lặp lại.
  2. Quy trình phối hợp: Chủ dự án tự chạy `mvn test` trong terminal của mình và thông báo kết quả kiểm thử cho AI tiếp tục công việc.

---

## 4. QUY TRÌNH GIÁM SÁT VÀ ĐIỀU CHỈNH RỦI RO HÀNG NGÀY

1. **Rà soát đầu giờ làm việc (Daily Standup cá nhân):** Đầu mỗi ca làm việc (5 giờ/ngày), chủ dự án rà soát lại Sổ đăng ký rủi ro, kiểm tra xem công việc trong ngày có chạm vào các rủi ro có điểm số $\ge 6$ hay không.
2. **Cập nhật trạng thái sau ca làm việc:** Nếu có rủi ro mới phát sinh hoặc một rủi ro tiềm ẩn trở thành sự cố thật, ghi nhận ngay vào nhật ký ngày `docs/nhat-ky/YYYY-MM-DD.md` và cập nhật cột "Trạng thái" trong bảng rủi ro này.
3. **Cột mốc đánh giá lại rủi ro:** Thực hiện đánh giá lại toàn diện toàn bộ danh mục rủi ro tại **Mốc Ngày 7 (Tag v0.5)** và **Mốc Ngày 12 (Code Freeze)**.

