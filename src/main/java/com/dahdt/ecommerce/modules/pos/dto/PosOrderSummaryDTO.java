package com.dahdt.ecommerce.modules.pos.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO tóm tắt thông tin đơn hàng POS trong danh sách đơn gần đây của cửa hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderSummaryDTO {

    /** Mã đơn hàng POS (ví dụ: HD202611081234) */
    private String orderCode;

    /** Thời điểm bán hàng hoàn tất */
    private LocalDateTime orderTime;

    /** Họ tên hoặc email thu ngân lập đơn */
    private String cashierName;

    /** Tổng tiền đơn hàng */
    private BigDecimal totalAmount;

    /** Phương thức thanh toán (CASH, CARD, QR) */
    private PaymentMethod paymentMethod;

    /** Đường dẫn liên kết xem hóa đơn (/pos/receipts/{orderCode}) */
    private String receiptUrl;
}

