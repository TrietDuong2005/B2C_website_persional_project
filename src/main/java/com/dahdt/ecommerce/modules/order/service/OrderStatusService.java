package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Interface dịch vụ quản lý trạng thái và vòng đời đơn hàng (OrderStatusService).
 * Áp dụng State Pattern cho việc chuyển đổi trạng thái và Observer Pattern cho việc hoàn kho khi hủy đơn.
 */
public interface OrderStatusService {

    /**
     * Chuyển trạng thái đơn hàng (xác nhận, đóng gói, giao hàng, hoàn tất hoặc hủy đơn).
     * Kiểm tra chuyển đổi hợp lệ qua State Pattern, cập nhật trạng thái bằng UPDATE có điều kiện nguyên tử,
     * ghi nhật ký order_status_history và thực thi các hiệu ứng phụ (hoàn kho, hoàn tiền, hoàn voucher).
     *
     * @param orderId ID đơn hàng cần chuyển trạng thái
     * @param targetStatus Trạng thái đích muốn chuyển sang
     * @param actorUserId ID người thực hiện (Admin, Store Manager hoặc Customer)
     * @param note Ghi chú lý do chuyển hoặc ghi chú vận hành
     */
    void changeStatus(Long orderId, OrderStatus targetStatus, Long actorUserId, String note);

    /**
     * Lấy chi tiết đơn hàng dành cho Admin/Store Manager:
     * Snapshot sản phẩm, thanh toán và dòng thời gian lịch sử trạng thái.
     *
     * @param orderId ID đơn hàng
     * @return OrderDetailResponseDTO chi tiết đơn hàng
     */
    OrderDetailResponseDTO getOrderDetailForAdmin(Long orderId);

    /**
     * Tìm kiếm và phân trang danh sách đơn hàng cho Admin/Store Manager:
     * Hỗ trợ lọc theo chi nhánh, trạng thái, kênh bán, từ khóa, khoảng ngày.
     * Tối ưu tránh triệt để N+1 query.
     *
     * @param storeId ID chi nhánh (null nếu là Admin xem toàn chuỗi)
     * @param status Trạng thái đơn (null nếu xem tất cả)
     * @param channel Kênh bán ONLINE / POS (null nếu xem tất cả)
     * @param keyword Từ khóa tìm kiếm mã đơn, tên/sđt người nhận
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param pageable Tham số phân trang (15 dòng/trang, sắp xếp mới nhất lên trước)
     * @return Danh sách phân trang OrderResponseDTO
     */
    Page<OrderResponseDTO> getOrdersForAdmin(Long storeId, OrderStatus status, OrderChannel channel,
                                            String keyword, LocalDate startDate, LocalDate endDate,
                                            Pageable pageable);
}

