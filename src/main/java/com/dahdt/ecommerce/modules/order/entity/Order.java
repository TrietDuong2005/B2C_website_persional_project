package com.dahdt.ecommerce.modules.order.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Đơn hàng đa kênh (bảng orders).
 * Khớp chính xác với migration V19.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Áp dụng mô hình Rich Domain Model: luật nghiệp vụ tính tiền, thêm sản phẩm và snapshot
 * nằm trực tiếp bên trong thực thể Order.
 */
@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "UQ_orders_order_code", columnNames = {"order_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã đơn hàng hiển thị duy nhất (ví dụ: DH202611070001) */
    @Column(name = "order_code", nullable = false, length = 50, unique = true)
    private String orderCode;

    /** Kênh bán hàng: ONLINE hoặc POS */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private OrderChannel channel;

    /** Chi nhánh tiếp nhận và chịu trách nhiệm xuất kho */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Tài khoản khách hàng đặt mua (có thể null nếu là khách lẻ mua trực tiếp tại quầy POS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    /** Nhân viên/thu ngân lập đơn tại quầy (null với đơn đặt online) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    /** Trạng thái đơn hàng: PENDING, CONFIRMED, PACKING, SHIPPING, COMPLETED, CANCELLED */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Tiền hàng trước chiết khấu (tổng tiền các sản phẩm) */
    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Tổng số tiền giảm giá từ voucher khuyến mãi */
    @Column(name = "discount_total", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;

    /** Cước phí vận chuyển giao hàng (0đ với đơn POS) */
    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /** Tổng số tiền khách hàng phải thanh toán = subtotal - discount_total + shipping_fee */
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** Mã voucher đã áp dụng */
    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    /** Snapshot: Tên người nhận hàng */
    @Column(name = "ship_name", length = 255)
    private String shipName;

    /** Snapshot: Số điện thoại nhận hàng */
    @Column(name = "ship_phone", length = 20)
    private String shipPhone;

    /** Snapshot: Địa chỉ giao hàng chi tiết */
    @Column(name = "ship_address", length = 500)
    private String shipAddress;

    /** Snapshot: Tọa độ vĩ độ nhận hàng */
    @Column(name = "ship_latitude", precision = 10, scale = 7)
    private BigDecimal shipLatitude;

    /** Snapshot: Tọa độ kinh độ nhận hàng */
    @Column(name = "ship_longitude", precision = 10, scale = 7)
    private BigDecimal shipLongitude;

    /** Ghi chú của khách hàng khi đặt đơn */
    @Column(name = "note", length = 500)
    private String note;

    /** Thời điểm đặt hàng */
    @Column(name = "placed_at", nullable = false)
    @Builder.Default
    private LocalDateTime placedAt = LocalDateTime.now();

    /** Thời điểm giao hàng thành công hoặc hoàn tất */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Danh sách các dòng mặt hàng trong đơn */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Lịch sử các lần chuyển trạng thái đơn hàng */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    @Builder.Default
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    /** Danh sách giao dịch thanh toán của đơn hàng */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Thêm sản phẩm vào đơn hàng và chụp ảnh dữ liệu (Snapshot) tại thời điểm mua:
     * tên sản phẩm, mã SKU, màu, size, đơn giá, số lượng, thành tiền.
     * Tự động tính lại tổng tiền đơn hàng sau khi thêm.
     *
     * @param variant Biến thể sản phẩm cần thêm
     * @param quantity Số lượng mua (phải > 0)
     * @return Dòng OrderItem đã được tạo và snapshot
     */
    public OrderItem addItem(ProductVariant variant, int quantity) {
        if (variant == null) {
            throw new BadRequestException("Biến thể sản phẩm không được để trống.");
        }
        if (quantity <= 0) {
            throw new BadRequestException("Số lượng mua phải lớn hơn 0 (nhận được: " + quantity + ").");
        }

        String prodName = (variant.getProduct() != null) ? variant.getProduct().getName() : "Sản phẩm";
        BigDecimal unitPrice = (variant.getPrice() != null) ? variant.getPrice() : BigDecimal.ZERO;
        BigDecimal lineTotal = MoneyUtil.roundMoney(unitPrice.multiply(BigDecimal.valueOf(quantity)));

        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        OrderItem item = OrderItem.builder()
                .order(this)
                .variant(variant)
                .productName(prodName)
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();

        this.items.add(item);
        recalculateTotals();
        return item;
    }

    /**
     * Áp dụng voucher giảm giá vào đơn hàng.
     *
     * @param code Mã voucher
     * @param discountValue Số tiền được giảm
     */
    public void applyVoucher(String code, BigDecimal discountValue) {
        this.voucherCode = code;
        this.discountTotal = (discountValue != null) ? MoneyUtil.roundMoney(discountValue) : BigDecimal.ZERO;
        recalculateTotals();
    }

    /**
     * Áp dụng phí vận chuyển vào đơn hàng.
     *
     * @param fee Phí ship
     */
    public void applyShippingFee(BigDecimal fee) {
        this.shippingFee = (fee != null) ? MoneyUtil.roundMoney(fee) : BigDecimal.ZERO;
        recalculateTotals();
    }

    /**
     * Tính toán lại toàn bộ các mức tiền của đơn hàng:
     * - subtotal = tổng lineTotal của tất cả các mặt hàng
     * - totalAmount = subtotal - discountTotal + shippingFee (không được âm)
     * - Mọi phép tính đều làm tròn HALF_UP về số nguyên đồng thông qua MoneyUtil.
     */
    public void recalculateTotals() {
        if (this.items == null || this.items.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
        } else {
            this.subtotal = this.items.stream()
                    .map(OrderItem::getLineTotal)
                    .filter(val -> val != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        this.subtotal = MoneyUtil.roundMoney(this.subtotal);

        BigDecimal discount = (this.discountTotal != null) ? MoneyUtil.roundMoney(this.discountTotal) : BigDecimal.ZERO;
        BigDecimal shipping = (this.shippingFee != null) ? MoneyUtil.roundMoney(this.shippingFee) : BigDecimal.ZERO;

        // total_amount = subtotal - discount_total + shipping_fee
        BigDecimal calculatedTotal = this.subtotal.subtract(discount).add(shipping);
        if (calculatedTotal.compareTo(BigDecimal.ZERO) < 0) {
            calculatedTotal = BigDecimal.ZERO;
        }
        this.totalAmount = MoneyUtil.roundMoney(calculatedTotal);
    }

    /**
     * Lấy tổng số lượng sản phẩm (đôi giày) có trong đơn hàng.
     */
    public int getTotalQuantity() {
        if (this.items == null || this.items.isEmpty()) {
            return 0;
        }
        return this.items.stream()
                .mapToInt(item -> (item.getQuantity() != null) ? item.getQuantity() : 0)
                .sum();
    }
}

