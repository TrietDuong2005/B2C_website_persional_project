package com.dahdt.ecommerce.modules.user.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.user.dto.UserProfileResponseDTO;
import com.dahdt.ecommerce.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Các API quản lý thông tin cá nhân")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin profile của chính mình", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<UserProfileResponseDTO> getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        UserProfileResponseDTO profile = userService.getMyProfile(currentUser.getId());
        return ApiResponse.success(profile, "Lấy thông tin cá nhân thành công");
    }
}
