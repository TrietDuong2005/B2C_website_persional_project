package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.event.OrderPlacedEvent;
import com.dahdt.ecommerce.modules.order.factory.OrderCodeGenerator;
import com.dahdt.ecommerce.modules.order.factory.OrderFactory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.payment.strategy.CashPaymentStrategy;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategyRegistry;
import com.dahdt.ecommerce.modules.pos.dto.*;
import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherStatus;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategyRegistry;
import com.dahdt.ecommerce.modules.promotion.strategy.FixedDiscountStrategy;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ bán hàng tại quầy POS (PosSaleService)")
class PosSaleServiceTest {

    private EmployeeRepository employeeRepository;
    private ProductVariantRepository productVariantRepository;
    private InventoryRepository inventoryRepository;
    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private PaymentRepository paymentRepository;
    private VoucherRepository voucherRepository;
    private VoucherUsageRepository voucherUsageRepository;
    private OrderFactory orderFactory;
    private OrderCodeGenerator orderCodeGenerator;
    private DiscountStrategyRegistry discountStrategyRegistry;
    private PaymentStrategyRegistry paymentStrategyRegistry;
    private ApplicationEventPublisher eventPublisher;

    private PosSaleService posSaleService;

    private User cashierUser;
    private Store cashierStore;
    private Employee cashierEmployee;
    private Product sampleProduct;
    private ProductVariant sampleVariant;
    private Inventory sampleInventory;
    private Voucher sampleVoucher;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        productVariantRepository = mock(ProductVariantRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderStatusHistoryRepository = mock(OrderStatusHistoryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        voucherRepository = mock(VoucherRepository.class);
        voucherUsageRepository = mock(VoucherUsageRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        orderCodeGenerator = mock(OrderCodeGenerator.class);
        when(orderCodeGenerator.generateUniqueOrderCode(OrderChannel.POS)).thenReturn("HD202611080001");
        orderFactory = new OrderFactory(orderCodeGenerator);

        // Mặc định save order trả về chính đối tượng order truyền vào (tránh null)
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        discountStrategyRegistry = new DiscountStrategyRegistry(List.of(new FixedDiscountStrategy()));
        paymentStrategyRegistry = new PaymentStrategyRegistry(List.of(new CashPaymentStrategy()));

        posSaleService = new PosSaleServiceImpl(
                employeeRepository,
                productVariantRepository,
                inventoryRepository,
                orderRepository,
                orderStatusHistoryRepository,
                paymentRepository,
                voucherRepository,
                voucherUsageRepository,
                orderFactory,
                discountStrategyRegistry,
                paymentStrategyRegistry,
                eventPublisher
        );

        cashierUser = User.builder().id(10L).email("cashier@trietstore.vn").build();
        cashierStore = Store.builder().id(2L).code("STORE-Q1").name("Triết Store - Quận 1").build();
        cashierEmployee = Employee.builder()
                .id(1L)
                .user(cashierUser)
                .store(cashierStore)
                .position(EmployeePosition.CASHIER)
                .status(EmployeeStatus.ACTIVE)
                .build();

        sampleProduct = Product.builder().id(1L).name("Giày Sneaker Nam").status(ProductStatus.ACTIVE).build();
        sampleVariant = ProductVariant.builder()
                .id(101L)
                .product(sampleProduct)
                .sku("SNK-01-42")
                .barcode("8930000000101")
                .color("Trắng")
                .size("42")
                .price(new BigDecimal("500000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        sampleInventory = Inventory.builder()
                .id(501L)
                .store(cashierStore)
                .variant(sampleVariant)
                .quantityOnHand(10)
                .build();

        sampleVoucher = Voucher.builder()
                .id(1L)
                .code("GIAM50K")
                .name("Giảm 50k")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("300000"))
                .usageLimit(100)
                .usedCount(5)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("previewSale: Tính đúng tạm tính, giảm giá, tổng tiền và tiền thối")
    void testPreviewSale_Success() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        when(voucherRepository.findByCode("GIAM50K")).thenReturn(Optional.of(sampleVoucher));

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 2))) // 2 * 500k = 1.000.000
                .voucherCode("GIAM50K")
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("1000000"))
                .build();

