package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;

/**
 * Form dữ liệu nhận hàng và đặt hàng trên giao diện Web (/checkout).
 * Áp dụng Bean Validation để kiểm tra dữ liệu đầu vào.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutForm {

    /** Họ và tên người nhận hàng (bắt buộc) */
    @NotBlank(message = "Họ và tên người nhận không được để trống")
    private String shipName;

    /** Số điện thoại nhận hàng (bắt buộc, 10 số bắt đầu bằng 0) */
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0")
    private String shipPhone;

    /** Tỉnh / Thành phố nhận hàng (bắt buộc) */
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;

    /** Quận / Huyện nhận hàng (bắt buộc) */
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    /** Phường / Xã nhận hàng (bắt buộc) */
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    /** Địa chỉ chi tiết nhận hàng: số nhà, tên đường (bắt buộc) */
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detailAddress;

    /** Tọa độ GPS nhận hàng (tùy chọn) */
    private BigDecimal shipLatitude;
    private BigDecimal shipLongitude;

    /** Ghi chú đơn hàng từ khách (tùy chọn) */
    private String note;

    /** Mã voucher giảm giá (tùy chọn) */
    private String voucherCode;

    /** Phương thức thanh toán (bắt buộc: COD hoặc EWALLET_MOCK) */
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    /**
     * Ghép các thành phần địa chỉ thành chuỗi địa chỉ giao hàng đầy đủ.
     */
    public String buildFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (detailAddress != null && !detailAddress.isBlank()) {
            sb.append(detailAddress.trim());
        }
        if (ward != null && !ward.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(ward.trim());
        }
        if (district != null && !district.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(district.trim());
        }
        if (province != null && !province.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(province.trim());
        }
        return sb.toString();
    }

    /**
     * Chuyển đổi Form sang PlaceOrderCommand để gọi Service.
     */
    public PlaceOrderCommand toCommand() {
        return PlaceOrderCommand.builder()
                .shipName(this.shipName != null ? this.shipName.trim() : null)
                .shipPhone(this.shipPhone != null ? this.shipPhone.trim() : null)
                .shipAddress(buildFullAddress())
                .shipLatitude(this.shipLatitude)
                .shipLongitude(this.shipLongitude)
                .note(this.note != null && !this.note.isBlank() ? this.note.trim() : null)
                .voucherCode(this.voucherCode != null && !this.voucherCode.trim().isBlank() ? this.voucherCode.trim().toUpperCase() : null)
                .paymentMethod(this.paymentMethod != null ? this.paymentMethod : PaymentMethod.COD)
                .build();
    }
}

