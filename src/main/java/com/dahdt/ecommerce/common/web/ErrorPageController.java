package com.dahdt.ecommerce.common.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các trang lỗi thân thiện giao diện web (Thymeleaf): 403, 404, 500.
 */
@Controller
public class ErrorPageController implements ErrorController {

    @GetMapping("/error/403")
    public String accessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }

    @GetMapping("/error/500")
    public String serverError(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error/500";
    }

    /**
     * Tự động bắt mã lỗi HTTP khi xảy ra lỗi ngoài các route định sẵn và chuyển tới trang HTML tương ứng.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return "error/403";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return "error/500";
            }
        }
        return "error/500";
    }
}

