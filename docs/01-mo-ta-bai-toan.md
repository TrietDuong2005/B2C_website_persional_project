# TÀI LIỆU MÔ TẢ BÀI TOÁN & PHẠM VI DỰ ÁN
## HỆ THỐNG BÁN LẺ CHUỖI CỬA HÀNG GIÀY DÉP "TRIẾT STORE" (WEBSITE + POS + ADMIN)

> **Mã dự án:** DA_HDT_QLDA  
> **Áp dụng cho 2 môn học:**  
> 1. Phát triển phần mềm hướng đối tượng (HDT)  
> 2. Quản lý dự án phần mềm (QLDA)  
> **Phiên bản tài liệu:** 1.0  
> **Ngày lập:** 03/11/2026 (Cập nhật 04/11/2026: Thu hẹp ngành hàng sang giày dép - Triết Store)  

---

## 1. BỐI CẢNH DỰ ÁN

Trong bối cảnh thị trường bán lẻ hiện đại cạnh tranh khốc liệt, chuỗi cửa hàng giày dép **"Triết Store"** (kinh doanh các dòng sản phẩm giày dép đa dạng: sneaker, giày da, giày boots, giày lười, giày thể thao, sandal, dép kẹp, dép slipper) đang đối mặt với bài toán vận hành phân tán và rời rạc:
- **Kênh bán hàng tại quầy (Offline - POS):** Khách hàng mua sắm trực tiếp tại cửa hàng cần thanh toán nhanh gọn bằng quét mã vạch SKU, áp dụng khuyến mãi tức thời, đa dạng hình thức thanh toán (tiền mặt, chuyển khoản QR, thẻ), và nhận hóa đơn rõ ràng.
- **Kênh bán hàng trực tuyến (Online - Shop Website):** Người tiêu dùng có xu hướng duyệt danh mục, chọn màu sắc, kích thước size EU (36–45) và đặt mua trực tiếp trên website để nhận hàng tận nơi (hình thức COD hoặc thanh toán điện tử giả lập).
- **Vấn đề cốt lõi về dữ liệu tồn kho:** Bán hàng đa kênh nhưng tồn kho không đồng bộ dẫn đến tình trạng bán vượt tồn kho (overselling) — khách đặt online đơn đã chốt nhưng tại cửa hàng nhân viên đã bán hết cho khách tại quầy. Ngoài ra, việc quản lý luân chuyển hàng giữa các chi nhánh gặp nhiều khó khăn, thiếu lịch sử ghi nhận biến động kho.
- **Bài toán quản trị chuỗi (Admin):** Chủ chuỗi cửa hàng và các nhà quản lý thiếu một công cụ tập trung để theo dõi doanh thu theo thời gian thực theo từng chi nhánh, kiểm soát trạng thái đơn hàng toàn kênh và đánh giá hiệu quả kinh doanh.

Hệ thống **Bán lẻ Chuỗi Cửa hàng Giày Dép "Triết Store" Đa kênh** được xây dựng nhằm giải quyết triệt để các vấn đề trên, cung cấp nền tảng quản lý tập trung và tích hợp 3 phân hệ người dùng trên cùng một cơ sở dữ liệu duy nhất:
1. **Phân hệ Shop (Website khách hàng):** Khách hàng duyệt sản phẩm, lọc theo danh mục, quản lý giỏ hàng, áp dụng voucher và đặt hàng online.
2. **Phân hệ POS (Điểm bán hàng tại quầy):** Thu ngân tra cứu/quét mã SKU, lên đơn tại quầy, áp dụng voucher, tính tiền thừa, thanh toán đa phương thức và in hóa đơn bán hàng.
3. **Phân hệ Admin (Quản trị chuỗi cửa hàng):** Quản lý tập trung danh mục, sản phẩm, biến thể, cửa hàng, nhân viên, tồn kho chi tiết từng điểm bán, đơn hàng toàn kênh, voucher khuyến mãi và dashboard thống kê doanh thu.

---

## 2. MỤC TIÊU DỰ ÁN

