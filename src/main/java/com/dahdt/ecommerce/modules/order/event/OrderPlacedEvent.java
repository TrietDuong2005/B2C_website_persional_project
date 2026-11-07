package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.modules.order.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ============================================================================
 * DESIGN PATTERN: OBSERVER (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Sự kiện Đơn hàng đã được đặt thành công (Event Object).
 * Lý do sử dụng:
 * 1. Giảm thiểu sự gắn kết (Decoupling): Phân tách logic tiếp nhận đơn hàng (OrderService)
 *    khỏi các tác vụ phụ trợ (trừ tồn kho, ghi sổ cái kho, gửi thông báo...).
 * 2. Đảm bảo tính mở rộng: Khi có thêm các tác vụ phát sinh sau khi đặt đơn
 *    (như gửi email xác nhận, tích điểm thành viên ở mức Could), chỉ cần đăng ký
 *    thêm Listener mới mà không cần can thiệp hay sửa đổi OrderService.
 * ============================================================================
 */
@Getter
public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}

