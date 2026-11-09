package com.dahdt.ecommerce.modules.pos.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

/**
 * Kết quả xem trước hóa đơn bán lẻ tại quầy POS (preview), tính toán tạm tính,
 * giảm giá, tổng tiền và tiền thối mà KHÔNG ghi bất kỳ dữ liệu nào vào CSDL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSalePreviewResponseDTO {

    /** Tạm tính tiền hàng */
    private BigDecimal subtotal;

    /** Tiền giảm giá từ voucher */
    private BigDecimal discountTotal;

    /** Tổng thanh toán = subtotal - discountTotal (phí ship = 0) */
    private BigDecimal totalAmount;

    /** Mã voucher áp dụng (nếu có) */
    private String voucherCode;

    /** Tên chương trình voucher (nếu có) */
    private String voucherName;

    /** Phương thức thanh toán được chọn */
    private PaymentMethod paymentMethod;

    /** Tiền khách đưa */
    private BigDecimal cashReceived;

    /** Tiền thối lại cho khách */
    private BigDecimal changeAmount;

    /** Tổng số lượng sản phẩm (đôi giày) */
    private Integer totalQuantity;
}

