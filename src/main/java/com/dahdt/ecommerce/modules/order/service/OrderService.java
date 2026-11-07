package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.modules.order.dto.OrderPreviewResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.PlaceOrderCommand;

/**
 * Interface nghiệp vụ lõi đặt hàng trực tuyến (OrderService).
 */
public interface OrderService {

    /**
     * Xem trước thông tin đơn hàng trước khi đặt (preview):
     * Tính toán tạm tính, giảm giá voucher, phí vận chuyển và tổng tiền dự kiến.
     * TUYỆT ĐỐI KHÔNG ghi bất kỳ dữ liệu nào vào cơ sở dữ liệu.
     *
     * @param userId ID tài khoản khách hàng đang đăng nhập
     * @param command Thông tin xem trước (voucher, địa chỉ, phương thức thanh toán)
     * @return OrderPreviewResponseDTO chứa các mức tiền tính toán
     */
    OrderPreviewResponseDTO previewOrder(Long userId, PlaceOrderCommand command);

    /**
     * Tiến hành đặt hàng trực tuyến (checkout):
     * Thực thi trong MỘT giao dịch @Transactional duy nhất theo 10 bước nghiêm ngặt:
     * 1. Lấy giỏ ACTIVE của khách, kiểm tra giỏ không rỗng, không có sản phẩm lỗi tồn kho.
     * 2. Lấy lại đơn giá trực tiếp từ DB, kiểm tra sản phẩm/biến thể đang ACTIVE kinh doanh.
     * 3. Xác định kho nguồn cung ứng online (kho tổng) và kiểm tra khả dụng tồn kho.
     * 4. Tạo thực thể Order qua OrderFactory và thêm sản phẩm snapshot qua Order.addItem.
     * 5. Áp voucher giảm giá (nếu có) và tính phí vận chuyển theo chính sách.
     * 6. Lưu đơn hàng và ghi nhận lịch sử trạng thái ban đầu (null -> PENDING).
     * 7. Tăng used_count của voucher bằng UPDATE có điều kiện nguyên tử và ghi voucher_usages.
     * 8. Phát sự kiện OrderPlacedEvent để thực hiện trừ kho và ghi sổ cái kho đồng bộ.
     * 9. Khởi tạo giao dịch thanh toán Payment qua PaymentStrategy tương ứng.
     * 10. Chuyển trạng thái giỏ hàng sang CONVERTED.
     *
     * @param userId ID tài khoản khách hàng đang đăng nhập
     * @param command Lệnh đặt hàng chứa thông tin nhận hàng, voucher và thanh toán
     * @return OrderResponseDTO chứa thông tin đơn hàng vừa tạo thành công
     */
    OrderResponseDTO placeOrder(Long userId, PlaceOrderCommand command);
}

