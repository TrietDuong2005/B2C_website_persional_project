package com.dahdt.ecommerce.modules.report.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.report.dto.DashboardReportDTO;
import com.dahdt.ecommerce.modules.report.dto.DashboardSummaryDTO;
import com.dahdt.ecommerce.modules.report.service.ReportService;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử MockMvc standaloneSetup cho AdminDashboardPageController.
 *
 * TUÂN THỦ QUY TẮC 17c:
 * - Đọc nội dung bằng getContentAsString(StandardCharsets.UTF_8).
 * - Cung cấp ArgumentResolver cho @AuthenticationPrincipal nếu có.
 * - Mọi assert đều có thông điệp mô tả chi tiết kèm giá trị thật.
 */
@DisplayName("Kiểm thử MockMvc standaloneSetup: AdminDashboardPageController")
class AdminDashboardPageControllerTest {

    private MockMvc mockMvc;
    private ReportService reportService;
    private StoreRepository storeRepository;
    private StoreScopeService storeScopeService;

    private User adminUser;
    private CustomUserDetails customUserDetails;
    private DashboardReportDTO mockReport;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        storeRepository = mock(StoreRepository.class);
        storeScopeService = mock(StoreScopeService.class);

        Role adminRole = Role.builder().id(1L).code("ADMIN").name("Quản trị viên").build();
        UserProfile profile = UserProfile.builder().fullName("Nguyễn Quản Trị").build();
        adminUser = User.builder()
                .id(1L)
                .email("admin@dahdt.com")
                .profile(profile)
                .roles(Set.of(adminRole))
                .build();
        customUserDetails = new CustomUserDetails(adminUser);

        when(storeScopeService.getCurrentUser()).thenReturn(adminUser);
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalRevenue(new BigDecimal("15000000.00"))
                .completedOrderCount(30)
                .averageOrderValue(new BigDecimal("500000.00"))
                .posRevenue(new BigDecimal("10000000.00"))
                .onlineRevenue(new BigDecimal("5000000.00"))
                .processingOrderCount(5)
                .expectedRevenue(new BigDecimal("2500000.00"))
                .cancelledOrderCount(2)
                .todayRevenue(new BigDecimal("1200000.00"))
                .todayCompletedOrderCount(3)
                .build();

        mockReport = DashboardReportDTO.builder()
                .filterStartDate(LocalDate.now().minusDays(29))
                .filterEndDate(LocalDate.now())
                .filterStoreId(null)
                .filterStoreName("Toàn hệ thống")
                .storeManager(false)
                .summary(summary)
                .dailyRevenues(Collections.emptyList())
                .storeRevenues(Collections.emptyList())
                .paymentMethodRevenues(Collections.emptyList())
                .orderStatusCounts(Collections.emptyList())
                .topSellingProducts(Collections.emptyList())
                .lowStockAlerts(Collections.emptyList())
                .pendingOnlineOrders(Collections.emptyList())
                .lowStockThreshold(5)
                .build();

        when(reportService.getDashboardData(any(), any(), any())).thenReturn(mockReport);
        when(storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE)).thenReturn(Collections.emptyList());

        AdminDashboardPageController controller = new AdminDashboardPageController(
                reportService, storeRepository, storeScopeService);

        // Quy tắc 17c: Tự dựng ArgumentResolver cho @AuthenticationPrincipal
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().isAssignableFrom(CustomUserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return customUserDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("GET /admin: Trả về HTTP 200, view 'admin/dashboard' và đầy đủ thuộc tính trong Model")
    void testGetAdminDashboard_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("report"))
                .andExpect(model().attributeExists("startDate"))
                .andExpect(model().attributeExists("endDate"))
                .andExpect(model().attributeExists("currentUserFullName"))
                .andExpect(model().attributeExists("currentUserEmail"))
                .andExpect(model().attributeExists("currentUserRoles"))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        assertEquals(200, httpStatus,
                "HTTP Status của GET /admin phải là 200 OK. Thực tế: " + httpStatus);

        DashboardReportDTO modelReport = (DashboardReportDTO) result.getModelAndView().getModel().get("report");
        assertNotNull(modelReport, "Model attribute 'report' không được null");
        assertEquals(0, modelReport.getSummary().getTotalRevenue().compareTo(new BigDecimal("15000000.00")),
                "Doanh thu tổng trong Model phải khớp với mock: 15.000.000 đ");
    }

    @Test
    @DisplayName("GET /admin/dashboard: URL thay thế trả về cùng view 'admin/dashboard'")
    void testGetAdminDashboard_AlternateUrl() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("report"));
    }

    @Test
    @DisplayName("Lọc với startDate > endDate: Hiển thị thông báo lỗi thân thiện, không sập trang, lấy dữ liệu mặc định")
    void testGetAdminDashboard_StartDateAfterEndDate_FriendlyError() throws Exception {
        LocalDate start = LocalDate.of(2026, 11, 20);
        LocalDate end = LocalDate.of(2026, 11, 10);

        MvcResult result = mockMvc.perform(get("/admin")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("errorMessage"))
                .andReturn();

        String errorMessage = (String) result.getModelAndView().getModel().get("errorMessage");
        assertNotNull(errorMessage, "Model attribute 'errorMessage' không được null khi startDate > endDate");
        assertTrue(errorMessage.contains("không được lớn hơn"),
                "Nội dung errorMessage phải chứa cảnh báo ngày hợp lệ. Thực tế: " + errorMessage);

        // Đảm bảo reportService vẫn được gọi với giá trị an toàn (null) để dựng trang
        verify(reportService, atLeastOnce()).getDashboardData(isNull(), isNull(), any());
    }

    @Test
    @DisplayName("STORE_MANAGER cố tình truyền storeId khác: Ném AccessDeniedException kích hoạt 403")
    void testGetAdminDashboard_StoreManager_AccessDenied() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(true);
        when(storeScopeService.getAssignedStoreId()).thenReturn(1L);
        doThrow(new AccessDeniedException("Bạn chỉ có quyền truy cập chi nhánh được phân công!"))
                .when(storeScopeService).validateStoreAccess(2L);

        // Khi STORE_MANAGER của store 1 cố tình truyền storeId=2 trên URL
        assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/admin").param("storeId", "2"));
        }, "Phải ném Exception (AccessDeniedException) khi STORE_MANAGER cố tình đổi storeId khác");
    }
}

