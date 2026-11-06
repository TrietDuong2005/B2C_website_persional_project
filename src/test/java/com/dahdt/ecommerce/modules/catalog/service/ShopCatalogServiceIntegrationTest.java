package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductFilterDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductItemDTO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test này bảo vệ lỗi SQL Server 145 (DISTINCT + ORDER BY biểu thức) mà mock không bắt được.
 *
 * Kiểm thử tích hợp ShopCatalogService chạy trên cơ sở dữ liệu SQL Server thật.
 * Mục đích: Xác nhận truy vấn sinh bởi JPA Specification khi sắp xếp theo giá không còn
 * gây lỗi SQL Error 145 (ORDER BY items must appear in the select list if SELECT DISTINCT is specified).
 *
 * Chỉ đọc (read-only), không thêm/sửa/xoá dữ liệu.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Kiểm thử tích hợp ShopCatalogService trên SQL Server thật (Chống lỗi SQL Error 145)")
class ShopCatalogServiceIntegrationTest {

    @Autowired
    private ShopCatalogService shopCatalogService;

    @Test
    @DisplayName("listProducts(): Sắp xếp giá tăng dần (price_asc) không lỗi và giá không giảm")
    void listProducts_whenSortPriceAsc_shouldReturnProductsInAscendingPriceOrder() {
        ShopProductFilterDTO filter = ShopProductFilterDTO.builder()
                .sort("price_asc")
                .build();

        Page<ShopProductItemDTO> page = assertDoesNotThrow(() ->
                shopCatalogService.listProducts(filter, PageRequest.of(0, 12))
        );

        assertNotNull(page);
        Assumptions.assumeTrue(page.getTotalElements() > 0, "Bỏ qua kiểm tra thứ tự vì cơ sở dữ liệu chưa có sản phẩm");

        List<ShopProductItemDTO> items = page.getContent();
        for (int i = 0; i < items.size() - 1; i++) {
            BigDecimal currentPrice = items.get(i).getMinPrice();
            BigDecimal nextPrice = items.get(i + 1).getMinPrice();
            if (currentPrice != null && nextPrice != null) {
                assertTrue(currentPrice.compareTo(nextPrice) <= 0,
                        String.format("Giá tăng dần không đúng: phần tử %d (%s) > phần tử %d (%s)",
                                i, currentPrice, i + 1, nextPrice));
            }
        }
    }

    @Test
    @DisplayName("listProducts(): Sắp xếp giá giảm dần (price_desc) không lỗi và giá không tăng")
    void listProducts_whenSortPriceDesc_shouldReturnProductsInDescendingPriceOrder() {
        ShopProductFilterDTO filter = ShopProductFilterDTO.builder()
                .sort("price_desc")
                .build();

        Page<ShopProductItemDTO> page = assertDoesNotThrow(() ->
                shopCatalogService.listProducts(filter, PageRequest.of(0, 12))
        );

        assertNotNull(page);
        Assumptions.assumeTrue(page.getTotalElements() > 0, "Bỏ qua kiểm tra thứ tự vì cơ sở dữ liệu chưa có sản phẩm");

        List<ShopProductItemDTO> items = page.getContent();
        for (int i = 0; i < items.size() - 1; i++) {
            BigDecimal currentPrice = items.get(i).getMinPrice();
            BigDecimal nextPrice = items.get(i + 1).getMinPrice();
            if (currentPrice != null && nextPrice != null) {
                assertTrue(currentPrice.compareTo(nextPrice) >= 0,
                        String.format("Giá giảm dần không đúng: phần tử %d (%s) < phần tử %d (%s)",
                                i, currentPrice, i + 1, nextPrice));
            }
        }
    }

    @Test
    @DisplayName("listProducts(): Kết hợp lọc danh mục gốc 'giay' và sắp xếp giá tăng dần")
    void listProducts_whenCategoryGiayAndSortPriceAsc_shouldSucceed() {
        ShopProductFilterDTO filter = ShopProductFilterDTO.builder()
                .category("giay")
                .sort("price_asc")
                .build();

        Page<ShopProductItemDTO> page = assertDoesNotThrow(() ->
                shopCatalogService.listProducts(filter, PageRequest.of(0, 12))
        );

        assertNotNull(page);
        Assumptions.assumeTrue(page.getTotalElements() > 0, "Bỏ qua kiểm tra thứ tự vì cơ sở dữ liệu chưa có sản phẩm danh mục 'giay'");

        List<ShopProductItemDTO> items = page.getContent();
        for (int i = 0; i < items.size() - 1; i++) {
            BigDecimal currentPrice = items.get(i).getMinPrice();
            BigDecimal nextPrice = items.get(i + 1).getMinPrice();
            if (currentPrice != null && nextPrice != null) {
                assertTrue(currentPrice.compareTo(nextPrice) <= 0,
                        String.format("Giá tăng dần không đúng: phần tử %d (%s) > phần tử %d (%s)",
                                i, currentPrice, i + 1, nextPrice));
            }
        }
    }

    @Test
    @DisplayName("listProducts(): Kết hợp lọc danh mục gốc 'giay' và sắp xếp giá giảm dần")
    void listProducts_whenCategoryGiayAndSortPriceDesc_shouldSucceed() {
        ShopProductFilterDTO filter = ShopProductFilterDTO.builder()
                .category("giay")
                .sort("price_desc")
                .build();

        Page<ShopProductItemDTO> page = assertDoesNotThrow(() ->
                shopCatalogService.listProducts(filter, PageRequest.of(0, 12))
        );

        assertNotNull(page);
        Assumptions.assumeTrue(page.getTotalElements() > 0, "Bỏ qua kiểm tra thứ tự vì cơ sở dữ liệu chưa có sản phẩm danh mục 'giay'");

        List<ShopProductItemDTO> items = page.getContent();
        for (int i = 0; i < items.size() - 1; i++) {
            BigDecimal currentPrice = items.get(i).getMinPrice();
            BigDecimal nextPrice = items.get(i + 1).getMinPrice();
            if (currentPrice != null && nextPrice != null) {
                assertTrue(currentPrice.compareTo(nextPrice) >= 0,
                        String.format("Giá giảm dần không đúng: phần tử %d (%s) < phần tử %d (%s)",
                                i, currentPrice, i + 1, nextPrice));
            }
        }
    }
}

