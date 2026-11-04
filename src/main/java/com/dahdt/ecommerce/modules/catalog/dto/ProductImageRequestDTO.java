package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO nhận dữ liệu gửi lên khi thêm hình ảnh sản phẩm qua URL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequestDTO {

    private Long id;

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    private Long variantId;

    @NotBlank(message = "URL hình ảnh không được để trống")
    @Size(max = 500, message = "URL hình ảnh tối đa 500 ký tự")
    private String url;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isThumbnail = false;
}

