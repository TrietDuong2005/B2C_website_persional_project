package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm danh mục theo slug URL.
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại hay chưa.
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra slug đã được dùng bởi bản ghi khác hay chưa (phục vụ cập nhật).
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Kiểm tra xem một danh mục có danh mục con trực thuộc hay không.
     */
    boolean existsByParentId(Long parentId);

    /**
     * Đếm số lượng sản phẩm đang trực thuộc một danh mục.
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Lấy toàn bộ danh mục sắp xếp theo sort_order và tên.
     */
    List<Category> findAllByOrderBySortOrderAscNameAsc();

    /**
     * Lấy danh mục gốc (parent_id IS NULL) kèm nạp trước (FETCH) danh mục con để tránh N+1.
     */
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children ch WHERE c.parent IS NULL ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategoriesWithChildren();

    /**
     * Lấy danh sách danh mục đang kích hoạt (phục vụ combobox chọn danh mục cha hoặc danh mục sản phẩm).
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();
}

