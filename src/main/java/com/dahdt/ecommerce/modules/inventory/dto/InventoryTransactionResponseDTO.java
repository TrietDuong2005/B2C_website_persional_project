package com.dahdt.ecommerce.modules.inventory.dto;

import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin dòng sổ cái kho (Audit Log).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionResponseDTO {

    private Long id;
    private Long variantId;
    private String sku;
    private String productName;
    private String color;
    private String size;

    private Long storeId;
    private String storeCode;
    private String storeName;

    private InventoryTransactionType type;
    private Integer quantityChange;
    private Integer quantityAfter;

    private String referenceType;
    private Long referenceId;
    private String note;

    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;

    public String getTypeLabel() {
        if (type == null) return "Khác";
        return switch (type) {
            case IMPORT -> "Nhập kho";
            case SALE -> "Xuất bán";
            case CANCEL -> "Hoàn đơn";
            case TRANSFER_IN -> "Nhập chuyển kho";
            case TRANSFER_OUT -> "Xuất chuyển kho";
            case ADJUST -> "Điều chỉnh kho";
        };
    }

    public String getTypeBadgeClass() {
        if (type == null) return "badge text-bg-secondary";
        return switch (type) {
            case IMPORT -> "badge text-bg-primary";
            case SALE -> "badge text-bg-danger";
            case CANCEL -> "badge text-bg-info";
            case TRANSFER_IN -> "badge text-bg-success";
            case TRANSFER_OUT -> "badge text-bg-warning";
            case ADJUST -> "badge text-bg-dark";
        };
    }
}

