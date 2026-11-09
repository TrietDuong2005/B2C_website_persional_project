package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.modules.pos.dto.PosSaleCommand;
import com.dahdt.ecommerce.modules.pos.dto.PosSalePreviewResponseDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleResponseDTO;

/**
 * Service xử lý nghiệp vụ bán lẻ và thanh toán trực tiếp tại quầy POS.
 */
public interface PosSaleService {

    /**
     * Xem trước thông tin hóa đơn bán hàng tại quầy (preview):
     * Tính toán tạm tính, giảm giá voucher, tổng thanh toán và tiền thối.
     * TUYỆT ĐỐI KHÔNG ghi bất kỳ dữ liệu nào vào cơ sở dữ liệu.
     *
     * @param userId ID tài khoản thu ngân đang đăng nhập
     * @param command Lệnh bán hàng chứa danh sách sản phẩm, voucher, phương thức thanh toán, tiền khách đưa
     * @return PosSalePreviewResponseDTO chứa các mức tiền tính toán
     */
    PosSalePreviewResponseDTO previewSale(Long userId, PosSaleCommand command);

    /**
     * Tiến hành thanh toán và hoàn tất đơn hàng bán lẻ tại quầy POS (checkout):
     * Thực thi trong MỘT giao dịch @Transactional duy nhất theo 9 bước:
     * 1. Xác định cửa hàng và nhân viên từ userId (CASHIER hoặc STORE_MANAGER, hồ sơ ACTIVE).
     * 2. Kiểm tra danh sách không rỗng, mỗi dòng số lượng từ 1 đến 20.
     * 3. Lấy lại đơn giá từ DB, kiểm tra biến thể và sản phẩm đang ACTIVE kinh doanh.
     * 4. Kiểm tra khả dụng tồn kho tại cửa hàng của thu ngân.
     * 5. Tạo đơn bằng OrderFactory.createPosOrder và thêm snapshot bằng Order.addItem.
     * 6. Áp voucher nếu có bằng DiscountStrategy, cập nhật used_count nguyên tử và ghi voucher_usages (user_id = null).
     * 7. Lưu đơn hàng và ghi order_status_history (null -> COMPLETED).
     * 8. Phát sự kiện OrderPlacedEvent để trừ tồn kho chi nhánh đồng bộ qua Observer.
     * 9. Tạo và lưu bản ghi Payment qua PaymentStrategy tương ứng.
     *
     * @param userId ID tài khoản thu ngân đang đăng nhập
     * @param command Lệnh bán hàng
     * @return PosSaleResponseDTO kết quả đơn hàng hoàn tất
     */
    PosSaleResponseDTO checkout(Long userId, PosSaleCommand command);
}

