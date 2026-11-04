package com.dahdt.ecommerce.modules.store.mapper;

import com.dahdt.ecommerce.modules.store.dto.StoreRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Store Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(target = "fullAddress", expression = "java(store.getFullAddress())")
    @Mapping(target = "employeeCount", ignore = true)
    StoreResponseDTO toDto(Store store);

    List<StoreResponseDTO> toDtoList(List<Store> stores);

    @Mapping(target = "employees", ignore = true)
    Store toEntity(StoreRequestDTO dto);
}

