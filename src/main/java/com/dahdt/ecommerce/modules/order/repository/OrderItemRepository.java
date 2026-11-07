package com.dahdt.ecommerce.modules.order.repository;

import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác các dòng chi tiết đơn hàng (bảng order_items).
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}

