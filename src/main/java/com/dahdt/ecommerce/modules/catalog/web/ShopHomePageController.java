package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductItemDTO;
import com.dahdt.ecommerce.modules.catalog.service.ShopCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller phục vụ trang chủ Shop (URL "/") của khách hàng.
 * Đổi tên từ CatalogPageController theo yêu cầu Ngày 5 của Project Brief.
 */
@Controller
@RequiredArgsConstructor
public class ShopHomePageController {

    private final ShopCatalogService shopCatalogService;

    /**
     * Hiển thị trang chủ Shop: Banner, danh mục tiêu biểu và 8 sản phẩm mới nhất.
     */
    @GetMapping("/")
    public String home(Model model) {
        List<ShopProductItemDTO> latestProducts = shopCatalogService.latestProducts(8);
        model.addAttribute("latestProducts", latestProducts);
        return "shop/index";
    }
}

