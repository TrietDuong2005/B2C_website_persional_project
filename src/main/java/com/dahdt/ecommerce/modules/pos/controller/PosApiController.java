package com.dahdt.ecommerce.modules.pos.controller;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.pos.dto.*;
import com.dahdt.ecommerce.modules.pos.service.PosCatalogService;
import com.dahdt.ecommerce.modules.pos.service.PosSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller phục vụ các yêu cầu AJAX JSON từ màn hình bán hàng tại quầy POS.
 * Nằm trong SecurityFilterChain session (Chain 2), CSRF được bật và kiểm tra qua token.
 */
@RestController
@RequestMapping("/pos/api")
@RequiredArgsConstructor
@Slf4j
public class PosApiController {

    private final PosCatalogService posCatalogService;
    private final PosSaleService posSaleService;

    /**
     * Tìm kiếm biến thể sản phẩm theo từ khóa (SKU, barcode, tên sản phẩm).
     * Trả về tối đa 20 dòng kèm tồn kho thực tế tại cửa hàng của thu ngân.
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<PosProductSearchResponseDTO>>> searchProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "q", required = false) String keyword) {
        Long userId = extractUserId(userDetails);
        List<PosProductSearchResponseDTO> results = posCatalogService.search(userId, keyword);
        return ResponseEntity.ok(ApiResponse.success(results, "Tìm kiếm sản phẩm thành công"));
    }

    /**
     * Xem trước thông tin hóa đơn bán lẻ tại quầy (preview):
     * Tính toán tạm tính, giảm giá voucher, tổng tiền và tiền thừa mà KHÔNG ghi CSDL.
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<PosSalePreviewResponseDTO>> previewSale(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PosSaleCommand command,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg, HttpStatus.BAD_REQUEST.value()));
        }
        Long userId = extractUserId(userDetails);
        PosSalePreviewResponseDTO preview = posSaleService.previewSale(userId, command);
        return ResponseEntity.ok(ApiResponse.success(preview, "Tính toán hóa đơn thành công"));
    }

    /**
     * Tiến hành thanh toán và hoàn tất đơn hàng bán lẻ tại quầy POS (checkout):
     * Trả về mã đơn để chuyển sang trang hóa đơn.
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<PosSaleResponseDTO>> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PosSaleCommand command,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg, HttpStatus.BAD_REQUEST.value()));
        }
        Long userId = extractUserId(userDetails);
        PosSaleResponseDTO response = posSaleService.checkout(userId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "Thanh toán thành công"));
    }

    private Long extractUserId(CustomUserDetails userDetails) {
        return (userDetails != null && userDetails.getUser() != null)
                ? userDetails.getUser().getId()
                : null;
    }
}

