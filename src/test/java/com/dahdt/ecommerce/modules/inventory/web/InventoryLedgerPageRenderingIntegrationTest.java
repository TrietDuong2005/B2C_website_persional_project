package com.dahdt.ecommerce.modules.inventory.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Kiểm thử tích hợp dựng giao diện Thymeleaf trang Sổ cái giao dịch kho (InventoryLedgerPageRenderingIntegrationTest).
 * Ràng buộc:
 * - Tuân thủ quy tắc 17(e) của brief: Full Spring Boot context + MockMvc có Spring Security, xác thực tài khoản ADMIN.
 * - Khẳng định HTTP status 200 OK.
 * - Khẳng định trang HTML kết thúc trọn vẹn ở cuối template (chứa </html> và script footer adminlte.min.js),
 *   không bị lỗi SpEL hay cú pháp Thymeleaf ngắt giữa chừng.
 * - Khẳng định giao diện hiển thị đầy đủ các nhãn loại nghiệp vụ bằng tiếng Việt đồng bộ:
 *   'Nhập kho', 'Bán hàng', 'Hoàn kho (hủy đơn)', 'Chuyển kho đến', 'Chuyển kho đi', 'Điều chỉnh'.
 * - Dọn dẹp SecurityContextHolder sau mỗi test method.
 * - Mọi assert tuân thủ quy tắc 17: có thông điệp mô tả kèm giá trị thật.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Kiểm thử tích hợp dựng giao diện Thymeleaf: Sổ cái giao dịch kho (/admin/inventory/ledger)")
class InventoryLedgerPageRenderingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private UsernamePasswordAuthenticationToken adminAuth;

    @BeforeEach
    void setUp() {
        // Nạp tài khoản ADMIN từ CSDL mẫu của môi trường dev
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu để thực hiện kiểm thử")));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/inventory/ledger: Trả về HTTP 200, trang HTML đầy đủ đến cuối và hiển thị nhãn tiếng Việt")
    void testRenderLedgerPage_RendersSuccessfullyWithVietnameseLabels() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/inventory/ledger")
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // 1. Kiểm tra HTTP Status trả về là 200 OK
        assertEquals(200, httpStatus,
                "HTTP Status của GET /admin/inventory/ledger phải là 200 OK. Thực tế: " + httpStatus);

        // 2. Kiểm tra trang dựng đầy đủ đến cuối (chứa thẻ đóng </html> và script footer)
        assertTrue(html.contains("</html>"),
                "Trang sổ kho phải render đầy đủ đến thẻ đóng </html> ở cuối trang. Độ dài HTML thực tế: " + html.length());

        assertTrue(html.contains("adminlte.min.js"),
                "Trang sổ kho phải nạp script footer adminlte.min.js. Độ dài HTML thực tế: " + html.length());

        // 3. Khẳng định tiêu đề và các thành phần chính của trang sổ kho xuất hiện
        assertTrue(html.contains("Sổ cái Giao dịch Kho"),
                "Trang phải chứa tiêu đề 'Sổ cái Giao dịch Kho'. Độ dài HTML thực tế: " + html.length());

        assertTrue(html.contains("Nhật ký biến động kho"),
                "Trang phải chứa tiêu đề bảng 'Nhật ký biến động kho'. Độ dài HTML thực tế: " + html.length());

        // 4. Khẳng định ô lọc loại giao dịch chứa đầy đủ 6 nhãn tiếng Việt đồng bộ
        assertTrue(html.contains("Nhập kho"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Nhập kho'. Độ dài HTML: " + html.length());

        assertTrue(html.contains("Bán hàng"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Bán hàng'. Độ dài HTML: " + html.length());

        assertTrue(html.contains("Hoàn kho (hủy đơn)"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Hoàn kho (hủy đơn)'. Độ dài HTML: " + html.length());

        assertTrue(html.contains("Chuyển kho đến"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Chuyển kho đến'. Độ dài HTML: " + html.length());

        assertTrue(html.contains("Chuyển kho đi"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Chuyển kho đi'. Độ dài HTML: " + html.length());

        assertTrue(html.contains("Điều chỉnh"),
                "Bộ lọc phải chứa nhãn tiếng Việt 'Điều chỉnh'. Độ dài HTML: " + html.length());
    }
}

