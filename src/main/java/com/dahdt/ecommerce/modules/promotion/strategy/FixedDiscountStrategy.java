package com.dahdt.ecommerce.modules.promotion.strategy;

import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 * ============================================================================
 * Vai trò: Thực hiện thuật toán giảm giá số tiền cố định (FIXED).
 * Lý do sử dụng:
 * - Đóng gói thuật toán giảm giá theo mức tiền cứng (ví dụ: giảm ngay 50.000đ).
 * - Đảm bảo nguyên tắc bảo toàn: Nếu số tiền giảm cố định lớn hơn giá trị tạm tính
 *   của đơn hàng, số tiền giảm chỉ tối đa bằng đúng tạm tính (đơn hàng không bao giờ âm tiền).
 * ============================================================================
 */
@Component
public class FixedDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType getSupportedType() {
        return DiscountType.FIXED;
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        if (voucher == null || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = voucher.getDiscountValue();
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Không bao giờ giảm vượt quá giá trị tạm tính
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        // Làm tròn HALF_UP về số nguyên đồng
        return MoneyUtil.roundMoney(discount);
    }
}

