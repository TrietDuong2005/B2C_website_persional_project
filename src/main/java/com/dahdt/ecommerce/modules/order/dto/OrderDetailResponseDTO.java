package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO hiển thị đầy đủ chi tiết đơn hàng: sản phẩm, thanh toán và lịch sử trạng thái.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponseDTO {

    private Long id;
    private String orderCode;
    private OrderChannel channel;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String voucherCode;
    private Integer totalQuantity;
    private LocalDateTime placedAt;
    private LocalDateTime completedAt;
    private String shipName;
    private String shipPhone;
    private String shipAddress;
    private BigDecimal shipLatitude;
    private BigDecimal shipLongitude;
    private String note;
    private Long storeId;
    private String storeName;
    private String customerName;
    private String cashierName;

    @Builder.Default
    private List<OrderItemResponseDTO> items = new ArrayList<>();

    @Builder.Default
    private List<PaymentResponseDTO> payments = new ArrayList<>();

    @Builder.Default
    private List<OrderStatusHistoryResponseDTO> statusHistories = new ArrayList<>();
}

