package com.dahdt.ecommerce.modules.promotion.strategy;

import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Thực hiện thuật toán tính giảm giá theo tỷ lệ phần trăm (PERCENT).
 * Lý do sử dụng:
 * - Đóng gói công thức tính chiết khấu theo tỷ lệ %:
 *     discount = subtotal * (discountValue / 100)
 * - Ràng buộc mức giảm tối đa: Nếu voucher có quy định max_discount_value, số tiền
 *   giảm sẽ bị chặn trên không được vượt quá mức trần này.
 * - Đảm bảo nguyên tắc bảo toàn: Không giảm quá giá trị tạm tính và làm tròn số nguyên đồng.
 * ============================================================================
 */
@Component
public class PercentDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType getSupportedType() {
        return DiscountType.PERCENT;
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        if (voucher == null || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = voucher.getDiscountValue();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Tính số tiền giảm thô: subtotal * (rate / 100)
        BigDecimal rawDiscount = subtotal.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Chặn trên bởi max_discount_value nếu có cấu hình
        if (voucher.getMaxDiscountValue() != null && voucher.getMaxDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
            if (rawDiscount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                rawDiscount = voucher.getMaxDiscountValue();
            }
        }

        // Không bao giờ giảm vượt quá giá trị tạm tính
        if (rawDiscount.compareTo(subtotal) > 0) {
            rawDiscount = subtotal;
        }

        // Làm tròn HALF_UP về số nguyên đồng
        return MoneyUtil.roundMoney(rawDiscount);
    }
}

