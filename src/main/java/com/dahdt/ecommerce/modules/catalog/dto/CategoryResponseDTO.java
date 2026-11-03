package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private Integer sortOrder;
    private List<CategoryResponseDTO> children; // Đệ quy cho cây danh mục
}
