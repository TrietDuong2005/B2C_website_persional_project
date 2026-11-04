package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng brands.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Tìm thương hiệu theo slug URL.
     */
    Optional<Brand> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại hay chưa.
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra slug đã được dùng bởi thương hiệu khác hay chưa.
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Kiểm tra tên thương hiệu đã tồn tại hay chưa.
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên thương hiệu đã tồn tại ở bản ghi khác hay chưa.
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Lấy toàn bộ thương hiệu theo thứ tự alphabet.
     */
    List<Brand> findAllByOrderByNameAsc();

    /**
     * Lấy danh sách thương hiệu đang hoạt động (dùng cho combobox sản phẩm).
     */
    List<Brand> findByIsActiveTrueOrderByNameAsc();

    /**
     * Tìm kiếm thương hiệu theo từ khóa có phân trang.
     */
    @Query("SELECT b FROM Brand b WHERE (:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.name ASC")
    Page<Brand> searchBrands(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Đếm số lượng sản phẩm trực thuộc một thương hiệu.
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    long countProductsByBrandId(@Param("brandId") Long brandId);
}

