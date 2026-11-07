package com.dahdt.ecommerce.modules.order.shipping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Kiểm thử tính phí vận chuyển đồng giá (FlatRateShippingFeeCalculator)")
class FlatRateShippingFeeCalculatorTest {

    private final FlatRateShippingFeeCalculator calculator = new FlatRateShippingFeeCalculator(
            new BigDecimal("30000"),
            new BigDecimal("1000000")
    );

    @Test
    @DisplayName("Dưới ngưỡng miễn phí: tính phí đồng giá 30.000đ")
    void testCalculate_BelowThreshold() {
        BigDecimal subtotalAfterDiscount = new BigDecimal("850000");
        BigDecimal fee = calculator.calculateShippingFee(subtotalAfterDiscount);

        assertMoneyEquals(new BigDecimal("30000"), fee);
    }

    @Test
    @DisplayName("Bằng đúng ngưỡng miễn phí: phí ship 0đ")
    void testCalculate_ExactThreshold() {
        BigDecimal subtotalAfterDiscount = new BigDecimal("1000000");
        BigDecimal fee = calculator.calculateShippingFee(subtotalAfterDiscount);

        assertMoneyEquals(BigDecimal.ZERO, fee);
    }

    @Test
    @DisplayName("Vượt trên ngưỡng miễn phí: phí ship 0đ")
    void testCalculate_AboveThreshold() {
        BigDecimal subtotalAfterDiscount = new BigDecimal("1500000");
        BigDecimal fee = calculator.calculateShippingFee(subtotalAfterDiscount);

        assertMoneyEquals(BigDecimal.ZERO, fee);
    }

    @Test
    @DisplayName("Đơn 0đ hoặc null: tính phí đồng giá 30.000đ")
    void testCalculate_ZeroOrNull() {
        assertMoneyEquals(new BigDecimal("30000"), calculator.calculateShippingFee(BigDecimal.ZERO));
        assertMoneyEquals(new BigDecimal("30000"), calculator.calculateShippingFee(null));
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}

