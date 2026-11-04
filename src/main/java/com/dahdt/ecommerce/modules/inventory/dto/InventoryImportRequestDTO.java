package com.dahdt.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO nhận dữ liệu yêu cầu Nhập kho (IMPORT).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryImportRequestDTO {

    @NotNull(message = "Chi nhánh không được để trống")
    private Long storeId;

    @NotNull(message = "Biến thể sản phẩm không được để trống")
    private Long variantId;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập kho phải lớn hơn hoặc bằng 1")
    private Integer quantity;

    private String note;
}

