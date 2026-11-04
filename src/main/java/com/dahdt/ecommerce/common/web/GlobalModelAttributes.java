package com.dahdt.ecommerce.common.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ControllerAdvice cung cấp các thuộc tính chung cho toàn bộ giao diện web (Thymeleaf).
 * Đưa tên thương hiệu (brandName) cấu hình từ application.yml vào Model của mọi template.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.brand.name:Triết Store}")
    private String brandName;

    @ModelAttribute("brandName")
    public String getBrandName() {
        return brandName;
    }
}

