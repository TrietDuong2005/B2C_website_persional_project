package com.dahdt.ecommerce.modules.pos.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Chi tiết mặt hàng trong kết quả đơn bán POS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleItemResponseDTO {

    private Long id;
    private Long variantId;
    private String productName;
    private String sku;
    private String color;
    private String size;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}

