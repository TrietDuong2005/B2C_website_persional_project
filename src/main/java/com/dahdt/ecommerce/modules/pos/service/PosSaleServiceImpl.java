package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.event.OrderPlacedEvent;
import com.dahdt.ecommerce.modules.order.factory.OrderFactory;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategy;
import com.dahdt.ecommerce.modules.payment.strategy.PaymentStrategyRegistry;
import com.dahdt.ecommerce.modules.pos.dto.*;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherStatus;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherUsageRepository;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategy;
import com.dahdt.ecommerce.modules.promotion.strategy.DiscountStrategyRegistry;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt nghiệp vụ bán hàng tại quầy POS (PosSaleService).
 * Tuân thủ mô hình phân lớp, Rich Domain Model và các Design Pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PosSaleServiceImpl implements PosSaleService {

    private final EmployeeRepository employeeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final OrderFactory orderFactory;
    private final DiscountStrategyRegistry discountStrategyRegistry;
    private final PaymentStrategyRegistry paymentStrategyRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PosSalePreviewResponseDTO previewSale(Long userId, PosSaleCommand command) {
        // 1. Xác định nhân viên và cửa hàng làm việc
        Employee employee = getActivePosEmployee(userId);
        Store store = employee.getStore();

        // 2. Kiểm tra danh sách mặt hàng
        validateSaleItems(command);

        // 3. Tính tạm tính và kiểm tra trạng thái sản phẩm
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (PosSaleItemCommand itemCmd : command.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemCmd.getVariantId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy biến thể sản phẩm có ID: " + itemCmd.getVariantId()));

            if (!variant.isSellable()) {
                throw new BadRequestException("Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() + "] hiện tạm ngừng kinh doanh.");
            }

            BigDecimal unitPrice = (variant.getPrice() != null) ? variant.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = MoneyUtil.roundMoney(unitPrice.multiply(BigDecimal.valueOf(itemCmd.getQuantity())));
            subtotal = subtotal.add(lineTotal);
            totalQuantity += itemCmd.getQuantity();
        }
        subtotal = MoneyUtil.roundMoney(subtotal);

        // 4. Tính toán giảm giá voucher nếu có
        BigDecimal discountTotal = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        String voucherName = null;

        if (command.getVoucherCode() != null && !command.getVoucherCode().trim().isBlank()) {
            String code = command.getVoucherCode().trim().toUpperCase();
            Voucher voucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá [" + code + "] không tồn tại trên hệ thống."));

            voucher.validateApplicable(subtotal, LocalDateTime.now());

            DiscountStrategy discountStrategy = discountStrategyRegistry.getStrategy(voucher.getDiscountType());
            discountTotal = discountStrategy.calculateDiscount(voucher, subtotal);
            appliedVoucherCode = voucher.getCode();
            voucherName = voucher.getName();
        }

        // 5. Tổng thanh toán = subtotal - discountTotal (phí vận chuyển = 0)
        BigDecimal totalAmount = subtotal.subtract(discountTotal);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }
        totalAmount = MoneyUtil.roundMoney(totalAmount);

        // 6. Tính tiền thừa (cho CASH)
        BigDecimal cashReceived = command.getCashReceived();
        BigDecimal changeAmount = null;
        if (command.getPaymentMethod() == PaymentMethod.CASH && cashReceived != null) {
            BigDecimal diff = cashReceived.subtract(totalAmount);
            changeAmount = (diff.compareTo(BigDecimal.ZERO) >= 0) ? MoneyUtil.roundMoney(diff) : BigDecimal.ZERO;
        }

        return PosSalePreviewResponseDTO.builder()
                .subtotal(subtotal)
                .discountTotal(discountTotal)
                .totalAmount(totalAmount)
                .voucherCode(appliedVoucherCode)
                .voucherName(voucherName)
                .paymentMethod(command.getPaymentMethod())
                .cashReceived(cashReceived != null ? MoneyUtil.roundMoney(cashReceived) : null)
                .changeAmount(changeAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    @Override
    @Transactional
    public PosSaleResponseDTO checkout(Long userId, PosSaleCommand command) {
        try {
            return executeCheckout(userId, command);
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Xung đột khóa lạc quan tại quầy POS của user [{}]: {}", userId, ex.getMessage());
            throw new BadRequestException("Tồn kho vừa thay đổi, vui lòng kiểm tra lại");
        }
    }

    private PosSaleResponseDTO executeCheckout(Long userId, PosSaleCommand command) {
        // =====================================================================
        // (1) Xác định cửa hàng và nhân viên
        // =====================================================================
        Employee employee = getActivePosEmployee(userId);
        Store store = employee.getStore();
        User cashierUser = employee.getUser();

        // =====================================================================
        // (2) Kiểm tra danh sách không rỗng, mỗi dòng số lượng từ 1 đến 20
        // =====================================================================
        validateSaleItems(command);

        // =====================================================================
        // (3) Lấy lại đơn giá từ DB, kiểm tra biến thể và sản phẩm ACTIVE
        // (4) Kiểm tra tồn tại cửa hàng của thu ngân
        // =====================================================================
        for (PosSaleItemCommand itemCmd : command.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemCmd.getVariantId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy biến thể sản phẩm có ID: " + itemCmd.getVariantId()));

            if (!variant.isSellable()) {
                throw new BadRequestException("Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() + "] hiện tạm ngừng kinh doanh.");
            }

            Inventory inv = inventoryRepository.findByStoreIdAndVariantId(store.getId(), variant.getId())
                    .orElse(null);
            int stock = (inv != null && inv.getQuantityOnHand() != null) ? inv.getQuantityOnHand() : 0;

            if (stock <= 0) {
                throw new InsufficientStockException("Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() + "] tại cửa hàng hiện đã hết hàng (tồn: 0).");
            }
            if (stock < itemCmd.getQuantity()) {
                throw new InsufficientStockException("Sản phẩm [" + variant.getProduct().getName() + " - " + variant.getSku() + "] tại cửa hàng hiện chỉ còn " + stock + " đôi, không đủ để bán.");
            }
        }

        // =====================================================================
        // (5) Tạo đơn bằng OrderFactory.createPosOrder và snapshot mặt hàng
        // =====================================================================
        Order order = orderFactory.createPosOrder(store, cashierUser);

        for (PosSaleItemCommand itemCmd : command.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemCmd.getVariantId()).orElseThrow();
            order.addItem(variant, itemCmd.getQuantity());
        }

        // =====================================================================
        // (6) Áp voucher nếu có bằng DiscountStrategy; voucher_usages.user_id = null
        //     Cập nhật used_count bằng câu UPDATE có điều kiện đã có
        // =====================================================================
        Voucher appliedVoucher = null;
        if (command.getVoucherCode() != null && !command.getVoucherCode().trim().isBlank()) {
            String code = command.getVoucherCode().trim().toUpperCase();
            appliedVoucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá [" + code + "] không tồn tại trên hệ thống."));

            appliedVoucher.validateApplicable(order.getSubtotal(), LocalDateTime.now());

            DiscountStrategy discountStrategy = discountStrategyRegistry.getStrategy(appliedVoucher.getDiscountType());
            BigDecimal discountAmount = discountStrategy.calculateDiscount(appliedVoucher, order.getSubtotal());
            order.applyVoucher(appliedVoucher.getCode(), discountAmount);
        }

        // =====================================================================
        // (7) Lưu đơn và ghi order_status_history (từ rỗng sang COMPLETED)
        // =====================================================================
        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(null)
                .toStatus(OrderStatus.COMPLETED)
                .changedBy(cashierUser)
                .note("Bán hàng tại quầy POS")
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        if (appliedVoucher != null) {
            int affected = voucherRepository.incrementUsedCountConditionally(
                    appliedVoucher.getId(),
                    VoucherStatus.ACTIVE,
                    LocalDateTime.now()
            );
            if (affected == 0) {
                throw new BadRequestException("Mã giảm giá [" + appliedVoucher.getCode() + "] đã hết lượt sử dụng hoặc không còn hiệu lực.");
            }

            VoucherUsage usage = VoucherUsage.builder()
                    .voucher(appliedVoucher)
                    .order(savedOrder)
                    .user(null) // Khách vãng lai tại quầy
                    .usedAt(LocalDateTime.now())
                    .build();
            voucherUsageRepository.save(usage);
        }

        // =====================================================================
        // (8) Phát OrderPlacedEvent để trừ kho chi nhánh đồng bộ qua Observer
        // =====================================================================
        eventPublisher.publishEvent(new OrderPlacedEvent(this, savedOrder));

        // =====================================================================
        // (9) Tạo Payment qua strategy của phương thức đã chọn
        // =====================================================================
        PaymentStrategy paymentStrategy = paymentStrategyRegistry.getPosStrategy(command.getPaymentMethod());
        Payment payment;
        if (command.getPaymentMethod() == PaymentMethod.CASH) {
            payment = paymentStrategy.processPayment(savedOrder, command.getCashReceived());
        } else {
            payment = paymentStrategy.processPayment(savedOrder);
        }
        paymentRepository.save(payment);
        savedOrder.getPayments().add(payment);

        log.info("Bán hàng tại quầy POS thành công: Mã đơn [{}], Chi nhánh [{}], Thu ngân [{}], Tổng tiền [{}]",
                savedOrder.getOrderCode(), store.getName(), cashierUser.getEmail(), savedOrder.getTotalAmount());

        // Ánh xạ sang DTO trả về
        String cashierFullName = "Thu ngân";
        if (cashierUser.getProfile() != null && cashierUser.getProfile().getFullName() != null && !cashierUser.getProfile().getFullName().isBlank()) {
            cashierFullName = cashierUser.getProfile().getFullName();
        } else if (cashierUser.getEmail() != null) {
            cashierFullName = cashierUser.getEmail();
        }

        List<PosSaleItemResponseDTO> itemDTOs = savedOrder.getItems().stream()
                .map(item -> PosSaleItemResponseDTO.builder()
                        .id(item.getId())
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .color(item.getColor())
                        .size(item.getSize())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return PosSaleResponseDTO.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .channel(savedOrder.getChannel().name())
                .status(savedOrder.getStatus().name())
                .storeId(store.getId())
                .storeName(store.getName())
                .cashierId(cashierUser.getId())
                .cashierName(cashierFullName)
                .subtotal(savedOrder.getSubtotal())
                .discountTotal(savedOrder.getDiscountTotal())
                .totalAmount(savedOrder.getTotalAmount())
                .voucherCode(savedOrder.getVoucherCode())
                .paymentMethod(payment.getMethod())
                .cashReceived(payment.getCashReceived())
                .changeAmount(payment.getChangeAmount())
                .transactionRef(payment.getTransactionRef())
                .placedAt(savedOrder.getPlacedAt())
                .completedAt(savedOrder.getCompletedAt())
                .totalQuantity(savedOrder.getTotalQuantity())
                .items(itemDTOs)
                .build();
    }

    private Employee getActivePosEmployee(Long userId) {
        if (userId == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Tài khoản không thuộc cửa hàng nào."));

        if (!employee.isActive()) {
            throw new BadRequestException("Tài khoản nhân viên đã bị khóa hoặc tạm dừng hoạt động.");
        }

        if (!employee.isCashier() && !employee.isManager()) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        if (employee.getStore() == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        return employee;
    }

    private void validateSaleItems(PosSaleCommand command) {
        if (command == null || command.getItems() == null || command.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng không được để trống.");
        }

        for (PosSaleItemCommand itemCmd : command.getItems()) {
            if (itemCmd.getVariantId() == null) {
                throw new BadRequestException("Mã biến thể sản phẩm không được để trống.");
            }
            if (itemCmd.getQuantity() == null || itemCmd.getQuantity() < 1 || itemCmd.getQuantity() > 20) {
                throw new BadRequestException("Số lượng mua mỗi sản phẩm phải từ 1 đến 20.");
            }
        }
    }
}

