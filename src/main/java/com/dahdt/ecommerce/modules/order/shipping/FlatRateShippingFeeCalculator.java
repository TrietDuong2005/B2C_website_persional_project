package com.dahdt.ecommerce.modules.order.shipping;

import com.dahdt.ecommerce.common.util.MoneyUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Tính cước vận chuyển đồng giá cố định (Flat-rate).
 * Đọc cấu hình từ application.yml:
 * - app.shipping.flat-fee (mặc định: 30.000đ)
 * - app.shipping.free-threshold (mặc định: 1.000.000đ)
 * Quy tắc:
 * - Miễn phí vận chuyển (0đ) khi giá trị (tạm tính - giảm giá) >= ngưỡng free-threshold.
 * - Ngược lại, áp dụng mức phí đồng giá flat-fee.
 * ============================================================================
 */
@Component
public class FlatRateShippingFeeCalculator implements ShippingFeeCalculator {

    private final BigDecimal flatFee;
    private final BigDecimal freeThreshold;

    public FlatRateShippingFeeCalculator(
            @Value("${app.shipping.flat-fee:30000}") BigDecimal flatFee,
            @Value("${app.shipping.free-threshold:1000000}") BigDecimal freeThreshold) {
        this.flatFee = flatFee;
        this.freeThreshold = freeThreshold;
    }

    @Override
    public BigDecimal calculateShippingFee(BigDecimal subtotalAfterDiscount) {
        BigDecimal effectiveAmount = (subtotalAfterDiscount != null) ? subtotalAfterDiscount : BigDecimal.ZERO;

        if (freeThreshold != null && effectiveAmount.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        return MoneyUtil.roundMoney(flatFee != null ? flatFee : BigDecimal.valueOf(30000));
    }
}

