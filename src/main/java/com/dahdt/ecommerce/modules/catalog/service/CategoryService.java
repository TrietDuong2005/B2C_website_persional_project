package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.mapper.CategoryMapper;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Lấy toàn bộ cây danh mục
     */
    public List<CategoryResponseDTO> getCategoryTree() {
        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category category = categoryMapper.toEntity(request);
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha không tồn tại"));
            category.setParent(parent);
        }
        
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        categoryMapper.updateEntityFromDto(request, category);
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha không tồn tại"));
            // Chống tự tham chiếu chính mình
            if (parent.getId().equals(category.getId())) {
                throw new IllegalArgumentException("Danh mục cha không hợp lệ");
            }
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));
        categoryRepository.delete(category);
    }
}
