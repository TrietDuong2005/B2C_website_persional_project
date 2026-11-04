package com.dahdt.ecommerce.modules.store.repository;

import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu chi nhánh / kho hàng (bảng stores).
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /** Tìm cửa hàng theo mã code */
    Optional<Store> findByCode(String code);

    /** Kiểm tra mã code đã tồn tại chưa */
    boolean existsByCode(String code);

    /** Kiểm tra mã code đã tồn tại cho cửa hàng khác hay chưa (khi cập nhật) */
    boolean existsByCodeAndIdNot(String code, Long id);

    /** Lấy danh sách tất cả các cửa hàng đang hoạt động, sắp xếp theo tên */
    List<Store> findByStatusOrderByNameAsc(StoreStatus status);

    /**
     * Tìm kiếm và lọc danh sách chi nhánh với bộ lọc đa điều kiện:
     * - keyword: tìm kiếm theo tên, mã code, số điện thoại hoặc địa chỉ
     * - type: lọc theo loại (STORE / WAREHOUSE)
     * - status: lọc theo trạng thái (ACTIVE / INACTIVE)
     */
    @Query("SELECT s FROM Store s " +
            "WHERE (:keyword IS NULL " +
            "       OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(s.detailAddress) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(s.district) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(s.province) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:type IS NULL OR s.type = :type) " +
            "AND (:status IS NULL OR s.status = :status)")
    Page<Store> searchStores(@Param("keyword") String keyword,
                             @Param("type") StoreType type,
                             @Param("status") StoreStatus status,
                             Pageable pageable);
}

