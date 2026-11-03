package com.dahdt.ecommerce.modules.store.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller phục vụ trang giao diện bán hàng tại quầy (POS) cho Thu ngân / Quản lý.
 */
@Controller
public class PosPageController {

    /**
     * Hiển thị trang giao diện khung POS bán hàng tại quầy.
     */
    @GetMapping("/pos")
    public String posHome(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getUser() != null) {
            String fullName = (userDetails.getUser().getProfile() != null && userDetails.getUser().getProfile().getFullName() != null)
                    ? userDetails.getUser().getProfile().getFullName()
                    : userDetails.getUsername();
            model.addAttribute("cashierName", fullName);
            model.addAttribute("cashierEmail", userDetails.getUsername());
        }
        return "pos/index";
    }
}

