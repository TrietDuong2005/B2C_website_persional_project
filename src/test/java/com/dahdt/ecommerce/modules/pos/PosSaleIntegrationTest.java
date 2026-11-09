package com.dahdt.ecommerce.modules.pos;

import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleCommand;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleItemCommand;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleResponseDTO;
import com.dahdt.ecommerce.modules.pos.service.PosSaleService;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * TEST TÍCH HỢP: BÁN HÀNG TẠI QUẦY POS TOÀN TRÌNH (PosSaleIntegrationTest)
 * ============================================================================
 * - Chạy trên Spring Boot context thật và CSDL SQL Server thật.
 * - Kiểm tra toàn bộ ràng buộc schema: CHECK constraint, FOREIGN KEY, UNIQUE order_code.
 * - @Transactional đảm bảo tự động ROLLBACK sau khi test kết thúc, tuyệt đối KHÔNG
 *   lưu lại dữ liệu rác trong cơ sở dữ liệu.
 * - Dùng Assumptions để kiểm tra tính sẵn sàng của dữ liệu mẫu (V28-V30, DevDataSeeder),
 *   tự động bỏ qua an toàn nếu chưa nạp đủ dữ liệu mẫu.
 * ============================================================================
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Test tích hợp: Bán hàng tại quầy POS toàn trình (Spring Boot + SQL Server)")
class PosSaleIntegrationTest {

    @Autowired
    private PosSaleService posSaleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherUsageRepository voucherUsageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Bán tại quầy bằng CASH có áp voucher: Trừ kho chi nhánh, ghi sổ kho SALE thu ngân, tăng used_count")
    void testPosSale_CashWithVoucher_Success() {
        // 1. Kiểm tra tài khoản thu ngân mẫu (từ DevDataSeeder)
        Optional<User> cashierOpt = userRepository.findByEmail("cashier@dahdt.com");
        Assumptions.assumeTrue(cashierOpt.isPresent(), "Cần tài khoản dev cashier@dahdt.com từ DevDataSeeder");
        User cashier = cashierOpt.get();

        Optional<Employee> empOpt = employeeRepository.findByUserId(cashier.getId());
        Assumptions.assumeTrue(empOpt.isPresent() && empOpt.get().getStore() != null,
                "Cần hồ sơ nhân viên của cashier@dahdt.com gắn với chi nhánh");
        Employee employee = empOpt.get();
        Store store = employee.getStore();

        // 2. Kiểm tra voucher mẫu GIAM50K (từ V30 seed_vouchers)
        Optional<Voucher> voucherOpt = voucherRepository.findByCode("GIAM50K");
        Assumptions.assumeTrue(voucherOpt.isPresent(), "Cần voucher GIAM50K từ migration V30");
        Voucher voucher = voucherOpt.get();
        int initialUsedCount = voucher.getUsedCount();

        // 3. Lấy ít nhất 2 biến thể có tồn kho >= 5 tại CỬA HÀNG CỦA THU NGÂN (không phải kho tổng)
        List<Inventory> storeInventories = inventoryRepository.findByStoreId(store.getId());
        List<Inventory> validInventories = storeInventories.stream()
                .filter(inv -> inv.getQuantityOnHand() != null && inv.getQuantityOnHand() >= 5)
                .filter(inv -> inv.getVariant() != null && inv.getVariant().isSellable())
                .limit(2)
                .toList();

        Assumptions.assumeTrue(validInventories.size() >= 2,
                "Cần ít nhất 2 biến thể có tồn kho >= 5 tại chi nhánh " + store.getCode() + " để test");

        Inventory inv1 = validInventories.get(0);
        Inventory inv2 = validInventories.get(1);

        int initialStock1 = inv1.getQuantityOnHand();
        int initialStock2 = inv2.getQuantityOnHand();

        BigDecimal price1 = inv1.getVariant().getPrice();
        BigDecimal price2 = inv2.getVariant().getPrice();
        BigDecimal expectedSubtotal = price1.add(price2);
        BigDecimal expectedDiscount = new BigDecimal("50000"); // GIAM50K
        BigDecimal expectedTotal = expectedSubtotal.subtract(expectedDiscount);

        // Khách đưa số tiền dư để có tiền thối
        BigDecimal cashReceived = expectedTotal.add(new BigDecimal("50000"));
        BigDecimal expectedChange = new BigDecimal("50000");

        // 4. Lập lệnh bán hàng PosSaleCommand
        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(
                        new PosSaleItemCommand(inv1.getVariant().getId(), 1),
                        new PosSaleItemCommand(inv2.getVariant().getId(), 1)
                ))
                .voucherCode("GIAM50K")
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(cashReceived)
                .build();

