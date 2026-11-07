package com.dahdt.ecommerce.modules.order.entity;

import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Kiểm thử quy tắc tính tiền và snapshot của Order Domain Entity")
class OrderEntityTest {

    @Test
    @DisplayName("addItem(): Chụp ảnh dữ liệu (snapshot) tên, sku, màu, size, đơn giá và tính thành tiền dòng")
    void testAddItem_Snapshot() {
        Order order = Order.builder().build();

        Product product = Product.builder().name("Giày Sneaker Running").build();
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku("RUN-DEN-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("1200000"))
                .build();

        OrderItem item = order.addItem(variant, 2);

        assertNotNull(item);
        assertEquals("Giày Sneaker Running", item.getProductName());
        assertEquals("RUN-DEN-42", item.getSku());
        assertEquals("Đen", item.getColor());
        assertEquals("42", item.getSize());
        assertMoneyEquals(new BigDecimal("1200000"), item.getUnitPrice());
        assertEquals(2, item.getQuantity());
        assertMoneyEquals(new BigDecimal("2400000"), item.getLineTotal());

        // Kiểm tra Order đã tự động cập nhật subtotal và totalAmount
        assertMoneyEquals(new BigDecimal("2400000"), order.getSubtotal());
        assertMoneyEquals(new BigDecimal("2400000"), order.getTotalAmount());
    }

    @Test
    @DisplayName("recalculateTotals(): total_amount = subtotal - discount_total + shipping_fee")
    void testRecalculateTotals() {
        Order order = Order.builder().build();

        Product product = Product.builder().name("Giày Tây Oxford").build();
        ProductVariant variant1 = ProductVariant.builder()
                .product(product)
                .sku("OXF-NAU-41")
                .color("Nâu")
                .size("41")
                .price(new BigDecimal("800000"))
                .build();

        ProductVariant variant2 = ProductVariant.builder()
                .product(product)
                .sku("OXF-DEN-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("1200000"))
                .build();

        order.addItem(variant1, 1); // 800.000
        order.addItem(variant2, 1); // 1.200.000 -> subtotal = 2.000.000

        assertMoneyEquals(new BigDecimal("2000000"), order.getSubtotal());

        // Áp dụng voucher giảm 100.000đ
        order.applyVoucher("GIAM100K", new BigDecimal("100000"));
        assertMoneyEquals(new BigDecimal("100000"), order.getDiscountTotal());

        // Áp dụng phí ship 30.000đ
        order.applyShippingFee(new BigDecimal("30000"));
        assertMoneyEquals(new BigDecimal("30000"), order.getShippingFee());

        // total = 2.000.000 - 100.000 + 30.000 = 1.930.000đ
        assertMoneyEquals(new BigDecimal("1930000"), order.getTotalAmount());
    }

    @Test
    @DisplayName("recalculateTotals(): Nếu giảm giá lớn hơn tạm tính, tổng thanh toán không bao giờ âm")
    void testRecalculateTotals_NonNegative() {
        Order order = Order.builder().build();
        Product product = Product.builder().name("Dép Slipper").build();
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku("DEP-01")
                .color("Xám")
                .size("40")
                .price(new BigDecimal("50000"))
                .build();

        order.addItem(variant, 1); // subtotal = 50.000
        order.applyVoucher("VIP200K", new BigDecimal("200000")); // giảm 200.000đ
        order.applyShippingFee(BigDecimal.ZERO);

        // subtotal 50.000 - discount 200.000 = -150.000 -> chặn về 0đ
        assertMoneyEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected money should not be null");
        assertNotNull(actual, "Actual money should not be null");
        assertEquals(0, expected.compareTo(actual),
                String.format("Expected <%s> but was <%s>", expected, actual));
    }
}

