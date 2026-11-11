package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.modules.cart.service.CartService;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.service.InventoryService;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.service.OrderService;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.pos.service.PosSaleService;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử đơn vị cho DemoDataSeeder và DemoDataPlanner (DemoDataSeederTest).
 * Tuân thủ yêu cầu:
 * - Kiểm thử hàm lập kế hoạch thuần Java (hạt giống cố định, số lượng đơn và phân bố trạng thái chuẩn).
 * - Kiểm thử logic bỏ qua khi đã có đơn demo [DEMO] (dùng Mockito, KHÔNG chạy thật trên DB).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Seeder dữ liệu mẫu demo (DemoDataSeederTest)")
class DemoDataSeederTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PosSaleService posSaleService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderStatusService orderStatusService;

    @InjectMocks
    private DemoDataSeeder demoDataSeeder;

    @Test
    @DisplayName("1. DemoDataPlanner: Cho ra đúng 25 đơn POS và 20 đơn ONLINE với phân bố trạng thái chính xác")
    void testPlanner_GeneratesExactOrderCountsAndStatusDistribution() {
        DemoDataPlanner planner = new DemoDataPlanner();

        // 1. Kiểm tra kế hoạch 25 đơn POS
        List<DemoDataPlanner.DemoPosOrderPlan> posPlans = planner.buildPosPlans();
        assertNotNull(posPlans, "Danh sách kế hoạch POS không được null");
        assertEquals(25, posPlans.size(), "Số lượng đơn POS phải đúng 25 đơn");

        // Kiểm tra thanh toán TIỀN MẶT, THẺ, QR xen kẽ
        long cashCount = posPlans.stream().filter(p -> p.getPaymentMethod() == PaymentMethod.CASH).count();
        long cardCount = posPlans.stream().filter(p -> p.getPaymentMethod() == PaymentMethod.CARD).count();
        long qrCount = posPlans.stream().filter(p -> p.getPaymentMethod() == PaymentMethod.QR).count();

        assertTrue(cashCount > 0, "Phải có đơn POS thanh toán tiền mặt (CASH)");
        assertTrue(cardCount > 0, "Phải có đơn POS thanh toán thẻ (CARD)");
        assertTrue(qrCount > 0, "Phải có đơn POS thanh toán mã QR");
        assertEquals(25, cashCount + cardCount + qrCount, "Tổng số phương thức thanh toán phải bằng 25");

        // Kiểm tra một số đơn POS có voucher
        long posVoucherCount = posPlans.stream().filter(p -> p.getVoucherCode() != null).count();
        assertTrue(posVoucherCount > 0, "Phải có một số đơn POS áp dụng voucher");

        // Kiểm tra tất cả đơn POS có ghi chú [DEMO]
        assertTrue(posPlans.stream().allMatch(p -> p.getNote().startsWith("[DEMO]")),
                "Tất cả đơn POS phải có ghi chú bắt đầu bằng [DEMO]");

        // 2. Kiểm tra kế hoạch 20 đơn ONLINE
        List<DemoDataPlanner.DemoOnlineOrderPlan> onlinePlans = planner.buildOnlinePlans();
        assertNotNull(onlinePlans, "Danh sách kế hoạch Online không được null");
        assertEquals(20, onlinePlans.size(), "Số lượng đơn Online phải đúng 20 đơn");

        // Kiểm tra phân bố trạng thái: 6 PENDING, 3 CONFIRMED, 2 PACKING, 2 SHIPPING, 5 COMPLETED, 2 CANCELLED
        Map<OrderStatus, Long> statusCounts = onlinePlans.stream()
                .collect(Collectors.groupingBy(DemoDataPlanner.DemoOnlineOrderPlan::getTargetStatus, Collectors.counting()));

        assertEquals(6L, statusCounts.getOrDefault(OrderStatus.PENDING, 0L), "Phải có đúng 6 đơn PENDING");
        assertEquals(3L, statusCounts.getOrDefault(OrderStatus.CONFIRMED, 0L), "Phải có đúng 3 đơn CONFIRMED");
        assertEquals(2L, statusCounts.getOrDefault(OrderStatus.PACKING, 0L), "Phải có đúng 2 đơn PACKING");
        assertEquals(2L, statusCounts.getOrDefault(OrderStatus.SHIPPING, 0L), "Phải có đúng 2 đơn SHIPPING");
        assertEquals(5L, statusCounts.getOrDefault(OrderStatus.COMPLETED, 0L), "Phải có đúng 5 đơn COMPLETED");
        assertEquals(2L, statusCounts.getOrDefault(OrderStatus.CANCELLED, 0L), "Phải có đúng 2 đơn CANCELLED");

        // Kiểm tra phương thức COD và Ví điện tử xen kẽ
        long codCount = onlinePlans.stream().filter(p -> p.getPaymentMethod() == PaymentMethod.COD).count();
        long ewalletCount = onlinePlans.stream().filter(p -> p.getPaymentMethod() == PaymentMethod.EWALLET_MOCK).count();
        assertEquals(10L, codCount, "Phải có đúng 10 đơn Online thanh toán COD");
        assertEquals(10L, ewalletCount, "Phải có đúng 10 đơn Online thanh toán Ví điện tử giả lập");

        // Kiểm tra tài khoản khách hàng: customer và customer2
        long customer1Count = onlinePlans.stream().filter(p -> "customer@dahdt.com".equals(p.getCustomerEmail())).count();
        long customer2Count = onlinePlans.stream().filter(p -> "customer2@dahdt.com".equals(p.getCustomerEmail())).count();
        assertEquals(10L, customer1Count, "Phải có 10 đơn của customer@dahdt.com");
        assertEquals(10L, customer2Count, "Phải có 10 đơn của customer2@dahdt.com");

        // Kiểm tra tất cả đơn Online có ghi chú [DEMO]
        assertTrue(onlinePlans.stream().allMatch(p -> p.getNote().startsWith("[DEMO]")),
                "Tất cả đơn Online phải có ghi chú bắt đầu bằng [DEMO]");

        // Kiểm tra thời gian rải trong khoảng 14 ngày (daysAgo từ 0 đến 13)
        assertTrue(posPlans.stream().allMatch(p -> p.getDaysAgo() >= 0 && p.getDaysAgo() < 14),
                "Thời gian đơn POS phải nằm trong 14 ngày gần nhất (0..13)");
        assertTrue(onlinePlans.stream().allMatch(p -> p.getDaysAgo() >= 0 && p.getDaysAgo() < 14),
                "Thời gian đơn Online phải nằm trong 14 ngày gần nhất (0..13)");
    }

    @Test
    @DisplayName("2. DemoDataPlanner: Hạt giống cố định cho ra kết quả lặp lại hoàn toàn giống nhau")
    void testPlanner_DeterministicSeed_ProducesIdenticalResults() {
        DemoDataPlanner planner1 = new DemoDataPlanner(12345L);
        DemoDataPlanner planner2 = new DemoDataPlanner(12345L);

        List<DemoDataPlanner.DemoPosOrderPlan> pos1 = planner1.buildPosPlans();
        List<DemoDataPlanner.DemoPosOrderPlan> pos2 = planner2.buildPosPlans();

        assertEquals(pos1.size(), pos2.size());
        for (int i = 0; i < pos1.size(); i++) {
            assertEquals(pos1.get(i).getPaymentMethod(), pos2.get(i).getPaymentMethod());
            assertEquals(pos1.get(i).getVoucherCode(), pos2.get(i).getVoucherCode());
            assertEquals(pos1.get(i).getDaysAgo(), pos2.get(i).getDaysAgo());
            assertEquals(pos1.get(i).getHour(), pos2.get(i).getHour());
        }

        List<DemoDataPlanner.DemoOnlineOrderPlan> online1 = planner1.buildOnlinePlans();
        List<DemoDataPlanner.DemoOnlineOrderPlan> online2 = planner2.buildOnlinePlans();

        assertEquals(online1.size(), online2.size());
        for (int i = 0; i < online1.size(); i++) {
            assertEquals(online1.get(i).getTargetStatus(), online2.get(i).getTargetStatus());
            assertEquals(online1.get(i).getPaymentMethod(), online2.get(i).getPaymentMethod());
            assertEquals(online1.get(i).getCustomerEmail(), online2.get(i).getCustomerEmail());
            assertEquals(online1.get(i).getDaysAgo(), online2.get(i).getDaysAgo());
        }
    }

    @Test
    @DisplayName("3. Idempotent: Khi đã có đơn hàng [DEMO], seeder bỏ qua toàn bộ (không gọi bất kỳ Service nào)")
    void testRun_WhenDemoOrdersAlreadyExist_SkipsEntirely() {
        // Giả lập hệ thống đã có đơn hàng [DEMO]
        when(orderRepository.existsByNoteStartingWithDemo()).thenReturn(true);
        demoDataSeeder.setDemoDataEnabled(true);

        demoDataSeeder.run(null);

        // Xác nhận đã kiểm tra Idempotent
        verify(orderRepository).existsByNoteStartingWithDemo();

        // Xác nhận TUYỆT ĐỐI không gọi service nghiệp vụ nào
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(posSaleService);
        verifyNoInteractions(cartService);
        verifyNoInteractions(orderService);
        verifyNoInteractions(orderStatusService);
    }

    @Test
    @DisplayName("4. Khi app.demo-data.enabled = false, seeder lập tức bỏ qua không truy vấn CSDL")
    void testRun_WhenDisabled_SkipsImmediatelyWithoutQueryingDatabase() {
        demoDataSeeder.setDemoDataEnabled(false);

        demoDataSeeder.run(null);

        // Không truy vấn bất kỳ repository nào
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("5. Khi thiếu tài khoản dev cần thiết, seeder dừng lại an toàn mà không ném Exception")
    void testRun_WhenAdminMissing_HandlesGracefully() {
        when(orderRepository.existsByNoteStartingWithDemo()).thenReturn(false);
        when(userRepository.findByEmail("admin@dahdt.com")).thenReturn(Optional.empty());
        demoDataSeeder.setDemoDataEnabled(true);

        assertDoesNotThrow(() -> demoDataSeeder.run(null));

        verify(orderRepository).existsByNoteStartingWithDemo();
        verify(userRepository).findByEmail("admin@dahdt.com");
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(posSaleService);
    }
}

