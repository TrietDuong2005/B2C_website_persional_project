package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantResponseDTO;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Biến thể sản phẩm (ProductVariant) và Hình ảnh (ProductImage).
 */
public interface ProductVariantService {

    /**
     * Lấy danh sách biến thể theo ID sản phẩm cha.
     */
    List<ProductVariantResponseDTO> getVariantsByProductId(Long productId);

    /**
     * Lấy chi tiết biến thể theo ID.
     */
    ProductVariantResponseDTO getVariantById(Long id);

    /**
     * Tạo mới biến thể (kiểm tra SKU unique, barcode unique, price > 0, cost_price >= 0).
     */
    ProductVariantResponseDTO createVariant(ProductVariantRequestDTO dto);

    /**
     * Cập nhật thông tin biến thể.
     */
    ProductVariantResponseDTO updateVariant(Long id, ProductVariantRequestDTO dto);

    /**
     * Bật/tắt trạng thái kinh doanh biến thể (ACTIVE <-> INACTIVE).
     */
    void toggleVariantStatus(Long id);

    /**
     * "Xoá" biến thể (chuyển sang INACTIVE, không xoá cứng).
     */
    void deleteVariant(Long id);

    /**
     * Thêm hình ảnh cho sản phẩm hoặc gắn riêng cho biến thể qua URL.
     */
    ProductImageResponseDTO addProductImage(ProductImageRequestDTO dto);

    /**
     * Xóa hình ảnh sản phẩm theo ID ảnh.
     */
    void deleteProductImage(Long imageId);
}

