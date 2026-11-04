package com.dahdt.ecommerce.modules.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin dòng tồn kho của một biến thể tại một chi nhánh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO {

    private Long id;
    private Long variantId;
    private String sku;
    private String barcode;
    private String productName;
    private String categoryName;
    private String color;
    private String size;
    private BigDecimal price;

    private Long storeId;
    private String storeCode;
    private String storeName;

    private Integer quantityOnHand;
    private Integer reorderLevel;
    private Long version;

    private LocalDateTime updatedAt;

    public String getStockStatus() {
        if (quantityOnHand == null || quantityOnHand <= 0) {
            return "OUT_OF_STOCK";
        }
        int threshold = (reorderLevel != null) ? reorderLevel : 5;
        if (quantityOnHand <= threshold) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }

    public String getStockStatusLabel() {
        return switch (getStockStatus()) {
            case "OUT_OF_STOCK" -> "Hết hàng";
            case "LOW_STOCK" -> "Sắp hết";
            default -> "Đủ hàng";
        };
    }

    public String getStockStatusBadgeClass() {
        return switch (getStockStatus()) {
            case "OUT_OF_STOCK" -> "badge text-bg-danger";
            case "LOW_STOCK" -> "badge text-bg-warning";
            default -> "badge text-bg-success";
        };
    }
}

