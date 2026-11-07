package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import com.dahdt.ecommerce.modules.cart.repository.CartRepository;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.fulfillment.FulfillmentSourceResolver;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import com.dahdt.ecommerce.modules.order.dto.OrderPreviewResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.PlaceOrderCommand;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.event.OrderPlacedEvent;
import com.dahdt.ecommerce.modules.order.factory.OrderCodeGenerator;
import com.dahdt.ecommerce.modules.order.factory.OrderFactory;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.shipping.ShippingFeeCalculator;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.payment.strategy.CodPaymentStrategy;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategyRegistry;
import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherStatus;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategyRegistry;
import com.dahdt.ecommerce.modules.promotion.strategy.FixedDiscountStrategy;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ OrderService (Đặt hàng online & Xem trước)")
class OrderServiceTest {

    private CartRepository cartRepository;
    private UserRepository userRepository;
    private ProductVariantRepository productVariantRepository;
    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private VoucherRepository voucherRepository;
    private VoucherUsageRepository voucherUsageRepository;
    private PaymentRepository paymentRepository;
    private FulfillmentSourceResolver fulfillmentSourceResolver;
    private OnlineAvailabilityService onlineAvailabilityService;
    private OrderFactory orderFactory;
    private ShippingFeeCalculator shippingFeeCalculator;
    private DiscountStrategyRegistry discountStrategyRegistry;
    private PaymentStrategyRegistry paymentStrategyRegistry;
    private ApplicationEventPublisher eventPublisher;
    private OrderMapper orderMapper;

    private OrderService orderService;

