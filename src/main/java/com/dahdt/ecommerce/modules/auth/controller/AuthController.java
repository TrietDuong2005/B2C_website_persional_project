package com.dahdt.ecommerce.modules.auth.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.modules.auth.dto.LoginRequestDTO;
import com.dahdt.ecommerce.modules.auth.dto.LoginResponseDTO;
import com.dahdt.ecommerce.modules.auth.dto.RegisterRequestDTO;
import com.dahdt.ecommerce.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API xác thực: Đăng nhập, Đăng ký")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản khách hàng mới")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ApiResponse.success(null, "Đăng ký tài khoản thành công");
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập để nhận Access Token và Refresh Token")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ApiResponse.success(response, "Đăng nhập thành công");
    }
}
