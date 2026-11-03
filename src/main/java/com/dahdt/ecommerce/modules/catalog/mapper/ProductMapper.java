package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.ProductRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.entity.AttributeValue;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BrandMapper.class})
public interface ProductMapper {

    @Mapping(target = "variants", ignore = true) // Sẽ map thủ công hoặc qua service
    @Mapping(target = "images", ignore = true)
    ProductResponseDTO toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Product toEntity(ProductRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(ProductRequestDTO dto, @MappingTarget Product product);

    @Mapping(target = "attributeValues", ignore = true) // Map thủ công trong service
    ProductResponseDTO.ProductVariantResponseDTO toVariantDto(ProductVariant variant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ProductVariant toVariantEntity(ProductVariantRequestDTO dto);
    
    @Mapping(target = "attributeName", source = "attribute.name")
    ProductResponseDTO.AttributeValueDTO toAttributeValueDto(AttributeValue attributeValue);
    
    ProductResponseDTO.ProductImageResponseDTO toImageDto(ProductImage image);
}