        PosSalePreviewResponseDTO preview = posSaleService.previewSale(10L, command);

        assertNotNull(preview, "Kết quả preview không được null");
        assertEquals(0, new BigDecimal("1000000").compareTo(preview.getSubtotal()),
                "Tạm tính kỳ vọng: 1.000.000, thực tế: " + preview.getSubtotal());
        assertEquals(0, new BigDecimal("50000").compareTo(preview.getDiscountTotal()),
                "Giảm giá kỳ vọng: 50.000, thực tế: " + preview.getDiscountTotal());
        assertEquals(0, new BigDecimal("950000").compareTo(preview.getTotalAmount()),
                "Tổng tiền kỳ vọng: 950.000, thực tế: " + preview.getTotalAmount());
        assertEquals("GIAM50K", preview.getVoucherCode(),
                "Mã voucher kỳ vọng GIAM50K, thực tế: " + preview.getVoucherCode());
        assertEquals(0, new BigDecimal("1000000").compareTo(preview.getCashReceived()),
                "Tiền khách đưa kỳ vọng: 1.000.000, thực tế: " + preview.getCashReceived());
        assertEquals(0, new BigDecimal("50000").compareTo(preview.getChangeAmount()),
                "Tiền thối kỳ vọng: 50.000, thực tế: " + preview.getChangeAmount());
        assertEquals(2, preview.getTotalQuantity(),
                "Tổng số lượng kỳ vọng: 2, thực tế: " + preview.getTotalQuantity());

