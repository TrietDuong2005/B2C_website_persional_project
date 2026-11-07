package com.dahdt.ecommerce.modules.order;

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
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.service.OrderService;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * TEST TÍCH HỢP: ĐẶT HÀNG TRỰC TUYẾN TOÀN TRÌNH (OrderPlacementIntegrationTest)
 * ============================================================================
 * - Chạy trên Spring Boot context thật và CSDL SQL Server thật.
 * - Kiểm tra toàn bộ ràng buộc schema: CHECK constraint, FOREIGN KEY, UNIQUE order_code.
 * - @Transactional đảm bảo tự động ROLLBACK sau khi test kết thúc, tuyệt đối KHÔNG
 *   lưu lại dữ liệu rác trong cơ sở dữ liệu.
 * - Sử dụng Assumptions để kiểm tra tính sẵn sàng của dữ liệu mẫu (V28-V30, DevDataSeeder),
 *   tự động bỏ qua an toàn nếu chưa nạp đủ dữ liệu mẫu.
 * ============================================================================
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Test tích hợp: Đặt hàng online toàn trình (Spring Boot + SQL Server)")
class OrderPlacementIntegrationTest {

    @Autowired
    private OrderService orderService;

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
    private VoucherUsageRepository voucherUsageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Đặt đơn COD thành công: Trừ kho tổng, ghi sổ kho SALE, tăng used_count voucher, chuyển giỏ CONVERTED")
    void testPlaceOrderOnline_FullFlow() {
        // 1. Kiểm tra tài khoản khách hàng mẫu (từ DevDataSeeder)
        Optional<User> customerOpt = userRepository.findByEmail("customer@dahdt.com");
        Assumptions.assumeTrue(customerOpt.isPresent(), "Cần tài khoản dev customer@dahdt.com từ DevDataSeeder");
        User customer = customerOpt.get();

        // 2. Kiểm tra kho tổng trực tuyến (từ V28 seed_stores)
        Optional<Store> warehouseOpt = storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
        Assumptions.assumeTrue(warehouseOpt.isPresent(), "Cần kho tổng WAREHOUSE đang ACTIVE để làm kho xuất online");
        Store warehouse = warehouseOpt.get();

        // 3. Kiểm tra voucher mẫu GIAM50K (từ V30 seed_vouchers)
        Optional<Voucher> voucherOpt = voucherRepository.findByCode("GIAM50K");
        Assumptions.assumeTrue(voucherOpt.isPresent(), "Cần voucher GIAM50K từ migration V30");
        Voucher voucher = voucherOpt.get();
        int initialUsedCount = voucher.getUsedCount();

        // 4. Lấy ít nhất 2 biến thể có tồn kho tại kho tổng (từ V29 seed_catalog_and_inventory)
        List<Inventory> warehouseInventories = inventoryRepository.findByStoreId(warehouse.getId());
        List<Inventory> validInventories = warehouseInventories.stream()
                .filter(inv -> inv.getQuantityOnHand() != null && inv.getQuantityOnHand() >= 5)
                .filter(inv -> inv.getVariant() != null && inv.getVariant().isSellable())
                .limit(2)
                .toList();

        Assumptions.assumeTrue(validInventories.size() >= 2, "Cần ít nhất 2 biến thể có tồn kho >= 5 tại kho tổng để test");

        Inventory inv1 = validInventories.get(0);
        Inventory inv2 = validInventories.get(1);

        int initialStock1 = inv1.getQuantityOnHand();
        int initialStock2 = inv2.getQuantityOnHand();

        // 5. Chuẩn bị giỏ hàng ACTIVE cho khách hàng chứa 2 biến thể trên
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
        cart.addItem(inv1.getVariant(), 1);
        cart.addItem(inv2.getVariant(), 1);
        Cart savedCart = cartRepository.save(cart);

        BigDecimal price1 = inv1.getVariant().getPrice();
        BigDecimal price2 = inv2.getVariant().getPrice();
        BigDecimal expectedSubtotal = price1.add(price2);

        // 6. Chuẩn bị lệnh đặt hàng PlaceOrderCommand kèm mã voucher GIAM50K
        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .shipName("Khách Hàng Thân Thiết")
                .shipPhone("0901000004")
                .shipAddress("123 Đường Nguyễn Thị Minh Khai, Phường Bến Nghé, Quận 1, TP.HCM")
                .note("Đơn hàng test tích hợp Ngày 6A")
                .voucherCode("GIAM50K")
                .paymentMethod(PaymentMethod.COD)
                .build();

        // 7. Thực thi nghiệp vụ đặt hàng
        OrderResponseDTO orderResponse = orderService.placeOrder(customer.getId(), command);

        // =====================================================================
        // KIỂM TRA KẾT QUẢ NGHIỆP VỤ VÀ TÍNH TOÀN VẸN CƠ SỞ DỮ LIỆU
        // =====================================================================
        assertNotNull(orderResponse);
        assertNotNull(orderResponse.getOrderCode());

        // A. Kiểm tra Order trong CSDL
        Order order = orderRepository.findByOrderCodeWithDetails(orderResponse.getOrderCode())
                .orElseThrow(() -> new AssertionError("Không tìm thấy đơn hàng vừa tạo trong CSDL"));

        assertEquals(OrderChannel.ONLINE, order.getChannel());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(warehouse.getId(), order.getStore().getId());
        assertEquals(customer.getId(), order.getCustomer().getId());
        assertEquals("GIAM50K", order.getVoucherCode());
        assertMoneyEquals(expectedSubtotal, order.getSubtotal(), "Tạm tính subtotal của đơn hàng");

        // Voucher GIAM50K giảm 50.000đ (đơn hàng > 300.000đ)
        BigDecimal expectedDiscount = new BigDecimal("50000.00");
        assertMoneyEquals(expectedDiscount, order.getDiscountTotal(), "Tổng tiền giảm giá discount_total");

        // Phí vận chuyển: nếu (subtotal - discount) >= 1.000.000đ thì 0đ, ngược lại 30.000đ
        BigDecimal subtotalAfterDiscount = expectedSubtotal.subtract(expectedDiscount);
        BigDecimal expectedShipping = (subtotalAfterDiscount.compareTo(new BigDecimal("1000000")) >= 0)
                ? BigDecimal.ZERO : new BigDecimal("30000.00");
        assertMoneyEquals(expectedShipping, order.getShippingFee(), "Phí vận chuyển shipping_fee");

        BigDecimal expectedTotal = subtotalAfterDiscount.add(expectedShipping);
        assertMoneyEquals(expectedTotal, order.getTotalAmount(), "Tổng thanh toán total_amount");

        // B. Kiểm tra Order Items (Snapshot)
        assertEquals(2, order.getItems().size());
        for (OrderItem item : order.getItems()) {
            assertNotNull(item.getProductName());
            assertNotNull(item.getSku());
            assertNotNull(item.getColor());
            assertNotNull(item.getSize());
            assertNotNull(item.getUnitPrice());
            assertEquals(1, item.getQuantity());
            assertMoneyEquals(item.getUnitPrice(), item.getLineTotal(), "Thành tiền dòng item = unit_price * 1");
        }

        // C. Kiểm tra Lịch sử trạng thái đơn hàng (order_status_history)
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId());
        assertFalse(histories.isEmpty());
        OrderStatusHistory initialHistory = histories.get(0);
        assertNull(initialHistory.getFromStatus());
        assertEquals(OrderStatus.PENDING, initialHistory.getToStatus());

