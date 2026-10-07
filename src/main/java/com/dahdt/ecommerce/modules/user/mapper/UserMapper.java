package com.dahdt.ecommerce.modules.user.mapper;

import com.dahdt.ecommerce.modules.user.dto.UserProfileResponseDTO;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.status", target = "status")
    @Mapping(source = "profile.id", target = "id")
    UserProfileResponseDTO toProfileDto(User user, UserProfile profile);
}
