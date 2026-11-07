package com.dahdt.ecommerce.modules.order.shipping;

import java.math.BigDecimal;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Định nghĩa thuật toán tính cước phí vận chuyển (ShippingFeeCalculator).
 * Lý do sử dụng:
 * - Hiện tại (Must Have): Áp dụng cước phí đồng giá cố định với chính sách miễn phí ship
 *   khi đạt giá trị đơn hàng ngưỡng tối thiểu (FlatRateShippingFeeCalculator).
 * - Tương lai (Should Have): Dễ dàng bổ sung DistanceBasedShippingFeeCalculator
 *   tính theo khoảng cách địa lý (công thức Haversine từ tọa độ latitude/longitude)
 *   mà không phá vỡ logic tính tiền đơn hàng hiện tại.
 * ============================================================================
 */
public interface ShippingFeeCalculator {

    /**
     * Tính toán chi phí vận chuyển dựa trên giá trị đơn hàng sau khi đã trừ giảm giá.
     *
     * @param subtotalAfterDiscount Giá trị hàng hóa thực tế sau chiết khấu voucher
     * @return Cước phí vận chuyển (>= 0), làm tròn số nguyên đồng
     */
    BigDecimal calculateShippingFee(BigDecimal subtotalAfterDiscount);
}