    private User sampleUser;
    private Store sampleStore;
    private Product sampleProduct;
    private ProductVariant variant1;
    private Cart sampleCart;
    private PlaceOrderCommand command;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        userRepository = mock(UserRepository.class);
        productVariantRepository = mock(ProductVariantRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderStatusHistoryRepository = mock(OrderStatusHistoryRepository.class);
        voucherRepository = mock(VoucherRepository.class);
        voucherUsageRepository = mock(VoucherUsageRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        fulfillmentSourceResolver = mock(FulfillmentSourceResolver.class);
        onlineAvailabilityService = mock(OnlineAvailabilityService.class);
        shippingFeeCalculator = mock(ShippingFeeCalculator.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        orderMapper = Mappers.getMapper(OrderMapper.class);

        OrderCodeGenerator codeGenerator = mock(OrderCodeGenerator.class);
        when(codeGenerator.generateUniqueOrderCode()).thenReturn("DH202611070001");
        orderFactory = new OrderFactory(codeGenerator);

        FixedDiscountStrategy fixedDiscountStrategy = new FixedDiscountStrategy();
        discountStrategyRegistry = new DiscountStrategyRegistry(List.of(fixedDiscountStrategy));

        CodPaymentStrategy codPaymentStrategy = new CodPaymentStrategy();
        paymentStrategyRegistry = new PaymentStrategyRegistry(List.of(codPaymentStrategy));

        orderService = new OrderServiceImpl(
                cartRepository,
                userRepository,
                productVariantRepository,
                orderRepository,
                orderStatusHistoryRepository,
                voucherRepository,
                voucherUsageRepository,
                paymentRepository,
                fulfillmentSourceResolver,
                onlineAvailabilityService,
                orderFactory,
                shippingFeeCalculator,
                discountStrategyRegistry,
                paymentStrategyRegistry,
                eventPublisher,
                orderMapper
        );

        sampleUser = User.builder().id(10L).email("customer@trietstore.vn").build();
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        sampleStore = Store.builder().id(1L).code("WH-TONG").name("Kho Tổng").build();
        when(fulfillmentSourceResolver.resolveDefaultSource()).thenReturn(sampleStore);

        sampleProduct = Product.builder().id(5L).name("Giày Sneaker Retro").status(ProductStatus.ACTIVE).build();
        variant1 = ProductVariant.builder()
                .id(101L)
                .product(sampleProduct)
                .sku("RETRO-DEN-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("600000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(variant1));

        sampleCart = Cart.builder()
                .id(1L)
                .user(sampleUser)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem item1 = CartItem.builder().id(11L).cart(sampleCart).variant(variant1).quantity(1).build();
        sampleCart.getItems().add(item1);

        when(cartRepository.findByUserIdAndStatusWithDetails(10L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(sampleCart));

        when(onlineAvailabilityService.getAvailableQuantities(any())).thenReturn(Map.of(101L, 10));
        when(shippingFeeCalculator.calculateShippingFee(any())).thenReturn(new BigDecimal("30000"));

        command = PlaceOrderCommand.builder()
                .shipName("Trần Văn B")
                .shipPhone("0987654321")
                .shipAddress("123 Nguyễn Huệ, Q1, HCM")
                .paymentMethod(PaymentMethod.COD)
                .build();
    }

    @Test
    @DisplayName("placeOrder: Luồng thành công chuẩn 10 bước chuyển giỏ CONVERTED và phát sự kiện trừ kho")
    void testPlaceOrder_SuccessFlow() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(999L);
            return o;
        });

        OrderResponseDTO response = orderService.placeOrder(10L, command);

        assertNotNull(response);
        assertEquals("DH202611070001", response.getOrderCode());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(OrderChannel.ONLINE, response.getChannel());
        assertMoneyEquals(new BigDecimal("600000"), response.getSubtotal());
        assertMoneyEquals(new BigDecimal("30000"), response.getShippingFee());
        // 600.000 + 30.000 = 630.000đ
        assertMoneyEquals(new BigDecimal("630000"), response.getTotalAmount());

        // Kiểm tra giỏ hàng chuyển sang CONVERTED
        assertEquals(CartStatus.CONVERTED, sampleCart.getStatus());
        verify(cartRepository, times(1)).save(sampleCart);

        // Kiểm tra lưu lịch sử trạng thái ban đầu
        verify(orderStatusHistoryRepository, times(1)).save(any());

        // Kiểm tra phát sự kiện OrderPlacedEvent
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));

        // Kiểm tra lưu thanh toán COD PENDING
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("placeOrder: Áp voucher thành công tăng used_count và lưu voucher_usages")
    void testPlaceOrder_WithVoucher() {
        command.setVoucherCode("GIAM50K");

        Voucher voucher = Voucher.builder()
                .id(7L)
                .code("GIAM50K")
                .name("Giảm 50k")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("300000"))
                .usageLimit(100)
                .usedCount(10)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build();

        when(voucherRepository.findByCode("GIAM50K")).thenReturn(Optional.of(voucher));
        when(voucherRepository.incrementUsedCountConditionally(eq(7L), eq(VoucherStatus.ACTIVE), any())).thenReturn(1);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(999L);
            return o;
        });

        OrderResponseDTO response = orderService.placeOrder(10L, command);

        assertNotNull(response);
        assertMoneyEquals(new BigDecimal("50000"), response.getDiscountTotal());
        // subtotal 600.000 - 50.000 + ship 30.000 = 580.000đ
        assertMoneyEquals(new BigDecimal("580000"), response.getTotalAmount());

        // Kiểm tra câu UPDATE điều kiện và ghi sổ voucher_usages
        verify(voucherRepository, times(1)).incrementUsedCountConditionally(eq(7L), eq(VoucherStatus.ACTIVE), any());
        verify(voucherUsageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("placeOrder: Giỏ hàng trống ném BadRequestException")
    void testPlaceOrder_EmptyCart_ThrowsException() {
        sampleCart.getItems().clear();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.placeOrder(10L, command));
        assertTrue(ex.getMessage().contains("Giỏ hàng của bạn đang trống"));
    }

    @Test
    @DisplayName("placeOrder: Sản phẩm tạm ngừng kinh doanh ném BadRequestException")
    void testPlaceOrder_ProductNotSellable_ThrowsException() {
        sampleProduct.setStatus(ProductStatus.HIDDEN);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.placeOrder(10L, command));
        assertTrue(ex.getMessage().contains("tạm ngừng kinh doanh"));
    }

    @Test
    @DisplayName("placeOrder: Sản phẩm hết hàng ném InsufficientStockException")
    void testPlaceOrder_OutOfStock_ThrowsException() {
        when(onlineAvailabilityService.getAvailableQuantities(any())).thenReturn(Map.of(101L, 0));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(10L, command));
        assertTrue(ex.getMessage().contains("hết hàng"));
    }

    @Test
    @DisplayName("placeOrder: Voucher hết lượt ném BadRequestException")
    void testPlaceOrder_VoucherOutOfLimit_ThrowsException() {
        command.setVoucherCode("HETLUOT");

        Voucher voucher = Voucher.builder()
                .id(8L)
                .code("HETLUOT")
                .status(VoucherStatus.ACTIVE)
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000"))
                .usageLimit(10)
                .usedCount(10) // Đã dùng hết
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .build();

        when(voucherRepository.findByCode("HETLUOT")).thenReturn(Optional.of(voucher));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.placeOrder(10L, command));
        assertTrue(ex.getMessage().contains("hết lượt"));
    }

    @Test
    @DisplayName("previewOrder: Tính đúng số tiền dự kiến mà KHÔNG ghi dữ liệu vào CSDL")
    void testPreviewOrder_Success() {
        OrderPreviewResponseDTO preview = orderService.previewOrder(10L, command);

        assertNotNull(preview);
        assertMoneyEquals(new BigDecimal("600000"), preview.getSubtotal());
        assertMoneyEquals(BigDecimal.ZERO, preview.getDiscountTotal());
        assertMoneyEquals(new BigDecimal("30000"), preview.getShippingFee());
        assertMoneyEquals(new BigDecimal("630000"), preview.getTotalAmount());
        assertEquals(1, preview.getTotalQuantity());

        // Đảm bảo không gọi bất kỳ lệnh ghi nào
        verify(orderRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}