### 2.1 Mục tiêu nghiệp vụ & sản phẩm
- **Đồng bộ hóa tồn kho theo thời gian thực:** Quản lý số lượng tồn kho độc lập cho từng cửa hàng, kiểm soát chặt chẽ bằng cơ chế khóa lạc quan (`optimistic locking`) và sổ cái giao dịch kho (`inventory_transactions`), loại bỏ hoàn toàn rủi ro tồn kho âm và bán vượt tồn.
- **Hỗ trợ bán hàng đa kênh hiệu quả:** Hỗ trợ quy trình bán hàng khép kín cho cả đơn hàng tại quầy (POS) và đơn hàng giao tận nơi (Online).
- **Tối ưu hóa thời gian giao dịch tại quầy:** Cho phép thu ngân tạo hóa đơn nhanh dưới 1 phút với thao tác tìm kiếm/quét mã SKU và tính tiền tự động.

### 2.2 Mục tiêu học phần
1. **Môn Phát triển phần mềm hướng đối tượng (HDT):**
   - Ứng dụng kiến trúc phân lớp chuẩn: `Controller (Web/REST) -> Service -> Repository -> Entity`.
   - Xây dựng mô hình miền giàu nghiệp vụ (*Rich Domain Model*): Entity chứa logic xử lý nghiệp vụ của chính nó (ví dụ: `Inventory.deduct()`, `Order.addItem()`, `Voucher.calculateDiscount()`), không biến Entity thành "anemic data holder".
   - Ứng dụng đầy đủ 4 nguyên lý hướng đối tượng: Đóng gói (Encapsulation), Kế thừa (Inheritance qua `BaseEntity`), Đa hình (Polymorphism qua các Strategy/State Interface), và Trừu tượng hóa (Abstraction).
   - Hiện thực hóa thành công **4 mẫu thiết kế (Design Patterns)** kinh điển:
     - **Strategy Pattern:** Xử lý đa phương thức thanh toán (`PaymentStrategy`) và các hình thức tính giảm giá voucher (`DiscountStrategy`).
     - **State Pattern:** Quản lý vòng đời và chuyển đổi trạng thái đơn hàng trực tuyến (`OrderState`).
     - **Factory Pattern:** Tạo đơn hàng chuyên biệt theo kênh `ONLINE` và `POS` (`OrderFactory`).
     - **Observer Pattern:** Sử dụng Spring Events (`OrderPlacedEvent`, `OrderCancelledEvent`) để tách biệt việc đặt hàng với xử lý trừ/hoàn kho.
2. **Môn Quản lý dự án phần mềm (QLDA):**
   - Lập kế hoạch dự án thực chiến với cấu trúc phân rã công việc (WBS 3 cấp).
   - Ước lượng công sức chính xác bằng kỹ thuật PERT 3 điểm (O, M, P).
   - Lập tiến độ với biểu đồ Gantt, xác định rõ đường găng (Critical Path) và các mốc kiểm soát chất lượng (Milestones).
   - Xây dựng Sổ đăng ký rủi ro (Risk Register) bao gồm các rủi ro kỹ thuật thực tế phát sinh khi kết hợp cùng AI.
   - Thiết lập Định nghĩa hoàn thành (DoD), kế hoạch đảm bảo chất lượng (QA/QC), quản lý nợ kỹ thuật và nhật ký theo dõi tiến độ hàng ngày.

---

## 3. PHẠM VI DỰ ÁN (THEO MÔ HÌNH MoSCoW)

Phạm vi dự án được chốt chặt chẽ theo mục 7 của `PROJECT_BRIEF.md`, nghiêm cấm việc tự ý mở rộng phạm vi (chống phình phạm vi - Scope Creep):

