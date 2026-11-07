package com.dahdt.ecommerce.modules.order.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity Lịch sử chuyển trạng thái đơn hàng (bảng order_status_history).
 * Khớp chính xác với migration V21.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Ghi lại toàn bộ tiến trình biến chuyển trạng thái đơn hàng phục vụ kiểm soát vận hành.
 */
@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Đơn hàng được ghi vết lịch sử */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Trạng thái ban đầu trước khi chuyển (null khi đơn hàng vừa được tạo mới) */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    /** Trạng thái đích sau khi chuyển */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OrderStatus toStatus;

    /** Người thực hiện chuyển trạng thái (null nếu do hệ thống/khách đặt online) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    /** Ghi chú lý do chuyển trạng thái hoặc ghi chú vận hành */
    @Column(name = "note", length = 500)
    private String note;

    /** Thời điểm diễn ra chuyển trạng thái */
    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
}

