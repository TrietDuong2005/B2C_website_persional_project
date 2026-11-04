package com.dahdt.ecommerce.modules.store.repository;

import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu hồ sơ nhân viên chuỗi (bảng employees).
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** Tìm hồ sơ nhân viên theo user_id */
    @Query("SELECT e FROM Employee e " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "JOIN FETCH e.store s " +
            "WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") Long userId);

    /** Kiểm tra xem một user đã được gán hồ sơ nhân viên ở cửa hàng nào chưa */
    boolean existsByUserId(Long userId);

    /** Đếm số nhân viên của một cửa hàng */
    long countByStoreId(Long storeId);

    /** Đếm số nhân viên đang hoạt động của một cửa hàng */
    long countByStoreIdAndStatus(Long storeId, EmployeeStatus status);

    /**
     * Lấy danh sách nhân viên theo cửa hàng kèm thông tin user và profile (tránh N+1).
     */
    @Query("SELECT e FROM Employee e " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "JOIN FETCH e.store s " +
            "WHERE e.store.id = :storeId " +
            "ORDER BY e.createdAt DESC")
    List<Employee> findByStoreIdWithDetails(@Param("storeId") Long storeId);

    /**
     * Lấy danh sách nhân viên theo cửa hàng có phân trang và tìm kiếm theo tên/email/sđt.
     */
    @Query(value = "SELECT e FROM Employee e " +
            "JOIN FETCH e.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "JOIN FETCH e.store s " +
            "WHERE e.store.id = :storeId " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR (p.fullName IS NOT NULL AND LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))))",
            countQuery = "SELECT COUNT(e) FROM Employee e " +
            "JOIN e.user u " +
            "LEFT JOIN u.profile p " +
            "WHERE e.store.id = :storeId " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR (p.fullName IS NOT NULL AND LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<Employee> searchEmployeesByStore(@Param("storeId") Long storeId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}

