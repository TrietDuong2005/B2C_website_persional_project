package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopFilterDataDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductDetailDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductFilterDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductItemDTO;
import com.dahdt.ecommerce.modules.catalog.service.ShopCatalogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller phục vụ xem danh sách, tìm kiếm, lọc và xem chi tiết sản phẩm phía khách hàng (Shop).
 * URL: /shop/products, /shop/products/{slug}
 */
@Controller
@RequestMapping("/shop/products")
@RequiredArgsConstructor
public class ShopProductPageController {

    private final ShopCatalogService shopCatalogService;

    /**
     * Hiển thị lưới sản phẩm, tìm kiếm, lọc và phân trang (12 sản phẩm / trang).
     */
    @GetMapping
    public String listProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) List<String> brand,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size,
            @RequestParam(name = "inStockOnly", required = false) Boolean inStockOnly,
            @RequestParam(name = "sort", defaultValue = "latest") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        ShopProductFilterDTO filter = ShopProductFilterDTO.builder()
                .keyword(keyword)
                .category(category)
                .brands(brand != null ? brand : List.of())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .color(color)
                .size(size)
                .inStockOnly(inStockOnly)
                .sort(sort)
                .build();

        int pageIndex = Math.max(0, page);
        int pageSize = 12; // 12 sản phẩm mỗi trang theo yêu cầu

        Page<ShopProductItemDTO> productPage = shopCatalogService.listProducts(filter, PageRequest.of(pageIndex, pageSize));
        ShopFilterDataDTO filterData = shopCatalogService.getFilterData();

        model.addAttribute("productPage", productPage);
        model.addAttribute("filter", filter);
        model.addAttribute("filterData", filterData);
        model.addAttribute("selectedBrands", filter.getBrands());

        return "shop/product-list";
    }

    /**
     * Hiển thị chi tiết một sản phẩm: ảnh, tên, thương hiệu, mô tả, chọn màu và size, giá và tồn đổi theo biến thể.
     */
    @GetMapping("/{slug}")
    public String productDetail(@PathVariable("slug") String slug, Model model) {
        ShopProductDetailDTO product = shopCatalogService.getProductDetail(slug);
        model.addAttribute("product", product);
        return "shop/product-detail";
    }

    /**
     * Xử lý trường hợp không tìm thấy sản phẩm hoặc sản phẩm bị HIDDEN: hiển thị trang 404 thân thiện.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }
}

