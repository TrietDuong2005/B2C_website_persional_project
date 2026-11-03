package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    private Long parentId; // Có thể null nếu là danh mục gốc

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String imageUrl;
    private Integer sortOrder = 0;
}
