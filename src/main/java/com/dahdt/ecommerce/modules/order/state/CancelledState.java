package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Đã hủy)
 * ============================================================================
 * Đại diện cho trạng thái CANCELLED:
 * - Đơn hàng đã bị hủy bỏ bởi khách hàng hoặc người quản trị.
 * - Đây là TRẠNG THÁI CUỐI CÙNG (Terminal State): không cho phép chuyển tiếp sang
 *   bất kỳ trạng thái nào khác.
 * - Không thể hủy lại một đơn đã hủy (canCancel = false).
 * - Hành vi đặc thù khi vào trạng thái: kích hoạt phát sự kiện OrderCancelledEvent
 *   để hoàn kho, xử lý hoàn tiền và hoàn lượt sử dụng voucher.
 * ============================================================================
 */
@Component
public class CancelledState implements OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }

    @Override
    public Set<OrderStatus> getValidNextStatuses() {
        return Collections.emptySet();
    }

    @Override
    public boolean canCancel() {
        return false;
    }
}

