package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * Helper hỗ trợ hiển thị nhãn tiếng Việt và huy hiệu (badge) CSS Bootstrap theo theme
 * cho trạng thái đơn hàng, trạng thái thanh toán và phương thức thanh toán.
 * Có thể gọi trực tiếp trong Thymeleaf: {@code @orderDisplay.getStatusLabel(order.status)}.
 */
@Component("orderDisplay")
public class OrderDisplayHelper {

    /**
     * Lấy nhãn tiếng Việt cho trạng thái đơn hàng.
     */
    public String getStatusLabel(OrderStatus status) {
        if (status == null) {
            return "Không xác định";
        }
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case PACKING -> "Đang đóng gói";
            case SHIPPING -> "Đang giao hàng";
            case COMPLETED -> "Hoàn tất";
            case CANCELLED -> "Đã hủy";
        };
    }

    /**
     * Lấy class màu badge Bootstrap theo theme cho trạng thái đơn hàng.
     */
    public String getStatusBadgeClass(OrderStatus status) {
        if (status == null) {
            return "badge text-bg-secondary";
        }
        return switch (status) {
            case PENDING -> "badge text-bg-warning";
            case CONFIRMED -> "badge text-bg-info text-dark";
            case PACKING -> "badge text-bg-primary";
            case SHIPPING -> "badge text-bg-primary";
            case COMPLETED -> "badge text-bg-success";
            case CANCELLED -> "badge text-bg-secondary";
        };
    }

    /**
     * Lấy nhãn tiếng Việt cho trạng thái thanh toán.
     */
    public String getPaymentStatusLabel(PaymentStatus status) {
        if (status == null) {
            return "Chờ thanh toán";
        }
        return switch (status) {
            case PENDING -> "Chờ thanh toán";
            case PAID -> "Đã thanh toán";
            case FAILED -> "Thanh toán thất bại";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    /**
     * Lấy class màu badge Bootstrap theo theme cho trạng thái thanh toán.
     */
    public String getPaymentStatusBadgeClass(PaymentStatus status) {
        if (status == null) {
            return "badge text-bg-warning";
        }
        return switch (status) {
            case PENDING -> "badge text-bg-warning";
            case PAID -> "badge text-bg-success";
            case FAILED -> "badge text-bg-danger";
            case REFUNDED -> "badge text-bg-secondary";
        };
    }

    /**
     * Lấy tên tiếng Việt dễ hiểu cho phương thức thanh toán.
     */
    public String getPaymentMethodLabel(PaymentMethod method) {
        if (method == null) {
            return "Chưa chọn";
        }
        return switch (method) {
            case COD -> "Thanh toán khi nhận hàng (COD)";
            case EWALLET_MOCK -> "Ví điện tử giả lập";
            case CASH -> "Tiền mặt tại quầy";
            case CARD -> "Thẻ ngân hàng (POS)";
            case QR -> "Quét mã QR";
        };
    }
}