        // D. Kiểm tra Giao dịch thanh toán COD (payments)
        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertEquals(1, payments.size());
        Payment payment = payments.get(0);
        assertEquals(PaymentMethod.COD, payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertMoneyEquals(expectedTotal, payment.getAmount(), "Số tiền giao dịch thanh toán COD");
        assertNull(payment.getPaidAt());

        // E. Kiểm tra Tồn kho kho tổng bị trừ đúng số lượng
        Inventory updatedInv1 = inventoryRepository.findById(inv1.getId()).orElseThrow();
        Inventory updatedInv2 = inventoryRepository.findById(inv2.getId()).orElseThrow();
        assertEquals(initialStock1 - 1, updatedInv1.getQuantityOnHand());
        assertEquals(initialStock2 - 1, updatedInv2.getQuantityOnHand());

        // F. Kiểm tra Sổ cái giao dịch kho (inventory_transactions) có dòng SALE
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAll().stream()
                .filter(tx -> "ORDER".equals(tx.getReferenceType()) && order.getId().equals(tx.getReferenceId()))
                .toList();
        assertEquals(2, transactions.size());
        for (InventoryTransaction tx : transactions) {
            assertEquals(InventoryTransactionType.SALE, tx.getType());
            assertEquals(-1, tx.getQuantityChange());
            assertTrue(tx.getNote().contains(order.getOrderCode()));
            assertEquals(customer.getId(), tx.getCreatedBy().getId());
        }

        // G. Kiểm tra Voucher tăng used_count và có bản ghi voucher_usages
        // Đồng bộ các thay đổi xuống DB và làm mới entity Voucher trong Persistence Context (1st-level cache)
        // do câu UPDATE @Modifying bulk update ghi trực tiếp xuống DB mà không tự động làm mới entity đã nạp
        entityManager.flush();
        entityManager.refresh(voucher);
        Voucher updatedVoucher = voucherRepository.findById(voucher.getId()).orElseThrow();
        assertEquals(initialUsedCount + 1, updatedVoucher.getUsedCount());

        List<VoucherUsage> usages = voucherUsageRepository.findByOrderId(order.getId());
        assertEquals(1, usages.size());
        assertEquals(voucher.getId(), usages.get(0).getVoucher().getId());
        assertEquals(customer.getId(), usages.get(0).getUser().getId());

        // H. Kiểm tra Giỏ hàng đã chuyển sang CONVERTED
        Cart updatedCart = cartRepository.findById(savedCart.getId()).orElseThrow();
        assertEquals(CartStatus.CONVERTED, updatedCart.getStatus());
    }

    /**
     * Helper so sánh giá trị tiền tệ BigDecimal bằng compareTo (bỏ qua sự khác biệt scale giữa scale 0 và scale 2).
     */
    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual, String description) {
        assertNotNull(expected, description + " - giá trị kỳ vọng không được null");
        assertNotNull(actual, description + " - giá trị thực tế không được null");
        assertEquals(0, expected.compareTo(actual),
                String.format("%s: kỳ vọng <%s> nhưng nhận được <%s> (scale: %d vs %d)",
                        description, expected, actual, expected.scale(), actual.scale()));
    }
}