### 3.1 Mức Must Have (Bắt buộc phải hoàn thành 100%)
- **Xác thực & Phân quyền:** Đăng nhập phân quyền dựa trên 4 vai trò (`CUSTOMER`, `CASHIER`, `STORE_MANAGER`, `ADMIN`). Hệ thống áp dụng 2 `SecurityFilterChain`: Stateless JWT cho `/api/**` và Form Login + Session cho trang web.
- **Quản lý Catalog:** CRUD danh mục sản phẩm (cấu trúc cây cha - con), thương hiệu, sản phẩm cha, biến thể sản phẩm (quản lý cụ thể màu sắc, kích cỡ, mã SKU, mã vạch barcode, giá bán lẻ, giá vốn, cân nặng), danh sách hình ảnh sản phẩm qua URL.
- **Quản lý Cửa hàng & Nhân viên:** Quản lý danh sách chi nhánh (cửa hàng bán lẻ, kho trung chuyển, địa chỉ, tọa độ GPS), hồ sơ nhân viên gắn liền với một cửa hàng cụ thể và chức vụ tương ứng.
- **Quản lý Tồn kho & Sổ kho:** Quản lý số lượng tồn kho theo từng cửa hàng (`inventories`) hỗ trợ khóa lạc quan `@Version`. Sổ cái biến động kho (`inventory_transactions`) dạng append-only ghi nhận chi tiết mọi giao dịch xuất, nhập, bán hàng, hủy đơn, điều chỉnh.
- **Phân hệ Shop (Website):** Xem danh sách sản phẩm, bộ lọc theo danh mục/thương hiệu/giá, tìm kiếm theo từ khóa, trang chi tiết sản phẩm và chọn biến thể màu/size, giỏ hàng cho khách hàng đã đăng nhập, luồng đặt hàng online (COD và thanh toán chuyển khoản/thẻ giả lập), áp dụng mã giảm giá (voucher).
- **Phân hệ POS (Quầy thu ngân):** Giao diện bán hàng chuyên dụng; tìm kiếm/quét mã SKU hoặc Barcode; thêm vào hóa đơn; áp dụng voucher; thanh toán đa hình thức (tiền mặt, chuyển khoản QR, thẻ giả lập); tự động tính tiền thừa trả khách; in hóa đơn thanh toán (HTML/CSS Print view).
- **Phân hệ Admin (Quản trị):** Quản lý đơn hàng từ tất cả các kênh (Online và POS); chuyển đổi trạng thái đơn hàng theo quy trình chuẩn; quản lý mã giảm giá voucher (mã, thời hạn, giới hạn số lượt dùng, giá trị giảm phần trăm hoặc cố định); dashboard thống kê (doanh thu theo cửa hàng, theo ngày, theo kênh bán; top sản phẩm bán chạy; danh sách cảnh báo tồn kho sắp hết dưới mức `reorder_level`).

### 3.2 Mức Should Have (Nên có nếu đúng tiến độ)
- **Quản lý ca thu ngân (`pos_shifts`):** Quy trình mở ca, đóng ca thu ngân, bàn giao tiền mặt đầu ca, kiểm đếm tiền mặt cuối ca và đối soát tiền thực thu với số liệu hệ thống.
- **Điều chuyển hàng giữa các cửa hàng (`stock_transfers`, `stock_transfer_items`):** Lập phiếu chuyển kho, trạng thái chuyển kho (Nháp -> Đang chuyển -> Đã nhận -> Đã hủy).
- **Định tuyến đơn hàng thông minh:** Đơn hàng online tự động gợi ý chọn cửa hàng gần địa chỉ khách hàng nhất còn đủ hàng và tính toán cước phí vận chuyển dựa trên khoảng cách địa lý (sử dụng công thức Haversine tính từ tọa độ lat/lng).

### 3.3 Mức Could Have (Có thể làm nếu dư dả thời gian)
- Đánh giá và chấm điểm sao sản phẩm (`reviews`) cho đơn hàng đã hoàn tất.
- Tích lũy điểm thưởng thành viên thân thiết khi mua sắm.
- Quy trình tiếp nhận yêu cầu đổi trả hàng tại cửa hàng hoặc online.

### 3.4 Mức Won't Have (Không thực hiện trong phạm vi đồ án, ghi làm hướng mở rộng)
- Các sự kiện Flash sale đếm ngược thời gian thực.
- Hệ thống quản lý tin tức, bài viết (CMS/Blog).
- Hệ thống tiếp nhận Ticket hỗ trợ chăm sóc khách hàng (CSKH), tổng đài.
- Quản lý chính sách bảo hành điện tử chuyên sâu.
- Tích hợp cổng SMS OTP thực tế, tích hợp mạng xã hội OAuth (Google/Facebook bên ngoài).
- Quy trình tạo đơn nhập hàng từ nhà cung cấp bên ngoài (PO - Purchase Order).
- Kết nối API cổng thanh toán thật (VNPay, MoMo, ZaloPay sandbox) hoặc đơn vị vận chuyển thật (GHN, GHTK).
- Giỏ hàng cho khách vãng lai (yêu cầu khách đăng nhập trước khi mua).
- Mô hình thuộc tính động EAV phức tạp (thay bằng cột `color`, `size` cố định trong bảng biến thể).

