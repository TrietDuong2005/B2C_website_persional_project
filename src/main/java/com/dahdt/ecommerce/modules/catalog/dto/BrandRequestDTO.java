package com.dahdt.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequestDTO {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String logoUrl;
}
