package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller phục vụ trang Quản lý Danh mục trong phân hệ Admin (/admin/categories).
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryPageController {

    private final CategoryService categoryService;

    /**
     * Hiển thị danh sách danh mục dạng cây và form thêm mới / chỉnh sửa.
     */
    @GetMapping
    public String listCategories(@RequestParam(value = "editId", required = false) Long editId, Model model) {
        populateModelData(model);

        if (editId != null) {
            CategoryResponseDTO category = categoryService.getCategoryById(editId);
            CategoryRequestDTO form = CategoryRequestDTO.builder()
                    .id(category.getId())
                    .parentId(category.getParentId())
                    .name(category.getName())
                    .sortOrder(category.getSortOrder())
                    .isActive(category.getIsActive())
                    .build();
            model.addAttribute("categoryForm", form);
            model.addAttribute("isEdit", true);
        } else if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryRequestDTO());
            model.addAttribute("isEdit", false);
        }

        return "admin/catalog/category-list";
    }

    /**
     * Lưu thông tin danh mục (thêm mới hoặc cập nhật).
     */
    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("categoryForm") CategoryRequestDTO categoryForm,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModelData(model);
            model.addAttribute("isEdit", categoryForm.getId() != null);
            return "admin/catalog/category-list";
        }

        try {
            if (categoryForm.getId() == null) {
                categoryService.createCategory(categoryForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục mới thành công!");
            } else {
                categoryService.updateCategory(categoryForm.getId(), categoryForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            }
        } catch (BadRequestException e) {
            populateModelData(model);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", categoryForm.getId() != null);
            return "admin/catalog/category-list";
        }

        return "redirect:/admin/categories";
    }

    /**
     * Bật/tắt trạng thái hiển thị của danh mục.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleCategoryStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái danh mục!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Ẩn danh mục (thực chất là xóa mềm).
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn danh mục thành công!");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private void populateModelData(Model model) {
        List<CategoryResponseDTO> categoryTree = categoryService.getCategoryTree();
        List<CategoryResponseDTO> allCategories = categoryService.getAllCategories();
        List<CategoryResponseDTO> activeCategories = categoryService.getActiveCategories();

        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("activeCategories", activeCategories);
    }
}

