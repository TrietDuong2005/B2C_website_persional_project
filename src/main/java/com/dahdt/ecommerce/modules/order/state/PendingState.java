package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Chờ xác nhận)
 * ============================================================================
 * Đại diện cho trạng thái PENDING:
 * - Trạng thái ban đầu của đơn hàng trực tuyến khi khách vừa đặt thành công.
 * - Trạng thái kế tiếp hợp lệ: CONFIRMED hoặc CANCELLED.
 * - Được phép hủy đơn (khách hàng hoặc quản trị viên).
 * ============================================================================
 */
@Component
public class PendingState implements OrderState {

    private static final Set<OrderStatus> VALID_NEXT = Collections.singleton(OrderStatus.CONFIRMED);

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }

    @Override
    public Set<OrderStatus> getValidNextStatuses() {
        return VALID_NEXT;
    }

    @Override
    public boolean canCancel() {
        return true;
    }
}

