package com.dahdt.ecommerce.modules.pos.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Dòng chi tiết mặt hàng trên hóa đơn bán lẻ POS (dữ liệu in ấn).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosReceiptItemDTO {

    private String productName;
    private String sku;
    private String color;
    private String size;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}

