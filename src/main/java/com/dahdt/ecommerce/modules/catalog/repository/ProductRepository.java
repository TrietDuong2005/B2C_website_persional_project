package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng products (sản phẩm cha).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Tìm sản phẩm theo slug URL.
     */
    Optional<Product> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại hay chưa.
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra slug đã được dùng bởi sản phẩm khác hay chưa.
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Lấy chi tiết sản phẩm kèm nạp sẵn danh mục và thương hiệu (tránh LazyInitializationException và N+1).
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndBrand(@Param("id") Long id);

    /**
     * Tìm kiếm sản phẩm phân trang theo từ khóa, danh mục, thương hiệu và trạng thái.
     * Sử dụng JOIN FETCH p.category và LEFT JOIN FETCH p.brand để tránh hoàn toàn vấn đề N+1 query.
     */
    @Query(value = "SELECT p FROM Product p " +
            "JOIN FETCH p.category c " +
            "LEFT JOIN FETCH p.brand b " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId) " +
            "AND (:brandId IS NULL OR b.id = :brandId) " +
            "AND (:status IS NULL OR p.status = :status)",
            countQuery = "SELECT COUNT(p) FROM Product p " +
                    "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:categoryId IS NULL OR p.category.id = :categoryId OR p.category.parent.id = :categoryId) " +
                    "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
                    "AND (:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("brandId") Long brandId,
                                 @Param("status") ProductStatus status,
                                 Pageable pageable);
}

