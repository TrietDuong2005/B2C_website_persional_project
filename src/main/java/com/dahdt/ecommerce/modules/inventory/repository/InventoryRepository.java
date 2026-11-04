package com.dahdt.ecommerce.modules.inventory.repository;

import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository truy xuất dữ liệu tồn kho sản phẩm theo chi nhánh (bảng inventories).
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Tìm bản ghi tồn kho theo cửa hàng và biến thể sản phẩm.
     */
    @Query("SELECT i FROM Inventory i " +
            "JOIN FETCH i.variant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH i.store s " +
            "WHERE i.store.id = :storeId AND i.variant.id = :variantId")
    Optional<Inventory> findByStoreIdAndVariantId(@Param("storeId") Long storeId, @Param("variantId") Long variantId);

    /**
     * Tìm bản ghi tồn kho theo ID kèm fetch các thực thể liên kết (tránh N+1).
     */
    @Query("SELECT i FROM Inventory i " +
            "JOIN FETCH i.variant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH i.store s " +
            "WHERE i.id = :id")
    Optional<Inventory> findByIdWithDetails(@Param("id") Long id);

    /**
     * Tìm kiếm và lọc danh sách tồn kho theo chi nhánh:
     * - JOIN FETCH biến thể, sản phẩm cha, danh mục (tránh N+1)
     * - keyword: tìm kiếm theo SKU biến thể, tên sản phẩm hoặc mã vạch barcode
     * - categoryId: lọc theo danh mục sản phẩm (bao gồm danh mục lá hoặc danh mục cha)
     * - stockStatus: OUT_OF_STOCK (tồn = 0), LOW_STOCK (0 < tồn <= reorder_level), IN_STOCK (tồn > reorder_level)
     */
    @Query(value = "SELECT i FROM Inventory i " +
            "JOIN FETCH i.variant v " +
            "JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.category c " +
            "JOIN FETCH i.store s " +
            "WHERE i.store.id = :storeId " +
            "AND (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId) " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(v.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:stockStatus IS NULL " +
            "     OR (:stockStatus = 'OUT_OF_STOCK' AND i.quantityOnHand = 0) " +
            "     OR (:stockStatus = 'LOW_STOCK' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.reorderLevel) " +
            "     OR (:stockStatus = 'IN_STOCK' AND i.quantityOnHand > i.reorderLevel))",
            countQuery = "SELECT COUNT(i) FROM Inventory i " +
            "JOIN i.variant v " +
            "JOIN v.product p " +
            "LEFT JOIN p.category c " +
            "WHERE i.store.id = :storeId " +
            "AND (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId) " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(v.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:stockStatus IS NULL " +
            "     OR (:stockStatus = 'OUT_OF_STOCK' AND i.quantityOnHand = 0) " +
            "     OR (:stockStatus = 'LOW_STOCK' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.reorderLevel) " +
            "     OR (:stockStatus = 'IN_STOCK' AND i.quantityOnHand > i.reorderLevel))")
    Page<Inventory> searchInventory(@Param("storeId") Long storeId,
                                    @Param("keyword") String keyword,
                                    @Param("categoryId") Long categoryId,
                                    @Param("stockStatus") String stockStatus,
                                    Pageable pageable);
}

