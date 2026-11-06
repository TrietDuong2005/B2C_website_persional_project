package com.dahdt.ecommerce.modules.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận dữ liệu form Thêm sản phẩm vào giỏ hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequestDTO {

    @NotNull(message = "Vui lòng chọn biến thể sản phẩm.")
    private Long variantId;

    @NotNull(message = "Số lượng không được để trống.")
    @Min(value = 1, message = "Số lượng tối thiểu là 1.")
    @Max(value = 10, message = "Số lượng tối đa mỗi lần thêm là 10.")
    @Builder.Default
    private Integer quantity = 1;
}

