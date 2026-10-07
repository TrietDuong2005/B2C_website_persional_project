package com.dahdt.ecommerce.modules.user.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.user.dto.AddressRequestDTO;
import com.dahdt.ecommerce.modules.user.dto.AddressResponseDTO;
import com.dahdt.ecommerce.modules.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Các API quản lý sổ địa chỉ giao hàng")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ của tôi", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<List<AddressResponseDTO>> getMyAddresses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<AddressResponseDTO> list = addressService.getMyAddresses(currentUser.getId());
        return ApiResponse.success(list, "Thành công");
    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<AddressResponseDTO> createAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                         @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO address = addressService.createAddress(currentUser.getId(), request);
        return ApiResponse.success(address, "Thêm địa chỉ thành công");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật địa chỉ", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<AddressResponseDTO> updateAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO address = addressService.updateAddress(currentUser.getId(), id, request);
        return ApiResponse.success(address, "Cập nhật địa chỉ thành công");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> deleteAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable Long id) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ApiResponse.success(null, "Xóa địa chỉ thành công");
    }
}
