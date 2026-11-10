package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ============================================================================
 * DESIGN PATTERN: OBSERVER (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Sự kiện Đơn hàng đã bị hủy bỏ (Event Object).
 * Lý do sử dụng:
 * 1. Phân tách (Decoupling) trách nhiệm: OrderStatusService chỉ tập trung vào việc
 *    quản lý vòng đời và thay đổi trạng thái đơn, không cần biết chi tiết quy trình
 *    hoàn kho vật lý hay cách thức cập nhật sổ cái kho.
 * 2. Đảm bảo tính mở rộng: Khi phát sinh thêm nghiệp vụ sau khi hủy (như gửi email
 *    thông báo hủy, ghi log bảo mật, hoàn điểm tích lũy ở mức Could), chỉ cần đăng ký
 *    thêm Listener mới mà không cần sửa đổi OrderStatusService.
 * ============================================================================
 */
@Getter
public class OrderCancelledEvent extends ApplicationEvent {

    /** Đơn hàng bị hủy */
    private final Order order;

    /** Người thực hiện thao tác hủy đơn (Admin, Store Manager hoặc Khách hàng) */
    private final User actorUser;

    /** Lý do hủy đơn hàng */
    private final String cancelReason;

    public OrderCancelledEvent(Object source, Order order, User actorUser, String cancelReason) {
        super(source);
        this.order = order;
        this.actorUser = actorUser;
        this.cancelReason = cancelReason;
    }
}

