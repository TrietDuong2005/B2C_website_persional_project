package com.dahdt.ecommerce.common.exception;

import com.dahdt.ecommerce.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Bộ xử lý lỗi tập trung chỉ dành riêng cho các REST Controller (trả về JSON ApiResponse).
 * Sử dụng annotations = RestController.class để tránh chặn các PageController giao diện web (Thymeleaf).
 * Đồng thời ném lại AccessDeniedException và AuthenticationException cho Spring Security xử lý
 * (chuyển hướng sang trang /error/403 hoặc trả về 401/403 theo cấu hình bảo mật).
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi không tìm thấy tài nguyên (ResourceNotFoundException) cho REST API.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("REST API ResourceNotFound: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Xử lý lỗi dữ liệu không hợp lệ hoặc vi phạm nghiệp vụ (BadRequestException) cho REST API.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        log.warn("REST API BadRequest: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Xử lý lỗi xung đột khóa lạc quan (Optimistic Locking) khi nhiều giao dịch cập nhật tồn kho cùng lúc.
     */
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(Exception ex) {
        log.warn("REST API OptimisticLockingFailure: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Một số sản phẩm vừa thay đổi tồn kho, vui lòng kiểm tra lại giỏ hàng", HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Xử lý lỗi validation dữ liệu đầu vào (@Valid) cho REST API.
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String message = "Dữ liệu yêu cầu không hợp lệ";
        if (ex.getBindingResult() != null && !ex.getBindingResult().getAllErrors().isEmpty()) {
            message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        log.warn("REST API Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Bắt tất cả các lỗi chưa được xử lý riêng (fallback handler) cho REST API.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) throws Exception {
        // Ném lại các ngoại lệ liên quan đến bảo mật và phân quyền để Spring Security xử lý
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }

        // Ghi log lỗi đầy đủ để developer debug
        log.error("Lỗi không xác định trong REST API: ", ex);

        // Trả về response lỗi 500 cho client REST
        ApiResponse<Object> response = ApiResponse.error(
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
