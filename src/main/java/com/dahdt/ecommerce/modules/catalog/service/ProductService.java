package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.ProductDetailResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface nghiệp vụ quản lý Sản phẩm cha (Product).
 */
public interface ProductService {

    /**
     * Lấy danh sách sản phẩm có phân trang, tìm kiếm theo từ khóa và lọc theo danh mục, thương hiệu, trạng thái.
     * Tối ưu tránh N+1 query.
     */
    Page<ProductResponseDTO> getProducts(String keyword, Long categoryId, Long brandId, ProductStatus status, Pageable pageable);

    /**
     * Lấy tóm tắt sản phẩm theo ID.
     */
    ProductResponseDTO getProductById(Long id);

    /**
     * Lấy chi tiết toàn diện sản phẩm kèm danh sách biến thể và hình ảnh.
     */
    ProductDetailResponseDTO getProductDetail(Long id);

    /**
     * Tạo mới sản phẩm với slug tự sinh duy nhất.
     */
    ProductResponseDTO createProduct(ProductRequestDTO dto);

    /**
     * Cập nhật thông tin cơ bản của sản phẩm.
     */
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);

    /**
     * Bật/tắt trạng thái hiển thị của sản phẩm (ACTIVE <-> HIDDEN).
     */
    void toggleProductStatus(Long id);

    /**
     * "Xoá" sản phẩm (thực chất là đổi trạng thái sang HIDDEN, không xoá cứng).
     */
    void deleteProduct(Long id);
}

