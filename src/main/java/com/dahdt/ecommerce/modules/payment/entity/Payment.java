package com.dahdt.ecommerce.modules.payment.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.modules.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Giao dịch thanh toán (bảng payments).
 * Khớp chính xác với migration V22.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Quản lý chi tiết thanh toán cho đơn hàng, hỗ trợ đa phương thức thanh toán qua Strategy Pattern.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Đơn hàng gắn liền với giao dịch thanh toán này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Phương thức thanh toán (CASH, CARD, QR, COD, EWALLET_MOCK) */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    /** Số tiền thanh toán */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** Trạng thái thanh toán: PENDING, PAID, FAILED, REFUNDED */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Mã tham chiếu giao dịch cổng thanh toán / ngân hàng / ví điện tử */
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    /** Tiền mặt khách đưa (dành cho đơn bán tại quầy POS) */
    @Column(name = "cash_received", precision = 18, scale = 2)
    private BigDecimal cashReceived;

    /** Tiền thối trả lại khách (dành cho đơn bán tại quầy POS) */
    @Column(name = "change_amount", precision = 18, scale = 2)
    private BigDecimal changeAmount;

    /** Thời điểm thanh toán thành công */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}

