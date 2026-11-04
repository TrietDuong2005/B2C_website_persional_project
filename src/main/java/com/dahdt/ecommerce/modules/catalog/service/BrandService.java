package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Thương hiệu sản phẩm.
 */
public interface BrandService {

    /**
     * Lấy danh sách thương hiệu có phân trang và tìm kiếm theo từ khóa.
     */
    Page<BrandResponseDTO> getBrands(String keyword, Pageable pageable);

    /**
     * Lấy danh sách thương hiệu đang hoạt động (dùng cho combobox sản phẩm).
     */
    List<BrandResponseDTO> getAllActiveBrands();

    /**
     * Lấy chi tiết thương hiệu theo ID.
     */
    BrandResponseDTO getBrandById(Long id);

    /**
     * Tạo mới thương hiệu với slug tự sinh duy nhất.
     */
    BrandResponseDTO createBrand(BrandRequestDTO dto);

    /**
     * Cập nhật thông tin thương hiệu.
     */
    BrandResponseDTO updateBrand(Long id, BrandRequestDTO dto);

    /**
     * Bật/tắt trạng thái hoạt động của thương hiệu.
     */
    void toggleBrandStatus(Long id);

    /**
     * "Xoá" thương hiệu (ẩn is_active = false, không xoá cứng).
     */
    void deleteBrand(Long id);
}

