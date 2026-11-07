package com.dahdt.ecommerce.modules.promotion.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity Lịch sử sử dụng mã giảm giá (bảng voucher_usages).
 * Khớp chính xác với migration V24.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Lưu vết từng lượt áp dụng voucher cho từng đơn hàng và tài khoản khách hàng.
 */
@Entity
@Table(name = "voucher_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUsage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Voucher được sử dụng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /** Đơn hàng áp dụng mã voucher */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Khách hàng sử dụng voucher (null nếu khách vãng lai mua tại quầy POS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Thời điểm sử dụng */
    @Column(name = "used_at", nullable = false)
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();
}

