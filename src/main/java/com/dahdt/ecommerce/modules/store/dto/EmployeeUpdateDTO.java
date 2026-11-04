package com.dahdt.ecommerce.modules.store.dto;

import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO nhận dữ liệu khi cập nhật chức vụ, điều chuyển chi nhánh hoặc đổi trạng thái nhân viên.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDTO {

    @NotNull(message = "ID nhân viên không được để trống")
    private Long id;

    @NotNull(message = "Vui lòng chọn chi nhánh công tác")
    private Long storeId;

    @NotNull(message = "Vui lòng chọn chức vụ")
    private EmployeePosition position;

    private EmployeeStatus status;
}

