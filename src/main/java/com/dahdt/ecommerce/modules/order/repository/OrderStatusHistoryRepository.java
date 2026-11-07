package com.dahdt.ecommerce.modules.order.repository;

import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository lưu vết và truy xuất lịch sử thay đổi trạng thái đơn hàng (bảng order_status_history).
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query("SELECT h FROM OrderStatusHistory h " +
            "LEFT JOIN FETCH h.changedBy " +
            "WHERE h.order.id = :orderId " +
            "ORDER BY h.changedAt ASC")
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(@Param("orderId") Long orderId);
}

