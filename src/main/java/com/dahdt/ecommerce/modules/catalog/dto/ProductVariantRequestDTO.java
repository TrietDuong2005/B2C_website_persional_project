package com.dahdt.ecommerce.modules.catalog.dto;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu khi thêm mới hoặc sửa biến thể sản phẩm.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequestDTO {

    private Long id;

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    @NotBlank(message = "Mã SKU không được để trống")
    @Size(max = 100, message = "Mã SKU tối đa 100 ký tự")
    private String sku;

    @Size(max = 100, message = "Mã vạch tối đa 100 ký tự")
    private String barcode;

    @NotBlank(message = "Màu sắc không được để trống")
    @Size(max = 50, message = "Màu sắc tối đa 50 ký tự")
    private String color;

    @NotBlank(message = "Kích cỡ không được để trống")
    @Size(max = 50, message = "Kích cỡ tối đa 50 ký tự")
    private String size;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Giá vốn không được để trống")
    @DecimalMin(value = "0.00", message = "Giá vốn phải từ 0 trở lên")
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @NotNull(message = "Khối lượng không được để trống")
    @Min(value = 0, message = "Khối lượng phải từ 0 gram")
    @Builder.Default
    private Integer weightGram = 0;

    @Builder.Default
    private ProductVariantStatus status = ProductVariantStatus.ACTIVE;

    /** Tùy chọn: URL ảnh nếu muốn tạo kèm ảnh cho biến thể này */
    @Size(max = 500, message = "URL hình ảnh tối đa 500 ký tự")
    private String imageUrl;
}

