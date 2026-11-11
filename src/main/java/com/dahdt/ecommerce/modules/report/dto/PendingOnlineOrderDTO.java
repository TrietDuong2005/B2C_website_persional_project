package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO chỉ đọc cho các đơn hàng trực tuyến (Shop) mới đặt đang chờ xác nhận.
 */
@Getter
@Builder
public class PendingOnlineOrderDTO {

    private final Long orderId;
    private final String orderCode;
    private final String shipName;
    private final String shipPhone;
    private final LocalDateTime placedAt;
    private final BigDecimal totalAmount;
}

