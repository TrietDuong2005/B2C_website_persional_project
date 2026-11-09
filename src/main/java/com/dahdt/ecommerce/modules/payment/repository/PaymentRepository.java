package com.dahdt.ecommerce.modules.payment.repository;

import com.dahdt.ecommerce.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository lưu vết và truy xuất giao dịch thanh toán (bảng payments).
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Tìm danh sách thanh toán theo danh sách ID đơn hàng (chống N+1 query khi hiển thị danh sách đơn).
     */
    List<Payment> findByOrderIdIn(Collection<Long> orderIds);
}

