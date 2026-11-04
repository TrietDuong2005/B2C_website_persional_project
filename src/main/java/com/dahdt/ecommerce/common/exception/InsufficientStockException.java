package com.dahdt.ecommerce.common.exception;

/**
 * Exception ném ra khi số lượng hàng trong kho không đủ để thực hiện xuất/bán/điều chỉnh giảm.
 * Kế thừa BadRequestException để tự động trả về mã lỗi 400 trong REST và hiển thị thông báo lỗi rõ ràng.
 */
public class InsufficientStockException extends BadRequestException {

    public InsufficientStockException(String message) {
        super(message);
    }
}

