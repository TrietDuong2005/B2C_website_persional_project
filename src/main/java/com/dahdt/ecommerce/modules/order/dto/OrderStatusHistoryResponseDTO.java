package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO hiển thị lịch sử biến chuyển trạng thái đơn hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponseDTO {

    private Long id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String changedByName;
    private String note;
    private LocalDateTime changedAt;
}

