package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Kiểm thử dựng trang Thymeleaf thật (AdminProductDetailPageRenderIntegrationTest)
 * Tuân thủ nghiêm ngặt Quy tắc 17e:
 * - Dùng @SpringBootTest + MockMvc có Spring Security và Thymeleaf Engine thật.
 * - Đặt tài khoản ADMIN vào SecurityContext.
 * - GET trang chi tiết / sửa sản phẩm (/admin/products/{id}).
 * - Khẳng định HTTP 200 và trang đầy đủ đến cuối (có ô chọn tệp, form multipart, footer).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional(readOnly = true)
@DisplayName("Kiểm thử dựng giao diện thật: Trang chi tiết và quản lý ảnh sản phẩm Admin (Quy tắc 17e)")
class AdminProductDetailPageRenderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User adminUser;
    private Product sampleProduct;
    private UsernamePasswordAuthenticationToken adminAuth;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu")));

        sampleProduct = productRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 sản phẩm trong CSDL mẫu"));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(adminAuth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Admin truy cập trang chi tiết sản phẩm: Dựng giao diện Thymeleaf hoàn chỉnh, có ô tải tệp và đến tận thẻ </html>")
    void testRenderProductDetailPageWithFileUpload() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MvcResult result = mockMvc.perform(get("/admin/products/" + sampleProduct.getId())
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status phải là 200 OK. Thực tế: " + httpStatus);

        // 1. Kiểm tra khối "Thêm ảnh vào bộ sưu tập"
        assertTrue(html.contains("Thêm ảnh vào bộ sưu tập"),
                "Trang phải chứa tiêu đề khối 'Thêm ảnh vào bộ sưu tập'");

        // 2. Kiểm tra form dùng multipart/form-data
        assertTrue(html.contains("enctype=\"multipart/form-data\""),
                "Form thêm ảnh phải có thuộc tính enctype='multipart/form-data' để hỗ trợ tải tệp");

        // 3. Kiểm tra ô chọn tệp từ máy
        assertTrue(html.contains("type=\"file\""),
                "Trang phải chứa ô chọn tệp type='file'");
        assertTrue(html.contains("id=\"imgFile\""),
                "Trang phải chứa phần tử id='imgFile'");
        assertTrue(html.contains("accept=\".jpg,.jpeg,.png,.webp\""),
                "Ô chọn tệp phải có thuộc tính accept='.jpg,.jpeg,.png,.webp'");

        // 4. Kiểm tra ô nhập URL vẫn được giữ nguyên
        assertTrue(html.contains("id=\"imgUrl\""),
                "Trang phải tiếp tục chứa ô nhập id='imgUrl' để nhập URL ảnh");

        // 5. Kiểm tra ô chọn 'Gắn với' với id mới 'imgTarget' và đầy đủ 3 nhóm tùy chọn theo NGÀY 12C
        assertTrue(html.contains("Gắn với"),
                "Trang phải chứa nhãn trường 'Gắn với'");
        assertTrue(html.contains("id=\"imgTarget\""),
                "Trang phải chứa trường chọn 'Gắn với' có id='imgTarget'");
        assertTrue(html.contains("Ảnh chung cho toàn sản phẩm"),
                "Trang phải chứa tùy chọn 'Ảnh chung cho toàn sản phẩm'");
        assertTrue(html.contains("label=\"Theo màu\""),
                "Trang phải chứa nhóm tùy chọn optgroup label='Theo màu'");
        assertTrue(html.contains("label=\"Biến thể cụ thể\""),
                "Trang phải chứa nhóm tùy chọn optgroup label='Biến thể cụ thể'");

        // 6. Kiểm tra các trường thứ tự hiển thị, ảnh đại diện
        assertTrue(html.contains("id=\"imgSortOrder\""),
                "Trang phải chứa trường thứ tự hiển thị id='imgSortOrder'");
        assertTrue(html.contains("id=\"imgIsThumbnail\""),
                "Trang phải chứa checkbox ảnh đại diện id='imgIsThumbnail'");

        // 7. Kiểm tra trang dựng đầy đủ đến tận cuối cùng (chân trang adminFooter và </html>)
        assertTrue(html.contains("app-footer") || html.contains("Retail Chain v2.0"),
                "Trang phải dựng hoàn chỉnh tới phần footer app-footer");
        assertTrue(html.contains("</html>"),
                "Trang phải dựng hoàn tất đến tận thẻ đóng </html>");
    }
}

