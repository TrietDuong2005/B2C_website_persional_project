package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Helper định dạng hiển thị đơn hàng (OrderDisplayHelper)")
class OrderDisplayHelperTest {

    private OrderDisplayHelper helper;

    @BeforeEach
    void setUp() {
        helper = new OrderDisplayHelper();
    }

    @Test
    @DisplayName("getStatusLabel: Trả về đúng nhãn tiếng Việt cho mọi OrderStatus")
    void testGetStatusLabel() {
        assertEquals("Chờ xác nhận", helper.getStatusLabel(OrderStatus.PENDING));
        assertEquals("Đã xác nhận", helper.getStatusLabel(OrderStatus.CONFIRMED));
        assertEquals("Đang đóng gói", helper.getStatusLabel(OrderStatus.PACKING));
        assertEquals("Đang giao hàng", helper.getStatusLabel(OrderStatus.SHIPPING));
        assertEquals("Hoàn tất", helper.getStatusLabel(OrderStatus.COMPLETED));
        assertEquals("Đã hủy", helper.getStatusLabel(OrderStatus.CANCELLED));
        assertEquals("Không xác định", helper.getStatusLabel(null));
    }

    @Test
    @DisplayName("getStatusBadgeClass: Trả về đúng class màu Bootstrap theo theme cho OrderStatus")
    void testGetStatusBadgeClass() {
        assertEquals("badge text-bg-warning", helper.getStatusBadgeClass(OrderStatus.PENDING));
        assertEquals("badge text-bg-info text-dark", helper.getStatusBadgeClass(OrderStatus.CONFIRMED));
        assertEquals("badge text-bg-primary", helper.getStatusBadgeClass(OrderStatus.PACKING));
        assertEquals("badge text-bg-primary", helper.getStatusBadgeClass(OrderStatus.SHIPPING));
        assertEquals("badge text-bg-success", helper.getStatusBadgeClass(OrderStatus.COMPLETED));
        assertEquals("badge text-bg-secondary", helper.getStatusBadgeClass(OrderStatus.CANCELLED));
        assertEquals("badge text-bg-secondary", helper.getStatusBadgeClass(null));
    }

    @Test
    @DisplayName("getPaymentStatusLabel: Trả về đúng nhãn tiếng Việt cho PaymentStatus")
    void testGetPaymentStatusLabel() {
        assertEquals("Chờ thanh toán", helper.getPaymentStatusLabel(PaymentStatus.PENDING));
        assertEquals("Đã thanh toán", helper.getPaymentStatusLabel(PaymentStatus.PAID));
        assertEquals("Thanh toán thất bại", helper.getPaymentStatusLabel(PaymentStatus.FAILED));
        assertEquals("Đã hoàn tiền", helper.getPaymentStatusLabel(PaymentStatus.REFUNDED));
        assertEquals("Chờ thanh toán", helper.getPaymentStatusLabel(null));
    }

    @Test
    @DisplayName("getPaymentStatusBadgeClass: Trả về đúng class màu Bootstrap cho PaymentStatus")
    void testGetPaymentStatusBadgeClass() {
        assertEquals("badge text-bg-warning", helper.getPaymentStatusBadgeClass(PaymentStatus.PENDING));
        assertEquals("badge text-bg-success", helper.getPaymentStatusBadgeClass(PaymentStatus.PAID));
        assertEquals("badge text-bg-danger", helper.getPaymentStatusBadgeClass(PaymentStatus.FAILED));
        assertEquals("badge text-bg-secondary", helper.getPaymentStatusBadgeClass(PaymentStatus.REFUNDED));
        assertEquals("badge text-bg-warning", helper.getPaymentStatusBadgeClass(null));
    }

    @Test
    @DisplayName("getPaymentMethodLabel: Trả về tên tiếng Việt rõ ràng cho phương thức thanh toán")
    void testGetPaymentMethodLabel() {
        assertEquals("Thanh toán khi nhận hàng (COD)", helper.getPaymentMethodLabel(PaymentMethod.COD));
        assertEquals("Ví điện tử giả lập", helper.getPaymentMethodLabel(PaymentMethod.EWALLET_MOCK));
        assertEquals("Tiền mặt tại quầy", helper.getPaymentMethodLabel(PaymentMethod.CASH));
        assertEquals("Thẻ ngân hàng (POS)", helper.getPaymentMethodLabel(PaymentMethod.CARD));
        assertEquals("Quét mã QR", helper.getPaymentMethodLabel(PaymentMethod.QR));
        assertEquals("Chưa chọn", helper.getPaymentMethodLabel(null));
    }
}

