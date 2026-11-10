package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Concrete State - Trạng thái Hoàn tất)
 * ============================================================================
 * Đại diện cho trạng thái COMPLETED:
 * - Khách hàng đã nhận hàng thành công (đơn Online) hoặc đơn bán thành công tại quầy (POS).
 * - Đây là TRẠNG THÁI CUỐI CÙNG (Terminal State) của vòng đời đơn hàng: không cho phép
 *   chuyển tiếp sang bất kỳ trạng thái nào khác.
 * - KHÔNG ĐƯỢC PHÉP HỦY ĐƠN.
 * - Hành vi đặc thù khi vào trạng thái: cập nhật completed_at và tự động chuyển
 *   giao dịch thanh toán COD từ PENDING sang PAID (được điều phối bởi OrderStatusService).
 * ============================================================================
 */
@Component
public class CompletedState implements OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.COMPLETED;
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

