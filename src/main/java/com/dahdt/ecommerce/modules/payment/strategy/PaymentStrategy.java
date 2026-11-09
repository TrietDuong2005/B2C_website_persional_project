package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;

import java.math.BigDecimal;

/**
 * ============================================================================
 * DESIGN PATTERN: STRATEGY (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Strategy Interface định nghĩa hợp đồng xử lý thanh toán đa phương thức.
 * Lý do sử dụng:
 * 1. Đa hình phương thức thanh toán: Hệ thống hỗ trợ nhiều kênh thanh toán khác nhau
 *    (COD, Ví điện tử mock, Tiền mặt tại quầy, Quẹt thẻ POS, VietQR). Mỗi phương thức
 *    có quy trình khởi tạo trạng thái (PENDING hay PAID ngay), sinh mã giao dịch và
 *    thời điểm hoàn tất khác nhau.
 * 2. Tuân thủ Open/Closed Principle (OCP): Khi bổ sung cổng thanh toán mới (như VNPay thật,
 *    MoMo thật, hoặc các phương thức POS ở Ngày 8-9), lập trình viên chỉ cần thêm
 *    một class triển khai mới mà không cần can thiệp vào OrderService lõi.
 * ============================================================================
 */
public interface PaymentStrategy {

    /**
     * Phương thức thanh toán mà Strategy này phụ trách (COD, EWALLET_MOCK, CASH, CARD, QR).
     */
    PaymentMethod getSupportedMethod();

    /**
     * Xác định phương thức thanh toán này có được phép sử dụng cho đơn hàng đặt trực tuyến (Online) hay không.
     * Ví dụ: COD và EWALLET_MOCK = true; CASH, CARD, QR tại quầy = false.
     */
    boolean isSupportedOnline();

    /**
     * Xác định phương thức thanh toán này có được phép sử dụng cho đơn hàng bán tại quầy (POS) hay không.
     * Ví dụ: CASH, CARD, QR = true; COD, EWALLET_MOCK = false.
     */
    boolean isSupportedPos();

    /**
     * Khởi tạo và xử lý bản ghi giao dịch thanh toán cho đơn hàng.
     *
     * @param order Đơn hàng cần thanh toán
     * @return Thực thể Payment đã được khởi tạo
     */
    Payment processPayment(Order order);

    /**
     * Khởi tạo và xử lý thanh toán có kèm số tiền khách đưa (dành cho thanh toán tiền mặt CASH tại quầy POS).
     * Mặc định chuyển tiếp gọi processPayment(order).
     *
     * @param order Đơn hàng cần thanh toán
     * @param cashReceived Số tiền mặt khách đưa
     * @return Thực thể Payment đã được khởi tạo
     */
    default Payment processPayment(Order order, BigDecimal cashReceived) {
        return processPayment(order);
    }
}

