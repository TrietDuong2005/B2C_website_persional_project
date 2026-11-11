package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu bảng product_images (hình ảnh sản phẩm).
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Lấy toàn bộ danh sách hình ảnh của sản phẩm theo thứ tự sắp xếp sort_order.
     */
    List<ProductImage> findByProductIdOrderBySortOrderAscIdAsc(Long productId);

    /**
     * Lấy danh sách ảnh gắn riêng cho biến thể.
     */
    List<ProductImage> findByVariantIdOrderBySortOrderAscIdAsc(Long variantId);

    /**
     * Lấy danh sách ảnh gắn theo màu sắc của sản phẩm.
     */
    List<ProductImage> findByProductIdAndColorOrderBySortOrderAscIdAsc(Long productId, String color);
}

