package com.dahdt.ecommerce.modules.pos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Dòng sản phẩm yêu cầu bán tại quầy POS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleItemCommand {

    @NotNull(message = "ID biến thể sản phẩm không được để trống")
    private Long variantId;

    @NotNull(message = "Số lượng mua không được để trống")
    @Min(value = 1, message = "Số lượng mua mỗi sản phẩm phải từ 1 đến 20")
    @Max(value = 20, message = "Số lượng mua mỗi sản phẩm phải từ 1 đến 20")
    private Integer quantity;
}

