package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

