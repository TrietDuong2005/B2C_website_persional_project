package com.dahdt.ecommerce.modules.catalog.dto.shop;

import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO dữ liệu cung cấp cho thanh công cụ bộ lọc:
 * - Cây danh mục đang hoạt động
 * - Thương hiệu đang hoạt động có sản phẩm
 * - Danh sách màu sắc, kích cỡ size
 * - Khoảng giá (min/max)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopFilterDataDTO {

    @Builder.Default
    private List<CategoryResponseDTO> categories = new ArrayList<>();

    @Builder.Default
    private List<BrandResponseDTO> brands = new ArrayList<>();

    @Builder.Default
    private List<String> colors = new ArrayList<>();

    @Builder.Default
    private List<String> sizes = new ArrayList<>();

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

