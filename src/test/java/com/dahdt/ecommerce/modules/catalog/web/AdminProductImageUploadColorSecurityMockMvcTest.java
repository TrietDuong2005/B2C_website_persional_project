package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử MockMvc Multipart thêm ảnh theo màu với Spring Security (NGÀY 12C - PHẦN 4).
 * Tuân thủ quy tắc 17:
 * - Đặt người dùng ADMIN đăng nhập đúng cách qua SecurityContext.
 * - Gửi tệp multipart qua mockMvc.perform(multipart(...)).
 * - Đọc kết quả bằng UTF-8: getContentAsString(StandardCharsets.UTF_8).
 * - Mọi assert có thông điệp mô tả kèm giá trị thật.
 * - Tự động rollback sau khi test kết thúc nhờ @Transactional.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Kiểm thử MockMvc multipart thêm ảnh theo màu với người dùng ADMIN đăng nhập (Quy tắc 17)")
class AdminProductImageUploadColorSecurityMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    private User adminUser;
    private UsernamePasswordAuthenticationToken adminAuth;
    private Product testProduct;

    // Chữ ký magic bytes hợp lệ cho tệp JPEG (FF D8 FF E0 ...)
    private static final byte[] VALID_JPEG_BYTES = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
    };

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

        testProduct = Product.builder()
                .name("Dép Slipper Test Upload Màu")
                .slug("dep-slipper-test-upload-mau-" + System.currentTimeMillis())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        // Tạo biến thể có màu 'Hồng Phấn'
        ProductVariant variant = ProductVariant.builder()
                .product(testProduct)
                .sku("SLIP-UPLOAD-HP-" + System.currentTimeMillis())
                .color("Hồng Phấn")
                .size("36")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();
        productVariantRepository.save(variant);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Admin gửi multipart tải ảnh lên gắn theo màu (kèm CSRF và vai trò ADMIN): Lưu thành công vào CSDL với color='Hồng Phấn' và variant=null")
    void testUploadMultipartImageByColor() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "dep-hong-phan.jpg", "image/jpeg", VALID_JPEG_BYTES
        );

        MvcResult result = mockMvc.perform(multipart("/admin/products/{id}/images/save", testProduct.getId())
                        .file(file)
                        .param("target", "color:Hồng Phấn")
                        .param("sortOrder", "1")
                        .param("isThumbnail", "false")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/" + testProduct.getId()))
                .andExpect(flash().attribute("successMessage", "Thêm ảnh thành công!"))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        assertEquals(302, httpStatus, "HTTP Status phải là 302 Redirection sau khi tải ảnh. Thực tế: " + httpStatus);

        // Đọc response dạng UTF-8
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNotNull(content, "Nội dung response không được null");

        // Kiểm tra trong CSDL: ảnh đã được lưu với cột color = 'Hồng Phấn' và variant = null
        List<ProductImage> savedImages = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(testProduct.getId());
        assertEquals(1, savedImages.size(),
                "Phải có đúng 1 ảnh được lưu vào CSDL cho sản phẩm này. Thực tế: " + savedImages.size());

        ProductImage savedImage = savedImages.get(0);
        assertEquals("Hồng Phấn", savedImage.getColor(),
                "Cột color của ảnh phải là 'Hồng Phấn'. Thực tế: " + savedImage.getColor());
        assertNull(savedImage.getVariant(),
                "Ảnh gắn theo màu thì variant_id phải là null. Thực tế: " + savedImage.getVariant());
        assertTrue(savedImage.getUrl().startsWith("/uploads/products/"),
                "Đường dẫn ảnh phải bắt đầu bằng '/uploads/products/'. Thực tế: " + savedImage.getUrl());
    }

    @Test
    @DisplayName("Bảo mật âm tính (1): Gửi multipart khi đăng nhập ADMIN nhưng THIẾU CSRF token -> Bị chặn với HTTP 403 Forbidden")
    void testUploadMultipartImageWithoutCsrfForbidden() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "dep-hong-phan.jpg", "image/jpeg", VALID_JPEG_BYTES
        );

        // Gửi request CÓ xác thực ADMIN nhưng KHÔNG có .with(csrf())
        MvcResult result = mockMvc.perform(multipart("/admin/products/{id}/images/save", testProduct.getId())
                        .file(file)
                        .param("target", "color:Hồng Phấn")
                        .param("sortOrder", "1")
                        .with(authentication(adminAuth)))
                .andExpect(status().isForbidden())
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        assertEquals(403, httpStatus,
                "Khi thiếu token CSRF, Spring Security phải từ chối với HTTP 403 Forbidden. Thực tế: " + httpStatus);

        // Đảm bảo không có hình ảnh nào được lưu vào CSDL
        List<ProductImage> savedImages = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(testProduct.getId());
        assertEquals(0, savedImages.size(),
                "Không có ảnh nào được lưu khi bị chặn CSRF. Thực tế số lượng: " + savedImages.size());
    }

    @Test
    @DisplayName("Bảo mật âm tính (2): Người dùng có vai trò CUSTOMER gửi multipart vào /admin -> Bị chặn với HTTP 403 Forbidden")
    void testUploadMultipartImageNonAdminForbidden() throws Exception {
        Role customerRole = Role.builder().code("CUSTOMER").name("Khách hàng").build();
        User customerUser = User.builder()
                .id(999L)
                .email("customer_security_test@dahdt.com")
                .status("active")
                .roles(Set.of(customerRole))
                .build();
        CustomUserDetails customerDetails = new CustomUserDetails(customerUser);
        UsernamePasswordAuthenticationToken customerAuth = new UsernamePasswordAuthenticationToken(
                customerDetails, null, customerDetails.getAuthorities());

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "dep-hong-phan.jpg", "image/jpeg", VALID_JPEG_BYTES
        );

        // Gửi request CÓ .with(csrf()) nhưng vai trò CUSTOMER không được phép vào /admin/**
        MvcResult result = mockMvc.perform(multipart("/admin/products/{id}/images/save", testProduct.getId())
                        .file(file)
                        .param("target", "color:Hồng Phấn")
                        .param("sortOrder", "1")
                        .with(authentication(customerAuth))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        assertEquals(403, httpStatus,
                "Người dùng không có vai trò ADMIN/STORE_MANAGER phải bị chặn với HTTP 403 Forbidden. Thực tế: " + httpStatus);

        // Đảm bảo không có hình ảnh nào được lưu vào CSDL
        List<ProductImage> savedImages = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(testProduct.getId());
        assertEquals(0, savedImages.size(),
                "Không có ảnh nào được lưu khi bị chặn phân quyền. Thực tế số lượng: " + savedImages.size());
    }
}