        // 5. Thực hiện checkout
        PosSaleResponseDTO response = posSaleService.checkout(cashier.getId(), command);
        assertNotNull(response, "Response từ checkout không được null");

        // Đồng bộ và xóa bộ đệm EntityManager để đọc dữ liệu thật từ DB
        entityManager.flush();
        entityManager.clear();

        // 6. Kiểm tra đơn hàng trong CSDL
        Order savedOrder = orderRepository.findByOrderCodeWithDetails(response.getOrderCode())
                .orElseThrow(() -> new AssertionError("Không tìm thấy đơn hàng POS vừa tạo với mã: " + response.getOrderCode()));

        assertTrue(savedOrder.getOrderCode().startsWith("HD"),
                "Mã đơn POS phải bắt đầu bằng HD, thực tế: " + savedOrder.getOrderCode());
        assertEquals(OrderChannel.POS, savedOrder.getChannel(),
                "Kênh đơn hàng phải là POS, thực tế: " + savedOrder.getChannel());
        assertEquals(OrderStatus.COMPLETED, savedOrder.getStatus(),
                "Trạng thái đơn hàng phải là COMPLETED, thực tế: " + savedOrder.getStatus());
        assertEquals(store.getId(), savedOrder.getStore().getId(),
                "Chi nhánh đơn hàng phải là chi nhánh thu ngân (" + store.getId() + "), thực tế: " + savedOrder.getStore().getId());
        assertNull(savedOrder.getCustomer(),
                "Đơn POS khách lẻ customer_id phải là null, thực tế: " + savedOrder.getCustomer());
        assertNotNull(savedOrder.getCashier(),
                "Thu ngân cashier không được null");
        assertEquals(cashier.getId(), savedOrder.getCashier().getId(),
                "Cashier ID kỳ vọng: " + cashier.getId() + ", thực tế: " + savedOrder.getCashier().getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(savedOrder.getShippingFee()),
                "Phí giao hàng POS phải bằng 0, thực tế: " + savedOrder.getShippingFee());
        assertEquals(0, expectedSubtotal.compareTo(savedOrder.getSubtotal()),
                "Tạm tính kỳ vọng: " + expectedSubtotal + ", thực tế: " + savedOrder.getSubtotal());
        assertEquals(0, expectedDiscount.compareTo(savedOrder.getDiscountTotal()),
                "Giảm giá kỳ vọng: " + expectedDiscount + ", thực tế: " + savedOrder.getDiscountTotal());
        assertEquals(0, expectedTotal.compareTo(savedOrder.getTotalAmount()),
                "Tổng tiền kỳ vọng: " + expectedTotal + ", thực tế: " + savedOrder.getTotalAmount());

        // Snapshot mặt hàng
        assertEquals(2, savedOrder.getItems().size(),
                "Số lượng mặt hàng snapshot kỳ vọng: 2, thực tế: " + savedOrder.getItems().size());
        for (OrderItem item : savedOrder.getItems()) {
            assertNotNull(item.getProductName(), "Tên sản phẩm snapshot không được null");
            assertNotNull(item.getSku(), "Mã SKU snapshot không được null");
            assertNotNull(item.getColor(), "Màu sắc snapshot không được null");
            assertNotNull(item.getSize(), "Kích cỡ snapshot không được null");
            assertNotNull(item.getUnitPrice(), "Đơn giá snapshot không được null");
            assertEquals(1, item.getQuantity(),
                "Số lượng mua snapshot kỳ vọng 1, thực tế: " + item.getQuantity());
            assertEquals(0, item.getUnitPrice().compareTo(item.getLineTotal()),
                "Thành tiền dòng kỳ vọng: " + item.getUnitPrice() + ", thực tế: " + item.getLineTotal());
        }

        // Lịch sử trạng thái (null -> COMPLETED do thu ngân thực hiện)
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(savedOrder.getId());
        assertFalse(histories.isEmpty(), "Lịch sử trạng thái đơn hàng không được rỗng");
        OrderStatusHistory initialHistory = histories.get(0);
        assertNull(initialHistory.getFromStatus(),
                "fromStatus ban đầu phải null, thực tế: " + initialHistory.getFromStatus());
        assertEquals(OrderStatus.COMPLETED, initialHistory.getToStatus(),
                "toStatus ban đầu phải COMPLETED, thực tế: " + initialHistory.getToStatus());
        assertEquals(cashier.getId(), initialHistory.getChangedBy().getId(),
                "Người chuyển trạng thái kỳ vọng thu ngân (" + cashier.getId() + "), thực tế: " + initialHistory.getChangedBy().getId());

