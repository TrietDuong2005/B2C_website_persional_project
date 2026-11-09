package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.modules.pos.dto.PosOrderSummaryDTO;
import com.dahdt.ecommerce.modules.pos.dto.PosReceiptResponseDTO;
import org.springframework.data.domain.Page;

/**
 * Service trích xuất và cung cấp dữ liệu hóa đơn bán lẻ POS phục vụ in ấn.
 */
public interface PosReceiptService {

    /**
     * Lấy dữ liệu hóa đơn bán lẻ đầy đủ để in ấn theo mã đơn hàng.
     * Quy tắc phân quyền theo Store Scope (mục 9 và quy tắc 14 của Project Brief):
     * - Quản trị viên (ADMIN): Được phép xem hóa đơn của mọi chi nhánh trong toàn chuỗi.
     * - Thu ngân (CASHIER) và Quản lý (STORE_MANAGER): Chỉ được phép xem hóa đơn thuộc chi nhánh mình đang công tác.
     * - Trường hợp cố tình xem hóa đơn của chi nhánh khác: ném AccessDeniedException để Spring Security kích hoạt mã 403.
     * - Trường hợp mã đơn không tồn tại: ném ResourceNotFoundException (404).
     *
     * @param userId ID người dùng đang thao tác
     * @param orderCode Mã đơn hàng hiển thị (ví dụ: HD202611081234)
     * @return PosReceiptResponseDTO chứa đầy đủ thông tin hóa đơn phục vụ in ấn
     */
    PosReceiptResponseDTO getReceipt(Long userId, String orderCode);

    /**
     * Lấy danh sách các đơn hàng POS gần đây của cửa hàng mà người dùng đang đăng nhập thuộc về.
     * Sắp xếp mới nhất trước, phân trang (mặc định 15 dòng).
     *
     * @param userId ID người dùng đang thao tác
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số bản ghi trên mỗi trang
     * @return Phân trang PosOrderSummaryDTO
     */
    Page<PosOrderSummaryDTO> getRecentOrders(Long userId, int page, int size);
}

