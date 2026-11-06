package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopFilterDataDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductDetailDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductFilterDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service chỉ đọc dành riêng cho khách hàng xem và tìm kiếm sản phẩm trên Shop Online.
 * Tách biệt hoàn toàn với ProductService của khu vực Quản trị Admin.
 */
public interface ShopCatalogService {

    /**
     * Tìm kiếm và lọc danh sách sản phẩm hiển thị trên lưới sản phẩm (/shop/products):
     * - Chỉ lấy sản phẩm ACTIVE có ít nhất một biến thể ACTIVE
     * - Lọc theo: từ khóa, danh mục (gốc + con), nhiều thương hiệu, khoảng giá, màu, size, chỉ còn hàng
     * - Sắp xếp: mới nhất / giá tăng / giá giảm / tên
     * - Tối ưu hoàn toàn chống N+1 query
     */
    Page<ShopProductItemDTO> listProducts(ShopProductFilterDTO filter, Pageable pageable);

    /**
     * Lấy dữ liệu phục vụ bộ lọc:
     * - Cây danh mục đang hoạt động
     * - Danh sách thương hiệu đang hoạt động có sản phẩm
     * - Danh sách màu sắc, kích cỡ size và khoảng giá từ các biến thể ACTIVE
     */
    ShopFilterDataDTO getFilterData();

    /**
     * Lấy chi tiết sản phẩm theo slug URL (/shop/products/{slug}):
     * - Ném ResourceNotFoundException nếu không tìm thấy hoặc sản phẩm đang HIDDEN
     * - Trả thông tin chi tiết, ảnh chung, ảnh theo biến thể, danh sách biến thể kèm giá và tồn online
     */
    ShopProductDetailDTO getProductDetail(String slug);

    /**
     * Lấy danh sách n sản phẩm mới nhất đang kinh doanh cho trang chủ Shop (URL "/").
     */
    List<ShopProductItemDTO> latestProducts(int limit);
}

