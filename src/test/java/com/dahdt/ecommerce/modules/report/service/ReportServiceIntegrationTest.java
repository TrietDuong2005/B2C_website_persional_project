package com.dahdt.ecommerce.modules.report.service;

import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.report.dto.*;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * KIỂM THỬ TÍCH HỢP: ReportServiceIntegrationTest (@SpringBootTest, chỉ ĐỌC)
 * ============================================================================
 * Chạy trên CSDL SQL Server thật với khoảng ngày rộng:
 *  1. Khẳng định không ném ngoại lệ (ClassCastException đã được khắc phục hoàn toàn).
 *  2. Mọi số liệu doanh thu và số lượng đơn đều không âm (>= 0).
 *  3. Tổng doanh thu theo cửa hàng = Tổng doanh thu theo ngày = Doanh thu tổng (so sánh bằng compareTo).
 *  4. Đối chiếu trực tiếp thẻ chỉ số với các đơn hàng COMPLETED đọc từ OrderRepository.
 *  5. Kiểm tra khoảng ngày rỗng: mọi giá trị bằng 0, không null.
 *  6. Tuân thủ quy tắc 17: Đặt Authentication admin trong SecurityContext và dọn sạch trong @AfterEach;
 *     mọi assert đều có thông điệp mô tả kèm giá trị thật.
 * ============================================================================
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional(readOnly = true)
@DisplayName("Kiểm thử tích hợp: Nghiệp vụ Báo cáo thống kê ReportService trên CSDL thật")
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        // Lấy tài khoản admin từ CSDL mẫu
        adminUser = userRepository.findByEmail("admin@dahdt.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode())))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Yêu cầu ít nhất 1 tài khoản ADMIN trong CSDL mẫu")));

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        // Quy tắc 17b: Luôn dọn sạch SecurityContextHolder sau kiểm thử
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Kiểm thử tính toán toàn vẹn dữ liệu: Tổng doanh thu theo cửa hàng = theo ngày = tổng số liệu, mọi số >= 0")
    void testDashboardData_IntegrityAndConsistency() {
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);

        DashboardReportDTO report = assertDoesNotThrow(
                () -> reportService.getDashboardData(startDate, endDate, null),
                "Gọi reportService.getDashboardData với khoảng ngày rộng không được ném ngoại lệ");

        assertNotNull(report, "DashboardReportDTO không được null");
        assertNotNull(report.getSummary(), "DashboardSummaryDTO không được null");

        BigDecimal totalRevenue = report.getSummary().getTotalRevenue();
        BigDecimal posRevenue = report.getSummary().getPosRevenue();
        BigDecimal onlineRevenue = report.getSummary().getOnlineRevenue();
        BigDecimal averageOrderValue = report.getSummary().getAverageOrderValue();
        BigDecimal expectedRevenue = report.getSummary().getExpectedRevenue();
        BigDecimal todayRevenue = report.getSummary().getTodayRevenue();

        // 1. Khẳng định mọi số liệu tiền tệ và số lượng đơn đều không âm
        assertTrue(totalRevenue.compareTo(BigDecimal.ZERO) >= 0,
                "Tổng doanh thu không được âm. Thực tế: " + totalRevenue);
        assertTrue(posRevenue.compareTo(BigDecimal.ZERO) >= 0,
                "Doanh thu POS không được âm. Thực tế: " + posRevenue);
        assertTrue(onlineRevenue.compareTo(BigDecimal.ZERO) >= 0,
                "Doanh thu Online không được âm. Thực tế: " + onlineRevenue);
        assertTrue(averageOrderValue.compareTo(BigDecimal.ZERO) >= 0,
                "Giá trị TB mỗi đơn không được âm. Thực tế: " + averageOrderValue);
        assertTrue(expectedRevenue.compareTo(BigDecimal.ZERO) >= 0,
                "Doanh thu dự kiến không được âm. Thực tế: " + expectedRevenue);
        assertTrue(todayRevenue.compareTo(BigDecimal.ZERO) >= 0,
                "Doanh thu hôm nay không được âm. Thực tế: " + todayRevenue);

        assertTrue(report.getSummary().getCompletedOrderCount() >= 0, "Số đơn hoàn thành không được âm");
        assertTrue(report.getSummary().getProcessingOrderCount() >= 0, "Số đơn đang xử lý không được âm");
        assertTrue(report.getSummary().getCancelledOrderCount() >= 0, "Số đơn hủy không được âm");
        assertTrue(report.getSummary().getTodayCompletedOrderCount() >= 0, "Số đơn hôm nay không được âm");

        // 2. Doanh thu POS + Doanh thu Online phải bằng Doanh thu tổng (so bằng compareTo)
        BigDecimal sumPosAndOnline = posRevenue.add(onlineRevenue);
        assertEquals(0, totalRevenue.compareTo(sumPosAndOnline),
                "Tổng doanh thu (" + totalRevenue + ") phải bằng tổng POS + Online (" + sumPosAndOnline + ")");

        // 3. Nếu CSDL đã có đơn hàng hoàn tất: Đối chiếu tính nhất quán giữa các bảng tổng hợp
        Assumptions.assumeTrue(report.getSummary().getCompletedOrderCount() > 0,
                "CSDL chưa có đơn hàng hoàn thành nào trong khoảng ngày test, bỏ qua đối chiếu số liệu chi tiết");

        // Tổng doanh thu từ bảng theo ngày (dailyRevenues) phải bằng Doanh thu tổng
        BigDecimal sumDailyRevenue = BigDecimal.ZERO;
        long sumDailyOrders = 0L;
        for (DailyRevenueDTO d : report.getDailyRevenues()) {
            assertTrue(d.getRevenue().compareTo(BigDecimal.ZERO) >= 0, "Doanh thu ngày không được âm: " + d.getDate());
            assertTrue(d.getOrderCount() >= 0, "Số đơn ngày không được âm: " + d.getDate());
            sumDailyRevenue = sumDailyRevenue.add(d.getRevenue());
            sumDailyOrders += d.getOrderCount();
        }

        assertEquals(0, totalRevenue.compareTo(sumDailyRevenue),
                "Tổng doanh thu theo ngày (" + sumDailyRevenue + ") phải bằng Tổng doanh thu (" + totalRevenue + ")");
        assertEquals(report.getSummary().getCompletedOrderCount(), sumDailyOrders,
                "Tổng số đơn theo ngày (" + sumDailyOrders + ") phải bằng Số đơn hoàn thành (" + report.getSummary().getCompletedOrderCount() + ")");

        // Tổng doanh thu từ bảng theo cửa hàng (storeRevenues) phải bằng Doanh thu tổng
        BigDecimal sumStoreRevenue = BigDecimal.ZERO;
        long sumStoreOrders = 0L;
        for (StoreRevenueDTO s : report.getStoreRevenues()) {
            assertTrue(s.getRevenue().compareTo(BigDecimal.ZERO) >= 0, "Doanh thu cửa hàng không được âm: " + s.getStoreCode());
            assertTrue(s.getOrderCount() >= 0, "Số đơn cửa hàng không được âm: " + s.getStoreCode());
            sumStoreRevenue = sumStoreRevenue.add(s.getRevenue());
            sumStoreOrders += s.getOrderCount();
        }

        assertEquals(0, totalRevenue.compareTo(sumStoreRevenue),
                "Tổng doanh thu theo cửa hàng (" + sumStoreRevenue + ") phải bằng Tổng doanh thu (" + totalRevenue + ")");
        assertEquals(report.getSummary().getCompletedOrderCount(), sumStoreOrders,
                "Tổng số đơn theo cửa hàng (" + sumStoreOrders + ") phải bằng Số đơn hoàn thành (" + report.getSummary().getCompletedOrderCount() + ")");
    }

    @Test
    @DisplayName("Thẻ chỉ số (a) Khoảng ngày có đơn: Đối chiếu trực tiếp doanh thu tổng với OrderRepository")
    void testSummaryMetrics_WideRange_MatchesOrderRepositoryDirectly() {
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        DashboardReportDTO report = assertDoesNotThrow(
                () -> reportService.getDashboardData(startDate, endDate, null),
                "Gọi reportService.getDashboardData không được ném ngoại lệ");

        // Đọc trực tiếp từ OrderRepository để đối chiếu độc lập
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal expectedTotalRevenue = BigDecimal.ZERO;
        long expectedCompletedCount = 0L;
        BigDecimal expectedPosRevenue = BigDecimal.ZERO;
        BigDecimal expectedOnlineRevenue = BigDecimal.ZERO;

        for (Order o : allOrders) {
            if (o.getStatus() == OrderStatus.COMPLETED && o.getCompletedAt() != null) {
                if (!o.getCompletedAt().isBefore(startDateTime) && !o.getCompletedAt().isAfter(endDateTime)) {
                    expectedCompletedCount++;
                    BigDecimal amt = MoneyUtil.roundMoney(o.getTotalAmount());
                    expectedTotalRevenue = expectedTotalRevenue.add(amt);
                    if (o.getChannel() == OrderChannel.POS) {
                        expectedPosRevenue = expectedPosRevenue.add(amt);
                    } else if (o.getChannel() == OrderChannel.ONLINE) {
                        expectedOnlineRevenue = expectedOnlineRevenue.add(amt);
                    }
                }
            }
        }

        DashboardSummaryDTO summary = report.getSummary();
        assertNotNull(summary, "DashboardSummaryDTO không được null");

        assertEquals(0, summary.getTotalRevenue().compareTo(expectedTotalRevenue),
                "Doanh thu tổng từ Dashboard (" + summary.getTotalRevenue()
                        + ") phải khớp chính xác với OrderRepository (" + expectedTotalRevenue + ")");

        assertEquals(expectedCompletedCount, summary.getCompletedOrderCount(),
                "Số đơn hoàn thành từ Dashboard (" + summary.getCompletedOrderCount()
                        + ") phải khớp với OrderRepository (" + expectedCompletedCount + ")");

        assertEquals(0, summary.getPosRevenue().compareTo(expectedPosRevenue),
                "Doanh thu POS từ Dashboard (" + summary.getPosRevenue()
                        + ") phải khớp với OrderRepository (" + expectedPosRevenue + ")");

        assertEquals(0, summary.getOnlineRevenue().compareTo(expectedOnlineRevenue),
                "Doanh thu Online từ Dashboard (" + summary.getOnlineRevenue()
                        + ") phải khớp với OrderRepository (" + expectedOnlineRevenue + ")");
    }

    @Test
    @DisplayName("Thẻ chỉ số (b) Khoảng ngày rỗng: Mọi giá trị bằng 0, không null")
    void testSummaryMetrics_EmptyDateRange_AllZeroNotNull() {
        // Khoảng ngày năm 1990 không thể có đơn nào
        LocalDate startDate = LocalDate.of(1990, 1, 1);
        LocalDate endDate = LocalDate.of(1990, 1, 2);

        DashboardReportDTO report = assertDoesNotThrow(
                () -> reportService.getDashboardData(startDate, endDate, null),
                "Khoảng ngày rỗng không được ném ngoại lệ");

        assertNotNull(report, "DashboardReportDTO không được null");
        DashboardSummaryDTO summary = report.getSummary();
        assertNotNull(summary, "DashboardSummaryDTO không được null");

        // Khẳng định mọi giá trị đều không null
        assertNotNull(summary.getTotalRevenue(), "totalRevenue không được null");
        assertNotNull(summary.getAverageOrderValue(), "averageOrderValue không được null");
        assertNotNull(summary.getPosRevenue(), "posRevenue không được null");
        assertNotNull(summary.getOnlineRevenue(), "onlineRevenue không được null");
        assertNotNull(summary.getExpectedRevenue(), "expectedRevenue không được null");
        assertNotNull(summary.getTodayRevenue(), "todayRevenue không được null");

        // Khẳng định các giá trị trong khoảng ngày rỗng đều bằng 0
        assertEquals(0, summary.getTotalRevenue().compareTo(BigDecimal.ZERO),
                "totalRevenue của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getTotalRevenue());
        assertEquals(0L, summary.getCompletedOrderCount(),
                "completedOrderCount của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getCompletedOrderCount());
        assertEquals(0, summary.getAverageOrderValue().compareTo(BigDecimal.ZERO),
                "averageOrderValue của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getAverageOrderValue());
        assertEquals(0, summary.getPosRevenue().compareTo(BigDecimal.ZERO),
                "posRevenue của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getPosRevenue());
        assertEquals(0, summary.getOnlineRevenue().compareTo(BigDecimal.ZERO),
                "onlineRevenue của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getOnlineRevenue());
        assertEquals(0L, summary.getProcessingOrderCount(),
                "processingOrderCount của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getProcessingOrderCount());
        assertEquals(0, summary.getExpectedRevenue().compareTo(BigDecimal.ZERO),
                "expectedRevenue của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getExpectedRevenue());
        assertEquals(0L, summary.getCancelledOrderCount(),
                "cancelledOrderCount của khoảng ngày rỗng phải bằng 0. Thực tế: " + summary.getCancelledOrderCount());

        // Hôm nay có thể có hoặc không có đơn nhưng phải >= 0 và không null
        assertTrue(summary.getTodayRevenue().compareTo(BigDecimal.ZERO) >= 0,
                "todayRevenue phải >= 0. Thực tế: " + summary.getTodayRevenue());
        assertTrue(summary.getTodayCompletedOrderCount() >= 0L,
                "todayCompletedOrderCount phải >= 0. Thực tế: " + summary.getTodayCompletedOrderCount());
    }
}
