package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;

/**
 * Lệnh yêu cầu đặt hàng trực tuyến (PlaceOrderCommand).
 * Chứa đầy đủ thông tin giao nhận, mã giảm giá và phương thức thanh toán.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderCommand {

    /** Tên người nhận hàng */
    @NotBlank(message = "Tên người nhận không được để trống")
    private String shipName;

    /** Số điện thoại nhận hàng */
    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại nhận hàng không đúng định dạng")
    private String shipPhone;

    /** Địa chỉ nhận hàng chi tiết */
    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String shipAddress;

    /** Tọa độ vĩ độ nhận hàng (tùy chọn) */
    private BigDecimal shipLatitude;

    /** Tọa độ kinh độ nhận hàng (tùy chọn) */
    private BigDecimal shipLongitude;

    /** Ghi chú đơn hàng từ khách */
    private String note;

    /** Mã voucher áp dụng (tùy chọn) */
    private String voucherCode;

    /** Phương thức thanh toán (bắt buộc: COD hoặc EWALLET_MOCK) */
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;
}

