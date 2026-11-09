package com.dahdt.ecommerce.modules.pos.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lệnh bán hàng tại quầy POS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleCommand {

    /** Danh sách các dòng mặt hàng cần bán (không được rỗng) */
    @NotEmpty(message = "Giỏ hàng không được để trống")
    @Valid
    private List<PosSaleItemCommand> items;

    /** Mã voucher giảm giá (tùy chọn) */
    private String voucherCode;

    /** Phương thức thanh toán (CASH, CARD, QR) */
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    /** Số tiền khách đưa (bắt buộc đối với phương thức CASH) */
    private BigDecimal cashReceived;
}

