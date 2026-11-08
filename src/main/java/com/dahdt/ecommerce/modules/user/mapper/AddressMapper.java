package com.dahdt.ecommerce.modules.user.mapper;

import com.dahdt.ecommerce.modules.user.dto.AddressRequestDTO;
import com.dahdt.ecommerce.modules.user.dto.AddressResponseDTO;
import com.dahdt.ecommerce.modules.user.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponseDTO toDto(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", source = "default")
    Address toEntity(AddressRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AddressRequestDTO dto, @MappingTarget Address address);
}
