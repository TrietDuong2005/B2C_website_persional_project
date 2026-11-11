package com.dahdt.ecommerce.modules.report.web;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
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
 * ============================================================================
 * KIỂM THỬ DỰNG GIAO DIỆN TEMPLATE THẬT (AdminDashboardPageIntegrationTest)
 * ============================================================================
 * Tuân thủ nghiêm ngặt Quy tắc 17e:
 *  - Dùng @SpringBootTest + @AutoConfigureMockMvc có Spring Security và Thymeleaf Engine thật.
 *  - Dựng trang Dashboard thật cho cả Quản trị viên (ADMIN) và Quản lý cửa hàng (STORE_MANAGER).
 *  - Kiểm tra các tình huống bộ lọc:
 *      1. Mặc định không truyền tham số (30 ngày gần nhất).
 *      2. Khoảng ngày rộng (2020-01-01 đến 2030-12-31).
 *      3. Lọc theo một cửa hàng cụ thể (storeId).
 *      4. Khoảng ngày rỗng tuyệt đối không có đơn nào (1990-01-01 đến 1990-01-02).
 *  - Khẳng định HTTP 200 và nội dung trang ĐẦY ĐỦ ĐẾN CUỐI TEMPLATE (chứa chuỗi giải thích cách
 *    tính doanh thu và adminFooter), phát hiện triệt để thuộc tính không tồn tại trong DTO
 *    hoặc giá trị null không an toàn trong biểu thức Thymeleaf/SpEL.
 * ============================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional(readOnly = true)
