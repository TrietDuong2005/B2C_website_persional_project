package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;
import com.dahdt.ecommerce.modules.cart.service.CartService;
import com.dahdt.ecommerce.modules.order.dto.*;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.service.OrderQueryService;
import com.dahdt.ecommerce.modules.order.service.OrderService;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.user.dto.AddressResponseDTO;
import com.dahdt.ecommerce.modules.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * PageController phục vụ toàn bộ quy trình đặt hàng và quản lý đơn hàng
 * cho khách hàng cá nhân (CUSTOMER) theo chuẩn Post-Redirect-Get (PRG).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ShopOrderPageController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final OrderStatusService orderStatusService;
    private final CartService cartService;
    private final AddressService addressService;

    // =========================================================================
    // PHẦN 1: TRANG THANH TOÁN (/checkout)
    // =========================================================================

    /**
     * Hiển thị trang thanh toán đơn hàng (GET /checkout).
     * Kiểm tra giỏ hàng rỗng hoặc có dòng vấn đề tồn kho để chuyển hướng về /cart.
     * Tự điền địa chỉ mặc định của khách từ AddressService (nếu có).
     */
    @GetMapping("/checkout")
    public String viewCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getId();
        CartResponseDTO cart = cartService.getCart(userId);

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống. Vui lòng chọn sản phẩm trước khi thanh toán.");
            return "redirect:/cart";
        }

        if (cart.isHasProblem()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng có sản phẩm bị hết hàng hoặc vượt tồn kho. Vui lòng cập nhật lại giỏ hàng.");
            return "redirect:/cart";
        }

        CheckoutForm form;
        if (model.containsAttribute("form")) {
            form = (CheckoutForm) model.getAttribute("form");
        } else {
            form = createPrefilledForm(userDetails);
        }

        OrderPreviewResponseDTO preview = calculateSafePreview(userId, form);

        model.addAttribute("cart", cart);
        model.addAttribute("form", form);
        model.addAttribute("preview", preview);

        return "shop/checkout";
    }

    /**
     * Tính lại đơn hàng khi khách bấm nút "Áp dụng" mã giảm giá (POST /checkout/preview).
     * Giữ nguyên toàn bộ dữ liệu form đã nhập mà không làm mất thông tin.
     */
    @PostMapping("/checkout/preview")
    public String previewOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("form") CheckoutForm form,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getId();
        CartResponseDTO cart = cartService.getCart(userId);

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        if (cart.isHasProblem()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng có sản phẩm bị hết hàng hoặc vượt tồn kho.");
            return "redirect:/cart";
        }

        PlaceOrderCommand command = form.toCommand();
        OrderPreviewResponseDTO preview;

        try {
            preview = orderService.previewOrder(userId, command);
            if (preview.getVoucherCode() != null) {
                model.addAttribute("voucherSuccess", "Áp dụng mã giảm giá [" + preview.getVoucherCode() + "] thành công! Tiết kiệm: " + MoneyUtil.format(preview.getDiscountTotal()));
            }
        } catch (BadRequestException ex) {
            model.addAttribute("voucherError", ex.getMessage());
            command.setVoucherCode(null);
            preview = orderService.previewOrder(userId, command);
        } catch (Exception ex) {
            model.addAttribute("voucherError", "Không thể áp dụng mã giảm giá: " + ex.getMessage());
            command.setVoucherCode(null);
            preview = orderService.previewOrder(userId, command);
        }

        model.addAttribute("cart", cart);
        model.addAttribute("form", form);
        model.addAttribute("preview", preview);

        return "shop/checkout";
    }

    /**
     * Tiến hành đặt hàng khi khách bấm nút "Đặt hàng" (POST /checkout/place).
     * Kiểm tra Bean Validation, gọi OrderService.placeOrder và chuyển hướng phù hợp.
     */
    @PostMapping("/checkout/place")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("form") CheckoutForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getId();
        CartResponseDTO cart = cartService.getCart(userId);

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        if (cart.isHasProblem()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng có sản phẩm bị hết hàng hoặc vượt tồn kho. Vui lòng kiểm tra lại.");
            return "redirect:/cart";
        }

        // Lỗi dữ liệu form: hiển thị lỗi cạnh từng ô nhập liệu
        if (bindingResult.hasErrors()) {
            OrderPreviewResponseDTO preview = calculateSafePreview(userId, form);
            model.addAttribute("cart", cart);
            model.addAttribute("form", form);
            model.addAttribute("preview", preview);
            return "shop/checkout";
        }

        PlaceOrderCommand command = form.toCommand();

        try {
            OrderResponseDTO orderResponse = orderService.placeOrder(userId, command);
            return "redirect:/checkout/success/" + orderResponse.getOrderCode();
        } catch (InsufficientStockException ex) {
            log.warn("Hết hàng khi đặt đơn user [{}]: {}", userId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        } catch (BadRequestException ex) {
            log.warn("Lỗi nghiệp vụ khi đặt đơn user [{}]: {}", userId, ex.getMessage());
            String msg = ex.getMessage();
            if (msg != null && (msg.contains("tồn kho") || msg.contains("hết hàng"))) {
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/cart";
            }
            // Lỗi voucher hoặc phương thức thanh toán: giữ lại form ở trang checkout
            model.addAttribute("errorMessage", msg);
            model.addAttribute("cart", cart);
            model.addAttribute("form", form);
            model.addAttribute("preview", calculateSafePreview(userId, form));
            return "shop/checkout";
        } catch (Exception ex) {
            log.error("Lỗi không mong muốn khi đặt đơn user [{}]: ", userId, ex);
            model.addAttribute("errorMessage", "Không thể hoàn tất đơn hàng: " + ex.getMessage());
            model.addAttribute("cart", cart);
            model.addAttribute("form", form);
            model.addAttribute("preview", calculateSafePreview(userId, form));
            return "shop/checkout";
        }
    }

    // =========================================================================
    // PHẦN 2: TRANG THÀNH CÔNG VÀ ĐƠN HÀNG CỦA TÔI
    // =========================================================================

    /**
     * Trang thông báo đặt hàng thành công (GET /checkout/success/{code}).
     * Chỉ chủ đơn hàng mới xem được (người khác truy cập sẽ ném 404).
     */
    @GetMapping("/checkout/success/{code}")
    public String checkoutSuccess(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("code") String code,
            Model model) {

        Long userId = userDetails.getId();
        OrderDetailResponseDTO order = orderQueryService.getCustomerOrderDetail(userId, code);
        model.addAttribute("order", order);
        return "shop/checkout-success";
    }

    /**
     * Danh sách đơn hàng đã mua của khách hàng (GET /account/orders).
     * Phân trang 10 đơn mỗi trang, sắp xếp đơn mới nhất lên trước.
     */
    @GetMapping("/account/orders")
    public String listCustomerOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10);
        Page<OrderResponseDTO> orders = orderQueryService.getCustomerOrders(userId, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        return "shop/order-list";
    }

    /**
     * Xem chi tiết một đơn hàng của khách hàng (GET /account/orders/{code}).
     * Nếu đơn không thuộc về khách hàng đang đăng nhập thì trả về trang 404.
     */
    @GetMapping("/account/orders/{code}")
    public String viewCustomerOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("code") String code,
            Model model) {

        Long userId = userDetails.getId();
        OrderDetailResponseDTO order = orderQueryService.getCustomerOrderDetail(userId, code);
        model.addAttribute("order", order);
        return "shop/order-detail";
    }

    /**
     * Khách hàng tự hủy đơn hàng trực tuyến của chính mình khi đơn đang ở trạng thái PENDING.
     * Áp dụng Post-Redirect-Get (PRG) và tái sử dụng OrderStatusService.
     */
    @PostMapping("/account/orders/{code}/cancel")
    public String cancelCustomerOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("code") String code,
            @RequestParam(name = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getId();
        try {
            OrderDetailResponseDTO order = orderQueryService.getCustomerOrderDetail(userId, code);
            String cancelNote = (reason != null && !reason.trim().isBlank())
                    ? "Khách hàng tự hủy đơn: " + reason.trim()
                    : "Khách hàng tự hủy đơn hàng";

            orderStatusService.changeStatus(order.getId(), OrderStatus.CANCELLED, userId, cancelNote);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng #" + code + " thành công!");
        } catch (BadRequestException | AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi khi khách hàng hủy đơn hàng [{}]: ", code, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng: " + ex.getMessage());
        }

        return "redirect:/account/orders/" + code;
    }

    // =========================================================================
    // HÀM HỖ TRỢ NỘI BỘ
    // =========================================================================

    /**
     * Khởi tạo form thanh toán với dữ liệu tự điền từ địa chỉ mặc định hoặc hồ sơ người dùng.
     */
    private CheckoutForm createPrefilledForm(CustomUserDetails userDetails) {
        CheckoutForm form = CheckoutForm.builder()
                .paymentMethod(PaymentMethod.COD)
                .build();

        try {
            List<AddressResponseDTO> addresses = addressService.getMyAddresses(userDetails.getId());
            AddressResponseDTO defaultAddr = addresses.stream()
                    .filter(AddressResponseDTO::isDefault)
                    .findFirst()
                    .orElse(addresses.isEmpty() ? null : addresses.get(0));

            if (defaultAddr != null) {
                form.setShipName(defaultAddr.getRecipientName());
                form.setShipPhone(defaultAddr.getPhone());
                form.setProvince(defaultAddr.getProvince());
                form.setDistrict(defaultAddr.getDistrict());
                form.setWard(defaultAddr.getWard());
                form.setDetailAddress(defaultAddr.getDetailAddress());
                form.setShipLatitude(defaultAddr.getLatitude());
                form.setShipLongitude(defaultAddr.getLongitude());
            } else if (userDetails.getUser() != null) {
                if (userDetails.getUser().getProfile() != null && userDetails.getUser().getProfile().getFullName() != null) {
                    form.setShipName(userDetails.getUser().getProfile().getFullName());
                }
                if (userDetails.getUser().getPhone() != null) {
                    form.setShipPhone(userDetails.getUser().getPhone());
                }
            }
        } catch (Exception ex) {
            log.warn("Không thể tải địa chỉ mặc định của user [{}]: {}", userDetails.getId(), ex.getMessage());
        }

        return form;
    }

    /**
     * Tính toán an toàn thông tin xem trước đơn hàng mà không làm ngắt trang nếu voucher lỗi.
     */
    private OrderPreviewResponseDTO calculateSafePreview(Long userId, CheckoutForm form) {
        PlaceOrderCommand cmd = form != null ? form.toCommand() : null;
        try {
            return orderService.previewOrder(userId, cmd);
        } catch (Exception ex) {
            if (cmd != null && cmd.getVoucherCode() != null) {
                cmd.setVoucherCode(null);
                try {
                    return orderService.previewOrder(userId, cmd);
                } catch (Exception ignored) {
                }
            }
            return OrderPreviewResponseDTO.builder()
                    .subtotal(java.math.BigDecimal.ZERO)
                    .discountTotal(java.math.BigDecimal.ZERO)
                    .shippingFee(java.math.BigDecimal.ZERO)
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .build();
        }
    }
}