        // Đảm bảo không ghi DB
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout: Luồng thành công đầy đủ 9 bước, tạo Payment CASH và trừ kho qua Observer")
    void testCheckout_Success() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        when(inventoryRepository.findByStoreIdAndVariantId(2L, 101L)).thenReturn(Optional.of(sampleInventory));
        when(voucherRepository.findByCode("GIAM50K")).thenReturn(Optional.of(sampleVoucher));
        when(voucherRepository.incrementUsedCountConditionally(eq(1L), eq(VoucherStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 2)))
                .voucherCode("GIAM50K")
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("1000000"))
                .build();

        PosSaleResponseDTO response = posSaleService.checkout(10L, command);

        assertNotNull(response, "Kết quả checkout không được null");
        assertEquals("HD202611080001", response.getOrderCode(),
                "Mã đơn hàng kỳ vọng HD202611080001, thực tế: " + response.getOrderCode());
        assertEquals("POS", response.getChannel(),
                "Kênh đơn hàng kỳ vọng POS, thực tế: " + response.getChannel());
        assertEquals("COMPLETED", response.getStatus(),
                "Trạng thái kỳ vọng COMPLETED, thực tế: " + response.getStatus());
        assertEquals(2L, response.getStoreId(),
                "Store ID kỳ vọng 2, thực tế: " + response.getStoreId());
        assertEquals(10L, response.getCashierId(),
                "Cashier ID kỳ vọng 10, thực tế: " + response.getCashierId());
        assertEquals(0, new BigDecimal("1000000").compareTo(response.getSubtotal()),
                "Subtotal kỳ vọng: 1.000.000, thực tế: " + response.getSubtotal());
        assertEquals(0, new BigDecimal("50000").compareTo(response.getDiscountTotal()),
                "Discount kỳ vọng: 50.000, thực tế: " + response.getDiscountTotal());
        assertEquals(0, new BigDecimal("950000").compareTo(response.getTotalAmount()),
                "Total kỳ vọng: 950.000, thực tế: " + response.getTotalAmount());
        assertEquals(0, new BigDecimal("1000000").compareTo(response.getCashReceived()),
                "Cash received kỳ vọng: 1.000.000, thực tế: " + response.getCashReceived());
        assertEquals(0, new BigDecimal("50000").compareTo(response.getChangeAmount()),
                "Change amount kỳ vọng: 50.000, thực tế: " + response.getChangeAmount());

        // Kiểm tra lưu Order
        verify(orderRepository, times(1)).save(any(Order.class));

        // Kiểm tra lưu OrderStatusHistory (null -> COMPLETED)
        ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository, times(1)).save(historyCaptor.capture());
        OrderStatusHistory hist = historyCaptor.getValue();
        assertNull(hist.getFromStatus(),
                "fromStatus ban đầu phải là null, thực tế: " + hist.getFromStatus());
        assertEquals(OrderStatus.COMPLETED, hist.getToStatus(),
                "toStatus kỳ vọng COMPLETED, thực tế: " + hist.getToStatus());
        assertEquals(cashierUser, hist.getChangedBy(),
                "Người thực hiện chuyển trạng thái kỳ vọng cashierUser, thực tế: " + hist.getChangedBy());

        // Kiểm tra lưu VoucherUsage với user_id là null
        ArgumentCaptor<VoucherUsage> usageCaptor = ArgumentCaptor.forClass(VoucherUsage.class);
        verify(voucherUsageRepository, times(1)).save(usageCaptor.capture());
        VoucherUsage usage = usageCaptor.getValue();
        assertNull(usage.getUser(),
                "user_id trong voucher_usages của đơn POS phải là null, thực tế: " + usage.getUser());
        assertEquals(sampleVoucher, usage.getVoucher(),
                "Voucher kỳ vọng sampleVoucher, thực tế: " + usage.getVoucher());

        // Kiểm tra phát OrderPlacedEvent
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));

        // Kiểm tra lưu Payment
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("checkout: Giỏ rỗng -> ném BadRequestException")
    void testCheckout_EmptyCart_ThrowsException() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));

        PosSaleCommand command = PosSaleCommand.builder()
                .items(Collections.emptyList())
                .paymentMethod(PaymentMethod.CASH)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posSaleService.checkout(10L, command));
        assertTrue(ex.getMessage().contains("Giỏ hàng không được để trống"),
                "Thông điệp ngoại lệ thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("checkout: Không thuộc cửa hàng nào -> ném BadRequestException")
    void testCheckout_NoStore_ThrowsException() {
        when(employeeRepository.findByUserId(99L)).thenReturn(Optional.empty());

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 1)))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posSaleService.checkout(99L, command));
        assertTrue(ex.getMessage().contains("Tài khoản không thuộc cửa hàng nào"),
                "Thông điệp ngoại lệ thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("checkout: Hết hàng tại cửa hàng -> ném InsufficientStockException")
    void testCheckout_OutOfStock_ThrowsException() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        sampleInventory.setQuantityOnHand(1); // Chỉ còn 1, mua 2
        when(inventoryRepository.findByStoreIdAndVariantId(2L, 101L)).thenReturn(Optional.of(sampleInventory));

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 2)))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> posSaleService.checkout(10L, command));
        assertTrue(ex.getMessage().contains("chỉ còn 1 đôi"),
                "Thông điệp ngoại lệ thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("checkout: Thiếu tiền mặt (CASH) -> ném BadRequestException")
    void testCheckout_InsufficientCash_ThrowsException() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        when(inventoryRepository.findByStoreIdAndVariantId(2L, 101L)).thenReturn(Optional.of(sampleInventory));

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 1))) // 500k
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("400000")) // Đưa 400k thiếu 100k
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posSaleService.checkout(10L, command));
        assertTrue(ex.getMessage().contains("chưa đủ"),
                "Thông điệp phải chứa từ khóa 'chưa đủ', thực tế: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("400.000"),
                "Thông điệp phải nêu số tiền khách đưa (400.000 đ), thực tế: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("500.000"),
                "Thông điệp phải nêu tổng tiền (500.000 đ), thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("checkout: Nhân viên bị khóa -> ném BadRequestException")
    void testCheckout_InactiveEmployee_ThrowsException() {
        cashierEmployee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));

        PosSaleCommand command = PosSaleCommand.builder()
                .items(List.of(new PosSaleItemCommand(101L, 1)))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posSaleService.checkout(10L, command));
        assertTrue(ex.getMessage().contains("bị khóa"),
                "Thông điệp ngoại lệ thực tế: " + ex.getMessage());
    }
}
