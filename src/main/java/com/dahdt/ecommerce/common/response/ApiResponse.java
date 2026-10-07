package com.dahdt.ecommerce.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class response chuẩn dùng chung cho MỌI API trong toàn hệ thống.
 *
 * Mọi API đều trả về dạng:
 * {
 *   "code": 200,
 *   "message": "Thành công",
 *   "data": { ... }
 * }
 *
 * Giúp frontend luôn biết cấu trúc response là gì, dễ xử lý hơn
 * so với việc mỗi API trả về 1 kiểu khác nhau.
 *
 * @param <T> kiểu dữ liệu của trường "data" (có thể là bất kỳ object nào)
 */
@Data                   // Lombok: tự động tạo getter, setter, toString, equals, hashCode
@Builder                // Lombok: cho phép tạo object theo pattern Builder (dễ đọc hơn constructor dài)
@NoArgsConstructor      // Lombok: tạo constructor không tham số (cần cho Jackson deserialization)
@AllArgsConstructor     // Lombok: tạo constructor đầy đủ tham số
public class ApiResponse<T> {

    /** Mã trạng thái HTTP (200 = thành công, 400 = lỗi client, 500 = lỗi server...) */
    private int code;

    /** Thông báo mô tả kết quả (tiếng Việt, dễ hiểu cho cả developer và frontend) */
    private String message;

    /** Dữ liệu trả về — có thể là 1 object, 1 danh sách, hoặc null nếu không có data */
    private T data;

    // ===== CÁC HÀM TIỆN ÍCH (static factory method) =====
    // Giúp tạo response nhanh mà không cần viết Builder dài dòng mỗi lần

    /**
     * Tạo response thành công có kèm data.
     * Ví dụ: ApiResponse.success("Lấy danh sách thành công", listProducts)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo response thành công không có data (dùng cho các API xóa, cập nhật...).
     * Ví dụ: ApiResponse.success("Xóa sản phẩm thành công")
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Tạo response lỗi với mã lỗi và thông báo tùy chỉnh.
     * Ví dụ: ApiResponse.error(404, "Không tìm thấy sản phẩm")
     */
    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
