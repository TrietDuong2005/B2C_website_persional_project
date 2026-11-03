package com.dahdt.ecommerce.modules.catalog.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller phục vụ trang chủ và xem danh mục / sản phẩm phía khách hàng (Shop).
 */
@Controller
public class CatalogPageController {

    /**
     * Hiển thị trang chủ website mua sắm trực tuyến (Shop).
     */
    @GetMapping("/")
    public String home(Model model) {
        return "shop/index";
    }
}

