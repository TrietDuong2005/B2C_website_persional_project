package com.dahdt.ecommerce.modules.auth.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * Controller phục vụ các trang web liên quan tới xác thực tài khoản (Thymeleaf).
 */
@Controller
public class AuthPageController {

    /**
     * Hiển thị trang đăng nhập form riêng.
     * Nếu người dùng đã đăng nhập trước đó thì tự động chuyển hướng theo vai trò.
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Authentication authentication,
                            Model model) {

        // Kiểm tra nếu đã xác thực thì không hiển thị form login nữa mà chuyển hướng
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_STORE_MANAGER")) {
                return "redirect:/admin";
            } else if (roles.contains("ROLE_CASHIER")) {
                return "redirect:/pos";
            } else {
                return "redirect:/";
            }
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
        }

        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống thành công.");
        }

        return "login";
    }
}

