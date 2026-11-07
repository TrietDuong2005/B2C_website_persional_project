package com.dahdt.ecommerce.modules.order.repository;

import com.dahdt.ecommerce.modules.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu đơn hàng đa kênh (bảng orders).
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Tìm đơn hàng theo mã đơn hiển thị duy nhất (ví dụ: DH202611070001).
     */
    Optional<Order> findByOrderCode(String orderCode);

    /**
     * Tìm đơn hàng kèm thông tin chi tiết các mặt hàng (JOIN FETCH chống N+1 query).
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.customer c " +
            "WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithDetails(@Param("orderCode") String orderCode);

    /**
     * Kiểm tra sự tồn tại của mã đơn hàng.
     */
    boolean existsByOrderCode(String orderCode);

    /**
     * Lấy danh sách đơn hàng của khách hàng theo phân trang, sắp xếp theo thời điểm đặt giảm dần.
     */
    Page<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);
}

