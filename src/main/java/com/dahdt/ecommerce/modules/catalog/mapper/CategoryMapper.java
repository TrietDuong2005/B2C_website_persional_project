package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Category toEntity(CategoryRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateEntityFromDto(CategoryRequestDTO dto, @MappingTarget Category category);
}
