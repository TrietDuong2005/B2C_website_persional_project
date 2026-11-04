package com.dahdt.ecommerce.modules.store.dto;

import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO nhận dữ liệu khi thêm mới nhân viên vào chi nhánh:
 * Nhập email của một tài khoản đã có, chọn chi nhánh và chức vụ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    @NotNull(message = "Chi nhánh không được để trống")
    private Long storeId;

    @NotBlank(message = "Email tài khoản không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Vui lòng chọn chức vụ (Quản lý / Thu ngân / Nhân viên)")
    private EmployeePosition position;
}

