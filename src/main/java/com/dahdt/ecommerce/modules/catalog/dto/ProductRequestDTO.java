package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDTO {
    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;

    @NotNull(message = "Brand ID không được để trống")
    private Long brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String description;
    private String status = "draft";
    private String productType = "variant";
}
