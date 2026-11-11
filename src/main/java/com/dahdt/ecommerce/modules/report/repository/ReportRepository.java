package com.dahdt.ecommerce.modules.report.repository;

import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.report.dto.projection.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Repository phục vụ truy vấn tổng hợp báo cáo thống kê Dashboard.
 *
 * NGUYÊN TẮC THIẾT KẾ:
 * 1. An toàn kiểu (Type-safe): Dùng biểu thức khởi tạo SELECT new com.dahdt...Row(...) trong JPQL,
 *    không dùng Object[] và không ép kiểu thủ công.
 * 2. Tuân thủ chuẩn SQL Server: Không dùng SELECT DISTINCT kèm ORDER BY biểu thức.
 * 3. Nhóm dữ liệu theo ngày bằng cast thời gian về kiểu ngày (cast(o.completedAt as LocalDate)).
 * 4. COALESCE(SUM(...), cast(0 as BigDecimal)) bảo đảm cùng kiểu dữ liệu BigDecimal với cột tiền.
 */
@Repository
public interface ReportRepository extends JpaRepository<Order, Long> {

    /**
     * Tổng hợp các chỉ số của đơn hàng HOÀN TẤT (COMPLETED) trong kỳ.
     * Trả về CompletedOrderSummaryRow có kiểu dữ liệu rõ ràng: Long, BigDecimal.
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.CompletedOrderSummaryRow(" +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal)), " +
            "COALESCE(SUM(CASE WHEN o.channel = com.dahdt.ecommerce.modules.order.entity.OrderChannel.POS THEN o.totalAmount ELSE cast(0 as BigDecimal) END), cast(0 as BigDecimal)), " +
            "COALESCE(SUM(CASE WHEN o.channel = com.dahdt.ecommerce.modules.order.entity.OrderChannel.ONLINE THEN o.totalAmount ELSE cast(0 as BigDecimal) END), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :startDateTime AND o.completedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId)")
    CompletedOrderSummaryRow getCompletedOrderSummary(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Tổng hợp số đơn và doanh thu dự kiến của các đơn hàng ĐANG XỬ LÝ (PENDING, CONFIRMED, PACKING, SHIPPING).
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.ProcessingOrderSummaryRow(" +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.status IN (:statuses) " +
            "AND o.placedAt >= :startDateTime AND o.placedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId)")
    ProcessingOrderSummaryRow getProcessingOrderSummary(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Đếm số lượng đơn hàng ĐÃ HỦY (CANCELLED) đặt trong kỳ.
     */
    @Query("SELECT COUNT(o) " +
            "FROM Order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.CANCELLED " +
            "AND o.placedAt >= :startDateTime AND o.placedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId)")
    long countCancelledOrders(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Tổng hợp doanh thu và số đơn hoàn thành của ngày HÔM NAY.
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.TodayCompletedSummaryRow(" +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :todayStart AND o.completedAt <= :todayEnd " +
            "AND (:storeId IS NULL OR o.store.id = :storeId)")
    TodayCompletedSummaryRow getTodayCompletedSummary(
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayEnd") LocalDateTime todayEnd,
            @Param("storeId") Long storeId);

    /**
     * Thống kê doanh thu theo từng ngày trong kỳ đối với các đơn hoàn thành.
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.DailyRevenueRow(" +
            "cast(o.completedAt as LocalDate), " +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :startDateTime AND o.completedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY cast(o.completedAt as LocalDate) " +
            "ORDER BY cast(o.completedAt as LocalDate) ASC")
    List<DailyRevenueRow> getDailyRevenue(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Thống kê doanh thu và số đơn hoàn thành theo từng cửa hàng (chi nhánh).
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.StoreRevenueRow(" +
            "o.store.id, " +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :startDateTime AND o.completedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY o.store.id")
    List<StoreRevenueRow> getStoreRevenueAggregates(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Thống kê doanh thu theo phương thức thanh toán đối với các đơn hoàn thành.
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.PaymentMethodRevenueRow(" +
            "p.method, " +
            "COUNT(p), " +
            "COALESCE(SUM(p.amount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Payment p " +
            "JOIN p.order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :startDateTime AND o.completedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY p.method " +
            "ORDER BY COALESCE(SUM(p.amount), cast(0 as BigDecimal)) DESC")
    List<PaymentMethodRevenueRow> getPaymentMethodRevenue(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Thống kê số lượng đơn và tổng tiền theo từng trạng thái đơn đặt trong kỳ.
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.OrderStatusCountRow(" +
            "o.status, " +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalAmount), cast(0 as BigDecimal))" +
            ") " +
            "FROM Order o " +
            "WHERE o.placedAt >= :startDateTime AND o.placedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY o.status")
    List<OrderStatusCountRow> getOrderStatusCounts(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId);

    /**
     * Top sản phẩm bán chạy nhất trong kỳ (tính từ các đơn hàng COMPLETED).
     */
    @Query("SELECT new com.dahdt.ecommerce.modules.report.dto.projection.TopSellingProductRow(" +
            "i.sku, " +
            "i.productName, " +
            "i.color, " +
            "i.size, " +
            "SUM(i.quantity), " +
            "COALESCE(SUM(i.lineTotal), cast(0 as BigDecimal))" +
            ") " +
            "FROM OrderItem i " +
            "JOIN i.order o " +
            "WHERE o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.COMPLETED " +
            "AND o.completedAt >= :startDateTime AND o.completedAt <= :endDateTime " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY i.sku, i.productName, i.color, i.size " +
            "ORDER BY SUM(i.quantity) DESC")
    List<TopSellingProductRow> getTopSellingProducts(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("storeId") Long storeId,
            Pageable pageable);

    /**
     * Cảnh báo các biến thể tồn kho thấp hơn hoặc bằng ngưỡng.
     * Sử dụng JOIN FETCH để nạp đủ variant, product cha và store, tránh triệt để N+1 query.
     */
    @Query("SELECT i FROM Inventory i " +
            "JOIN FETCH i.variant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH i.store s " +
            "WHERE (:storeId IS NULL OR s.id = :storeId) " +
            "AND i.quantityOnHand <= :threshold " +
            "ORDER BY i.quantityOnHand ASC, p.name ASC, v.sku ASC")
    List<Inventory> findLowStockInventories(
            @Param("storeId") Long storeId,
            @Param("threshold") int threshold,
            Pageable pageable);

    /**
     * 5 đơn hàng trực tuyến mới nhất đang chờ xác nhận (PENDING).
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.channel = com.dahdt.ecommerce.modules.order.entity.OrderChannel.ONLINE " +
            "AND o.status = com.dahdt.ecommerce.modules.order.entity.OrderStatus.PENDING " +
            "ORDER BY o.placedAt DESC")
    List<Order> findLatestPendingOnlineOrders(Pageable pageable);
}
