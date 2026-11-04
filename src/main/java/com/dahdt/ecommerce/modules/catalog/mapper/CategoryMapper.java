package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Category Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "children", ignore = true)
    CategoryResponseDTO toDto(Category category);

    List<CategoryResponseDTO> toDtoList(List<Category> categories);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "slug", ignore = true)
    Category toEntity(CategoryRequestDTO dto);
}
