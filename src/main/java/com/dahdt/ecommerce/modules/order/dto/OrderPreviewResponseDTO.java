package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO trả về kết quả xem trước đơn hàng (preview), tính toán tổng tiền,
 * giảm giá voucher và cước phí vận chuyển mà không ghi bất kỳ dữ liệu nào vào CSDL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPreviewResponseDTO {

    /** Tạm tính tiền hàng */
    private BigDecimal subtotal;

    /** Tiền giảm giá từ voucher */
    private BigDecimal discountTotal;

    /** Cước phí vận chuyển */
    private BigDecimal shippingFee;

    /** Tổng thanh toán dự kiến = subtotal - discountTotal + shippingFee */
    private BigDecimal totalAmount;

    /** Mã voucher đã áp dụng */
    private String voucherCode;

    /** Tên chương trình voucher */
    private String voucherName;

    /** Tổng số lượng sản phẩm (đôi giày) */
    private Integer totalQuantity;

    /** Địa chỉ giao hàng */
    private String shippingAddress;

    /** Phương thức thanh toán được chọn */
    private PaymentMethod paymentMethod;
}

