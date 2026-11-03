package com.dahdt.ecommerce.modules.catalog.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Catalog")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thương hiệu")
    public ApiResponse<List<BrandResponseDTO>> getAllBrands() {
        List<BrandResponseDTO> list = brandService.getAllBrands();
        return ApiResponse.success(list, "Lấy danh sách thương hiệu thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm thương hiệu mới (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<BrandResponseDTO> createBrand(@Valid @RequestBody BrandRequestDTO request) {
        BrandResponseDTO response = brandService.createBrand(request);
        return ApiResponse.success(response, "Thêm thương hiệu thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa thương hiệu (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<BrandResponseDTO> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequestDTO request) {
        BrandResponseDTO response = brandService.updateBrand(id, request);
        return ApiResponse.success(response, "Cập nhật thương hiệu thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa thương hiệu (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.success(null, "Xóa thương hiệu thành công");
    }
}
