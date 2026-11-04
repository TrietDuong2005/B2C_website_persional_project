package com.dahdt.ecommerce.modules.catalog.dto;

import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO nhận dữ liệu gửi lên khi tạo mới hoặc cập nhật thông tin sản phẩm cha.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    private Long id;

    @NotNull(message = "Vui lòng chọn danh mục cho sản phẩm")
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    private String description;

    @Size(max = 500, message = "URL ảnh thu nhỏ tối đa 500 ký tự")
    private String thumbnailUrl;

    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
}

