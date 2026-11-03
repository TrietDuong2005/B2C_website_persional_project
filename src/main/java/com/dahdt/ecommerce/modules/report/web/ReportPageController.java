package com.dahdt.ecommerce.modules.report.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller phục vụ trang quản trị Admin và báo cáo / thống kê.
 */
@Controller
public class ReportPageController {

    /**
     * Hiển thị trang Dashboard quản trị cho ADMIN và STORE_MANAGER.
     * Cung cấp thông tin tên người dùng và vai trò cho giao diện khung.
     */
    @GetMapping("/admin")
    public String adminDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getUser() != null) {
            String fullName = (userDetails.getUser().getProfile() != null && userDetails.getUser().getProfile().getFullName() != null)
                    ? userDetails.getUser().getProfile().getFullName()
                    : userDetails.getUsername();
            model.addAttribute("currentUserFullName", fullName);
            model.addAttribute("currentUserEmail", userDetails.getUsername());
            model.addAttribute("currentUserRoles", userDetails.getUser().getRoles());
        }
        return "admin/dashboard";
    }
}

