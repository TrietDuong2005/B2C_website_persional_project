package com.dahdt.ecommerce.modules.promotion.strategy;

import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import com.dahdt.ecommerce.modules.promotion.entity.Voucher;

import java.math.BigDecimal;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Strategy Interface định nghĩa thuật toán tính toán giá trị giảm giá.
 * Lý do sử dụng:
 * 1. Đa hình thuật toán giảm giá: Tách rời cách tính chiết khấu theo tỷ lệ phần trăm
 *    (PercentDiscountStrategy) và giảm số tiền cố định (FixedDiscountStrategy).
 * 2. Tuân thủ nguyên tắc Open/Closed (OCP): Khi phát sinh hình thức khuyến mãi mới
 *    (ví dụ: mua 2 tặng 1, giảm giá bậc thang...), hệ thống chỉ cần bổ sung
 *    implementation mới mà không phải sửa đổi mã nguồn xử lý đơn hàng hiện có.
 * ============================================================================
 */
public interface DiscountStrategy {

    /**
     * Xác định loại hình giảm giá mà Strategy này hỗ trợ (PERCENT hoặc FIXED).
     */
    DiscountType getSupportedType();

    /**
     * Tính toán số tiền được giảm giá thực tế cho đơn hàng dựa trên voucher và tạm tính.
     * Quy tắc chung:
     * - Không bao giờ giảm vượt quá giá trị tạm tính (subtotal).
     * - Số tiền giảm không âm.
     * - Làm tròn số tiền về số nguyên đồng (HALF_UP).
     *
     * @param voucher Voucher đang xét
     * @param subtotal Giá trị tạm tính trước giảm giá
     * @return Số tiền được giảm giá thực tế (>= 0)
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal);
}

