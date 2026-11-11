package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO nhận dữ liệu gửi lên khi thêm hình ảnh sản phẩm qua tệp tải lên hoặc qua URL.
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

    /**
     * Tệp ảnh tải lên từ máy cá nhân (chấp nhận jpg, jpeg, png, webp).
     */
    private MultipartFile imageFile;

    /**
     * Đường dẫn URL hình ảnh (nếu thêm qua URL thay vì tải tệp).
     */
    @Size(max = 500, message = "URL hình ảnh tối đa 500 ký tự")
    private String url;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isThumbnail = false;
}


