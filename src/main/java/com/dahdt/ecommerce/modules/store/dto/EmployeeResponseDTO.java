package com.dahdt.ecommerce.modules.store.dto;

import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin nhân viên chuỗi cho giao diện quản trị.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private Long storeId;
    private String storeCode;
    private String storeName;
    private EmployeePosition position;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getPositionLabel() {
        if (position == null) return "Nhân viên";
        return switch (position) {
            case MANAGER -> "Quản lý cửa hàng";
            case CASHIER -> "Thu ngân";
            case STAFF -> "Nhân viên";
        };
    }

    public String getPositionBadgeClass() {
        if (position == null) return "badge text-bg-secondary";
        return switch (position) {
            case MANAGER -> "badge text-bg-danger";
            case CASHIER -> "badge text-bg-info";
            case STAFF -> "badge text-bg-secondary";
        };
    }

    public String getStatusLabel() {
        return EmployeeStatus.ACTIVE.equals(status) ? "Đang làm việc" : "Đã khóa / Nghỉ việc";
    }

    public String getStatusBadgeClass() {
        return EmployeeStatus.ACTIVE.equals(status) ? "badge text-bg-success" : "badge text-bg-secondary";
    }
}