        // Kiểm tra thanh toán CASH
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(savedOrder.getId())
                .orElseThrow(() -> new AssertionError("Không tìm thấy giao dịch thanh toán cho đơn hàng"));
        assertEquals(PaymentMethod.CASH, payment.getMethod(),
                "Phương thức thanh toán kỳ vọng CASH, thực tế: " + payment.getMethod());
        assertEquals(PaymentStatus.PAID, payment.getStatus(),
                "Trạng thái thanh toán kỳ vọng PAID, thực tế: " + payment.getStatus());
        assertEquals(0, expectedTotal.compareTo(payment.getAmount()),
                "Số tiền thanh toán kỳ vọng: " + expectedTotal + ", thực tế: " + payment.getAmount());
        assertNotNull(payment.getCashReceived(), "Tiền khách đưa không được null");
        assertEquals(0, cashReceived.compareTo(payment.getCashReceived()),
                "Tiền khách đưa kỳ vọng: " + cashReceived + ", thực tế: " + payment.getCashReceived());
        assertNotNull(payment.getChangeAmount(), "Tiền thối lại không được null");
        assertEquals(0, expectedChange.compareTo(payment.getChangeAmount()),
                "Tiền thối lại kỳ vọng: " + expectedChange + ", thực tế: " + payment.getChangeAmount());
        assertNotNull(payment.getPaidAt(), "paidAt không được null");

        // Kiểm tra tồn kho tại CỬA HÀNG (phải giảm đúng 1 đôi cho mỗi biến thể)
        Inventory inv1After = inventoryRepository.findByStoreIdAndVariantId(store.getId(), inv1.getVariant().getId()).orElseThrow();
        Inventory inv2After = inventoryRepository.findByStoreIdAndVariantId(store.getId(), inv2.getVariant().getId()).orElseThrow();
        assertEquals(initialStock1 - 1, inv1After.getQuantityOnHand(),
                "Tồn kho biến thể 1 tại chi nhánh kỳ vọng: " + (initialStock1 - 1) + ", thực tế: " + inv1After.getQuantityOnHand());
        assertEquals(initialStock2 - 1, inv2After.getQuantityOnHand(),
                "Tồn kho biến thể 2 tại chi nhánh kỳ vọng: " + (initialStock2 - 1) + ", thực tế: " + inv2After.getQuantityOnHand());

        // Kiểm tra dòng sổ kho SALE do thu ngân tạo
        List<InventoryTransaction> orderTxs = inventoryTransactionRepository.findAll().stream()
                .filter(tx -> "ORDER".equals(tx.getReferenceType()) && savedOrder.getId().equals(tx.getReferenceId()))
                .toList();
        assertEquals(2, orderTxs.size(),
                "Phải có đúng 2 dòng sổ kho SALE cho 2 biến thể, thực tế: " + orderTxs.size());
        for (InventoryTransaction tx : orderTxs) {
            assertEquals(InventoryTransactionType.SALE, tx.getType(),
                    "Loại giao dịch sổ kho kỳ vọng SALE, thực tế: " + tx.getType());
            assertEquals(-1, tx.getQuantityChange(),
                    "Số lượng thay đổi sổ kho kỳ vọng -1, thực tế: " + tx.getQuantityChange());
            assertNotNull(tx.getCreatedBy(), "created_by sổ kho không được null");
            assertEquals(cashier.getId(), tx.getCreatedBy().getId(),
                    "CreatedBy của sổ kho kỳ vọng thu ngân (" + cashier.getId() + "), thực tế: " + tx.getCreatedBy().getId());
            assertTrue(tx.getNote().contains("Bán tại quầy"),
                    "Note sổ kho phải chứa 'Bán tại quầy', thực tế: " + tx.getNote());
            assertTrue(tx.getNote().contains(savedOrder.getOrderCode()),
                    "Note sổ kho phải chứa mã đơn " + savedOrder.getOrderCode() + ", thực tế: " + tx.getNote());
        }

        // Kiểm tra voucher tăng used_count và có voucher_usages với user_id NULL
        // Đọc lại entity từ DB bằng entityManager.find(Lớp, id) thay vì refresh tham chiếu cũ đã bị detach
        entityManager.flush();
        Voucher voucherAfter = entityManager.find(Voucher.class, voucher.getId());
        assertNotNull(voucherAfter, "Voucher phải tồn tại trong CSDL sau khi bán hàng");
        assertEquals(initialUsedCount + 1, voucherAfter.getUsedCount(),
                "used_count của voucher phải tăng thêm 1. Giá trị ban đầu: " + initialUsedCount + ", giá trị thực tế: " + voucherAfter.getUsedCount());

