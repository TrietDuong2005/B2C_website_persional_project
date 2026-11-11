package com.dahdt.ecommerce.modules.catalog.mapper;

import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa ProductImage Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.sku", target = "variantSku")
    @Mapping(target = "variantName", expression = "java(image.getVariant() != null ? (image.getVariant().getColor() + \" / \" + image.getVariant().getSize()) : null)")
    ProductImageResponseDTO toDto(ProductImage image);

    List<ProductImageResponseDTO> toDtoList(List<ProductImage> images);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "variant", ignore = true)
    ProductImage toEntity(ProductImageRequestDTO dto);
}
