package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * ============================================================================
 * KIỂM THỬ DỰNG GIAO DIỆN TEMPLATE THYMELEAF (OrderTemplateRenderingIntegrationTest)
 * ============================================================================
 * Kiểm thử render thực tế (Full Spring Boot MVC context + Spring Security + Thymeleaf Engine):
 *  1. Đơn hàng ONLINE ở toàn bộ các trạng thái: PENDING, CONFIRMED, PACKING, SHIPPING, COMPLETED, CANCELLED.
 *  2. Đơn hàng POS tại quầy với mọi phương thức (CASH, CARD, QR) và mọi trạng thái thanh toán (PENDING, PAID, FAILED, REFUNDED).
 *  3. Khẳng định HTTP 200 và trang KHÔNG BỊ CỤT giữa chừng (chứa orderCode và các chuỗi định danh ở cuối template).
 *  4. Dựng trang chi tiết đơn hàng phía khách hàng (/account/orders/{code}).
 *  5. Dựng hóa đơn bán lẻ POS (/pos/receipts/{orderCode}).
 *  6. @Transactional tự động rollback toàn bộ dữ liệu mẫu sau mỗi ca kiểm thử.
 * ============================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Kiểm thử dựng giao diện template Thymeleaf: Chi tiết đơn hàng Admin, Khách hàng & Hóa đơn POS")
class OrderTemplateRenderingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    private User adminUser;
    private User customerUser;
    private Store sampleStore;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        // Lấy tài khoản ADMIN từ CSDL mẫu (do DevDataSeeder hoặc migration nạp)
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu")));

        // Lấy tài khoản CUSTOMER từ CSDL mẫu
        customerUser = userRepository.findByEmail("customer@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "CUSTOMER".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản CUSTOMER trong CSDL mẫu")));

        // Lấy chi nhánh mẫu
        sampleStore = storeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 chi nhánh Store trong CSDL mẫu"));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());

        CustomUserDetails customerDetails = new CustomUserDetails(customerUser);
        customerAuth = new UsernamePasswordAuthenticationToken(customerDetails, null, customerDetails.getAuthorities());

        // Đặt người dùng admin vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
    }

    @AfterEach
    void tearDown() {
        // Dọn sạch SecurityContextHolder sau mỗi test method
        SecurityContextHolder.clearContext();
    }

    /**
     * Tạo đơn hàng kiểm thử kèm snapshot dòng sản phẩm và lịch sử trạng thái ban đầu.
     */
    private Order createOrder(OrderChannel channel, OrderStatus status, User customer, User cashier) {
        String uniqueSuffix = System.currentTimeMillis() + "" + System.nanoTime() % 10000;
        String orderCode = "DH-" + channel + "-" + uniqueSuffix;

        Order order = Order.builder()
                .orderCode(orderCode)
                .channel(channel)
                .store(sampleStore)
                .customer(customer)
                .cashier(cashier)
                .status(status)
                .subtotal(new BigDecimal("500000.00"))
                .discountTotal(BigDecimal.ZERO)
                .shippingFee(channel == OrderChannel.ONLINE ? new BigDecimal("30000.00") : BigDecimal.ZERO)
                .totalAmount(channel == OrderChannel.ONLINE ? new BigDecimal("530000.00") : new BigDecimal("500000.00"))
                .shipName(channel == OrderChannel.ONLINE ? "Nguyễn Văn Nhận" : null)
                .shipPhone(channel == OrderChannel.ONLINE ? "0901234567" : null)
                .shipAddress(channel == OrderChannel.ONLINE ? "123 Đường Lê Lợi, Quận 1, TP.HCM" : null)
                .note("Đơn hàng kiểm thử dựng giao diện Thymeleaf")
                .placedAt(LocalDateTime.now().minusHours(2))
                .completedAt(status == OrderStatus.COMPLETED ? LocalDateTime.now().minusMinutes(10) : null)
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .statusHistories(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .productName("Giày Sneaker Running Pro X")
                .sku("SKU-RENDER-" + uniqueSuffix)
                .color("Đen / Trắng")
                .size("42")
                .unitPrice(new BigDecimal("500000.00"))
                .quantity(1)
                .lineTotal(new BigDecimal("500000.00"))
                .build();
        order.getItems().add(item);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(null)
                .toStatus(status)
                .changedBy(adminUser)
                .note("Khởi tạo đơn trạng thái: " + status)
                .changedAt(LocalDateTime.now().minusHours(1))
                .build();
        order.getStatusHistories().add(history);

        return orderRepository.save(order);
    }

    /**
     * Gắn giao dịch thanh toán vào đơn hàng và lưu ngay xuống CSDL.
     */
    private Payment addPayment(Order order, PaymentMethod method, PaymentStatus status,
                               BigDecimal cashReceived, BigDecimal changeAmount, String txnRef) {
        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(status)
                .cashReceived(cashReceived)
                .changeAmount(changeAmount)
                .transactionRef(txnRef)
                .paidAt(status == PaymentStatus.PAID ? LocalDateTime.now().minusMinutes(30) : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        order.getPayments().add(savedPayment);
        entityManager.flush();
        return savedPayment;
    }

    // =========================================================================
    // 1. KIỂM THỬ ĐƠN HÀNG ONLINE Ở TẤT CẢ CÁC TRẠNG THÁI (PENDING -> CANCELLED)
    // =========================================================================
    @ParameterizedTest(name = "Render chi tiết đơn Online trên Admin với trạng thái: {0}")
    @EnumSource(OrderStatus.class)
    @DisplayName("Đơn hàng ONLINE ở mọi trạng thái: Dựng trang hoàn chỉnh, không lỗi SpEL, đủ phần đuôi")
    void testRenderAdminOnlineOrder_AllStatuses(OrderStatus orderStatus) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        Order order = createOrder(OrderChannel.ONLINE, orderStatus, customerUser, null);
        addPayment(order, PaymentMethod.COD, PaymentStatus.PENDING, null, null, null);

        MvcResult result = mockMvc.perform(get("/admin/orders/{id}", order.getId())
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus,
                "HTTP Status của GET /admin/orders/" + order.getId() + " phải là 200 OK. Thực tế: " + httpStatus);

        assertTrue(html.contains(order.getOrderCode()),
                "Nội dung trang admin/order/order-detail phải chứa mã đơn hàng '" + order.getOrderCode()
                        + "'. Độ dài trang: " + html.length());

        // Khẳng định trang dựng đầy đủ đến CUỐI template (không bị cụt ở dòng 365 vì thiếu cashReceived)
        assertTrue(html.contains("Quay lại danh sách đơn hàng"),
                "Nội dung trang admin/order/order-detail phải dựng đầy đủ đến nút quay lại ở cuối trang. Độ dài trang: "
                        + html.length());

        assertTrue(html.contains("adminlte.min.js"),
                "Nội dung trang admin/order/order-detail phải chứa script adminlte.min.js từ adminFooter. Độ dài trang: "
                        + html.length());

        assertTrue(html.contains("globalConfirmModal"),
                "Nội dung trang admin/order/order-detail phải chứa modal xác nhận thao tác dùng chung ở footer. Độ dài trang: "
                        + html.length());

        // Nếu trạng thái cho phép hủy đơn (PENDING, CONFIRMED, PACKING), modal hủy đơn phải có mặt
        if (orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.PACKING) {
            assertTrue(html.contains("cancelOrderModal"),
                    "Đơn hàng Online ở trạng thái " + orderStatus + " phải render modal hủy đơn hàng (#cancelOrderModal). Độ dài trang: "
                            + html.length());
        }
    }

    // =========================================================================
    // 2. KIỂM THỬ ĐƠN HÀNG POS (CASH, CARD, QR) VỚI THANH TOÁN Ở MỌI TRẠNG THÁI
    // =========================================================================
    @Test
    @DisplayName("Đơn hàng POS thanh toán TIỀN MẶT (CASH) ở mọi trạng thái thanh toán (PENDING, PAID, FAILED, REFUNDED)")
    void testRenderAdminPosOrder_CashPayment_AllPaymentStatuses() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        PaymentStatus[] paymentStatuses = PaymentStatus.values();
        for (PaymentStatus pStatus : paymentStatuses) {
            Order order = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
            BigDecimal cashReceived = new BigDecimal("600000.00");
            BigDecimal changeAmount = new BigDecimal("100000.00");

            addPayment(order, PaymentMethod.CASH, pStatus, cashReceived, changeAmount, null);

            MvcResult result = mockMvc.perform(get("/admin/orders/{id}", order.getId())
                            .with(authentication(adminAuth)))
                    .andReturn();

            int httpStatus = result.getResponse().getStatus();
            String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertEquals(200, httpStatus,
                    "HTTP Status của GET /admin/orders/" + order.getId() + " [POS CASH - " + pStatus + "] phải là 200 OK. Thực tế: " + httpStatus);

            assertTrue(html.contains(order.getOrderCode()),
                    "Trang chi tiết đơn POS phải chứa mã đơn: " + order.getOrderCode() + ". Độ dài trang: " + html.length());

            // ĐỐI CHIẾU THỰC TẾ: cashReceived và changeAmount đã được map và hiển thị thành công
            assertTrue(html.contains("600.000 đ"),
                    "Trang chi tiết đơn POS tiền mặt phải hiển thị tiền khách đưa '600.000 đ'. Độ dài trang: " + html.length());

            assertTrue(html.contains("100.000 đ"),
                    "Trang chi tiết đơn POS tiền mặt phải hiển thị tiền thối lại '100.000 đ'. Độ dài trang: " + html.length());

            assertTrue(html.contains("Quay lại danh sách đơn hàng"),
                    "Trang chi tiết đơn POS tiền mặt phải dựng đầy đủ đến nút quay lại ở cuối trang. Độ dài trang: " + html.length());

            assertTrue(html.contains("adminlte.min.js"),
                    "Trang chi tiết đơn POS tiền mặt phải chứa adminFooter. Độ dài trang: " + html.length());
        }
    }

    @Test
    @DisplayName("Đơn hàng POS thanh toán THẺ (CARD) ở mọi trạng thái thanh toán (PENDING, PAID, FAILED, REFUNDED)")
    void testRenderAdminPosOrder_CardPayment_AllPaymentStatuses() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        PaymentStatus[] paymentStatuses = PaymentStatus.values();
        for (PaymentStatus pStatus : paymentStatuses) {
            Order order = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
            String txnRef = "TXN-CARD-" + pStatus + "-" + System.nanoTime() % 10000;

            addPayment(order, PaymentMethod.CARD, pStatus, null, null, txnRef);

            MvcResult result = mockMvc.perform(get("/admin/orders/{id}", order.getId())
                            .with(authentication(adminAuth)))
                    .andReturn();

            int httpStatus = result.getResponse().getStatus();
            String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertEquals(200, httpStatus,
                    "HTTP Status của GET /admin/orders/" + order.getId() + " [POS CARD - " + pStatus + "] phải là 200 OK. Thực tế: " + httpStatus);

            assertTrue(html.contains(order.getOrderCode()),
                    "Trang chi tiết đơn POS thẻ phải chứa mã đơn: " + order.getOrderCode() + ". Độ dài trang: " + html.length());

            assertTrue(html.contains(txnRef),
                    "Trang chi tiết đơn POS thẻ phải hiển thị mã tham chiếu giao dịch '" + txnRef + "'. Độ dài trang: " + html.length());

            assertTrue(html.contains("Quay lại danh sách đơn hàng"),
                    "Trang chi tiết đơn POS thẻ phải dựng đầy đủ đến nút quay lại ở cuối trang. Độ dài trang: " + html.length());

            assertTrue(html.contains("adminlte.min.js"),
                    "Trang chi tiết đơn POS thẻ phải chứa adminFooter. Độ dài trang: " + html.length());
        }
    }

    @Test
    @DisplayName("Đơn hàng POS thanh toán VIETQR (QR) ở mọi trạng thái thanh toán (PENDING, PAID, FAILED, REFUNDED)")
    void testRenderAdminPosOrder_QrPayment_AllPaymentStatuses() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        PaymentStatus[] paymentStatuses = PaymentStatus.values();
        for (PaymentStatus pStatus : paymentStatuses) {
            Order order = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
            String txnRef = "TXN-QR-" + pStatus + "-" + System.nanoTime() % 10000;

            addPayment(order, PaymentMethod.QR, pStatus, null, null, txnRef);

            MvcResult result = mockMvc.perform(get("/admin/orders/{id}", order.getId())
                            .with(authentication(adminAuth)))
                    .andReturn();

            int httpStatus = result.getResponse().getStatus();
            String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertEquals(200, httpStatus,
                    "HTTP Status của GET /admin/orders/" + order.getId() + " [POS QR - " + pStatus + "] phải là 200 OK. Thực tế: " + httpStatus);

            assertTrue(html.contains(order.getOrderCode()),
                    "Trang chi tiết đơn POS QR phải chứa mã đơn: " + order.getOrderCode() + ". Độ dài trang: " + html.length());

            assertTrue(html.contains(txnRef),
                    "Trang chi tiết đơn POS QR phải hiển thị mã tham chiếu giao dịch '" + txnRef + "'. Độ dài trang: " + html.length());

            assertTrue(html.contains("Quay lại danh sách đơn hàng"),
                    "Trang chi tiết đơn POS QR phải dựng đầy đủ đến nút quay lại ở cuối trang. Độ dài trang: " + html.length());

            assertTrue(html.contains("adminlte.min.js"),
                    "Trang chi tiết đơn POS QR phải chứa adminFooter. Độ dài trang: " + html.length());
        }
    }

    // =========================================================================
    // 3. DỰNG TRANG CHI TIẾT ĐƠN HÀNG PHÍA KHÁCH HÀNG (/account/orders/{code})
    // =========================================================================
    @Test
    @DisplayName("Khách hàng xem chi tiết đơn hàng: Dựng trang hoàn chỉnh, chứa mã đơn và footer khách")
    void testRenderCustomerOrderDetailPage() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(customerAuth);

        Order order = createOrder(OrderChannel.ONLINE, OrderStatus.PENDING, customerUser, null);
        addPayment(order, PaymentMethod.COD, PaymentStatus.PENDING, null, null, null);

        MvcResult result = mockMvc.perform(get("/account/orders/{code}", order.getOrderCode())
                        .with(authentication(customerAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus,
                "HTTP Status của GET /account/orders/" + order.getOrderCode() + " phải là 200 OK. Thực tế: " + httpStatus);

        assertTrue(html.contains(order.getOrderCode()),
                "Nội dung trang shop/order-detail phải chứa mã đơn hàng '" + order.getOrderCode() + "'. Độ dài trang: " + html.length());

        assertTrue(html.contains("Quay lại danh sách đơn"),
                "Nội dung trang shop/order-detail phải dựng đầy đủ đến nút 'Quay lại danh sách đơn' ở cuối trang. Độ dài trang: " + html.length());

        assertTrue(html.contains("bootstrap.bundle.min.js"),
                "Nội dung trang shop/order-detail phải chứa script bootstrap từ footer. Độ dài trang: " + html.length());
    }

    // =========================================================================
    // 4. DỰNG HÓA ĐƠN BÁN LẺ POS (/pos/receipts/{orderCode})
    // =========================================================================
    @Test
    @DisplayName("Dựng hóa đơn bán lẻ POS (/pos/receipts/{orderCode}) cho đơn CASH: hiển thị đủ tiền đưa, tiền thừa và lời cảm ơn cuối phiếu")
    void testRenderPosReceipt_CashPayment() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        Order order = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
        BigDecimal cashReceived = new BigDecimal("700000.00");
        BigDecimal changeAmount = new BigDecimal("200000.00");
        addPayment(order, PaymentMethod.CASH, PaymentStatus.PAID, cashReceived, changeAmount, null);

        MvcResult result = mockMvc.perform(get("/pos/receipts/{orderCode}", order.getOrderCode())
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus,
                "HTTP Status của GET /pos/receipts/" + order.getOrderCode() + " phải là 200 OK. Thực tế: " + httpStatus);

        assertTrue(html.contains(order.getOrderCode()),
                "Hóa đơn POS phải chứa mã đơn: " + order.getOrderCode() + ". Độ dài trang: " + html.length());

        assertTrue(html.contains("700.000 đ"),
                "Hóa đơn POS tiền mặt phải hiển thị tiền khách đưa '700.000 đ'. Độ dài trang: " + html.length());

        assertTrue(html.contains("200.000 đ"),
                "Hóa đơn POS tiền mặt phải hiển thị tiền thừa '200.000 đ'. Độ dài trang: " + html.length());

        // Khẳng định chuỗi ở CUỐI template hóa đơn
        assertTrue(html.contains("CẢM ƠN QUÝ KHÁCH & HẸN GẶP LẠI!"),
                "Hóa đơn POS phải dựng đầy đủ đến dòng kết thúc 'CẢM ƠN QUÝ KHÁCH & HẸN GẶP LẠI!'. Độ dài trang: " + html.length());

        assertTrue(html.contains("Phiếu bán hàng minh họa"),
                "Hóa đơn POS phải chứa ghi chú cuối phiếu 'Phiếu bán hàng minh họa'. Độ dài trang: " + html.length());
    }

    @Test
    @DisplayName("Dựng hóa đơn bán lẻ POS (/pos/receipts/{orderCode}) cho đơn CARD & QR: hiển thị mã chuẩn y và lời cảm ơn cuối phiếu")
    void testRenderPosReceipt_CardAndQrPayment() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        // Ca 1: Thẻ CARD
        Order orderCard = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
        String txnCard = "TXN-CARD-RECEIPT-" + System.nanoTime() % 10000;
        addPayment(orderCard, PaymentMethod.CARD, PaymentStatus.PAID, null, null, txnCard);

        MvcResult resCard = mockMvc.perform(get("/pos/receipts/{orderCode}", orderCard.getOrderCode())
                        .with(authentication(adminAuth)))
                .andReturn();

        int statusCard = resCard.getResponse().getStatus();
        String htmlCard = resCard.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, statusCard,
                "HTTP Status hóa đơn POS thẻ phải là 200 OK. Thực tế: " + statusCard);
        assertTrue(htmlCard.contains(txnCard),
                "Hóa đơn POS thẻ phải hiển thị mã giao dịch '" + txnCard + "'. Độ dài trang: " + htmlCard.length());
        assertTrue(htmlCard.contains("CẢM ƠN QUÝ KHÁCH & HẸN GẶP LẠI!"),
                "Hóa đơn POS thẻ phải dựng đến cuối phiếu. Độ dài trang: " + htmlCard.length());

        // Ca 2: Quét mã QR
        Order orderQr = createOrder(OrderChannel.POS, OrderStatus.COMPLETED, null, adminUser);
        String txnQr = "TXN-QR-RECEIPT-" + System.nanoTime() % 10000;
        addPayment(orderQr, PaymentMethod.QR, PaymentStatus.PAID, null, null, txnQr);

        MvcResult resQr = mockMvc.perform(get("/pos/receipts/{orderCode}", orderQr.getOrderCode())
                        .with(authentication(adminAuth)))
                .andReturn();

        int statusQr = resQr.getResponse().getStatus();
        String htmlQr = resQr.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, statusQr,
                "HTTP Status hóa đơn POS QR phải là 200 OK. Thực tế: " + statusQr);
        assertTrue(htmlQr.contains(txnQr),
                "Hóa đơn POS QR phải hiển thị mã giao dịch '" + txnQr + "'. Độ dài trang: " + htmlQr.length());
        assertTrue(htmlQr.contains("CẢM ƠN QUÝ KHÁCH & HẸN GẶP LẠI!"),
                "Hóa đơn POS QR phải dựng đến cuối phiếu. Độ dài trang: " + htmlQr.length());
    }
}

