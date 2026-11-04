package com.dahdt.ecommerce.modules.inventory.mapper;

import com.dahdt.ecommerce.modules.inventory.dto.InventoryTransactionResponseDTO;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa InventoryTransaction Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.sku", target = "sku")
    @Mapping(source = "variant.product.name", target = "productName")
    @Mapping(source = "variant.color", target = "color")
    @Mapping(source = "variant.size", target = "size")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.code", target = "storeCode")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(target = "createdByName", expression = "java(resolveCreatorName(transaction))")
    InventoryTransactionResponseDTO toDto(InventoryTransaction transaction);

    List<InventoryTransactionResponseDTO> toDtoList(List<InventoryTransaction> transactions);

    default String resolveCreatorName(InventoryTransaction transaction) {
        if (transaction == null || transaction.getCreatedBy() == null) {
            return "Hệ thống";
        }
        if (transaction.getCreatedBy().getProfile() != null && transaction.getCreatedBy().getProfile().getFullName() != null) {
            return transaction.getCreatedBy().getProfile().getFullName();
        }
        return transaction.getCreatedBy().getEmail();
    }
}

