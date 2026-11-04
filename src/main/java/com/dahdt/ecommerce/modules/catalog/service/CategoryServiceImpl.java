package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.common.util.SlugUtil;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.mapper.CategoryMapper;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý Danh mục sản phẩm.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::enrichDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
        return rootCategories.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return enrichDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        Category category = categoryMapper.toEntity(dto);

        // Gán danh mục cha nếu có
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha ID: " + dto.getParentId()));
            category.setParent(parent);
        }

        // Tự sinh slug duy nhất từ tên
        String uniqueSlug = generateUniqueSlug(dto.getName(), null);
        category.setSlug(uniqueSlug);

        Category saved = categoryRepository.save(category);
        return enrichDto(saved);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        // Quy tắc nghiệp vụ: Không để danh mục làm cha của chính nó
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new BadRequestException("Danh mục không thể làm cha của chính nó");
        }

        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Cập nhật danh mục cha
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha ID: " + dto.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        // Tự sinh lại slug duy nhất nếu tên thay đổi
        String uniqueSlug = generateUniqueSlug(dto.getName(), id);
        category.setSlug(uniqueSlug);

        Category updated = categoryRepository.save(category);
        return enrichDto(updated);
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        category.setIsActive(!Boolean.TRUE.equals(category.getIsActive()));
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        // Quy tắc 1: Không xoá danh mục còn danh mục con
        if (categoryRepository.existsByParentId(id)) {
            throw new BadRequestException("Không thể ẩn danh mục này vì vẫn còn danh mục con trực thuộc");
        }

        // Quy tắc 2: Không xoá danh mục còn sản phẩm
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BadRequestException("Không thể ẩn danh mục này vì vẫn còn " + productCount + " sản phẩm đang trực thuộc");
        }

        // Quy tắc 3: Xóa = ẩn (is_active = false), không xóa cứng
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    // =========================================================================
    // HÀM BỔ TRỢ (HELPER METHODS)
    // =========================================================================

    private CategoryResponseDTO enrichDto(Category category) {
        CategoryResponseDTO dto = categoryMapper.toDto(category);
        dto.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));
        return dto;
    }

    private CategoryResponseDTO toTreeDto(Category category) {
        CategoryResponseDTO dto = enrichDto(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponseDTO> childDtos = new ArrayList<>();
            for (Category child : category.getChildren()) {
                childDtos.add(toTreeDto(child));
            }
            dto.setChildren(childDtos);
        }
        return dto;
    }

    private String generateUniqueSlug(String name, Long existingId) {
        String baseSlug = SlugUtil.toSlug(name);
        if (baseSlug.isBlank()) {
            baseSlug = "danh-muc";
        }
        String candidate = baseSlug;
        int count = 1;
        while (existingId == null
                ? categoryRepository.existsBySlug(candidate)
                : categoryRepository.existsBySlugAndIdNot(candidate, existingId)) {
            candidate = baseSlug + "-" + count++;
        }
        return candidate;
    }
}

