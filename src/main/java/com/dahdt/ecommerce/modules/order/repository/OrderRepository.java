package com.dahdt.ecommerce.modules.order.repository;

import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * Lấy danh sách đơn hàng POS theo chi nhánh cửa hàng, phân trang và sắp xếp mới nhất trước (tránh N+1 query).
     */
    @Query(value = "SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.cashier c " +
            "LEFT JOIN FETCH c.profile " +
            "LEFT JOIN FETCH o.store s " +
            "WHERE o.channel = :channel AND o.store.id = :storeId " +
            "ORDER BY o.placedAt DESC",
            countQuery = "SELECT COUNT(o) FROM Order o WHERE o.channel = :channel AND o.store.id = :storeId")
    Page<Order> findByChannelAndStoreIdOrderByPlacedAtDesc(
            @Param("channel") OrderChannel channel,
            @Param("storeId") Long storeId,
            Pageable pageable);

    /**
     * Cập nhật trạng thái đơn hàng bằng một câu UPDATE có điều kiện nguyên tử.
     * Chống xung đột đồng thời (Race Condition) và đảm bảo tính bất biến (Idempotency):
     * Chỉ cập nhật khi trạng thái hiện tại trong DB đúng bằng expectedStatus.
     * Nếu có 2 thao tác cùng bấm hủy cùng lúc, chỉ 1 thao tác trả về số dòng > 0,
     * ngăn chặn việc chạy hiệu ứng phụ (hoàn kho) 2 lần.
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :targetStatus, o.completedAt = :completedAt, o.updatedAt = :now " +
            "WHERE o.id = :orderId AND o.status = :expectedStatus")
    int updateStatusConditionally(@Param("orderId") Long orderId,
                                  @Param("expectedStatus") OrderStatus expectedStatus,
                                  @Param("targetStatus") OrderStatus targetStatus,
                                  @Param("completedAt") LocalDateTime completedAt,
                                  @Param("now") LocalDateTime now);

    /**
     * Tìm kiếm và lọc danh sách đơn hàng cho Admin/Store Manager:
     * - JOIN FETCH store, customer, cashier (và profile) để tránh hoàn toàn N+1 query.
     * - Lọc theo chi nhánh (bắt buộc với STORE_MANAGER), trạng thái đơn, kênh bán, từ khóa, khoảng ngày.
     * - Sắp xếp mới nhất lên trước.
     */
    @Query(value = "SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH c.profile cp " +
            "LEFT JOIN FETCH o.cashier ca " +
            "LEFT JOIN FETCH ca.profile cap " +
            "WHERE (:storeId IS NULL OR o.store.id = :storeId) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:channel IS NULL OR o.channel = :channel) " +
            "AND (:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(o.shipName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(o.shipPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:fromDate IS NULL OR o.placedAt >= :fromDate) " +
            "AND (:toDate IS NULL OR o.placedAt <= :toDate) " +
            "ORDER BY o.placedAt DESC",
            countQuery = "SELECT COUNT(o) FROM Order o " +
            "WHERE (:storeId IS NULL OR o.store.id = :storeId) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:channel IS NULL OR o.channel = :channel) " +
            "AND (:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(o.shipName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(o.shipPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:fromDate IS NULL OR o.placedAt >= :fromDate) " +
            "AND (:toDate IS NULL OR o.placedAt <= :toDate)")
    Page<Order> searchOrdersAdmin(
            @Param("storeId") Long storeId,
            @Param("status") OrderStatus status,
            @Param("channel") OrderChannel channel,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    /**
     * Tìm đơn hàng theo ID kèm fetch đầy đủ chi tiết items, variants, product, store, customer, cashier.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH c.profile cp " +
            "LEFT JOIN FETCH o.cashier ca " +
            "LEFT JOIN FETCH ca.profile cap " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}

