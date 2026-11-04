package com.dahdt.ecommerce.modules.store.mapper;

import com.dahdt.ecommerce.modules.store.dto.EmployeeResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Employee Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.profile.fullName", target = "fullName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.code", target = "storeCode")
    @Mapping(source = "store.name", target = "storeName")
    EmployeeResponseDTO toDto(Employee employee);

    List<EmployeeResponseDTO> toDtoList(List<Employee> employees);
}

