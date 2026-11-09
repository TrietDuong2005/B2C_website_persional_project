package com.dahdt.ecommerce.modules.pos.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu hóa đơn bán lẻ POS đầy đủ phục vụ chức năng in hóa đơn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosReceiptResponseDTO {

    /** Tên chi nhánh cửa hàng */
    private String storeName;

    /** Địa chỉ đầy đủ của cửa hàng */
    private String storeAddress;

    /** Số điện thoại liên hệ cửa hàng */
    private String storePhone;

    /** Mã đơn hàng / Số hóa đơn */
    private String orderCode;

    /** Thời điểm bán hàng */
    private LocalDateTime orderTime;

    /** Họ tên thu ngân phụ trách */
    private String cashierName;

    /** Danh sách mặt hàng snapshot */
    private List<PosReceiptItemDTO> items;

    /** Tạm tính tiền hàng */
    private BigDecimal subtotal;

    /** Tiền giảm giá voucher */
    private BigDecimal discountTotal;

    /** Tổng tiền thanh toán */
    private BigDecimal totalAmount;

    /** Hình thức thanh toán */
    private PaymentMethod paymentMethod;

    /** Tiền khách đưa (nếu là tiền mặt CASH) */
    private BigDecimal cashReceived;

    /** Tiền thối trả lại khách (nếu là tiền mặt CASH) */
    private BigDecimal changeAmount;

    /** Mã giao dịch / Mã chuẩn y (nếu thanh toán CARD hoặc QR) */
    private String transactionRef;

    /** Ghi chú đơn hàng */
    private String note;
}

