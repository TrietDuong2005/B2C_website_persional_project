package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import com.dahdt.ecommerce.modules.cart.repository.CartRepository;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.fulfillment.FulfillmentSourceResolver;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import com.dahdt.ecommerce.modules.order.dto.OrderPreviewResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.PlaceOrderCommand;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.event.OrderPlacedEvent;
import com.dahdt.ecommerce.modules.order.factory.OrderFactory;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.shipping.ShippingFeeCalculator;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategy;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategyRegistry;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherStatus;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategy;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategyRegistry;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cài đặt nghiệp vụ lõi đặt hàng trực tuyến (OrderService).
 * Đảm bảo kiến trúc phân lớp, tuân thủ nguyên tắc Rich Domain Model và các Design Pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final PaymentRepository paymentRepository;
    private final FulfillmentSourceResolver fulfillmentSourceResolver;
    private final OnlineAvailabilityService onlineAvailabilityService;
    private final OrderFactory orderFactory;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final DiscountStrategyRegistry discountStrategyRegistry;
    private final PaymentStrategyRegistry paymentStrategyRegistry;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderPreviewResponseDTO previewOrder(Long userId, PlaceOrderCommand command) {
        if (userId == null) {
            throw new BadRequestException("Vui lòng đăng nhập để xem thông tin đơn hàng.");
        }

        // 1. Lấy giỏ hàng ACTIVE của khách hàng
        Cart cart = cartRepository.findByUserIdAndStatusWithDetails(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng của bạn đang trống."));

        if (cart.isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống.");
        }

        // Tính tạm tính theo đơn giá DB hiện thời
        BigDecimal subtotal = cart.subtotal();
        BigDecimal discountTotal = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        String voucherName = null;

        // 2. Tính giảm giá nếu có voucher
        if (command != null && command.getVoucherCode() != null && !command.getVoucherCode().trim().isBlank()) {
            String code = command.getVoucherCode().trim().toUpperCase();
            Voucher voucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá [" + code + "] không tồn tại trên hệ thống."));

            voucher.validateApplicable(subtotal, LocalDateTime.now());

            DiscountStrategy discountStrategy = discountStrategyRegistry.getStrategy(voucher.getDiscountType());
            discountTotal = discountStrategy.calculateDiscount(voucher, subtotal);
            appliedVoucherCode = voucher.getCode();
            voucherName = voucher.getName();
        }

        // 3. Tính phí vận chuyển dựa trên (tạm tính - giảm giá)
        BigDecimal subtotalAfterDiscount = subtotal.subtract(discountTotal);
        if (subtotalAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            subtotalAfterDiscount = BigDecimal.ZERO;
        }
        BigDecimal shippingFee = shippingFeeCalculator.calculateShippingFee(subtotalAfterDiscount);

        // 4. Tổng thanh toán = subtotal - discountTotal + shippingFee
        BigDecimal totalAmount = subtotal.subtract(discountTotal).add(shippingFee);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }
        totalAmount = MoneyUtil.roundMoney(totalAmount);

        return OrderPreviewResponseDTO.builder()
                .subtotal(subtotal)
                .discountTotal(discountTotal)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .voucherCode(appliedVoucherCode)
                .voucherName(voucherName)
                .totalQuantity(cart.getTotalQuantity())
                .shippingAddress(command != null ? command.getShipAddress() : null)
                .paymentMethod(command != null ? command.getPaymentMethod() : null)
                .build();
    }

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(Long userId, PlaceOrderCommand command) {
        if (userId == null) {
            throw new BadRequestException("Vui lòng đăng nhập để tiến hành đặt hàng.");
        }
        if (command == null) {
            throw new BadRequestException("Thông tin đặt hàng không hợp lệ.");
        }

        try {
            return executePlaceOrder(userId, command);
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Xung đột khóa lạc quan khi đặt đơn của user [{}]: {}", userId, ex.getMessage());
            throw new BadRequestException("Một số sản phẩm vừa thay đổi tồn kho, vui lòng kiểm tra lại giỏ hàng");
        }
    }

    /**
     * Thực thi 10 bước nghiệp vụ đặt hàng trong cùng một giao dịch.
     */
    private OrderResponseDTO executePlaceOrder(Long userId, PlaceOrderCommand command) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng với ID: " + userId));

        // =====================================================================
        // BƯỚC 1: Lấy giỏ ACTIVE của khách, kiểm tra không rỗng và không có vấn đề tồn kho
        // =====================================================================
        Cart cart = cartRepository.findByUserIdAndStatusWithDetails(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng của bạn đang trống."));

        if (cart.isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống.");
        }

        List<CartItem> cartItems = cart.getItems();
        List<Long> variantIds = cartItems.stream()
                .map(item -> item.getVariant().getId())
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Integer> stockMap = onlineAvailabilityService.getAvailableQuantities(variantIds);

        // =====================================================================
        // BƯỚC 2: Lấy lại đơn giá từ DB, kiểm tra biến thể và sản phẩm còn ACTIVE
        // BƯỚC 3: Xác định kho nguồn và kiểm tra tồn kho chi tiết
        // =====================================================================
        Store sourceStore = fulfillmentSourceResolver.resolveDefaultSource();
        if (sourceStore == null) {
            throw new ResourceNotFoundException("Không tìm thấy kho nguồn cung ứng online.");
        }

        for (CartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getVariant().getId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy biến thể sản phẩm có ID: " + item.getVariant().getId()));

            if (!variant.isSellable()) {
                throw new BadRequestException(
                        "Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() +
                                "] hiện tạm ngừng kinh doanh, vui lòng cập nhật lại giỏ hàng."
                );
            }

            int availableStock = stockMap.getOrDefault(variant.getId(), 0);
            if (availableStock <= 0) {
                throw new InsufficientStockException(
                        "Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() +
                                "] hiện đã hết hàng trong kho."
                );
            }
            if (item.getQuantity() > availableStock) {
                throw new InsufficientStockException(
                        "Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() +
                                "] trong giỏ (" + item.getQuantity() + " đôi) vượt quá tồn kho hiện có (" +
                                availableStock + " đôi)."
                );
            }
        }

        // =====================================================================
        // BƯỚC 4: Tạo đơn bằng OrderFactory và Order.addItem (lưu snapshot)
        // =====================================================================
        Order order = orderFactory.createOnlineOrder(
                sourceStore,
                customer,
                command.getShipName(),
                command.getShipPhone(),
                command.getShipAddress(),
                command.getShipLatitude(),
                command.getShipLongitude(),
                command.getNote()
        );

        for (CartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getVariant().getId()).orElseThrow();
            // Order.addItem tự snapshot: tên, sku, color, size, unitPrice từ DB, quantity, lineTotal
            order.addItem(variant, item.getQuantity());
        }

        // =====================================================================
        // BƯỚC 5: Áp voucher (nếu có) và tính phí vận chuyển
        // =====================================================================
        Voucher appliedVoucher = null;
        if (command.getVoucherCode() != null && !command.getVoucherCode().trim().isBlank()) {
            String code = command.getVoucherCode().trim().toUpperCase();
            appliedVoucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá [" + code + "] không tồn tại trên hệ thống."));

            // Entity Voucher tự kiểm tra tính hợp lệ: trạng thái, thời gian, số lượt, đơn tối thiểu
            appliedVoucher.validateApplicable(order.getSubtotal(), LocalDateTime.now());

            DiscountStrategy discountStrategy = discountStrategyRegistry.getStrategy(appliedVoucher.getDiscountType());
            BigDecimal discountAmount = discountStrategy.calculateDiscount(appliedVoucher, order.getSubtotal());
            order.applyVoucher(appliedVoucher.getCode(), discountAmount);
        }

        // Tính cước vận chuyển dựa trên (tạm tính - giảm giá)
        BigDecimal subtotalAfterDiscount = order.getSubtotal().subtract(order.getDiscountTotal());
        if (subtotalAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            subtotalAfterDiscount = BigDecimal.ZERO;
        }
        BigDecimal shippingFee = shippingFeeCalculator.calculateShippingFee(subtotalAfterDiscount);
        order.applyShippingFee(shippingFee);

        // =====================================================================
        // BƯỚC 6: Lưu đơn hàng và ghi order_status_history (từ rỗng sang PENDING)
        // =====================================================================
        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .changedBy(customer)
                .note("Khách hàng đặt đơn hàng trực tuyến")
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(initialHistory);

        // =====================================================================
        // BƯỚC 7: Tăng used_count của voucher bằng 1 câu UPDATE có điều kiện nguyên tử
        // =====================================================================
        // Câu UPDATE có điều kiện ở bước 7 để hai khách dùng mã cùng lúc không vượt số lượt,
        // giải quyết triệt để xung đột đồng thời (race condition) trực tiếp trên cơ sở dữ liệu:
        // Nếu số dòng bị ảnh hưởng = 0 chứng tỏ mã vừa bị dùng hết lượt hoặc vừa hết hạn.
        if (appliedVoucher != null) {
            int affectedRows = voucherRepository.incrementUsedCountConditionally(
                    appliedVoucher.getId(),
                    VoucherStatus.ACTIVE,
                    LocalDateTime.now()
            );
            if (affectedRows == 0) {
                throw new BadRequestException("Mã giảm giá [" + appliedVoucher.getCode() + "] đã hết lượt sử dụng hoặc không còn hiệu lực.");
            }

            // Ghi nhật ký sử dụng voucher vào bảng voucher_usages
            VoucherUsage usage = VoucherUsage.builder()
                    .voucher(appliedVoucher)
                    .order(savedOrder)
                    .user(customer)
                    .usedAt(LocalDateTime.now())
                    .build();
            voucherUsageRepository.save(usage);
        }

        // =====================================================================
        // BƯỚC 8: Phát OrderPlacedEvent (Observer pattern - trừ kho đồng bộ)
        // =====================================================================
        // Listener chạy đồng bộ trong cùng transaction: trừ kho, kiểm tra tồn và khóa lạc quan @Version
        eventPublisher.publishEvent(new OrderPlacedEvent(this, savedOrder));

        // =====================================================================
        // BƯỚC 9: Tạo Payment qua PaymentStrategy và lưu
        // =====================================================================
        PaymentStrategy paymentStrategy = paymentStrategyRegistry.getOnlineStrategy(command.getPaymentMethod());
        Payment payment = paymentStrategy.processPayment(savedOrder);
        paymentRepository.save(payment);
        savedOrder.getPayments().add(payment);

        // =====================================================================
        // BƯỚC 10: Chuyển giỏ hàng sang trạng thái CONVERTED
        // =====================================================================
        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        log.info("Đặt hàng trực tuyến thành công: Mã đơn [{}], Khách hàng [{}], Tổng tiền [{}]",
                savedOrder.getOrderCode(), customer.getEmail(), savedOrder.getTotalAmount());

        return orderMapper.toResponseDTO(savedOrder);
    }
}

