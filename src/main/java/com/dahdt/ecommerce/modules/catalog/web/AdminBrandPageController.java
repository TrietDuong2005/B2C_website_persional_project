package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ trang Quản lý Thương hiệu trong phân hệ Admin (/admin/brands).
 */
@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class AdminBrandPageController {

    private final BrandService brandService;

    /**
     * Hiển thị danh sách thương hiệu có phân trang, tìm kiếm và form thêm/sửa.
     */
    @GetMapping
    public String listBrands(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "editId", required = false) Long editId,
                             Model model) {
        Page<BrandResponseDTO> brandPage = brandService.getBrands(keyword, PageRequest.of(page, size));
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("keyword", keyword);

        if (editId != null) {
            BrandResponseDTO brand = brandService.getBrandById(editId);
            BrandRequestDTO form = BrandRequestDTO.builder()
                    .id(brand.getId())
                    .name(brand.getName())
                    .isActive(brand.getIsActive())
                    .build();
            model.addAttribute("brandForm", form);
            model.addAttribute("isEdit", true);
        } else if (!model.containsAttribute("brandForm")) {
            model.addAttribute("brandForm", new BrandRequestDTO());
            model.addAttribute("isEdit", false);
        }

        return "admin/catalog/brand-list";
    }

    /**
     * Lưu thông tin thương hiệu (thêm mới hoặc cập nhật).
     */
    @PostMapping("/save")
    public String saveBrand(@Valid @ModelAttribute("brandForm") BrandRequestDTO brandForm,
                            BindingResult bindingResult,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Page<BrandResponseDTO> brandPage = brandService.getBrands(keyword, PageRequest.of(page, size));
            model.addAttribute("brandPage", brandPage);
            model.addAttribute("keyword", keyword);
            model.addAttribute("isEdit", brandForm.getId() != null);
            return "admin/catalog/brand-list";
        }

        try {
            if (brandForm.getId() == null) {
                brandService.createBrand(brandForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu mới thành công!");
            } else {
                brandService.updateBrand(brandForm.getId(), brandForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công!");
            }
        } catch (BadRequestException e) {
            Page<BrandResponseDTO> brandPage = brandService.getBrands(keyword, PageRequest.of(page, size));
            model.addAttribute("brandPage", brandPage);
            model.addAttribute("keyword", keyword);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", brandForm.getId() != null);
            return "admin/catalog/brand-list";
        }

        return "redirect:/admin/brands";
    }

    /**
     * Bật/tắt trạng thái hoạt động của thương hiệu.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.toggleBrandStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi trạng thái thương hiệu!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/brands";
    }

    /**
     * Ẩn thương hiệu (xóa mềm).
     */
    @PostMapping("/{id}/delete")
    public String deleteBrand(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/brands";
    }
}

