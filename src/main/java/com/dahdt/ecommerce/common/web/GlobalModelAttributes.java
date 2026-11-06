package com.dahdt.ecommerce.common.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.service.CartService;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * ControllerAdvice cung cấp các thuộc tính chung cho toàn bộ giao diện web (Thymeleaf):
 * 1. Tên thương hiệu (brandName) cấu hình từ application.yml vào Model của mọi template.
 * 2. Số lượng món hàng trong giỏ (cartItemCount) cho biểu tượng giỏ hàng (chỉ truy vấn khi là CUSTOMER đã đăng nhập).
 * 3. Cây danh mục đang hoạt động (shopCategories) phục vụ hiển thị menu danh mục trên navbar Shop.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    @Value("${app.brand.name:Triết Store}")
    private String brandName;

    private final CartService cartService;
    private final CategoryRepository categoryRepository;

    @ModelAttribute("brandName")
    public String getBrandName() {
        return brandName;
    }

    /**
     * Cung cấp đường dẫn hiện tại (path kèm query string nếu có)
     * để gắn vào tham số redirect cho nút Đăng nhập trên navbar và trang sản phẩm.
     */
    @ModelAttribute("currentUri")
    public String getCurrentUri(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return "/";
        }
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return (query != null && !query.isBlank()) ? (uri + "?" + query) : uri;
    }

    /**
     * Lấy tổng số lượng món hàng trong giỏ để hiển thị huy hiệu (badge) trên navbar.
     * Chỉ thực hiện truy vấn khi người dùng đã đăng nhập với vai trò CUSTOMER.
     */
    @ModelAttribute("cartItemCount")
    public int getCartItemCount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean isCustomer = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));
            if (isCustomer && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return cartService.countActiveCartItems(userDetails.getId());
            }
        }
        return 0;
    }

    /**
     * Cung cấp danh sách 2 danh mục gốc và các danh mục con cho navbar Shop trên mọi trang.
     */
    @ModelAttribute("shopCategories")
    public List<Category> getShopCategories() {
        try {
            return categoryRepository.findActiveRootCategoriesWithChildren();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
