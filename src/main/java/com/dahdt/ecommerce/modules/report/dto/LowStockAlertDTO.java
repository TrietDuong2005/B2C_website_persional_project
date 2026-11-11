package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO chỉ đọc cho các mặt hàng biến thể có số lượng tồn kho chạm hoặc thấp hơn ngưỡng cảnh báo.
 */
@Getter
@Builder
public class LowStockAlertDTO {

    private final Long inventoryId;
    private final String storeName;
    private final String sku;
    private final String productName;
    private final String color;
    private final String size;
    private final int quantityOnHand;
    private final int reorderLevel;
    private final boolean outOfStock;
}

