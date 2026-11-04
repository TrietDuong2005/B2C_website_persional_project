package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Danh mục sản phẩm.
 */
public interface CategoryService {

    /**
     * Lấy toàn bộ danh sách danh mục phẳng.
     */
    List<CategoryResponseDTO> getAllCategories();

    /**
     * Lấy cấu trúc cây danh mục cha - con (Root Categories kèm children).
     */
    List<CategoryResponseDTO> getCategoryTree();

    /**
     * Lấy danh sách danh mục đang kích hoạt (phục vụ chọn cha hoặc gán sản phẩm).
     */
    List<CategoryResponseDTO> getActiveCategories();

    /**
     * Lấy chi tiết một danh mục theo ID.
     */
    CategoryResponseDTO getCategoryById(Long id);

    /**
     * Tạo mới danh mục với slug tự sinh duy nhất.
     */
    CategoryResponseDTO createCategory(CategoryRequestDTO dto);

    /**
     * Cập nhật thông tin danh mục.
     */
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto);

    /**
     * Bật/tắt trạng thái hiển thị của danh mục.
     */
    void toggleCategoryStatus(Long id);

    /**
     * "Xoá" danh mục (thực chất là ẩn isActive = false).
     * Bị chặn nếu còn danh mục con hoặc còn sản phẩm.
     */
    void deleteCategory(Long id);
}

