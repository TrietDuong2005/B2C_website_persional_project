package com.dahdt.ecommerce.common.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.service.CartService;
import com.dahdt.ecommerce.modules.cart.web.ShopCartPageController;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import com.dahdt.ecommerce.modules.catalog.web.ShopHomePageController;
import com.dahdt.ecommerce.modules.catalog.web.ShopProductPageController;
import com.dahdt.ecommerce.modules.order.web.ShopOrderPageController;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * ControllerAdvice chỉ áp dụng cho các Controller thuộc khu Shop:
 * 1. Số lượng món hàng trong giỏ (cartItemCount) cho biểu tượng giỏ hàng trên navbar Shop.
 * 2. Cây danh mục đang hoạt động (shopCategories) cho menu danh mục trên navbar Shop.
 *
 * Tách riêng để các trang quản trị (/admin/**), bán tại quầy (/pos/**) và xác thực (/login)
 * không phải thực thi các truy vấn danh mục Shop và giỏ hàng không cần thiết.
 */
@ControllerAdvice(assignableTypes = {
        ShopHomePageController.class,
        ShopProductPageController.class,
        ShopCartPageController.class,
        ShopOrderPageController.class
})
@RequiredArgsConstructor
public class ShopModelAttributes {

    private final CartService cartService;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy tổng số lượng món hàng trong giỏ để hiển thị huy hiệu (badge) trên navbar Shop.
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
     * Cung cấp danh sách 2 danh mục gốc và các danh mục con cho navbar Shop.
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

