package com.dahdt.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO nhận dữ liệu yêu cầu Điều chỉnh tồn kho (ADJUST).
 * Số lượng biến động có thể âm hoặc dương (nhưng khác 0).
 * Ghi chú lý do là BẮT BUỘC theo nghiệp vụ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAdjustRequestDTO {

    @NotNull(message = "Chi nhánh không được để trống")
    private Long storeId;

    @NotNull(message = "Biến thể sản phẩm không được để trống")
    private Long variantId;

    @NotNull(message = "Số lượng điều chỉnh không được để trống")
    private Integer quantityChange;

    @NotBlank(message = "Lý do điều chỉnh kho là bắt buộc")
    private String note;
}

