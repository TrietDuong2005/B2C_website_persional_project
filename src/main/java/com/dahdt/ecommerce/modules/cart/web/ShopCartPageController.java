package com.dahdt.ecommerce.modules.cart.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.dto.AddToCartRequestDTO;
import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;
import com.dahdt.ecommerce.modules.cart.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ trang giỏ hàng (/cart) cho khách hàng (CUSTOMER).
 * Áp dụng mô hình Post-Redirect-Get (PRG), kèm flash message tiếng Việt.
 */
@Controller
@RequiredArgsConstructor
public class ShopCartPageController {

    private final CartService cartService;

    /**
     * Xem bảng giỏ hàng: danh sách sản phẩm, số lượng, đơn giá, thành tiền, tạm tính và cảnh báo.
     */
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        CartResponseDTO cart = cartService.getCart(userId);
        model.addAttribute("cart", cart);
        return "shop/cart";
    }

    /**
     * Thêm biến thể sản phẩm vào giỏ hàng (Form POST có CSRF).
     */
    @PostMapping("/cart/add")
    public String addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute AddToCartRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return getSafeRedirectUrl(request);
        }

        try {
            cartService.addItem(userDetails.getId(), requestDto.getVariantId(), requestDto.getQuantity());
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
        }

        return getSafeRedirectUrl(request);
    }

    /**
     * Cập nhật số lượng của một dòng sản phẩm trong giỏ (Form POST có CSRF).
     */
    @PostMapping("/cart/update")
    public String updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.updateItemQuantity(userDetails.getId(), itemId, quantity);
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng thành công.");
            }
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật số lượng: " + ex.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Xóa một dòng sản phẩm khỏi giỏ hàng (Form POST có CSRF).
     */
    @PostMapping("/cart/remove")
    public String removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("itemId") Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.removeItem(userDetails.getId(), itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + ex.getMessage());
        }

        return "redirect:/cart";
    }

    private String getSafeRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank() && !referer.contains("/cart/add")) {
            return "redirect:" + referer;
        }
        return "redirect:/cart";
    }
}
