package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductImageMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductVariantMapper;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị cho thứ tự ưu tiên chọn ảnh và kiểm tra ràng buộc (NGÀY 12C - PHẦN 4).
 * Tuân thủ quy tắc 17:
 * - Mọi assert có thông điệp mô tả kèm giá trị thật.
 * - Thứ tự chọn ảnh:
 *   (1) Ảnh gắn đúng biến thể
 *   (2) Ảnh gắn màu của biến thể
 *   (3) Ảnh chung của sản phẩm
 *   (4) Ảnh minh họa mặc định
 * - So khớp màu không phân biệt chữ hoa thường và khoảng trắng thừa.
 * - Ảnh gắn đồng thời biến thể và màu bị từ chối với BadRequestException.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử đơn vị: Thứ tự ưu tiên chọn ảnh biến thể và ràng buộc gắn ảnh (NGÀY 12C)")
class ProductImageResolverTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductVariantMapper productVariantMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    private Product product;
    private ProductVariant variantPink36;
    private ProductVariant variantPink37;
    private ProductVariant variantCream36;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Dép Slipper Bánh Mì Nữ Đế Đúc")
                .thumbnailUrl("/images/thumb-default.jpg")
                .build();

        variantPink36 = ProductVariant.builder()
                .id(101L)
                .product(product)
                .sku("SLIP-HP-36")
                .color("Hồng Phấn")
                .size("36")
                .price(new BigDecimal("199000"))
                .build();

        variantPink37 = ProductVariant.builder()
                .id(102L)
                .product(product)
                .sku("SLIP-HP-37")
                .color("Hồng Phấn")
                .size("37")
                .price(new BigDecimal("199000"))
                .build();

        variantCream36 = ProductVariant.builder()
                .id(103L)
                .product(product)
                .sku("SLIP-KS-36")
                .color("Kem Sữa")
                .size("36")
                .price(new BigDecimal("199000"))
                .build();
    }

    @Test
    @DisplayName("(1) Biến thể có ảnh riêng: Ưu tiên trả về ảnh gắn đúng biến thể cụ thể")
    void testResolve_VariantSpecificImagePreferred() {
        ProductImage varImg = ProductImage.builder()
                .id(1L)
                .product(product)
                .variant(variantPink36)
                .color(null)
                .url("/images/shoe-pink-36-detail.jpg")
                .sortOrder(1)
                .build();

        ProductImage colorImg = ProductImage.builder()
                .id(2L)
                .product(product)
                .variant(null)
                .color("Hồng Phấn")
                .url("/images/shoe-color-pink.jpg")
                .sortOrder(2)
                .build();

        ProductImage generalImg = ProductImage.builder()
                .id(3L)
                .product(product)
                .variant(null)
                .color(null)
                .url("/images/shoe-general.jpg")
                .sortOrder(3)
                .build();

        List<ProductImage> images = List.of(varImg, colorImg, generalImg);

        String actualUrl = ProductImageResolver.resolveImageUrl(variantPink36, images, product.getThumbnailUrl());

        assertEquals("/images/shoe-pink-36-detail.jpg", actualUrl,
                "Khi biến thể có ảnh riêng, phải trả về đúng ảnh của biến thể đó. Giá trị thực tế: " + actualUrl);
    }

    @Test
    @DisplayName("(2) Chỉ có ảnh màu (không có ảnh riêng biến thể): Trả về ảnh gắn màu của biến thể")
    void testResolve_ColorImageWhenNoVariantSpecificImage() {
        // variantPink37 không có ảnh riêng, chỉ có ảnh của màu 'Hồng Phấn'
        ProductImage colorImg = ProductImage.builder()
                .id(2L)
                .product(product)
                .variant(null)
                .color("Hồng Phấn")
                .url("/images/shoe-color-pink.jpg")
                .sortOrder(2)
                .build();

        ProductImage generalImg = ProductImage.builder()
                .id(3L)
                .product(product)
                .variant(null)
                .color(null)
                .url("/images/shoe-general.jpg")
                .sortOrder(3)
                .build();

        List<ProductImage> images = List.of(colorImg, generalImg);

        String actualUrl = ProductImageResolver.resolveImageUrl(variantPink37, images, product.getThumbnailUrl());

        assertEquals("/images/shoe-color-pink.jpg", actualUrl,
                "Khi không có ảnh riêng cho biến thể nhưng có ảnh gắn theo màu, phải trả về ảnh màu. Giá trị thực tế: " + actualUrl);
    }

    @Test
    @DisplayName("(3) Chỉ có ảnh chung (không có ảnh biến thể và màu chưa có ảnh riêng): Trả về ảnh chung")
    void testResolve_GeneralImageWhenNoColorImage() {
        // variantCream36 màu 'Kem Sữa', thư viện chỉ có ảnh màu 'Hồng Phấn' và ảnh chung
        ProductImage pinkColorImg = ProductImage.builder()
                .id(2L)
                .product(product)
                .variant(null)
                .color("Hồng Phấn")
                .url("/images/shoe-color-pink.jpg")
                .sortOrder(1)
                .build();

        ProductImage generalImg = ProductImage.builder()
                .id(3L)
                .product(product)
                .variant(null)
                .color(null)
                .url("/images/shoe-general.jpg")
                .sortOrder(2)
                .build();

        List<ProductImage> images = List.of(pinkColorImg, generalImg);

        String actualUrl = ProductImageResolver.resolveImageUrl(variantCream36, images, product.getThumbnailUrl());

        assertEquals("/images/shoe-general.jpg", actualUrl,
                "Khi màu Kem Sữa chưa có ảnh riêng, phải fallback về ảnh chung của sản phẩm. Giá trị thực tế: " + actualUrl);
    }

    @Test
    @DisplayName("(4) Không có gì (không có ảnh riêng, không ảnh màu, không ảnh chung, không thumbnail): Trả về ảnh minh họa mặc định")
    void testResolve_DefaultPlaceholderWhenNothingAvailable() {
        List<ProductImage> emptyImages = Collections.emptyList();

        String actualUrl = ProductImageResolver.resolveImageUrl(variantPink36, emptyImages, null);

        assertEquals(ProductImageResolver.DEFAULT_PLACEHOLDER_IMAGE, actualUrl,
                "Khi không có bất kỳ hình ảnh nào, phải trả về đường dẫn ảnh minh họa mặc định. Giá trị thực tế: " + actualUrl);
    }

    @Test
    @DisplayName("(5) So khớp màu không phân biệt hoa thường và khoảng trắng thừa")
    void testResolve_CaseInsensitiveAndWhitespaceColorMatching() {
        // Ảnh lưu với chữ hoa và khoảng trắng thừa
        ProductImage colorImg = ProductImage.builder()
                .id(10L)
                .product(product)
                .variant(null)
                .color("   HỒNG PHẤN   ")
                .url("/images/shoe-color-pink-uppercase.jpg")
                .sortOrder(1)
                .build();

        List<ProductImage> images = List.of(colorImg);

        // Biến thể có màu chữ thường
        ProductVariant variantLower = ProductVariant.builder()
                .id(105L)
                .product(product)
                .color("hồng phấn")
                .size("36")
                .build();

        String actualUrl = ProductImageResolver.resolveImageUrl(variantLower, images, null);

        assertEquals("/images/shoe-color-pink-uppercase.jpg", actualUrl,
                "So khớp màu sắc phải chấp nhận khác biệt chữ hoa/thường và khoảng trắng thừa. Giá trị thực tế: " + actualUrl);
    }

    @Test
    @DisplayName("(6) Ảnh gắn đồng thời biến thể và màu bị từ chối với BadRequestException")
    void testAddProductImage_RejectBothVariantAndColor() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductImageRequestDTO invalidDto = ProductImageRequestDTO.builder()
                .productId(1L)
                .variantId(101L)
                .color("Hồng Phấn")
                .url("https://example.com/invalid.jpg")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productVariantService.addProductImage(invalidDto);
        }, "Phải ném BadRequestException khi gắn đồng thời variantId và color");

        assertTrue(exception.getMessage().contains("Một hình ảnh chỉ được gắn với một trong ba loại"),
                "Thông điệp ngoại lệ phải giải thích quy tắc 1 trong 3 loại gắn ảnh. Thực tế: " + exception.getMessage());
    }
}

