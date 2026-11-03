package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.Data;

@Data
public class BrandResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private String logoUrl;
}
