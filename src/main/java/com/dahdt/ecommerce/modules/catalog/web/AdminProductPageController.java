package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.*;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.service.BrandService;
import com.dahdt.ecommerce.modules.catalog.service.CategoryService;
import com.dahdt.ecommerce.modules.catalog.service.ProductService;
import com.dahdt.ecommerce.modules.catalog.service.ProductVariantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ trang Quản lý Sản phẩm và Biến thể trong phân hệ Admin (/admin/products).
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductPageController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    /**
     * Danh sách sản phẩm có phân trang, tìm kiếm và lọc đa điều kiện.
     */
    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(value = "brandId", required = false) Long brandId,
                               @RequestParam(value = "status", required = false) ProductStatus status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductResponseDTO> productPage = productService.getProducts(keyword, categoryId, brandId, status, pageRequest);

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getAllActiveBrands());

        return "admin/catalog/product-list";
    }

    /**
     * Form tạo mới sản phẩm cha.
     */
    @GetMapping("/new")
    public String newProductForm(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductRequestDTO());
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getAllActiveBrands());
        return "admin/catalog/product-form";
    }

    /**
     * Form chỉnh sửa sản phẩm cha.
     */
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);
        ProductRequestDTO form = ProductRequestDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .name(product.getName())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .status(product.getStatus())
                .build();

        model.addAttribute("productForm", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getAllActiveBrands());
        return "admin/catalog/product-form";
    }

    /**
     * Lưu thông tin sản phẩm cha (tạo mới hoặc cập nhật).
     */
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("productForm") ProductRequestDTO productForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", productForm.getId() != null);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getAllActiveBrands());
            return "admin/catalog/product-form";
        }

        try {
            if (productForm.getId() == null) {
                ProductResponseDTO created = productService.createProduct(productForm);
                redirectAttributes.addFlashAttribute("successMessage", "Tạo sản phẩm mới thành công! Bạn có thể thêm biến thể ngay dưới đây.");
                return "redirect:/admin/products/" + created.getId();
            } else {
                productService.updateProduct(productForm.getId(), productForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
                return "redirect:/admin/products/" + productForm.getId();
            }
        } catch (BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", productForm.getId() != null);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getAllActiveBrands());
            return "admin/catalog/product-form";
        }
    }

    /**
     * Bật/tắt trạng thái hiển thị của sản phẩm (ACTIVE <-> HIDDEN).
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleProductStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * Ẩn sản phẩm (xóa mềm).
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * Trang xem chi tiết sản phẩm: quản lý biến thể và hình ảnh.
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id,
                                @RequestParam(value = "editVariantId", required = false) Long editVariantId,
                                Model model) {
        ProductDetailResponseDTO product = productService.getProductDetail(id);
        model.addAttribute("product", product);

        // Chuẩn bị form biến thể
        if (editVariantId != null) {
            ProductVariantResponseDTO variant = productVariantService.getVariantById(editVariantId);
            ProductVariantRequestDTO variantForm = ProductVariantRequestDTO.builder()
                    .id(variant.getId())
                    .productId(id)
                    .sku(variant.getSku())
                    .barcode(variant.getBarcode())
                    .color(variant.getColor())
                    .size(variant.getSize())
                    .price(variant.getPrice())
                    .costPrice(variant.getCostPrice())
                    .weightGram(variant.getWeightGram())
                    .status(variant.getStatus())
                    .imageUrl(variant.getImageUrl())
                    .build();
            model.addAttribute("variantForm", variantForm);
            model.addAttribute("isVariantEdit", true);
        } else if (!model.containsAttribute("variantForm")) {
            ProductVariantRequestDTO variantForm = new ProductVariantRequestDTO();
            variantForm.setProductId(id);
            model.addAttribute("variantForm", variantForm);
            model.addAttribute("isVariantEdit", false);
        }

        // Chuẩn bị form ảnh
        if (!model.containsAttribute("imageForm")) {
            ProductImageRequestDTO imageForm = new ProductImageRequestDTO();
            imageForm.setProductId(id);
            model.addAttribute("imageForm", imageForm);
        }

        return "admin/catalog/product-detail";
    }

    /**
     * Thêm mới hoặc cập nhật biến thể sản phẩm.
     */
    @PostMapping("/{id}/variants/save")
    public String saveVariant(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("variantForm") ProductVariantRequestDTO variantForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        variantForm.setProductId(id);

        if (bindingResult.hasErrors()) {
            ProductDetailResponseDTO product = productService.getProductDetail(id);
            model.addAttribute("product", product);
            model.addAttribute("isVariantEdit", variantForm.getId() != null);
            if (!model.containsAttribute("imageForm")) {
                ProductImageRequestDTO imageForm = new ProductImageRequestDTO();
                imageForm.setProductId(id);
                model.addAttribute("imageForm", imageForm);
            }
            return "admin/catalog/product-detail";
        }

        try {
            if (variantForm.getId() == null) {
                productVariantService.createVariant(variantForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm biến thể SKU " + variantForm.getSku() + " thành công!");
            } else {
                productVariantService.updateVariant(variantForm.getId(), variantForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật biến thể thành công!");
            }
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("variantForm", variantForm);
        }

        return "redirect:/admin/products/" + id;
    }

    /**
     * Bật/tắt trạng thái kinh doanh của biến thể.
     */
    @PostMapping("/{id}/variants/{variantId}/toggle-status")
    public String toggleVariantStatus(@PathVariable("id") Long id,
                                      @PathVariable("variantId") Long variantId,
                                      RedirectAttributes redirectAttributes) {
        try {
            productVariantService.toggleVariantStatus(variantId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái biến thể!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products/" + id;
    }

    /**
     * Ẩn biến thể (chuyển sang INACTIVE).
     */
    @PostMapping("/{id}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable("id") Long id,
                                @PathVariable("variantId") Long variantId,
                                RedirectAttributes redirectAttributes) {
        try {
            productVariantService.deleteVariant(variantId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngưng kinh doanh biến thể!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products/" + id;
    }

    /**
     * Thêm hình ảnh mới cho sản phẩm / biến thể qua tệp tải lên hoặc qua URL.
     */
    @PostMapping("/{id}/images/save")
    public String saveImage(@PathVariable("id") Long id,
                            @ModelAttribute("imageForm") ProductImageRequestDTO imageForm,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        imageForm.setProductId(id);

        boolean hasUrl = imageForm.getUrl() != null && !imageForm.getUrl().trim().isEmpty();
        boolean hasFile = imageForm.getImageFile() != null && !imageForm.getImageFile().isEmpty();

        if (!hasUrl && !hasFile) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn tệp ảnh tải lên hoặc nhập URL hình ảnh!");
            return "redirect:/admin/products/" + id;
        }

        if (hasUrl && hasFile) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ được chọn một trong hai phương thức: tải tệp lên hoặc nhập URL hình ảnh, không dùng cả hai!");
            return "redirect:/admin/products/" + id;
        }

        try {
            productVariantService.addProductImage(imageForm);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm ảnh thành công!");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi lưu hình ảnh");
        }

        return "redirect:/admin/products/" + id;
    }

    /**
     * Xử lý lỗi vượt kích thước tệp tải lên (MaxUploadSizeExceededException) cho trang admin.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Dung lượng tệp tải lên vượt quá giới hạn tối đa cho phép!");
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/admin/products/")) {
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("products".equals(parts[i]) && i + 1 < parts.length && parts[i + 1].matches("\\d+")) {
                    return "redirect:/admin/products/" + parts[i + 1];
                }
            }
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/admin/products";
    }

    /**
     * Xóa hình ảnh sản phẩm.
     */
    @PostMapping("/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable("id") Long id,
                              @PathVariable("imageId") Long imageId,
                              RedirectAttributes redirectAttributes) {
        try {
            productVariantService.deleteProductImage(imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa ảnh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products/" + id;
    }
}

