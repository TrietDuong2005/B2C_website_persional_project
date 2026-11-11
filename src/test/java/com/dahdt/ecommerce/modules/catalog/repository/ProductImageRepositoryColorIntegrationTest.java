package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp trên SQL Server thật (NGÀY 12C - PHẦN 4).
 * Xác thực:
 * - Migration V31 đã thực thi thành công (cột color tồn tại trên bảng product_images).
 * - Lưu và đọc hình ảnh theo sản phẩm.
 * - Lọc danh sách hình ảnh theo màu sắc (findByProductIdAndColorOrderBySortOrderAscIdAsc).
 * - Tự động rollback sau khi test kết thúc nhờ @Transactional.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Kiểm thử tích hợp SQL Server: Migration V31, cột color và lọc ảnh theo màu")
class ProductImageRepositoryColorIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Migration V31 thành công: Lưu ảnh có cột color, đọc theo sản phẩm và lọc theo màu sắc")
    void testSaveAndQueryImagesByColor() {
        Category sampleCategory = categoryRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 danh mục trong CSDL"));

        Product product = Product.builder()
                .name("Dép Slipper Test V31 Integration")
                .slug("dep-slipper-test-v31-" + System.currentTimeMillis())
                .category(sampleCategory)
                .status(ProductStatus.ACTIVE)
                .thumbnailUrl("/images/test-thumb.jpg")
                .build();
        Product savedProduct = productRepository.save(product);

        // Lưu 3 ảnh: 1 ảnh màu Hồng Phấn, 1 ảnh màu Kem Sữa, 1 ảnh chung (color = null)
        ProductImage pinkImg = ProductImage.builder()
                .product(savedProduct)
                .color("Hồng Phấn")
                .url("/images/test-pink.jpg")
                .sortOrder(1)
                .build();

        ProductImage creamImg = ProductImage.builder()
                .product(savedProduct)
                .color("Kem Sữa")
                .url("/images/test-cream.jpg")
                .sortOrder(2)
                .build();

        ProductImage generalImg = ProductImage.builder()
                .product(savedProduct)
                .color(null)
                .url("/images/test-general.jpg")
                .sortOrder(3)
                .build();

        productImageRepository.saveAll(List.of(pinkImg, creamImg, generalImg));

        // 1. Đọc toàn bộ ảnh theo sản phẩm
        List<ProductImage> allImages = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(savedProduct.getId());
        assertEquals(3, allImages.size(),
                "Sản phẩm phải có đủ 3 ảnh trong bộ sưu tập. Thực tế: " + allImages.size());

        // 2. Lọc ảnh theo màu "Hồng Phấn"
        List<ProductImage> pinkImages = productImageRepository.findByProductIdAndColorOrderBySortOrderAscIdAsc(
                savedProduct.getId(), "Hồng Phấn");
        assertEquals(1, pinkImages.size(),
                "Chỉ có đúng 1 ảnh thuộc màu 'Hồng Phấn'. Thực tế: " + pinkImages.size());
        assertEquals("Hồng Phấn", pinkImages.get(0).getColor(),
                "Màu sắc của ảnh lọc được phải là 'Hồng Phấn'. Thực tế: " + pinkImages.get(0).getColor());
        assertEquals("/images/test-pink.jpg", pinkImages.get(0).getUrl(),
                "Đường dẫn URL ảnh màu Hồng Phấn không khớp. Thực tế: " + pinkImages.get(0).getUrl());

        // 3. Lọc ảnh theo màu "Kem Sữa"
        List<ProductImage> creamImages = productImageRepository.findByProductIdAndColorOrderBySortOrderAscIdAsc(
                savedProduct.getId(), "Kem Sữa");
        assertEquals(1, creamImages.size(),
                "Chỉ có đúng 1 ảnh thuộc màu 'Kem Sữa'. Thực tế: " + creamImages.size());
        assertEquals("/images/test-cream.jpg", creamImages.get(0).getUrl(),
                "Đường dẫn URL ảnh màu Kem Sữa không khớp. Thực tế: " + creamImages.get(0).getUrl());
    }
}

