package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.catalog.entity.*;
import com.dahdt.ecommerce.modules.catalog.repository.BrandRepository;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Kiểm thử DỰNG TRANG THẬT bằng Thymeleaf Engine (NGÀY 12C - PHẦN 4, Tuân thủ Quy tắc 17e).
 * Kịch bản:
 * - Tạo 1 sản phẩm có 2 màu ('Hồng Phấn' và 'Kem Sữa'), mỗi màu 2 size ('36' và '37') -> 4 biến thể.
 * - Thư viện ảnh có:
 *   + 1 ảnh gắn biến thể cụ thể (SKU SLIP-HP-36)
 *   + 1 ảnh gắn màu ('Hồng Phấn')
 *   + 1 ảnh chung cho sản phẩm
 * - Khẳng định:
 *   1. Trang Admin /admin/products/{id}: HTTP 200, dựng trang đến tận thẻ </html>, nhãn 'Chung', 'Màu: Hồng Phấn',
 *      'Biến thể: SLIP-HP-36', các nhóm optgroup 'Theo màu' và 'Biến thể cụ thể'.
 *   2. Trang Shop /shop/products/{slug}: HTTP 200, dựng trang đến tận thẻ </html>, có gallery ảnh và nút chọn màu/size.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Kiểm thử dựng trang thật: Trang Admin và Shop với sản phẩm 2 màu x 2 size + 3 loại ảnh (Quy tắc 17e)")
class ProductDetailPageColorRenderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    private User adminUser;
    private UsernamePasswordAuthenticationToken adminAuth;
    private Product demoProduct;
    private ProductVariant varPink36;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu")));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        Category category = categoryRepository.findAll().stream().findFirst().orElse(null);
        Brand brand = brandRepository.findAll().stream().findFirst().orElse(null);

        String uniqueSuffix = String.valueOf(System.currentTimeMillis());

        // Tạo sản phẩm demo: Dép Slipper Bánh Mì Nữ Đế Đúc
        demoProduct = Product.builder()
                .name("Dép Slipper Bánh Mì Nữ Đế Đúc Test")
                .slug("dep-slipper-banh-mi-nu-de-duc-test-" + uniqueSuffix)
                .category(category)
                .brand(brand)
                .description("Dép bánh mì đế đúc êm ái phong cách Hàn Quốc.")
                .thumbnailUrl("/images/demo-thumb.jpg")
                .status(ProductStatus.ACTIVE)
                .build();
        demoProduct = productRepository.save(demoProduct);

        // Tạo 4 biến thể: 2 màu x 2 size
        varPink36 = ProductVariant.builder()
                .product(demoProduct)
                .sku("SLIP-HP-36-" + uniqueSuffix)
                .color("Hồng Phấn")
                .size("36")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        ProductVariant varPink37 = ProductVariant.builder()
                .product(demoProduct)
                .sku("SLIP-HP-37-" + uniqueSuffix)
                .color("Hồng Phấn")
                .size("37")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        ProductVariant varCream36 = ProductVariant.builder()
                .product(demoProduct)
                .sku("SLIP-KS-36-" + uniqueSuffix)
                .color("Kem Sữa")
                .size("36")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        ProductVariant varCream37 = ProductVariant.builder()
                .product(demoProduct)
                .sku("SLIP-KS-37-" + uniqueSuffix)
                .color("Kem Sữa")
                .size("37")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        productVariantRepository.saveAll(List.of(varPink36, varPink37, varCream36, varCream37));

        // Tạo 3 loại ảnh:
        // (1) Ảnh gắn biến thể cụ thể: cho varPink36
        ProductImage imgVariant = ProductImage.builder()
                .product(demoProduct)
                .variant(varPink36)
                .color(null)
                .url("/images/shoe-pink-36-var.jpg")
                .sortOrder(1)
                .isThumbnail(false)
                .build();

        // (2) Ảnh gắn theo màu: cho màu 'Hồng Phấn'
        ProductImage imgColor = ProductImage.builder()
                .product(demoProduct)
                .variant(null)
                .color("Hồng Phấn")
                .url("/images/shoe-pink-color.jpg")
                .sortOrder(2)
                .isThumbnail(false)
                .build();

        // (3) Ảnh chung cho toàn sản phẩm
        ProductImage imgGeneral = ProductImage.builder()
                .product(demoProduct)
                .variant(null)
                .color(null)
                .url("/images/shoe-general.jpg")
                .sortOrder(3)
                .isThumbnail(true)
                .build();

        productImageRepository.saveAll(List.of(imgVariant, imgColor, imgGeneral));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Admin: Dựng thật trang sửa sản phẩm /admin/products/{id} có đủ nhãn 'Biến thể', 'Màu', 'Chung' và optgroup")
    void testRenderAdminProductDetailPageWithColorOptions() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MvcResult result = mockMvc.perform(get("/admin/products/" + demoProduct.getId())
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status trang Admin phải là 200 OK. Thực tế: " + httpStatus);

        // 1. Trang hoàn chỉnh đến tận cuối
        assertTrue(html.contains("</html>"), "Trang admin phải dựng hoàn chỉnh đến tận thẻ đóng </html>");

        // 2. Ô dropdown 'Gắn với' có nhóm 'Theo màu' và nhóm 'Biến thể cụ thể'
        assertTrue(html.contains("Gắn với"), "Form phải có nhãn 'Gắn với'");
        assertTrue(html.contains("Ảnh chung cho toàn sản phẩm"), "Dropdown phải có tùy chọn 'Ảnh chung cho toàn sản phẩm'");
        assertTrue(html.contains("label=\"Theo màu\""), "Dropdown phải có <optgroup label='Theo màu'>");
        assertTrue(html.contains("label=\"Biến thể cụ thể\""), "Dropdown phải có <optgroup label='Biến thể cụ thể'>");
        assertTrue(html.contains("Màu: Hồng Phấn"), "Dropdown phải chứa tùy chọn 'Màu: Hồng Phấn'");
        assertTrue(html.contains("Màu: Kem Sữa"), "Dropdown phải chứa tùy chọn 'Màu: Kem Sữa'");

        // 3. Bộ sưu tập ảnh hiển thị rõ ràng 3 loại nhãn: 'Biến thể: <SKU>', 'Màu: <tên màu>', 'Chung'
        assertTrue(html.contains("Biến thể: " + varPink36.getSku()),
                "Bộ sưu tập phải hiển thị nhãn 'Biến thể: " + varPink36.getSku() + "'");
        assertTrue(html.contains("Màu: Hồng Phấn"),
                "Bộ sưu tập phải hiển thị nhãn 'Màu: Hồng Phấn'");
        assertTrue(html.contains("Chung"),
                "Bộ sưu tập phải hiển thị nhãn 'Chung'");
    }

    @Test
    @DisplayName("Shop: Dựng thật trang chi tiết sản phẩm /shop/products/{slug} có đủ gallery, màu và size")
    void testRenderShopProductDetailPageWithColorsAndGallery() throws Exception {
        MvcResult result = mockMvc.perform(get("/shop/products/" + demoProduct.getSlug()))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status trang Shop phải là 200 OK. Thực tế: " + httpStatus);

        // 1. Trang hoàn chỉnh đến tận cuối
        assertTrue(html.contains("</html>"), "Trang shop phải dựng hoàn chỉnh đến tận thẻ đóng </html>");

        // 2. Có ảnh chính và gallery
        assertTrue(html.contains("id=\"main-product-image\""), "Trang phải có thẻ ảnh chính id='main-product-image'");
        assertTrue(html.contains("id=\"gallery-thumbnails\""), "Trang phải có container gallery thumbnails id='gallery-thumbnails'");

        // 3. Có đầy đủ 2 màu và các kích cỡ
        assertTrue(html.contains("Hồng Phấn"), "Trang phải hiển thị lựa chọn màu 'Hồng Phấn'");
        assertTrue(html.contains("Kem Sữa"), "Trang phải hiển thị lựa chọn màu 'Kem Sữa'");
        assertTrue(html.contains("36"), "Trang phải hiển thị lựa chọn size '36'");
        assertTrue(html.contains("37"), "Trang phải hiển thị lựa chọn size '37'");
    }
}

