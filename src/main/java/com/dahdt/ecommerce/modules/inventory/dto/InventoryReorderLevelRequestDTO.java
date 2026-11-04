package com.dahdt.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO nhận dữ liệu yêu cầu cập nhật ngưỡng đặt lại hàng (reorder_level).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReorderLevelRequestDTO {

    @NotNull(message = "ID dòng tồn kho không được để trống")
    private Long inventoryId;

    @NotNull(message = "Mức đặt lại hàng không được để trống")
    @Min(value = 0, message = "Mức đặt lại hàng phải lớn hơn hoặc bằng 0")
    private Integer reorderLevel;
}

