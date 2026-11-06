package com.dahdt.ecommerce.modules.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO hiển thị chi tiết một dòng sản phẩm trong giỏ hàng.
 * Đầy đủ thông tin: tên, màu, size, ảnh nhỏ, đơn giá hiện tại, số lượng, thành tiền,
 * cờ cảnh báo (hết hàng / vượt tồn kho).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    /** ID của dòng CartItem */
    private Long id;

    /** ID của biến thể sản phẩm */
    private Long variantId;

    /** Tên sản phẩm mẫu */
    private String productName;

    /** Slug sản phẩm mẫu để link tới trang chi tiết */
    private String productSlug;

    /** Mã SKU của biến thể */
    private String sku;

    /** Màu sắc biến thể */
    private String color;

    /** Kích cỡ size biến thể */
    private String size;

    /** Ảnh nhỏ đại diện của sản phẩm hoặc biến thể */
    private String imageUrl;

    /** Đơn giá hiện tại lấy từ DB (bảng product_variants) */
    private BigDecimal unitPrice;

    /** Số lượng khách chọn mua trong giỏ */
    private Integer quantity;

    /** Thành tiền = đơn giá * số lượng */
    private BigDecimal lineTotal;

    /** Tồn kho khả dụng tại kho online hiện tại */
    private Integer availableStock;

    /** Cờ cảnh báo: Hết hàng hoàn toàn (availableStock <= 0) */
    private boolean outOfStock;

    /** Cờ cảnh báo: Vượt tồn kho (số lượng trong giỏ > tồn kho khả dụng) */
    private boolean stockExceeded;

    /** Cảnh báo sản phẩm không còn kinh doanh (variant hoặc product inactive) */
    private boolean unavailable;

    /** Lời nhắn cảnh báo hiển thị trên giao diện (nếu có vấn đề) */
    private String warningMessage;
}