---

## 4. CÁC TÁC NHÂN HỆ THỐNG (ACTORS)

| STT | Actor | Vai trò trong hệ thống | Phân hệ sử dụng | Mô tả trách nhiệm chính |
|:---:|---|---|---|---|
| 1 | **Khách hàng (`CUSTOMER`)** | Người mua hàng trực tuyến | Shop (Website) | Duyệt sản phẩm, tìm kiếm, quản lý giỏ hàng cá nhân, nhập địa chỉ nhận hàng, đặt đơn online, áp dụng mã voucher, tra cứu lịch sử mua hàng cá nhân. |
| 2 | **Thu ngân (`CASHIER`)** | Nhân viên bán hàng tại quầy | POS (Point of Sale) | Tra cứu nhanh thông tin sản phẩm bằng SKU/mã vạch, tạo hóa đơn bán tại quầy, áp dụng voucher, nhận tiền và tính tiền thối cho khách, in hóa đơn xuất cho khách hàng. |
| 3 | **Quản lý cửa hàng (`STORE_MANAGER`)** | Quản lý vận hành chi nhánh | POS, Admin | Toàn quyền thao tác trên POS tại chi nhánh; truy cập phân hệ Admin nhưng bị giới hạn phạm vi dữ liệu: chỉ xem/sửa tồn kho, đơn hàng, nhân viên và doanh thu thuộc chi nhánh mình phụ trách. |
| 4 | **Quản trị viên hệ thống (`ADMIN`)** | Quản trị cấp cao nhất | Admin toàn hệ thống | Toàn quyền kiểm soát hệ thống: quản lý danh mục, sản phẩm, giá bán, toàn bộ cửa hàng, nhân viên, kho hàng toàn chuỗi, duyệt đơn toàn kênh, cấu hình voucher và xem báo cáo tổng hợp cấp tập đoàn/chuỗi. |

---

## 5. CÁC GIẢ ĐỊNH VÀ RÀNG BUỘC DỰ ÁN

### 5.1 Giả định (Assumptions)
1. Cơ sở dữ liệu Microsoft SQL Server phiên bản 2019/2022 đã được cài đặt sẵn trên môi trường cục bộ và kết nối ổn định qua cổng 1433.
2. Các thư viện CSS/JS giao diện (Bootstrap 5, Bootstrap Icons, Chart.js) được nạp trực tiếp qua mạng Internet (CDN) để tinh gọn mã nguồn, môi trường phát triển và máy tính demo luôn có kết nối mạng ổn định.
3. Kênh thanh toán trực tuyến (thẻ tín dụng, chuyển khoản ngân hàng) và POS (quẹt thẻ, quét QR) được mô phỏng giả lập thành công qua phần mềm mà không cần ký hợp đồng cổng thanh toán thật.
4. Mỗi nhân viên chỉ thuộc biên chế làm việc tại một cửa hàng vật lý duy nhất tại một thời điểm.
5. **Hạn chót nộp bài ngày 17/11/2026 là GIẢ ĐỊNH:** Hiện tại nhà trường/khoa chưa có thông báo chính thức về lịch nộp bài và thi vấn đáp, ngày 17/11/2026 được ước lượng dựa trên khung lịch thi chung của các môn học khác trong học kỳ.

