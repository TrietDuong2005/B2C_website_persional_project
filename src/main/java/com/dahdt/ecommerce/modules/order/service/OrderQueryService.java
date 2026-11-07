package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface truy vấn đơn hàng dành cho khách hàng (OrderQueryService).
 */
public interface OrderQueryService {

    /**
     * Lấy danh sách đơn hàng đã mua của khách hàng theo phân trang, sắp xếp mới nhất lên trước.
     *
     * @param customerId ID tài khoản khách hàng
     * @param pageable Tham số phân trang
     * @return Danh sách phân trang các đơn hàng
     */
    Page<OrderResponseDTO> getCustomerOrders(Long customerId, Pageable pageable);

    /**
     * Xem chi tiết một đơn hàng cụ thể theo mã đơn hàng.
     * Kiểm tra quyền sở hữu: nếu đơn không thuộc về khách hàng đang đăng nhập,
     * ném ResourceNotFoundException (tuyệt đối không để lộ việc đơn hàng đó có tồn tại hay không).
     *
     * @param customerId ID tài khoản khách hàng
     * @param orderCode Mã đơn hàng (ví dụ: DH202611071234)
     * @return OrderDetailResponseDTO chi tiết sản phẩm, thanh toán và lịch sử trạng thái
     */
    OrderDetailResponseDTO getCustomerOrderDetail(Long customerId, String orderCode);
}

