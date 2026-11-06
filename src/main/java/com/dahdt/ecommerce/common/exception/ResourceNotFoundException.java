package com.dahdt.ecommerce.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception khi không tìm thấy tài nguyên (user, sản phẩm, đơn hàng...).
 * Trả về HTTP 404 Not Found qua GlobalExceptionHandler cho REST API hoặc trang lỗi 404 cho web.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
