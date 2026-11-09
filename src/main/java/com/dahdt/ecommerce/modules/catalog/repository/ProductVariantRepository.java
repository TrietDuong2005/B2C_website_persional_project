package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng product_variants (biến thể sản phẩm).
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Tìm biến thể theo mã SKU.
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Kiểm tra mã SKU đã tồn tại hay chưa.
     */
    boolean existsBySku(String sku);

    /**
     * Kiểm tra mã SKU đã tồn tại ở biến thể khác hay chưa.
     */
    boolean existsBySkuAndIdNot(String sku, Long id);

    /**
     * Kiểm tra mã barcode đã tồn tại hay chưa.
     */
    boolean existsByBarcode(String barcode);

    /**
     * Kiểm tra mã barcode đã tồn tại ở biến thể khác hay chưa.
     */
    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    /**
     * Lấy danh sách biến thể của một sản phẩm.
     */
    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

    /**
     * Đếm số lượng biến thể của sản phẩm.
     */
    long countByProductId(Long productId);

    /**
     * Lấy danh sách biến thể của sản phẩm theo trạng thái.
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.status = :status ORDER BY v.price ASC, v.id ASC")
    List<ProductVariant> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus status);

    /**
     * Lấy danh sách biến thể của nhiều sản phẩm cùng lúc (tránh N+1).
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id IN :productIds AND v.status = :status ORDER BY v.price ASC, v.id ASC")
    List<ProductVariant> findByProductIdInAndStatus(@Param("productIds") Collection<Long> productIds, @Param("status") com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus status);

    /**
     * Lấy toàn bộ danh sách màu sắc đang có của các biến thể ACTIVE thuộc sản phẩm ACTIVE (cho bộ lọc Shop).
     */
    @Query("SELECT DISTINCT v.color FROM ProductVariant v WHERE v.status = com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus.ACTIVE AND v.product.status = com.dahdt.ecommerce.modules.catalog.entity.ProductStatus.ACTIVE ORDER BY v.color ASC")
    List<String> findActiveColors();

    /**
     * Lấy toàn bộ danh sách kích cỡ size đang có của các biến thể ACTIVE thuộc sản phẩm ACTIVE (cho bộ lọc Shop).
     */
    @Query("SELECT DISTINCT v.size FROM ProductVariant v WHERE v.status = com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus.ACTIVE AND v.product.status = com.dahdt.ecommerce.modules.catalog.entity.ProductStatus.ACTIVE")
    List<String> findActiveSizes();

    /**
     * Tìm giá thấp nhất trong toàn bộ các biến thể ACTIVE của sản phẩm ACTIVE.
     */
    @Query("SELECT MIN(v.price) FROM ProductVariant v WHERE v.status = com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus.ACTIVE AND v.product.status = com.dahdt.ecommerce.modules.catalog.entity.ProductStatus.ACTIVE")
    BigDecimal findMinActivePrice();

    /**
     * Tìm giá cao nhất trong toàn bộ các biến thể ACTIVE của sản phẩm ACTIVE.
     */
    @Query("SELECT MAX(v.price) FROM ProductVariant v WHERE v.status = com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus.ACTIVE AND v.product.status = com.dahdt.ecommerce.modules.catalog.entity.ProductStatus.ACTIVE")
    BigDecimal findMaxActivePrice();

    /**
     * Tìm kiếm biến thể ACTIVE thuộc sản phẩm ACTIVE phục vụ bán hàng tại quầy POS:
     * - Khớp chính xác SKU
     * - Khớp chính xác Barcode
     * - Khớp từ khóa trong tên sản phẩm cha
     * - Eager fetch Product để tránh N+1
     * - Giới hạn bằng Pageable (tối đa 20 dòng)
     */
    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p " +
            "WHERE v.status = com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus.ACTIVE " +
            "AND p.status = com.dahdt.ecommerce.modules.catalog.entity.ProductStatus.ACTIVE " +
            "AND (LOWER(v.sku) = LOWER(:kw) " +
            "     OR v.barcode = :kw " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "ORDER BY CASE " +
            "    WHEN LOWER(v.sku) = LOWER(:kw) THEN 0 " +
            "    WHEN v.barcode = :kw THEN 1 " +
            "    ELSE 2 END, p.name ASC, v.sku ASC")
    List<ProductVariant> searchActiveVariantsForPos(@Param("kw") String kw, Pageable pageable);
}

