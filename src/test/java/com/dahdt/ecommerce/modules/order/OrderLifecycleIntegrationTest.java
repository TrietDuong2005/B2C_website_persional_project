package com.dahdt.ecommerce.modules.order;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import com.dahdt.ecommerce.modules.cart.repository.CartRepository;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.PlaceOrderCommand;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.service.OrderService;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * TEST TÍCH HỢP: VÒNG ĐỜI ĐƠN HÀNG, STATE PATTERN, HOÀN KHO OBSERVER
 * (OrderLifecycleIntegrationTest)
 * ============================================================================
 * - Chạy trên Spring Boot context thật và CSDL SQL Server thật.
 * - @Transactional tự động rollback sau mỗi test method, đảm bảo không để lại dữ liệu rác.
 * - Kiểm tra:
 *   (a) Vòng đời trọn vẹn: PENDING -> CONFIRMED -> PACKING -> SHIPPING -> COMPLETED,
 *       kiểm tra completed_at, lịch sử trạng thái đủ 5 bước, payment COD chuyển PAID.
 *   (b) Hủy đơn có voucher: hoàn kho kho tổng, có dòng sổ kho CANCEL số lượng dương,
 *       voucher used_count giảm 1, payment COD chuyển FAILED.
 *   (c) Chống đổi hai lần cùng lúc: hủy 2 lần thì lần 2 bị từ chối và kho chỉ hoàn đúng 1 lần.
 *   (d) Chuyển trạng thái không hợp lệ bị từ chối.
 *   (e) Khách hàng tự hủy đơn của chính mình khi đang PENDING thành công.
 * ============================================================================
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Test tích hợp: Vòng đời đơn hàng, State Pattern và Hoàn kho (Spring Boot + SQL Server)")
class OrderLifecycleIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Dọn sạch SecurityContextHolder sau mỗi test method để không ảnh hưởng giữa các ca kiểm thử.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Thiết lập Authentication hợp lệ vào SecurityContextHolder bằng CustomUserDetails của tài khoản thực hiện.
     */
    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * So sánh tiền tệ chuẩn xác với compareTo (theo quy định dự án).
     */
    private static void assertMoneyEquals(BigDecimal expected, BigDecimal actual, String fieldName) {
        assertNotNull(actual, fieldName + " không được null");
        assertEquals(0, expected.compareTo(actual),
                fieldName + " không khớp: mong đợi " + expected + " nhưng thực tế là " + actual);
    }

    /**
     * Tính toán số lượng cần mua sao cho (price * quantity >= minOrderValue).
     * Không gõ cứng giá trị tối thiểu, mà suy ra động từ cấu hình voucher.
     */
    private int calculateNeededQuantity(BigDecimal price, BigDecimal minOrderValue) {
        if (minOrderValue == null || minOrderValue.compareTo(BigDecimal.ZERO) <= 0) {
            return 1;
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return 1;
        }
        int qty = minOrderValue.divide(price, 0, RoundingMode.CEILING).intValue();
        return Math.max(qty, 1);
    }

    /**
     * Tìm biến thể tại kho tổng đủ số lượng tồn để tạm tính >= minOrderValue.
     */
    private Inventory findEligibleInventory(Store warehouse, BigDecimal minOrderValue) {
        List<Inventory> warehouseInventories = inventoryRepository.findByStoreId(warehouse.getId());
        for (Inventory inv : warehouseInventories) {
            if (inv.getVariant() == null || !inv.getVariant().isSellable() || inv.getVariant().getPrice() == null) {
                continue;
            }
            BigDecimal price = inv.getVariant().getPrice();
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            int neededQty = calculateNeededQuantity(price, minOrderValue);
            if (inv.getQuantityOnHand() != null && inv.getQuantityOnHand() >= neededQty + 2) {
                return inv;
            }
        }
        return null;
    }

    /**
     * Helper tạo và đặt 1 đơn hàng online COD cho khách hàng mẫu.
     * Tự động đọc min_order_value của voucher trong DB (nếu có) và chọn số lượng đủ điều kiện.
     */
    private Order createSampleOnlineOrder(String voucherCode) {
        Optional<User> customerOpt = userRepository.findByEmail("customer@dahdt.com");
        Assumptions.assumeTrue(customerOpt.isPresent(), "Cần tài khoản dev customer@dahdt.com");
        User customer = customerOpt.get();

        Optional<Store> warehouseOpt = storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
        Assumptions.assumeTrue(warehouseOpt.isPresent(), "Cần kho tổng WAREHOUSE đang ACTIVE");
        Store warehouse = warehouseOpt.get();

        BigDecimal minOrderValue = BigDecimal.ZERO;
        if (voucherCode != null && !voucherCode.isBlank()) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode);
            Assumptions.assumeTrue(voucherOpt.isPresent(), "Cần voucher " + voucherCode + " trong CSDL");
            Voucher voucher = voucherOpt.get();
            if (voucher.getMinOrderValue() != null) {
                minOrderValue = voucher.getMinOrderValue();
            }
        }

        Inventory inv = findEligibleInventory(warehouse, minOrderValue);
        Assumptions.assumeTrue(inv != null,
                "Cần ít nhất 1 biến thể tại kho tổng đủ số lượng tồn để đạt giá trị tối thiểu của đơn hàng (min: " + minOrderValue + ")");

        int orderQty = calculateNeededQuantity(inv.getVariant().getPrice(), minOrderValue);

        // Chuẩn bị giỏ hàng
        cartRepository.findByUserIdAndStatus(customer.getId(), CartStatus.ACTIVE)
                .ifPresent(c -> {
                    c.setStatus(CartStatus.ABANDONED);
                    cartRepository.save(c);
                });

        Cart cart = Cart.builder()
                .user(customer)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        cart.addItem(inv.getVariant(), orderQty);
        cartRepository.save(cart);

        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .shipName("Nguyễn Văn A")
                .shipPhone("0901234567")
                .shipAddress("123 Nguyễn Trãi, Quận 1, TP.HCM")
                .note("Đơn test vòng đời")
                .voucherCode(voucherCode)
                .paymentMethod(PaymentMethod.COD)
                .build();

        OrderResponseDTO response = orderService.placeOrder(customer.getId(), command);
        entityManager.flush();

        return orderRepository.findById(response.getId())
                .orElseThrow(() -> new AssertionError("Không tìm thấy đơn hàng vừa tạo với ID: " + response.getId()));
    }

    // =========================================================================
    // KỊCH BẢN (A): ĐI HẾT VÒNG ĐỜI PENDING -> COMPLETED
    // =========================================================================
    @Test
    @DisplayName("(a) Đi hết vòng PENDING -> CONFIRMED -> PACKING -> SHIPPING -> COMPLETED, kiểm tra completed_at và COD PAID")
    void testFullOrderLifecycle_PendingToCompleted() {
        User admin = userRepository.findByEmail("admin@dahdt.com")
                .orElseThrow(() -> new AssertionError("Cần tài khoản admin@dahdt.com"));
        authenticateAs(admin);

        Order order = createSampleOnlineOrder(null);
        Long orderId = order.getId();
        assertEquals(OrderStatus.PENDING, order.getStatus(),
                "Trạng thái khởi tạo phải là PENDING, thực tế là: " + order.getStatus());

        // 1. PENDING -> CONFIRMED
        orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, admin.getId(), "Admin xác nhận đơn");
        entityManager.flush();
        Order o1 = orderRepository.findById(orderId).orElseThrow();
        entityManager.refresh(o1);
        assertEquals(OrderStatus.CONFIRMED, o1.getStatus(),
                "Đơn hàng phải chuyển sang CONFIRMED, thực tế là: " + o1.getStatus());

        // 2. CONFIRMED -> PACKING
        orderStatusService.changeStatus(orderId, OrderStatus.PACKING, admin.getId(), "Kho bắt đầu đóng gói");
        entityManager.flush();
        Order o2 = orderRepository.findById(orderId).orElseThrow();
        entityManager.refresh(o2);
        assertEquals(OrderStatus.PACKING, o2.getStatus(),
                "Đơn hàng phải chuyển sang PACKING, thực tế là: " + o2.getStatus());

        // 3. PACKING -> SHIPPING
        orderStatusService.changeStatus(orderId, OrderStatus.SHIPPING, admin.getId(), "Bàn giao vận chuyển");
        entityManager.flush();
        Order o3 = orderRepository.findById(orderId).orElseThrow();
        entityManager.refresh(o3);
        assertEquals(OrderStatus.SHIPPING, o3.getStatus(),
                "Đơn hàng phải chuyển sang SHIPPING, thực tế là: " + o3.getStatus());

        // 4. SHIPPING -> COMPLETED
        orderStatusService.changeStatus(orderId, OrderStatus.COMPLETED, admin.getId(), "Giao hàng thành công");
        entityManager.flush();
        Order o4 = orderRepository.findById(orderId).orElseThrow();
        entityManager.refresh(o4);
        assertEquals(OrderStatus.COMPLETED, o4.getStatus(),
                "Đơn hàng phải chuyển sang COMPLETED, thực tế là: " + o4.getStatus());
        assertNotNull(o4.getCompletedAt(), "Thời điểm completed_at phải được gán giá trị thời gian thực tế");

        // Kiểm tra lịch sử trạng thái (đủ 5 bước: tạo ban đầu + 4 lần chuyển)
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
        assertEquals(5, histories.size(),
                "Lịch sử trạng thái phải có đủ 5 bản ghi ghi nhận. Thực tế là: " + histories.size());

        assertEquals(OrderStatus.PENDING, histories.get(0).getToStatus(),
                "Lịch sử bước 0 phải là PENDING, thực tế là: " + histories.get(0).getToStatus());
        assertEquals(OrderStatus.CONFIRMED, histories.get(1).getToStatus(),
                "Lịch sử bước 1 phải là CONFIRMED, thực tế là: " + histories.get(1).getToStatus());
        assertEquals(OrderStatus.PACKING, histories.get(2).getToStatus(),
                "Lịch sử bước 2 phải là PACKING, thực tế là: " + histories.get(2).getToStatus());
        assertEquals(OrderStatus.SHIPPING, histories.get(3).getToStatus(),
                "Lịch sử bước 3 phải là SHIPPING, thực tế là: " + histories.get(3).getToStatus());
        assertEquals(OrderStatus.COMPLETED, histories.get(4).getToStatus(),
                "Lịch sử bước 4 phải là COMPLETED, thực tế là: " + histories.get(4).getToStatus());

        // Kiểm tra giao dịch thanh toán COD tự động chuyển sang PAID
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        assertFalse(payments.isEmpty(), "Đơn hàng phải có bản ghi thanh toán");
        Payment codPayment = payments.get(0);
        assertEquals(PaymentMethod.COD, codPayment.getMethod(),
                "Phương thức thanh toán phải là COD, thực tế là: " + codPayment.getMethod());
        assertEquals(PaymentStatus.PAID, codPayment.getStatus(),
                "Thanh toán COD khi hoàn tất đơn phải có status = PAID, thực tế là: " + codPayment.getStatus());
        assertNotNull(codPayment.getPaidAt(), "Thời điểm paid_at của thanh toán COD phải được gán thời gian thực");
    }

    // =========================================================================
    // KỊCH BẢN (B): ĐẶT ĐƠN CÓ VOUCHER RỒI HỦY -> HOÀN KHO, HOÀN VOUCHER, HOÀN TIỀN
    // =========================================================================
    @Test
    @DisplayName("(b) Đặt đơn có voucher rồi hủy: Tồn kho trở lại ban đầu, có sổ kho CANCEL (+), used_count giảm 1, payment FAILED")
    void testCancelOrderWithVoucher_RestoresInventoryAndVoucher() {
        User admin = userRepository.findByEmail("admin@dahdt.com")
                .orElseThrow(() -> new AssertionError("Cần tài khoản admin@dahdt.com"));

        Optional<Voucher> voucherOpt = voucherRepository.findByCode("GIAM50K");
        Assumptions.assumeTrue(voucherOpt.isPresent(), "Cần voucher GIAM50K trong CSDL");
        Voucher voucherBefore = voucherOpt.get();
        BigDecimal minOrderValue = voucherBefore.getMinOrderValue() != null ? voucherBefore.getMinOrderValue() : BigDecimal.ZERO;
        int vCountBefore = voucherBefore.getUsedCount();

        Optional<Store> warehouseOpt = storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
        Assumptions.assumeTrue(warehouseOpt.isPresent(), "Cần kho tổng WAREHOUSE đang ACTIVE");
        Store warehouse = warehouseOpt.get();

        Inventory targetInv = findEligibleInventory(warehouse, minOrderValue);
        Assumptions.assumeTrue(targetInv != null,
                "Cần ít nhất 1 biến thể tại kho tổng đủ số lượng tồn để đạt giá trị tối thiểu của voucher (min: " + minOrderValue + ")");
        int initialStock = targetInv.getQuantityOnHand();
        Long variantId = targetInv.getVariant().getId();
        int orderQty = calculateNeededQuantity(targetInv.getVariant().getPrice(), minOrderValue);

        // 1. Đặt đơn online có voucher GIAM50K
        Order order = createSampleOnlineOrder("GIAM50K");
        Long orderId = order.getId();
        entityManager.flush();

        // Kiểm tra tồn kho đã bị trừ đúng số lượng đặt khi đặt đơn
        Inventory invAfterPlace = inventoryRepository.findByStoreIdAndVariantId(warehouse.getId(), variantId).orElseThrow();
        entityManager.refresh(invAfterPlace);
        assertEquals(initialStock - orderQty, invAfterPlace.getQuantityOnHand(),
                "Tồn kho sau khi đặt đơn phải giảm đúng " + orderQty + " đôi. Ban đầu: " + initialStock + ", sau đặt: " + invAfterPlace.getQuantityOnHand());

        // Kiểm tra used_count voucher đã tăng 1 (Đồng bộ và làm mới Persistence Context do câu UPDATE bulk @Modifying)
        entityManager.flush();
        Voucher vAfterPlace = voucherRepository.findByCode("GIAM50K").orElseThrow();
        entityManager.refresh(vAfterPlace);
        assertEquals(vCountBefore + 1, vAfterPlace.getUsedCount(),
                "Voucher used_count sau khi đặt phải tăng 1. Trước: " + vCountBefore + ", sau: " + vAfterPlace.getUsedCount());

        // 2. Admin thực hiện HỦY ĐƠN HÀNG (đặt Authentication của admin vào SecurityContextHolder)
        authenticateAs(admin);
        orderStatusService.changeStatus(orderId, OrderStatus.CANCELLED, admin.getId(), "Khách đổi ý muốn hủy đơn");
        entityManager.flush();

        // A. Kiểm tra tồn kho kho tổng ĐÃ TRỞ LẠI NHƯ BAN ĐẦU
        Inventory invAfterCancel = inventoryRepository.findByStoreIdAndVariantId(warehouse.getId(), variantId).orElseThrow();
        entityManager.refresh(invAfterCancel);
        assertEquals(initialStock, invAfterCancel.getQuantityOnHand(),
                "Tồn kho sau khi hủy đơn phải được hoàn lại đúng bằng ban đầu: " + initialStock + ", thực tế: " + invAfterCancel.getQuantityOnHand());

        // B. Kiểm tra Sổ cái giao dịch kho (Audit Log) có dòng CANCEL với số lượng DƯƠNG (+orderQty)
        List<InventoryTransaction> cancelTransactions = inventoryTransactionRepository.findAll().stream()
                .filter(tx -> "ORDER".equals(tx.getReferenceType()) && orderId.equals(tx.getReferenceId()))
                .filter(tx -> tx.getType() == InventoryTransactionType.CANCEL)
                .toList();
        assertFalse(cancelTransactions.isEmpty(), "Phải có dòng sổ kho loại CANCEL ghi nhận việc hoàn kho");
        InventoryTransaction cancelTx = cancelTransactions.get(0);

        assertEquals(orderQty, cancelTx.getQuantityChange(),
                "Số lượng biến động sổ kho của dòng CANCEL phải là số DƯƠNG (+" + orderQty + "), thực tế là: " + cancelTx.getQuantityChange());
        assertEquals(initialStock, cancelTx.getQuantityAfter(),
                "Tồn sau biến động phải bằng tồn ban đầu: " + initialStock + ", thực tế là: " + cancelTx.getQuantityAfter());
        assertEquals("ORDER", cancelTx.getReferenceType(),
                "Loại tham chiếu sổ kho phải là ORDER, thực tế là: " + cancelTx.getReferenceType());
        assertEquals(admin.getId(), cancelTx.getCreatedBy().getId(),
                "Người tạo giao dịch kho phải là admin ID = " + admin.getId() + ", thực tế là: " + cancelTx.getCreatedBy().getId());
        assertTrue(cancelTx.getNote().contains(order.getOrderCode()),
                "Ghi chú sổ kho phải có mã đơn hàng. Ghi chú: " + cancelTx.getNote() + ", mã đơn: " + order.getOrderCode());

        // C. Kiểm tra used_count của voucher GIẢM 1 (hoàn lượt sử dụng)
        entityManager.flush();
        Voucher vAfterCancel = voucherRepository.findByCode("GIAM50K").orElseThrow();
        entityManager.refresh(vAfterCancel);
        assertEquals(vCountBefore, vAfterCancel.getUsedCount(),
                "Voucher used_count sau khi hủy đơn phải giảm 1 về mức ban đầu: " + vCountBefore + ", thực tế là: " + vAfterCancel.getUsedCount());

        // D. Kiểm tra thanh toán COD PENDING chuyển sang FAILED
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        assertFalse(payments.isEmpty(), "Đơn hàng phải có bản ghi thanh toán");
        assertEquals(PaymentStatus.FAILED, payments.get(0).getStatus(),
                "Thanh toán COD PENDING khi hủy phải chuyển FAILED, thực tế là: " + payments.get(0).getStatus());
    }

    // =========================================================================
    // KỊCH BẢN (C): HỦY HAI LẦN CHỈ HOÀN KHO ĐÚNG MỘT LẦN
    // =========================================================================
    @Test
    @DisplayName("(c) Bấm hủy 2 lần: Lần 2 bị từ chối và tồn kho chỉ hoàn đúng một lần duy nhất")
    void testDoubleCancel_OnlyRestoresOnce() {
        User admin = userRepository.findByEmail("admin@dahdt.com").orElseThrow();
        authenticateAs(admin);

        Optional<Store> warehouseOpt = storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
        Assumptions.assumeTrue(warehouseOpt.isPresent(), "Cần kho tổng WAREHOUSE đang ACTIVE");
        Store warehouse = warehouseOpt.get();

        Order order = createSampleOnlineOrder(null);
        Long orderId = order.getId();
        OrderItem item = order.getItems().get(0);
        Long variantId = item.getVariant().getId();
        int orderQty = item.getQuantity();
        entityManager.flush();

        Inventory invBeforeCancel = inventoryRepository.findByStoreIdAndVariantId(warehouse.getId(), variantId).orElseThrow();
        entityManager.refresh(invBeforeCancel);
        int stockBeforeCancel = invBeforeCancel.getQuantityOnHand();

        // Lần hủy 1: Thành công
        assertDoesNotThrow(() -> orderStatusService.changeStatus(orderId, OrderStatus.CANCELLED, admin.getId(), "Hủy lần 1"));
        entityManager.flush();

        Inventory invAfterFirstCancel = inventoryRepository.findByStoreIdAndVariantId(warehouse.getId(), variantId).orElseThrow();
        entityManager.refresh(invAfterFirstCancel);
        assertEquals(stockBeforeCancel + orderQty, invAfterFirstCancel.getQuantityOnHand(),
                "Tồn kho phải tăng đúng số lượng hủy (" + orderQty + ") sau lần hủy đầu. Trước: " + stockBeforeCancel + ", sau: " + invAfterFirstCancel.getQuantityOnHand());

        // Lần hủy 2: Bị từ chối vì đơn đã ở trạng thái CANCELLED
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderStatusService.changeStatus(orderId, OrderStatus.CANCELLED, admin.getId(), "Hủy lần 2"));
        assertNotNull(ex.getMessage(), "Phải có thông báo lỗi từ chối thao tác lần hai");

        // Tồn kho không bị tăng thêm lần nữa
        Inventory invAfterSecondCancel = inventoryRepository.findByStoreIdAndVariantId(warehouse.getId(), variantId).orElseThrow();
        entityManager.refresh(invAfterSecondCancel);
        assertEquals(stockBeforeCancel + orderQty, invAfterSecondCancel.getQuantityOnHand(),
                "Tồn kho sau lần bấm hủy thứ 2 không được tăng thêm lần nữa. Mong đợi: " + (stockBeforeCancel + orderQty) + ", thực tế: " + invAfterSecondCancel.getQuantityOnHand());

        // Kiểm tra sổ kho chỉ có đúng 1 dòng CANCEL duy nhất cho đơn này
        List<InventoryTransaction> cancelTransactions = inventoryTransactionRepository.findAll().stream()
                .filter(tx -> "ORDER".equals(tx.getReferenceType()) && orderId.equals(tx.getReferenceId()))
                .filter(tx -> tx.getType() == InventoryTransactionType.CANCEL)
                .toList();
        assertEquals(1, cancelTransactions.size(),
                "Dù bấm hủy 2 lần, chỉ có đúng 1 dòng sổ kho CANCEL được ghi nhận. Thực tế: " + cancelTransactions.size());
    }

    // =========================================================================
    // KỊCH BẢN (D): CHUYỂN TRẠNG THÁI KHÔNG HỢP LỆ BỊ TỪ CHỐI
    // =========================================================================
    @Test
    @DisplayName("(d) Chuyển trạng thái sai quy tắc vòng đời bị từ chối")
    void testInvalidTransitions_Rejected() {
        User admin = userRepository.findByEmail("admin@dahdt.com").orElseThrow();
        authenticateAs(admin);

        Order order = createSampleOnlineOrder(null);
        Long orderId = order.getId();
        entityManager.flush();

        // 1. Nhảy cóc từ PENDING trực tiếp sang SHIPPING -> Bị từ chối
        BadRequestException ex1 = assertThrows(BadRequestException.class,
                () -> orderStatusService.changeStatus(orderId, OrderStatus.SHIPPING, admin.getId(), "Nhảy cóc"));
        assertNotNull(ex1.getMessage(), "Phải có thông báo lỗi khi nhảy cóc sang SHIPPING");

        // 2. Nhảy cóc từ PENDING sang COMPLETED -> Bị từ chối
        BadRequestException ex2 = assertThrows(BadRequestException.class,
                () -> orderStatusService.changeStatus(orderId, OrderStatus.COMPLETED, admin.getId(), "Nhảy cóc"));
        assertNotNull(ex2.getMessage(), "Phải có thông báo lỗi khi nhảy cóc sang COMPLETED");

        // Đi đến SHIPPING theo đúng thứ tự hợp lệ
        orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, admin.getId(), "Xác nhận");
        orderStatusService.changeStatus(orderId, OrderStatus.PACKING, admin.getId(), "Đóng gói");
        orderStatusService.changeStatus(orderId, OrderStatus.SHIPPING, admin.getId(), "Đang giao");
        entityManager.flush();

        // 3. Đơn đang SHIPPING thì KHÔNG ĐƯỢC HỦY
        BadRequestException ex3 = assertThrows(BadRequestException.class,
                () -> orderStatusService.changeStatus(orderId, OrderStatus.CANCELLED, admin.getId(), "Hủy khi đang giao"));
        assertTrue(ex3.getMessage().contains("không thể hủy"),
                "Thông báo lỗi hủy đơn đang giao phải chứa 'không thể hủy', thực tế là: " + ex3.getMessage());

        // 4. Quay lui từ SHIPPING về PACKING -> Bị từ chối
        BadRequestException ex4 = assertThrows(BadRequestException.class,
                () -> orderStatusService.changeStatus(orderId, OrderStatus.PACKING, admin.getId(), "Quay lui"));
        assertNotNull(ex4.getMessage(), "Phải có thông báo lỗi khi quay lui về PACKING");
    }

    // =========================================================================
    // KỊCH BẢN (E): KHÁCH HÀNG TỰ HỦY ĐƠN HÀNG CỦA CHÍNH MÌNH (PENDING)
    // =========================================================================
    @Test
    @DisplayName("(e) Khách hàng tự hủy đơn của chính mình khi đơn đang PENDING thành công")
    void testCustomerSelfCancel_Success() {
        User customer = userRepository.findByEmail("customer@dahdt.com")
                .orElseThrow(() -> new AssertionError("Cần tài khoản dev customer@dahdt.com"));

        Order order = createSampleOnlineOrder(null);
        Long orderId = order.getId();
        entityManager.flush();

        // Đặt Authentication của khách hàng vào SecurityContextHolder
        authenticateAs(customer);

        // Khách tự hủy đơn của chính mình khi đơn đang PENDING
        assertDoesNotThrow(() -> orderStatusService.changeStatus(
                orderId, OrderStatus.CANCELLED, customer.getId(), "Khách tự đổi ý hủy đơn"));
        entityManager.flush();

        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        entityManager.refresh(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus(),
                "Đơn hàng phải chuyển sang CANCELLED sau khi khách tự hủy, thực tế: " + cancelledOrder.getStatus());
    }
}
