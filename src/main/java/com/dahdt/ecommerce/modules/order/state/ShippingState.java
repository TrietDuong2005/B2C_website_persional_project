package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Đang giao hàng)
 * ============================================================================
 * Đại diện cho trạng thái SHIPPING:
 * - Đơn hàng đã bàn giao cho đối tác vận chuyển và đang trên đường giao tới khách.
 * - Trạng thái kế tiếp hợp lệ: COMPLETED.
 * - KHÔNG ĐƯỢC PHÉP HỦY ĐƠN (hàng đã xuất kho và đang trên đường vận chuyển).
 * ============================================================================
 */
@Component
public class ShippingState implements OrderState {

    private static final Set<OrderStatus> VALID_NEXT = Collections.singleton(OrderStatus.COMPLETED);

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPING;
    }

    @Override
    public Set<OrderStatus> getValidNextStatuses() {
        return VALID_NEXT;
    }

    @Override
    public boolean canCancel() {
        return false;
    }
}

