package com.dahdt.ecommerce.modules.promotion.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Voucher khuyến mãi (bảng vouchers).
 * Khớp chính xác với migration V23.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Chứa luật nghiệp vụ tự kiểm tra tính hợp lệ của chính nó (Rich Domain Model).
 */
@Entity
@Table(name = "vouchers",
        uniqueConstraints = @UniqueConstraint(name = "UQ_vouchers_code", columnNames = {"code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã voucher duy nhất (ví dụ: HELLOSUMMER, GIAM50K) */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /** Tên chương trình ưu đãi hoặc mô tả hiển thị */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Loại hình giảm giá: PERCENT (theo %) hoặc FIXED (số tiền cố định) */
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /** Giá trị giảm (số % hoặc số tiền tùy discountType) */
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    /** Giá trị đơn hàng tối thiểu để được áp dụng mã */
    @Column(name = "min_order_value", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    /** Mức giảm tối đa (áp dụng cho PERCENT; có thể null nếu không giới hạn) */
    @Column(name = "max_discount_value", precision = 18, scale = 2)
    private BigDecimal maxDiscountValue;

    /** Giới hạn tổng số lượt sử dụng toàn hệ thống (null nếu không giới hạn) */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** Số lượt đã sử dụng */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /** Thời điểm bắt đầu có hiệu lực */
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    /** Thời điểm kết thúc hiệu lực */
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    /** Trạng thái kích hoạt: ACTIVE hoặc INACTIVE */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Kiểm tra toàn diện điều kiện áp dụng voucher cho một đơn hàng với giá trị tạm tính cụ thể.
     * Ném BadRequestException với thông báo tiếng Việt nêu rõ lý do nếu vi phạm.
     *
     * @param subtotal Giá trị tạm tính của đơn hàng
     * @param atTime Thời điểm kiểm tra (thường là LocalDateTime.now())
     * @throws BadRequestException khi voucher không hợp lệ
     */
    public void validateApplicable(BigDecimal subtotal, LocalDateTime atTime) {
        LocalDateTime checkTime = (atTime != null) ? atTime : LocalDateTime.now();

        // 1. Kiểm tra trạng thái hoạt động
        if (this.status != VoucherStatus.ACTIVE) {
            throw new BadRequestException("Mã giảm giá [" + this.code + "] hiện không hoạt động hoặc đã bị vô hiệu hóa.");
        }

        // 2. Kiểm tra thời gian bắt đầu
        if (this.startAt != null && checkTime.isBefore(this.startAt)) {
            throw new BadRequestException("Mã giảm giá [" + this.code + "] chưa đến thời gian áp dụng.");
        }

        // 3. Kiểm tra hạn sử dụng
        if (this.endAt != null && checkTime.isAfter(this.endAt)) {
            throw new BadRequestException("Mã giảm giá [" + this.code + "] đã hết hạn sử dụng.");
        }

        // 4. Kiểm tra lượt sử dụng
        if (this.usageLimit != null && this.usedCount != null && this.usedCount >= this.usageLimit) {
            throw new BadRequestException("Mã giảm giá [" + this.code + "] đã hết lượt sử dụng.");
        }

        // 5. Kiểm tra giá trị đơn hàng tối thiểu
        BigDecimal effectiveSubtotal = (subtotal != null) ? subtotal : BigDecimal.ZERO;
        BigDecimal requiredMin = (this.minOrderValue != null) ? this.minOrderValue : BigDecimal.ZERO;
        if (effectiveSubtotal.compareTo(requiredMin) < 0) {
            throw new BadRequestException(
                    "Đơn hàng chưa đạt giá trị tối thiểu " + MoneyUtil.format(requiredMin) +
                            " để áp dụng mã giảm giá [" + this.code + "]."
            );
        }
    }

    /**
     * Kiểm tra nhanh voucher có đang trong trạng thái và thời gian hiệu lực hay không.
     */
    public boolean isActiveAndTimeValid(LocalDateTime atTime) {
        LocalDateTime checkTime = (atTime != null) ? atTime : LocalDateTime.now();
        if (this.status != VoucherStatus.ACTIVE) {
            return false;
        }
        if (this.startAt != null && checkTime.isBefore(this.startAt)) {
            return false;
        }
        if (this.endAt != null && checkTime.isAfter(this.endAt)) {
            return false;
        }
        return true;
    }
}

