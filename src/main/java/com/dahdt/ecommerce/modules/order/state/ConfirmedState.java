package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Đã xác nhận)
 * ============================================================================
 * Đại diện cho trạng thái CONFIRMED:
 * - Đơn hàng đã được nhân viên/quản trị viên xác nhận thông tin và sẵn sàng đóng gói.
 * - Trạng thái kế tiếp hợp lệ: PACKING hoặc CANCELLED.
 * - Được phép hủy đơn (quản trị viên).
 * ============================================================================
 */
@Component
public class ConfirmedState implements OrderState {

    private static final Set<OrderStatus> VALID_NEXT = Collections.singleton(OrderStatus.PACKING);

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CONFIRMED;
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

