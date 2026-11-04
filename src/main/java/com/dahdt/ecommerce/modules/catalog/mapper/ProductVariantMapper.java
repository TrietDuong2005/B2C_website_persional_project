package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa ProductVariant Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "sellable", expression = "java(variant.isSellable())")
    @Mapping(target = "imageUrl", ignore = true)
    ProductVariantResponseDTO toDto(ProductVariant variant);

    List<ProductVariantResponseDTO> toDtoList(List<ProductVariant> variants);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "images", ignore = true)
    ProductVariant toEntity(ProductVariantRequestDTO dto);
}
