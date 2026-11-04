package com.dahdt.ecommerce.modules.inventory.repository;

import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository truy xuất dữ liệu sổ cái giao dịch kho (bảng inventory_transactions).
 * Sổ kho chỉ hỗ trợ thao tác đọc (SELECT) và thêm (INSERT), tuyệt đối không update hay delete.
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Tìm kiếm và truy vấn sổ cái kho với bộ lọc đa điều kiện:
     * - storeId: lọc theo chi nhánh (nếu null và là ADMIN thì xem toàn chuỗi)
     * - sku: tìm kiếm theo SKU biến thể hoặc tên sản phẩm
     * - type: lọc theo loại nghiệp vụ (IMPORT, SALE, CANCEL, TRANSFER_IN, TRANSFER_OUT, ADJUST)
     * - fromDate, toDate: khoảng thời gian phát sinh giao dịch
     * - Sắp xếp mặc định: mới nhất lên trước (createdAt DESC)
     */
    @Query(value = "SELECT it FROM InventoryTransaction it " +
            "JOIN FETCH it.variant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH it.store s " +
            "LEFT JOIN FETCH it.createdBy u " +
            "LEFT JOIN FETCH u.profile " +
            "WHERE (:storeId IS NULL OR it.store.id = :storeId) " +
            "AND (:sku IS NULL " +
            "     OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :sku, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :sku, '%'))) " +
            "AND (:type IS NULL OR it.type = :type) " +
            "AND (:fromDate IS NULL OR it.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR it.createdAt <= :toDate)",
            countQuery = "SELECT COUNT(it) FROM InventoryTransaction it " +
            "JOIN it.variant v " +
            "JOIN v.product p " +
            "WHERE (:storeId IS NULL OR it.store.id = :storeId) " +
            "AND (:sku IS NULL " +
            "     OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :sku, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :sku, '%'))) " +
            "AND (:type IS NULL OR it.type = :type) " +
            "AND (:fromDate IS NULL OR it.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR it.createdAt <= :toDate)")
    Page<InventoryTransaction> searchTransactions(@Param("storeId") Long storeId,
                                                  @Param("sku") String sku,
                                                  @Param("type") InventoryTransactionType type,
                                                  @Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate,
                                                  Pageable pageable);
}

