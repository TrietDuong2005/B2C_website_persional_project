package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về thông tin danh mục cho view Thymeleaf và API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {

    private Long id;
    private Long parentId;
    private String parentName;
    private String name;
    private String slug;
    private Integer sortOrder;
    private Boolean isActive;
    private long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<CategoryResponseDTO> children = new ArrayList<>();
}

