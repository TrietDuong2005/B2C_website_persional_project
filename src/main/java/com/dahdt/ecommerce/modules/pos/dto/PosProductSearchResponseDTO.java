package com.dahdt.ecommerce.modules.pos.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin biến thể sản phẩm tìm kiếm phục vụ màn hình POS.
 * Bao gồm thông tin biến thể, đơn giá và số lượng tồn kho tại chính cửa hàng của thu ngân.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosProductSearchResponseDTO {

    /** ID biến thể sản phẩm */
    private Long variantId;

    /** Mã SKU của biến thể */
    private String sku;

    /** Mã vạch barcode */
    private String barcode;

    /** Tên sản phẩm */
    private String productName;

    /** Màu sắc */
    private String color;

    /** Kích cỡ size */
    private String size;

    /** Đơn giá bán tại quầy */
    private BigDecimal unitPrice;

    /** Số lượng tồn thực tế tại cửa hàng của thu ngân */
    private Integer quantityOnHand;
}

