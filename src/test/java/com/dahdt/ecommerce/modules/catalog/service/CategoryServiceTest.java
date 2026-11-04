package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.mapper.CategoryMapper;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category parentCategory;

    @BeforeEach
    void setUp() {
        parentCategory = Category.builder()
                .id(1L)
                .name("Thời trang nam")
                .slug("thoi-trang-nam")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Chặn xoá/ẩn danh mục khi vẫn còn danh mục con trực thuộc")
    void testDeleteCategoryHavingChildrenThrowsBadRequestException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByParentId(1L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoryService.deleteCategory(1L)
        );

        assertTrue(ex.getMessage().contains("vẫn còn danh mục con trực thuộc"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn xoá/ẩn danh mục khi vẫn còn sản phẩm trực thuộc")
    void testDeleteCategoryHavingProductsThrowsBadRequestException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);
        when(categoryRepository.countProductsByCategoryId(1L)).thenReturn(5L);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoryService.deleteCategory(1L)
        );

        assertTrue(ex.getMessage().contains("vẫn còn 5 sản phẩm đang trực thuộc"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn cập nhật danh mục làm cha của chính nó")
    void testUpdateCategorySelfParentThrowsBadRequestException() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .id(1L)
                .parentId(1L) // Gán cha là chính nó
                .name("Thời trang nam")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoryService.updateCategory(1L, request)
        );

        assertTrue(ex.getMessage().contains("Danh mục không thể làm cha của chính nó"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xoá mềm danh mục thành công: chuyển isActive sang false (không xoá cứng)")
    void testDeleteCategorySoftDeletesSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);
        when(categoryRepository.countProductsByCategoryId(1L)).thenReturn(0L);

        categoryService.deleteCategory(1L);

        assertFalse(parentCategory.getIsActive(), "Danh mục phải được chuyển sang isActive = false");
        verify(categoryRepository, times(1)).save(parentCategory);
        verify(categoryRepository, never()).delete(any());
    }
}