### 5.2 Ràng buộc (Constraints)
1. **Ràng buộc nhân sự:** Dự án do **01 người duy nhất** đảm nhiệm toàn bộ các vai trò: Quản lý dự án (PM), Phân tích nghiệp vụ (BA), Kiến trúc sư hệ thống, Lập trình viên Full-stack (Dev) và Chuyên viên kiểm thử (QA/QC).
2. **Ràng buộc thời gian & Quy tắc điều hành tiến độ:** 
   - Tổng thời lượng dự án: **14 ngày làm việc liên tục** (áp dụng trục ngày tương đối **N1 – N14**, ngày N1 tương ứng **03/11/2026**).
   - Hạn chót nộp bài: **17/11/2026 là GIẢ ĐỊNH** (chưa có thông báo chính thức, ước lượng theo lịch thi các môn khác).
   - **Quy tắc điều hành tiến độ theo ngày tương đối (N1–N14):**
     - Quản lý toàn bộ tiến độ bằng ngày tương đối N1–N14; khi có ngày nộp chính thức chỉ cần tịnh tiến ngày dương lịch tương ứng, không xáo trộn cấu trúc WBS.
     - **Mốc đóng băng mã nguồn (Code Freeze):** Luôn ấn định bằng **Hạn nộp chính thức trừ đi 3 ngày** (tương ứng ngày **N12**). Sau mốc này tuyệt đối không viết thêm mã nguồn hay sửa đổi tính năng.
     - **Nếu hạn nộp thật MUỘN hơn 17/11/2026:** **Tuyệt đối KHÔNG mở rộng phạm vi (Zero Scope Creep)**, không thêm tính năng mới; phần thời gian dôi dư được dùng làm vùng đệm (buffer) tăng cường kiểm thử, diễn tập demo và hoàn thiện 2 bản báo cáo học phần.
     - **Nếu hạn nộp thật SỚM hơn 17/11/2026:** Kích hoạt ngay lập tức danh mục cắt giảm phạm vi (**Cut-list**) sớm hơn tương ứng (cắt toàn bộ mức Should/Could) để bảo vệ 100% phạm vi 24 bảng Must và hoàn thành đóng băng mã nguồn trước hạn nộp 3 ngày.
   - **Lịch bận / ngày làm ít giờ:** Không có ngày bận; toàn bộ 14 ngày làm việc đều đặn theo đúng định mức chuẩn 5.0 giờ/ngày.
3. **Ràng buộc công sức:** 
   - Số giờ làm việc hiệu quả mỗi ngày: **5.0 giờ/ngày** (áp dụng đồng đều cho cả 14 ngày).
   - Tổng quỹ thời gian làm việc toàn dự án: $14 \text{ ngày} \times 5.0 \text{ giờ} = 70.0 \text{ giờ}$.
4. **Ràng buộc công nghệ & kiến trúc:**
   - Ngôn ngữ: Java 17; Framework: Spring Boot 3.x MVC.
   - Giao diện: Server-side Rendering bằng Thymeleaf kết hợp Bootstrap 5, gom toàn bộ trong một project duy nhất (Monolith tinh gọn).
   - Cơ sở dữ liệu & Migration: Microsoft SQL Server, quản lý phiên bản lược đồ bằng Flyway với cơ chế `ddl-auto: validate`. Tuyệt đối không sửa migration đã chạy.
   - Phân chia URL: `/shop/**` cho khách hàng, `/pos/**` cho điểm bán lẻ, `/admin/**` cho ban quản trị, `/api/v1/**` cho các API phục vụ gọi AJAX.
5. **Ràng buộc phương pháp thực hiện:**
   - 100% mã nguồn được hỗ trợ sinh bởi AI. Nhà phát triển đóng vai trò thẩm định, đọc hiểu kiến trúc, giám sát quy ước đặt tên và kiểm soát chất lượng qua từng ngày.
   - Mỗi lát cắt công việc hàng ngày phải kết thúc bằng lệnh build thật (`mvn clean compile`) với kết quả `BUILD SUCCESS` trước khi bàn giao.

---

## 6. THUẬT NGỮ & KHÁI NIỆM NGHIỆP VỤ CHUYÊN NGÀNH

