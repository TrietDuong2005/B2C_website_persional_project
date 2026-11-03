package com.dahdt.ecommerce.modules.catalog.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Các API quản lý danh mục, thương hiệu, sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "Lấy toàn bộ cây danh mục")
    public ApiResponse<List<CategoryResponseDTO>> getCategoryTree() {
        List<CategoryResponseDTO> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree, "Lấy cây danh mục thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm danh mục mới (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.createCategory(request);
        return ApiResponse.success(response, "Thêm danh mục thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa danh mục (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<CategoryResponseDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.updateCategory(id, request);
        return ApiResponse.success(response, "Cập nhật danh mục thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null, "Xóa danh mục thành công");
    }
}
