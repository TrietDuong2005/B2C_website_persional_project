package com.dahdt.ecommerce.modules.catalog.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Catalog")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Tìm kiếm và lọc danh sách sản phẩm (có phân trang)")
    public ApiResponse<Page<ProductResponseDTO>> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            Pageable pageable) {
        Page<ProductResponseDTO> page = productService.searchProducts(categoryId, brandId, pageable);
        return ApiResponse.success(page, "Lấy danh sách sản phẩm thành công");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết 1 sản phẩm")
    public ApiResponse<ProductResponseDTO> getProductDetail(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductDetail(id);
        return ApiResponse.success(product, "Lấy chi tiết sản phẩm thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo sản phẩm gốc (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO response = productService.createProduct(request);
        return ApiResponse.success(response, "Tạo sản phẩm thành công");
    }

    @PostMapping("/variants")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm biến thể (SKU) cho sản phẩm (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<ProductResponseDTO> addVariant(@Valid @RequestBody ProductVariantRequestDTO request) {
        ProductResponseDTO response = productService.addVariant(request);
        return ApiResponse.success(response, "Thêm biến thể thành công");
    }

    @PostMapping("/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm ảnh cho sản phẩm hoặc biến thể (Chỉ ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> addImage(@Valid @RequestBody ProductImageRequestDTO request) {
        productService.addImage(request);
        return ApiResponse.success(null, "Thêm ảnh thành công");
    }
}
