package com.dahdt.ecommerce.common.exception;

import com.dahdt.ecommerce.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Bộ xử lý lỗi tập trung cho toàn bộ ứng dụng.
 *
 * Thay vì mỗi Controller phải tự try-catch, class này sẽ "bắt" tất cả
 * các exception xảy ra trong hệ thống và trả về response thống nhất
 * (theo định dạng ApiResponse).
 *
 * Sau này khi code các module nghiệp vụ, sẽ bổ sung thêm các @ExceptionHandler
 * cho từng loại lỗi cụ thể (vd: ResourceNotFoundException, BadRequestException...).
 */
@Slf4j                  // Lombok: tạo sẵn biến "log" để ghi log
@RestControllerAdvice   // Spring: đánh dấu class này xử lý exception cho toàn bộ REST controller
public class GlobalExceptionHandler {

    /**
     * Bắt tất cả các lỗi chưa được xử lý riêng (fallback handler).
     * Đây là "lưới an toàn" cuối cùng — nếu có lỗi gì bất ngờ xảy ra,
     * API vẫn trả về response đúng định dạng thay vì trả về HTML lỗi mặc định.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        // Ghi log lỗi đầy đủ (stack trace) để developer debug
        log.error("Lỗi không xác định: ", ex);

        // Trả về response lỗi 500 cho client
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
