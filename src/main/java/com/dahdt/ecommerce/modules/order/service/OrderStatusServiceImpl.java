package com.dahdt.ecommerce.modules.order.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.*;
import com.dahdt.ecommerce.modules.order.event.OrderCancelledEvent;
import com.dahdt.ecommerce.modules.order.mapper.OrderMapper;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.dahdt.ecommerce.modules.order.state.OrderState;
import com.dahdt.ecommerce.modules.order.state.OrderStateFactory;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.promotion.repository.VoucherRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý trạng thái và vòng đời đơn hàng (OrderStatusService).
 * Áp dụng State Pattern cho kiểm soát quy tắc chuyển đổi và Observer Pattern cho xử lý hoàn kho.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final OrderStateFactory orderStateFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final StoreScopeService storeScopeService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public void changeStatus(Long orderId, OrderStatus targetStatus, Long actorUserId, String note) {
        if (orderId == null) {
            throw new BadRequestException("Mã ID đơn hàng không được để trống.");
        }
        if (targetStatus == null) {
            throw new BadRequestException("Trạng thái đích không được để trống.");
        }

        // 1. Tìm đơn hàng kèm thông tin chi tiết các mặt hàng để chuẩn bị hoàn kho nếu hủy
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // 2. Chặn chuyển đổi với đơn POS:
        // Đơn POS tạo ở COMPLETED và là trạng thái cuối, mọi chuyển đổi hoặc hủy đơn POS đều bị từ chối
        if (order.getChannel() == OrderChannel.POS) {
            throw new BadRequestException("Đơn hàng tại quầy POS đã hoàn tất và không hỗ trợ thay đổi trạng thái hoặc hủy đơn.");
        }

        // 3. Kiểm tra phạm vi quyền hạn của người thực hiện
        User actor = (actorUserId != null) ? userRepository.findById(actorUserId).orElse(null) : null;

        // Quy tắc phân quyền theo mục 14 của Brief:
        // - STORE_MANAGER chỉ xem đơn của đúng cửa hàng mình; đơn online do kho tổng phụ trách nên chỉ ADMIN xử lý,
        //   STORE_MANAGER không được phép đổi trạng thái đơn ONLINE.
        if (storeScopeService.isCurrentStoreManager()) {
            log.warn("STORE_MANAGER [{}] cố gắng thay đổi trạng thái đơn online [{}]", actorUserId, order.getOrderCode());
            throw new AccessDeniedException("Quản lý cửa hàng chỉ có quyền xem đơn hàng chi nhánh và không được phép thay đổi trạng thái đơn hàng trực tuyến.");
        }

        // - Nếu người thực hiện là CUSTOMER (khách hàng tự hủy đơn của chính mình):
        //   Chỉ được phép hủy đơn của chính mình và chỉ khi đơn đang ở trạng thái PENDING.
        boolean isAdmin = storeScopeService.isCurrentAdmin();
        if (!isAdmin) {
            if (order.getCustomer() == null || actorUserId == null || !order.getCustomer().getId().equals(actorUserId)) {
                throw new AccessDeniedException("Bạn không có quyền thao tác trên đơn hàng của người khác.");
            }
            if (targetStatus != OrderStatus.CANCELLED || order.getStatus() != OrderStatus.PENDING) {
                throw new BadRequestException("Khách hàng chỉ có thể hủy đơn hàng của chính mình khi đơn đang ở trạng thái Chờ xác nhận.");
            }
        }

        // 4. Áp dụng State Pattern: lấy đối tượng State hiện tại và kiểm tra tính hợp lệ của chuyển đổi
        OrderState currentState = orderStateFactory.getState(order.getStatus());
        currentState.validateTransition(targetStatus);

        // 5. Chống đổi hai lần cùng lúc (Race Condition / Double Cancel):
        // Do bảng orders không có cột version, sử dụng câu UPDATE có điều kiện status hiện tại
        // và kiểm tra số dòng bị ảnh hưởng. Nếu affectedRows == 0 nghĩa là đã có thao tác khác
        // thay đổi trạng thái trước đó -> ném ngoại lệ và dừng lại, ngăn chặn chạy hiệu ứng phụ lần 2.
        OrderStatus expectedStatus = order.getStatus();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime completedAt = (targetStatus == OrderStatus.COMPLETED) ? now : order.getCompletedAt();

        int affectedRows = orderRepository.updateStatusConditionally(
                orderId, expectedStatus, targetStatus, completedAt, now);

        if (affectedRows == 0) {
            throw new BadRequestException("Đơn hàng đã được thay đổi trạng thái bởi một thao tác khác, vui lòng làm mới trang.");
        }

        // Cập nhật lại trạng thái trong bộ nhớ đối tượng
        order.setStatus(targetStatus);
        if (targetStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(completedAt);
        }

        // 6. Ghi vết lịch sử trạng thái vào bảng order_status_history
        String historyNote = (note != null && !note.trim().isBlank()) ? note.trim() : null;
        if (historyNote == null) {
            if (targetStatus == OrderStatus.CANCELLED) {
                historyNote = "Hủy đơn hàng";
            } else if (targetStatus == OrderStatus.COMPLETED) {
                historyNote = "Đơn hàng đã hoàn tất giao dịch";
            } else {
                historyNote = "Chuyển trạng thái đơn sang " + targetStatus;
            }
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(expectedStatus)
                .toStatus(targetStatus)
                .changedBy(actor)
                .note(historyNote)
                .changedAt(now)
                .build();
        orderStatusHistoryRepository.save(history);

        // 7. Thực thi các hiệu ứng phụ đặc thù của trạng thái đích:
        if (targetStatus == OrderStatus.COMPLETED) {
            // Khi vào trạng thái COMPLETED:
            // Với đơn thanh toán COD, chuyển payment từ PENDING sang PAID (paid_at là thời điểm đó)
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            for (Payment p : payments) {
                if (p.getMethod() == PaymentMethod.COD && p.getStatus() == PaymentStatus.PENDING) {
                    p.setStatus(PaymentStatus.PAID);
                    p.setPaidAt(now);
                    paymentRepository.save(p);
                    log.info("Đã cập nhật trạng thái thanh toán COD sang PAID cho đơn hàng [{}]", order.getOrderCode());
                }
            }
        } else if (targetStatus == OrderStatus.CANCELLED) {
            // Khi vào trạng thái CANCELLED:
            // A. Hoàn tiền giả lập: payment PAID (ví điện tử) chuyển REFUNDED; payment PENDING (COD) chuyển FAILED
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            for (Payment p : payments) {
                if (p.getStatus() == PaymentStatus.PAID) {
                    p.setStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(p);
                    log.info("Đã chuyển trạng thái thanh toán sang REFUNDED cho đơn hàng bị hủy [{}]", order.getOrderCode());
                } else if (p.getStatus() == PaymentStatus.PENDING) {
                    p.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(p);
                    log.info("Đã chuyển trạng thái thanh toán sang FAILED cho đơn hàng bị hủy [{}]", order.getOrderCode());
                }
            }

            // B. Hoàn lượt voucher: nếu đơn có voucher thì giảm used_count bằng câu UPDATE nguyên tử
            // (chỉ giảm khi used_count > 0), giữ nguyên dòng voucher_usages để lưu vết
            if (order.getVoucherCode() != null && !order.getVoucherCode().trim().isBlank()) {
                String voucherCode = order.getVoucherCode().trim();
                voucherRepository.findByCode(voucherCode).ifPresent(v -> {
                    int updatedRows = voucherRepository.decrementUsedCountConditionally(v.getId(), now);
                    log.info("Đã hoàn lượt voucher [{}] cho đơn hàng bị hủy [{}] (kết quả update: {})",
                            voucherCode, order.getOrderCode(), updatedRows);
                });
            }

            // C. Phát sự kiện OrderCancelledEvent (Observer Pattern) để thực hiện hoàn kho đồng bộ
            eventPublisher.publishEvent(new OrderCancelledEvent(this, order, actor, historyNote));
        }

        log.info("Chuyển trạng thái đơn hàng [{}] từ [{}] sang [{}] thành công bởi user [{}]",
                order.getOrderCode(), expectedStatus, targetStatus, actorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponseDTO getOrderDetailForAdmin(Long orderId) {
        if (orderId == null) {
            throw new BadRequestException("Mã ID đơn hàng không được để trống.");
        }

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra phân quyền: Quản lý cửa hàng chỉ được xem đơn thuộc cửa hàng mình
        if (storeScopeService.isCurrentStoreManager()) {
            storeScopeService.validateStoreAccess(order.getStore().getId());
        }

        // Tải lịch sử trạng thái theo thứ tự thời gian tăng dần
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);

        // Tải danh sách giao dịch thanh toán
        List<Payment> payments = paymentRepository.findByOrderId(orderId);

        OrderDetailResponseDTO dto = orderMapper.toDetailDTO(order);
        dto.setStatusHistories(orderMapper.toStatusHistoryDTOList(histories));
        dto.setPayments(orderMapper.toPaymentDTOList(payments));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersForAdmin(Long storeId, OrderStatus status, OrderChannel channel,
                                                    String keyword, LocalDate startDate, LocalDate endDate,
                                                    Pageable pageable) {
        // Kiểm tra phạm vi chi nhánh cho STORE_MANAGER
        Long effectiveStoreId = storeId;
        if (storeScopeService.isCurrentStoreManager()) {
            Long assignedStoreId = storeScopeService.getAssignedStoreId();
            if (assignedStoreId == null) {
                throw new AccessDeniedException("Tài khoản của bạn chưa được phân công chi nhánh!");
            }
            effectiveStoreId = assignedStoreId;
        }

        LocalDateTime fromDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (endDate != null) ? endDate.atTime(23, 59, 59, 999999999) : null;
        String kw = (keyword != null && !keyword.trim().isBlank()) ? keyword.trim() : null;

        // Truy vấn phân trang đơn hàng kèm JOIN FETCH store, customer, cashier (chống N+1)
        Page<Order> orderPage = orderRepository.searchOrdersAdmin(
                effectiveStoreId, status, channel, kw, fromDateTime, toDateTime, pageable);

        if (orderPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Tải thanh toán theo lô (Batch fetch) để chống triệt để N+1 query
        List<Long> orderIds = orderPage.getContent().stream()
                .map(Order::getId)
                .toList();

        Map<Long, List<Payment>> paymentsByOrderId = paymentRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(p -> p.getOrder().getId()));

        List<OrderResponseDTO> dtoList = orderPage.getContent().stream().map(o -> {
            OrderResponseDTO dto = orderMapper.toResponseDTO(o);
            List<Payment> oPayments = paymentsByOrderId.getOrDefault(o.getId(), Collections.emptyList());
            if (!oPayments.isEmpty()) {
                Payment firstPayment = oPayments.get(0);
                dto.setPaymentMethod(firstPayment.getMethod());
                dto.setPaymentStatus(firstPayment.getStatus());
            }
            return dto;
        }).toList();

        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }
}