        List<VoucherUsage> usages = voucherUsageRepository.findByOrderId(savedOrder.getId());
        assertEquals(1, usages.size(),
                "Phải có đúng 1 bản ghi ghi nhận voucher_usages cho đơn hàng, thực tế: " + usages.size());
        VoucherUsage usage = usages.get(0);
        assertEquals(voucher.getId(), usage.getVoucher().getId(),
                "Voucher ID trong voucher_usages kỳ vọng: " + voucher.getId() + ", thực tế: " + usage.getVoucher().getId());
        assertNull(usage.getUser(),
                "user_id trong voucher_usages phải là NULL đối với đơn POS khách vãng lai, thực tế: " + usage.getUser());
    }

    @Test
    @DisplayName("Bán tại quầy bằng CARD: Trạng thái PAID, có approval code, tiền đưa/thừa null, trừ tồn kho")
    void testPosSale_CardPayment_Success() {
        Optional<User> cashierOpt = userRepository.findByEmail("cashier@dahdt.com");
        Assumptions.assumeTrue(cashierOpt.isPresent(), "Cần tài khoản dev cashier@dahdt.com từ DevDataSeeder");
        User cashier = cashierOpt.get();

        Optional<Employee> empOpt = employeeRepository.findByUserId(cashier.getId());
        Assumptions.assumeTrue(empOpt.isPresent() && empOpt.get().getStore() != null,
                "Cần hồ sơ nhân viên của cashier@dahdt.com gắn với chi nhánh");
        Employee employee = empOpt.get();
        Store store = employee.getStore();

        List<Inventory> storeInventories = inventoryRepository.findByStoreId(store.getId());
        List<Inventory> validInventories = storeInventories.stream()
                .filter(inv -> inv.getQuantityOnHand() != null && inv.getQuantityOnHand() >= 5)
                .filter(inv -> inv.getVariant() != null && inv.getVariant().isSellable())
                .limit(1)
                .toList();

        Assumptions.assumeTrue(!validInventories.isEmpty(), "Cần ít nhất 1 biến thể có tồn kho >= 5 tại chi nhánh để test");
        Inventory inv = validInventories.get(0);
        int initialStock = inv.getQuantityOnHand();
        BigDecimal price = inv.getVariant().getPrice();

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(inv.getVariant().getId(), 1)))
                .voucherCode(null)
                .paymentMethod(PaymentMethod.CARD)
                .cashReceived(null)
                .build();

        PosSaleResponseDTO response = posSaleService.checkout(cashier.getId(), command);
        assertNotNull(response, "Response checkout thẻ không được null");

        entityManager.flush();
        entityManager.clear();

        Order savedOrder = orderRepository.findByOrderCodeWithDetails(response.getOrderCode())
                .orElseThrow(() -> new AssertionError("Không tìm thấy đơn hàng với mã: " + response.getOrderCode()));
        assertEquals(OrderChannel.POS, savedOrder.getChannel(),
                "Kênh đơn hàng phải là POS, thực tế: " + savedOrder.getChannel());
        assertEquals(OrderStatus.COMPLETED, savedOrder.getStatus(),
                "Trạng thái đơn hàng phải là COMPLETED, thực tế: " + savedOrder.getStatus());
        assertEquals(0, price.compareTo(savedOrder.getTotalAmount()),
                "Tổng tiền kỳ vọng: " + price + ", thực tế: " + savedOrder.getTotalAmount());

        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(savedOrder.getId())
                .orElseThrow(() -> new AssertionError("Không tìm thấy giao dịch thanh toán thẻ"));
        assertEquals(PaymentMethod.CARD, payment.getMethod(),
                "Phương thức thanh toán kỳ vọng CARD, thực tế: " + payment.getMethod());
        assertEquals(PaymentStatus.PAID, payment.getStatus(),
                "Trạng thái thanh toán kỳ vọng PAID, thực tế: " + payment.getStatus());
        assertNotNull(payment.getTransactionRef(), "Mã chuẩn y không được null");
        assertTrue(payment.getTransactionRef().startsWith("APPR-"),
                "Mã chuẩn y thẻ phải bắt đầu bằng APPR-, thực tế: " + payment.getTransactionRef());
        assertNull(payment.getCashReceived(),
                "Tiền khách đưa khi thanh toán thẻ phải là null, thực tế: " + payment.getCashReceived());
        assertNull(payment.getChangeAmount(),
                "Tiền thối khi thanh toán thẻ phải là null, thực tế: " + payment.getChangeAmount());
        assertNotNull(payment.getPaidAt(), "paidAt thanh toán thẻ không được null");

        Inventory invAfter = inventoryRepository.findByStoreIdAndVariantId(store.getId(), inv.getVariant().getId()).orElseThrow();
        assertEquals(initialStock - 1, invAfter.getQuantityOnHand(),
                "Tồn kho sau khi bán thẻ kỳ vọng: " + (initialStock - 1) + ", thực tế: " + invAfter.getQuantityOnHand());
    }
}
