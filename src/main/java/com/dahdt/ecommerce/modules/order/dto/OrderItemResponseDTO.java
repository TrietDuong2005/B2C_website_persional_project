package com.dahdt.ecommerce.modules.order.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO hiển thị chi tiết dòng sản phẩm trong đơn hàng (snapshot).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private Long variantId;
    private String productName;
    private String sku;
    private String color;
    private String size;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private String imageUrl;
}