@DisplayName("Kiểm thử dựng giao diện thật: Dashboard Admin & Store Manager (Quy tắc 17e)")
class AdminDashboardPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    private User adminUser;
    private User managerUser;
    private Store sampleStore;

    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken managerAuth;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu")));

        managerUser = userRepository.findByEmail("manager@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "STORE_MANAGER".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElse(adminUser));

        sampleStore = storeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 chi nhánh trong CSDL mẫu"));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());

        CustomUserDetails managerDetails = new CustomUserDetails(managerUser);
        managerAuth = new UsernamePasswordAuthenticationToken(managerDetails, null, managerDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(adminAuth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Admin truy cập Dashboard mặc định (30 ngày gần nhất): Dựng hoàn chỉnh đến tận chân trang")
    void testRenderDashboard_Admin_DefaultFilter() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MvcResult result = mockMvc.perform(get("/admin")
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status phải là 200 OK. Thực tế: " + httpStatus);

        // Khẳng định các thành phần tiêu đề và bộ lọc
        assertTrue(html.contains("Dashboard Thống kê Doanh số"),
                "Trang phải chứa tiêu đề chính 'Dashboard Thống kê Doanh số'. Độ dài trang: " + html.length());
        assertTrue(html.contains("Bộ lọc thống kê"),
                "Trang phải chứa khối 'Bộ lọc thống kê'. Độ dài trang: " + html.length());

        // Khẳng định trang dựng ĐẦY ĐỦ ĐẾN CUỐI TEMPLATE (Quy tắc 17e)
        assertTrue(html.contains("Quy tắc & Định nghĩa số liệu báo cáo doanh thu:"),
                "Trang phải dựng đầy đủ đến phần ghi chú giải thích cách tính doanh thu ở cuối trang. Độ dài: " + html.length());
        assertTrue(html.contains("DOANH THU THỰC TẾ:"),
                "Trang phải chứa định nghĩa 'DOANH THU THỰC TẾ:' ở cuối trang. Độ dài: " + html.length());
        assertTrue(html.contains("adminlte.min.js"),
                "Trang phải chứa adminFooter nạp script adminlte.min.js. Độ dài: " + html.length());
    }

    @Test
    @DisplayName("Admin truy cập Dashboard với khoảng ngày rộng: Dựng hoàn chỉnh mọi bảng dữ liệu")
    void testRenderDashboard_Admin_WideDateRange() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MvcResult result = mockMvc.perform(get("/admin")
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2030-12-31")
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status khoảng ngày rộng phải là 200 OK. Thực tế: " + httpStatus);
        assertTrue(html.contains("Bảng doanh thu theo ngày trong kỳ"),
                "Trang phải chứa bảng doanh thu theo ngày. Độ dài: " + html.length());
        assertTrue(html.contains("Bảng doanh thu và số đơn theo chi nhánh"),
                "Admin phải thấy bảng doanh thu theo chi nhánh. Độ dài: " + html.length());
        assertTrue(html.contains("Top 10 sản phẩm bán chạy nhất trong kỳ"),
                "Trang phải chứa bảng top 10 sản phẩm bán chạy. Độ dài: " + html.length());
        assertTrue(html.contains("Cảnh báo tồn kho thấp"),
                "Trang phải chứa bảng cảnh báo tồn kho thấp. Độ dài: " + html.length());
        assertTrue(html.contains("Quy tắc & Định nghĩa số liệu báo cáo doanh thu:"),
                "Trang phải dựng đầy đủ đến cuối trang. Độ dài: " + html.length());
    }

    @Test
    @DisplayName("Admin truy cập Dashboard lọc theo một chi nhánh cụ thể: Dựng hoàn chỉnh")
    void testRenderDashboard_Admin_SpecificStore() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        MvcResult result = mockMvc.perform(get("/admin")
                        .param("storeId", sampleStore.getId().toString())
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status lọc theo storeId phải là 200 OK. Thực tế: " + httpStatus);
        assertTrue(html.contains("Quy tắc & Định nghĩa số liệu báo cáo doanh thu:"),
                "Trang phải dựng đầy đủ đến cuối trang khi lọc theo cửa hàng cụ thể. Độ dài: " + html.length());
    }

    @Test
    @DisplayName("Dashboard với khoảng ngày rỗng (không có đơn): Dựng an toàn, hiển thị trạng thái rỗng thân thiện")
    void testRenderDashboard_EmptyDateRange() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        // Khoảng ngày năm 1990 không thể có đơn nào
        MvcResult result = mockMvc.perform(get("/admin")
                        .param("startDate", "1990-01-01")
                        .param("endDate", "1990-01-02")
                        .with(authentication(adminAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "HTTP Status khoảng ngày rỗng phải là 200 OK. Thực tế: " + httpStatus);
        assertTrue(html.contains("0 đ"),
                "Khoảng ngày rỗng phải hiển thị doanh thu 0 đ an toàn. Độ dài: " + html.length());
        assertTrue(html.contains("Quy tắc & Định nghĩa số liệu báo cáo doanh thu:"),
                "Khoảng ngày rỗng vẫn phải dựng đầy đủ đến phần ghi chú cuối trang. Độ dài: " + html.length());
        assertTrue(html.contains("adminlte.min.js"),
                "Khoảng ngày rỗng vẫn phải chứa footer đầy đủ. Độ dài: " + html.length());
    }

    @Test
    @DisplayName("Quản lý cửa hàng (STORE_MANAGER) truy cập Dashboard: Chỉ thấy dữ liệu chi nhánh mình, không lộ cửa hàng khác")
    void testRenderDashboard_StoreManager_ScopedAccess() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(managerAuth);

        MvcResult result = mockMvc.perform(get("/admin")
                        .with(authentication(managerAuth)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(200, httpStatus, "STORE_MANAGER truy cập /admin phải trả về 200 OK. Thực tế: " + httpStatus);

        // STORE_MANAGER KHÔNG được thấy bảng so sánh doanh thu các chi nhánh khác
        assertTrue(!html.contains("Bảng doanh thu và số đơn theo chi nhánh (Chỉ Admin)"),
                "STORE_MANAGER tuyệt đối không được nhìn thấy bảng doanh thu các chi nhánh khác");

        // STORE_MANAGER KHÔNG được thấy danh sách đơn hàng trực tuyến chờ xác nhận (vì thuộc kho tổng)
        assertTrue(!html.contains("Đơn trực tuyến chờ xác nhận mới nhất"),
                "STORE_MANAGER không được thấy khối đơn trực tuyến chờ xác nhận của kho tổng");

        // Nhưng vẫn dựng đầy đủ đến cuối trang
        assertTrue(html.contains("Quy tắc & Định nghĩa số liệu báo cáo doanh thu:"),
                "Trang của STORE_MANAGER phải dựng đầy đủ đến phần ghi chú cuối trang. Độ dài: " + html.length());
        assertTrue(html.contains("adminlte.min.js"),
                "Trang của STORE_MANAGER phải chứa footer hoàn chỉnh. Độ dài: " + html.length());
    }
}

