package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Đang đóng gói)
 * ============================================================================
 * Đại diện cho trạng thái PACKING:
 * - Kho hàng đang tiến hành chuẩn bị, lấy hàng và đóng gói sản phẩm.
 * - Trạng thái kế tiếp hợp lệ: SHIPPING hoặc CANCELLED.
 * - Được phép hủy đơn (quản trị viên).
 * ============================================================================
 */
@Component
public class PackingState implements OrderState {

    private static final Set<OrderStatus> VALID_NEXT = Collections.singleton(OrderStatus.SHIPPING);

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PACKING;
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

