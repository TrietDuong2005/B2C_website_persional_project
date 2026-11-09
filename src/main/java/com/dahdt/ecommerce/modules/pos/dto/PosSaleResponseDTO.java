package com.dahdt.ecommerce.modules.pos.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kết quả trả về sau khi hoàn tất checkout đơn hàng bán tại quầy POS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleResponseDTO {

    private Long orderId;
    private String orderCode;
    private String channel;
    private String status;
    private Long storeId;
    private String storeName;
    private Long cashierId;
    private String cashierName;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal totalAmount;
    private String voucherCode;
    private PaymentMethod paymentMethod;
    private BigDecimal cashReceived;
    private BigDecimal changeAmount;
    private String transactionRef;
    private LocalDateTime placedAt;
    private LocalDateTime completedAt;
    private Integer totalQuantity;
    private List<PosSaleItemResponseDTO> items;
}

