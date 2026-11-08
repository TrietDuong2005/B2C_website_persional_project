package com.dahdt.ecommerce.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ControllerAdvice cung cấp các thuộc tính chung cho toàn bộ giao diện web (Thymeleaf):
 * 1. Tên thương hiệu (brandName) cấu hình từ application.yml vào Model của mọi template.
 * 2. Đường dẫn hiện tại (currentUri) phục vụ gắn redirect URL khi đăng nhập.
 * 3. Cờ hiển thị gợi ý tài khoản dev (devHints) cấu hình từ app.dev-hints.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    @Value("${app.brand.name:Triết Store}")
    private String brandName;

    @Value("${app.dev-hints:false}")
    private boolean devHints;

    @ModelAttribute("brandName")
    public String getBrandName() {
        return brandName;
    }

    @ModelAttribute("devHints")
    public boolean isDevHints() {
        return devHints;
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
}
