package com.dahdt.ecommerce.modules.order.factory;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * DESIGN PATTERN: FACTORY (Khởi tạo - Creational Pattern)
 * ============================================================================
 * Vai trò: OrderFactory chịu trách nhiệm đóng gói logic khởi tạo các loại đơn hàng
 *          đa kênh (Online và POS).
 * Lý do sử dụng:
 * 1. Che giấu sự phức tạp khi khởi tạo đối tượng Order: Đơn Online và đơn POS có
 *    các thiết lập ban đầu rất khác biệt (kênh channel, trạng thái ban đầu PENDING vs
 *    COMPLETED, người tạo customer vs cashier, thông tin giao hàng có hoặc null).
 * 2. Đảm bảo tính nhất quán: Mọi đơn hàng tạo ra đều tuân thủ đúng các giá trị mặc định,
 *    có mã đơn duy nhất chuẩn hóa sinh từ OrderCodeGenerator.
 * 3. Điểm mở rộng cho Ngày 8-9: Phương thức createPosOrder sẽ được bổ sung tại đây
 *    mà không làm thay đổi luồng tạo đơn Online hiện có.
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
public class OrderFactory {

    private final OrderCodeGenerator orderCodeGenerator;

    /**
     * Khởi tạo đơn hàng trực tuyến (Shop Online):
     * - Kênh bán: ONLINE
     * - Trạng thái ban đầu: PENDING (Chờ xử lý)
     * - Chi nhánh xuất kho: Kho nguồn do FulfillmentSourceResolver xác định
     * - Khách hàng: Tài khoản người dùng đang đăng nhập
     * - Thu ngân: null (do khách tự đặt)
     * - Thông tin giao nhận: Lưu dạng ảnh chụp (snapshot) họ tên, số điện thoại, địa chỉ, tọa độ
     *
     * @param store Kho nguồn cung ứng online
     * @param customer Tài khoản khách hàng
     * @param shipName Tên người nhận hàng
     * @param shipPhone SĐT người nhận
     * @param shipAddress Địa chỉ giao hàng chi tiết
     * @param shipLatitude Tọa độ vĩ độ giao hàng (nếu có)
     * @param shipLongitude Tọa độ kinh độ giao hàng (nếu có)
     * @param note Ghi chú của khách
     * @return Đối tượng Order mới được khởi tạo ở trạng thái PENDING
     */
    public Order createOnlineOrder(Store store,
                                   User customer,
                                   String shipName,
                                   String shipPhone,
                                   String shipAddress,
                                   BigDecimal shipLatitude,
                                   BigDecimal shipLongitude,
                                   String note) {
        String code = orderCodeGenerator.generateUniqueOrderCode();

        return Order.builder()
                .orderCode(code)
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .store(store)
                .customer(customer)
                .cashier(null)
                .subtotal(BigDecimal.ZERO)
                .discountTotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .voucherCode(null)
                .shipName(shipName != null ? shipName.trim() : null)
                .shipPhone(shipPhone != null ? shipPhone.trim() : null)
                .shipAddress(shipAddress != null ? shipAddress.trim() : null)
                .shipLatitude(shipLatitude)
                .shipLongitude(shipLongitude)
                .note(note != null ? note.trim() : null)
                .placedAt(LocalDateTime.now())
                .completedAt(null)
                .build();
    }

    // =========================================================================
    // ĐIỂM MỞ RỘNG CHO NGÀY 8-9 (POS - BÁN HÀNG TẠI QUẦY):
    // public Order createPosOrder(Store store, User cashier, User customerOrNull) {
    //     // Kênh POS: channel = POS, status = COMPLETED, cashier = cashier, ship_* = null
    // }
    // =========================================================================
}

