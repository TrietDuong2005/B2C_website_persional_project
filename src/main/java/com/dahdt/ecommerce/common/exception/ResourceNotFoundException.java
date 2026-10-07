package com.dahdt.ecommerce.common.exception;

/**
 * Exception khi không tìm thấy tài nguyên (user, sản phẩm, đơn hàng...).
 * Trả về HTTP 404 Not Found qua GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