- **Sản phẩm cha (Parent Product / Generic Product):** Khái niệm đại diện cho mẫu sản phẩm giày dép chung (ví dụ: *"Giày Sneaker Nike Air Force 1 Nam"*). Sản phẩm cha mang các thuộc tính chung như tên, mô tả, danh mục, thương hiệu nhưng **không thể bán trực tiếp và không quản lý số lượng tồn kho**.
- **Biến thể sản phẩm (Product Variant):** Thực thể hàng hóa cụ thể được tạo ra từ sự kết hợp của Sản phẩm cha với Màu sắc (`color`) và Kích thước size EU (`size`) (ví dụ: *"Giày Sneaker Nike Air Force 1 Nam - Màu Trắng - Size 40"*). Đây là đơn vị thương mại trực tiếp được định giá bán (`price`), giá vốn (`cost_price`), cân nặng và quản lý số lượng tồn kho.
- **SKU (Stock Keeping Unit):** Mã định danh duy nhất cho từng biến thể sản phẩm dùng để phân loại và kiểm soát hàng hóa trong toàn bộ hệ thống kho (ví dụ: `SNK-NIKE-AF1-TRANG-40`). Mỗi biến thể có duy nhất một mã SKU không trùng lặp.
- **Barcode (Mã vạch):** Chuỗi ký tự số/mã vạch in trên tem nhãn dán vào sản phẩm vật lý, cho phép đầu đọc mã vạch tại quầy POS quét nhanh vào hóa đơn.
- **POS (Point of Sale):** Điểm bán lẻ hoặc ứng dụng màn hình bán hàng tại quầy thu ngân của cửa hàng vật lý, tập trung vào tốc độ xử lý đơn hàng và tính tiện dụng cho nhân viên thu ngân.
- **Tồn kho theo cửa hàng (Store Inventory):** Bản ghi thể hiện số lượng hàng hóa thực tế đang có sẵn (`quantity_on_hand`) của một biến thể cụ thể tại một chi nhánh cửa hàng xác định. Một biến thể có thể có số lượng tồn kho khác nhau tại các chi nhánh khác nhau.
- **Mức tồn tối thiểu (Reorder Level):** Ngưỡng cảnh báo tồn kho thấp. Khi số lượng thực tế trong cửa hàng giảm xuống dưới mức này, hệ thống sẽ cảnh báo trên Dashboard để quản lý kịp thời lập phiếu nhập hoặc chuyển hàng.
- **Sổ cái kho (Inventory Transaction):** Bảng ghi nhận lịch sử biến động kho theo nguyên tắc kế toán dồn tích (chỉ thêm mới, không bao giờ sửa/xóa - append-only). Mỗi giao dịch lưu rõ loại biến động (`SALE`, `CANCEL`, `IMPORT`, `TRANSFER_IN`, `TRANSFER_OUT`, `ADJUST`), số lượng thay đổi âm/dương, số tồn sau biến động và mã tham chiếu tương ứng.
- **Khóa lạc quan (Optimistic Locking):** Cơ chế chống xung đột đồng thời trong môi trường nhiều giao dịch cùng lúc. Bảng tồn kho sử dụng cột `version` (`@Version` trong JPA). Khi hai giao dịch cùng đọc một bản ghi tồn kho và cùng ghi lại, giao dịch đến sau sẽ bị chặn lại và báo lỗi nếu phiên bản đã bị giao dịch trước thay đổi.
- **Snapshot dữ liệu đơn hàng (Data Snapshotting):** Nguyên tắc bảo toàn tính toàn vẹn lịch sử. Khi đơn hàng được tạo, các thông tin như tên sản phẩm, mã SKU, màu sắc, kích cỡ, đơn giá tại thời điểm mua sẽ được sao chụp cố định vào bảng `order_items`. Nếu sau này sản phẩm cha bị sửa tên hoặc tăng/giảm giá, dữ liệu hóa đơn cũ vẫn giữ nguyên vẹn giá trị lịch sử.
- **Ca thu ngân (POS Shift):** Phiên làm việc độc lập của thu ngân tại quầy tính tiền, bắt đầu từ lúc nhận quầy và khai báo số tiền mặt ban đầu (`opening_cash`) đến lúc chốt ca, kiểm đếm số tiền thực tế (`closing_cash`) và bàn giao cho ca tiếp theo.
- **Voucher / Mã giảm giá:** Công cụ xúc tiến bán hàng, thiết lập mức giảm theo tỷ lệ phần trăm (`PERCENT`) kèm mức giảm tối đa, hoặc số tiền cố định (`FIXED`), đi kèm điều kiện giá trị đơn hàng tối thiểu (`min_order_value`) và giới hạn số lượt áp dụng.

