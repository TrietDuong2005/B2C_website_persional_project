package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Brand Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "productCount", ignore = true)
    BrandResponseDTO toDto(Brand brand);

    List<BrandResponseDTO> toDtoList(List<Brand> brands);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "slug", ignore = true)
    Brand toEntity(BrandRequestDTO dto);
}
