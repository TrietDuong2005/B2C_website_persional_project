package com.dahdt.ecommerce.modules.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO hiển thị toàn bộ giỏ hàng của khách hàng.
 * Bao gồm danh sách các dòng sản phẩm, tạm tính, tổng số lượng,
 * và cờ hasProblem để khóa nút Thanh toán nếu có bất kỳ dòng nào bị hết hàng hoặc vượt tồn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    /** ID giỏ hàng */
    private Long id;

    /** Danh sách các dòng món hàng trong giỏ */
    @Builder.Default
    private List<CartItemResponseDTO> items = new ArrayList<>();

    /** Tạm tính tiền hàng (chưa bao gồm phí ship và voucher) */
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Tổng số lượng sản phẩm (đôi giày) có trong giỏ */
    @Builder.Default
    private int totalQuantity = 0;

    /** Giỏ hàng có đang rỗng không */
    @Builder.Default
    private boolean isEmpty = true;

    /**
     * Cờ cảnh báo toàn giỏ: true nếu có ít nhất một dòng bị hết hàng, vượt tồn, hoặc ngừng bán.
     * Khi hasProblem = true, giao diện sẽ khóa nút "Thanh toán".
     */
    @Builder.Default
    private boolean hasProblem = false;
}

