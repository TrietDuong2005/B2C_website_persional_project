package com.dahdt.ecommerce.modules.promotion.strategy;

import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử các chiến lược giảm giá (DiscountStrategy)")
class DiscountStrategyTest {

    private final PercentDiscountStrategy percentStrategy = new PercentDiscountStrategy();
    private final FixedDiscountStrategy fixedStrategy = new FixedDiscountStrategy();

    @Test
    @DisplayName("PercentDiscountStrategy: Giảm đúng % và làm tròn số nguyên đồng")
    void testPercentDiscount_Normal() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10.00")) // 10%
                .maxDiscountValue(null) // Không chặn trần
                .build();

        BigDecimal subtotal = new BigDecimal("450000");
        BigDecimal discount = percentStrategy.calculateDiscount(voucher, subtotal);

        // 10% của 450.000đ = 45.000đ
        assertMoneyEquals(new BigDecimal("45000"), discount);
    }

    @Test
    @DisplayName("PercentDiscountStrategy: Chặn trên bởi max_discount_value khi vượt trần")
    void testPercentDiscount_CappedByMaxDiscountValue() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10.00")) // 10%
                .maxDiscountValue(new BigDecimal("50000")) // Tối đa 50.000đ
                .build();

        BigDecimal subtotal = new BigDecimal("1200000"); // 10% là 120.000đ > 50.000đ
        BigDecimal discount = percentStrategy.calculateDiscount(voucher, subtotal);

        assertMoneyEquals(new BigDecimal("50000"), discount);
    }

    @Test
    @DisplayName("PercentDiscountStrategy: Không bao giờ giảm vượt quá giá trị tạm tính")
    void testPercentDiscount_NotExceedSubtotal() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("150.00")) // 150%
                .maxDiscountValue(null)
                .build();

        BigDecimal subtotal = new BigDecimal("200000");
        BigDecimal discount = percentStrategy.calculateDiscount(voucher, subtotal);

        assertMoneyEquals(new BigDecimal("200000"), discount);
    }

    @Test
    @DisplayName("PercentDiscountStrategy: Làm tròn HALF_UP về số nguyên đồng")
    void testPercentDiscount_RoundingHalfUp() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("15.00")) // 15%
                .maxDiscountValue(null)
                .build();

        BigDecimal subtotal = new BigDecimal("105500");
        // 105.500 * 0.15 = 15.825 -> làm tròn 15.825
        BigDecimal discount = percentStrategy.calculateDiscount(voucher, subtotal);

        assertMoneyEquals(new BigDecimal("15825"), discount);
    }

    @Test
    @DisplayName("FixedDiscountStrategy: Giảm đúng số tiền cố định")
    void testFixedDiscount_Normal() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000"))
                .build();

        BigDecimal subtotal = new BigDecimal("350000");
        BigDecimal discount = fixedStrategy.calculateDiscount(voucher, subtotal);

        assertMoneyEquals(new BigDecimal("50000"), discount);
    }

    @Test
    @DisplayName("FixedDiscountStrategy: Không bao giờ giảm vượt quá tạm tính")
    void testFixedDiscount_NotExceedSubtotal() {
        Voucher voucher = Voucher.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("100000")) // Giảm 100.000đ
                .build();

        BigDecimal subtotal = new BigDecimal("60000"); // Đơn chỉ có 60.000đ
        BigDecimal discount = fixedStrategy.calculateDiscount(voucher, subtotal);

        assertMoneyEquals(new BigDecimal("60000"), discount);
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}

