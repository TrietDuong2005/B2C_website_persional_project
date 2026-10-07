package com.dahdt.ecommerce.common.exception;

/**
 * Exception khi dữ liệu đầu vào không hợp lệ hoặc vi phạm quy tắc nghiệp vụ.
 * Trả về HTTP 400 Bad Request qua GlobalExceptionHandler.
 * Ví dụ: email đã tồn tại, mật khẩu quá ngắn, đặt hàng vượt tồn kho...
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
