package com.dahdt.ecommerce.modules.store.dto;

import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi nhánh / kho hàng cho giao diện web.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponseDTO {

    private Long id;
    private String code;
    private String name;
    private StoreType type;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private String fullAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private StoreStatus status;
    private Long employeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getTypeLabel() {
        return StoreType.WAREHOUSE.equals(type) ? "Kho tổng / bãi" : "Cửa hàng bán lẻ";
    }

    public String getTypeBadgeClass() {
        return StoreType.WAREHOUSE.equals(type) ? "badge text-bg-warning" : "badge text-bg-primary";
    }

    public String getStatusLabel() {
        return StoreStatus.ACTIVE.equals(status) ? "Đang hoạt động" : "Ngừng hoạt động";
    }

    public String getStatusBadgeClass() {
        return StoreStatus.ACTIVE.equals(status) ? "badge text-bg-success" : "badge text-bg-secondary";
    }
}

