package com.dahdt.ecommerce.modules.catalog.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin thương hiệu cho view Thymeleaf và API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponseDTO {

    private Long id;
    private String name;
    private String slug;
    private Boolean isActive;
    private long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

