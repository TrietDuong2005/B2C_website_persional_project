package com.dahdt.ecommerce.modules.inventory.mapper;

import com.dahdt.ecommerce.modules.inventory.dto.InventoryResponseDTO;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Inventory Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.sku", target = "sku")
    @Mapping(source = "variant.barcode", target = "barcode")
    @Mapping(source = "variant.product.name", target = "productName")
    @Mapping(source = "variant.product.category.name", target = "categoryName")
    @Mapping(source = "variant.color", target = "color")
    @Mapping(source = "variant.size", target = "size")
    @Mapping(source = "variant.price", target = "price")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.code", target = "storeCode")
    @Mapping(source = "store.name", target = "storeName")
    InventoryResponseDTO toDto(Inventory inventory);

    List<InventoryResponseDTO> toDtoList(List<Inventory> inventories);
}

