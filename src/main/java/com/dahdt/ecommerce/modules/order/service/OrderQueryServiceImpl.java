package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ truy vấn đơn hàng của khách hàng (OrderQueryService).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderResponseDTO> getCustomerOrders(Long customerId, Pageable pageable) {
        if (customerId == null) {
            return Page.empty(pageable);
        }
        Page<Order> orderPage = orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId, pageable);
        return orderPage.map(orderMapper::toResponseDTO);
    }

    @Override
    public OrderDetailResponseDTO getCustomerOrderDetail(Long customerId, String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new ResourceNotFoundException("Mã đơn hàng không hợp lệ.");
        }

        // Tìm đơn hàng kèm thông tin chi tiết các mặt hàng
        Order order = orderRepository.findByOrderCodeWithDetails(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode));

        // Kiểm tra quyền sở hữu đơn hàng: nếu không khớp với khách đang đăng nhập,
        // ném ResourceNotFoundException thay vì 403 để tuyệt đối không để lộ sự tồn tại của đơn hàng.
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode);
        }

        // Nạp danh sách thanh toán và lịch sử trạng thái nếu chưa được fetch
        if (order.getPayments() == null || order.getPayments().isEmpty()) {
            List<Payment> payments = paymentRepository.findByOrderId(order.getId());
            order.setPayments(payments);
        }

        if (order.getStatusHistories() == null || order.getStatusHistories().isEmpty()) {
            List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId());
            order.setStatusHistories(histories);
        }

        return orderMapper.toDetailDTO(order);
    }
}

