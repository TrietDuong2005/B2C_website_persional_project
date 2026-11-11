package com.dahdt.ecommerce.modules.report.service;

import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.order.web.OrderDisplayHelper;
import com.dahdt.ecommerce.modules.report.dto.DailyRevenueDTO;
import com.dahdt.ecommerce.modules.report.dto.DashboardReportDTO;
import com.dahdt.ecommerce.modules.report.dto.projection.*;
import com.dahdt.ecommerce.modules.report.repository.ReportRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử đơn vị nghiệp vụ ReportServiceImpl (Mockito).
 *
 * MỤC TIÊU:
 * 1. Kiểm tra tính giá trị trung bình mỗi đơn (AOV) khi có đơn và khi số đơn = 0.
 * 2. Kiểm tra điền ngày trống bằng 0 trong bảng doanh thu theo ngày.
 * 3. Kiểm tra kiểm soát phạm vi truy cập (Store Scoping) của STORE_MANAGER.
 * 4. Kiểm tra cấu hình ngưỡng cảnh báo tồn kho thấp (low-stock-threshold).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử đơn vị: Nghiệp vụ Báo cáo thống kê ReportService (Mockito)")
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreScopeService storeScopeService;

    @Mock
    private OrderDisplayHelper orderDisplayHelper;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Store sampleStore;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reportService, "lowStockThreshold", 5);
        ReflectionTestUtils.setField(reportService, "lowStockMaxRows", 20);
        ReflectionTestUtils.setField(reportService, "topProductsLimit", 10);
        ReflectionTestUtils.setField(reportService, "pendingOrdersLimit", 5);
        ReflectionTestUtils.setField(reportService, "defaultDaysRange", 30);

        sampleStore = Store.builder()
                .id(1L)
                .code("STORE-Q1")
                .name("Triết Store Quận 1")
                .status(StoreStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Tính giá trị trung bình mỗi đơn (AOV): Chia đúng khi có đơn, trả về 0 khi chưa có đơn")
    void testAverageOrderValue_Calculation() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        // Ca 1: Có 2 đơn hoàn tất, tổng tiền 1.000.000 đ -> AOV phải là 500.000 đ
        CompletedOrderSummaryRow compSummary = new CompletedOrderSummaryRow(
                2L, new BigDecimal("1000000.00"), new BigDecimal("600000.00"), new BigDecimal("400000.00"));
        when(reportRepository.getCompletedOrderSummary(any(), any(), any())).thenReturn(compSummary);
        when(reportRepository.getProcessingOrderSummary(any(), any(), any(), any()))
                .thenReturn(new ProcessingOrderSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.countCancelledOrders(any(), any(), any())).thenReturn(0L);
        when(reportRepository.getTodayCompletedSummary(any(), any(), any()))
                .thenReturn(new TodayCompletedSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.getDailyRevenue(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getStoreRevenueAggregates(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getPaymentMethodRevenue(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getOrderStatusCounts(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getTopSellingProducts(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLowStockInventories(any(), anyInt(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLatestPendingOnlineOrders(any())).thenReturn(Collections.emptyList());
        when(storeRepository.findByStatusOrderByNameAsc(any())).thenReturn(Collections.emptyList());

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 10);

        DashboardReportDTO result = reportService.getDashboardData(start, end, null);

        assertNotNull(result, "DashboardReportDTO không được null");
        assertEquals(0, result.getSummary().getAverageOrderValue().compareTo(new BigDecimal("500000")),
                "Giá trị TB mỗi đơn phải là 500.000 đ. Thực tế: " + result.getSummary().getAverageOrderValue());

        // Ca 2: 0 đơn hoàn tất -> AOV phải là 0 đ (không lỗi ArithmeticException chia cho 0)
        when(reportRepository.getCompletedOrderSummary(any(), any(), any()))
                .thenReturn(new CompletedOrderSummaryRow(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        DashboardReportDTO zeroResult = reportService.getDashboardData(start, end, null);
        assertEquals(0, zeroResult.getSummary().getAverageOrderValue().compareTo(BigDecimal.ZERO),
                "Khi số đơn = 0, giá trị TB phải là 0 đ. Thực tế: " + zeroResult.getSummary().getAverageOrderValue());
    }

    @Test
    @DisplayName("Điền ngày trống bằng 0: Toàn bộ ngày trong kỳ đều có mặt trong bảng theo ngày")
    void testDailyRevenue_FillEmptyDaysWithZero() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 5); // 5 ngày: 1, 2, 3, 4, 5

        when(reportRepository.getCompletedOrderSummary(any(), any(), any()))
                .thenReturn(new CompletedOrderSummaryRow(1L, new BigDecimal("250000.00"), new BigDecimal("250000.00"), BigDecimal.ZERO));
        when(reportRepository.getProcessingOrderSummary(any(), any(), any(), any()))
                .thenReturn(new ProcessingOrderSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.countCancelledOrders(any(), any(), any())).thenReturn(0L);
        when(reportRepository.getTodayCompletedSummary(any(), any(), any()))
                .thenReturn(new TodayCompletedSummaryRow(0L, BigDecimal.ZERO));

        // Giả lập DB chỉ có dữ liệu ngày 03/11 (1 đơn, 250.000 đ)
        DailyRevenueRow rowNov3 = new DailyRevenueRow(LocalDate.of(2026, 11, 3), 1L, new BigDecimal("250000.00"));
        when(reportRepository.getDailyRevenue(any(), any(), any())).thenReturn(List.of(rowNov3));

        when(reportRepository.getStoreRevenueAggregates(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getPaymentMethodRevenue(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getOrderStatusCounts(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getTopSellingProducts(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLowStockInventories(any(), anyInt(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLatestPendingOnlineOrders(any())).thenReturn(Collections.emptyList());
        when(storeRepository.findByStatusOrderByNameAsc(any())).thenReturn(Collections.emptyList());

        DashboardReportDTO result = reportService.getDashboardData(start, end, null);

        List<DailyRevenueDTO> dailyList = result.getDailyRevenues();
        assertEquals(5, dailyList.size(),
                "Bảng doanh thu theo ngày phải có đủ 5 dòng cho 5 ngày từ 01/11 đến 05/11. Thực tế: " + dailyList.size());

        // Ngày 01/11: Không có đơn -> orderCount = 0, revenue = 0
        DailyRevenueDTO d1 = dailyList.get(0);
        assertEquals(LocalDate.of(2026, 11, 1), d1.getDate());
        assertEquals(0L, d1.getOrderCount(), "Ngày 01/11 phải có 0 đơn");
        assertEquals(0, d1.getRevenue().compareTo(BigDecimal.ZERO), "Ngày 01/11 phải có doanh thu 0 đ");

        // Ngày 03/11: Có 1 đơn -> orderCount = 1, revenue = 250.000 đ
        DailyRevenueDTO d3 = dailyList.get(2);
        assertEquals(LocalDate.of(2026, 11, 3), d3.getDate());
        assertEquals(1L, d3.getOrderCount(), "Ngày 03/11 phải có 1 đơn");
        assertEquals(0, d3.getRevenue().compareTo(new BigDecimal("250000")), "Ngày 03/11 phải có doanh thu 250.000 đ");

        // Ngày 05/11: Không có đơn -> orderCount = 0, revenue = 0
        DailyRevenueDTO d5 = dailyList.get(4);
        assertEquals(LocalDate.of(2026, 11, 5), d5.getDate());
        assertEquals(0L, d5.getOrderCount(), "Ngày 05/11 phải có 0 đơn");
        assertEquals(0, d5.getRevenue().compareTo(BigDecimal.ZERO), "Ngày 05/11 phải có doanh thu 0 đ");
    }

    @Test
    @DisplayName("Phạm vi truy cập của STORE_MANAGER: Ép về chi nhánh mình, doanh thu online = 0, ẩn bảng cửa hàng khác")
    void testStoreScope_StoreManager() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(true);
        when(storeScopeService.getAssignedStoreId()).thenReturn(1L);
        when(storeScopeService.getAssignedStore()).thenReturn(sampleStore);

        CompletedOrderSummaryRow compSummary = new CompletedOrderSummaryRow(
                5L, new BigDecimal("2500000.00"), new BigDecimal("2500000.00"), new BigDecimal("500000.00"));
        when(reportRepository.getCompletedOrderSummary(any(), any(), eq(1L))).thenReturn(compSummary);
        when(reportRepository.getProcessingOrderSummary(any(), any(), any(), eq(1L)))
                .thenReturn(new ProcessingOrderSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.countCancelledOrders(any(), any(), eq(1L))).thenReturn(0L);
        when(reportRepository.getTodayCompletedSummary(any(), any(), eq(1L)))
                .thenReturn(new TodayCompletedSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.getDailyRevenue(any(), any(), eq(1L))).thenReturn(Collections.emptyList());
        when(reportRepository.getPaymentMethodRevenue(any(), any(), eq(1L))).thenReturn(Collections.emptyList());
        when(reportRepository.getOrderStatusCounts(any(), any(), eq(1L))).thenReturn(Collections.emptyList());
        when(reportRepository.getTopSellingProducts(any(), any(), eq(1L), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLowStockInventories(eq(1L), anyInt(), any())).thenReturn(Collections.emptyList());

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 10);

        DashboardReportDTO result = reportService.getDashboardData(start, end, null);

        assertTrue(result.isStoreManager(), "isStoreManager phải là true");
        assertEquals(1L, result.getFilterStoreId(), "FilterStoreId phải được ép về storeId của manager (1L)");
        assertEquals(0, result.getSummary().getOnlineRevenue().compareTo(BigDecimal.ZERO),
                "Doanh thu online của STORE_MANAGER phải luôn là 0 đ");
        assertTrue(result.getStoreRevenues().isEmpty(),
                "STORE_MANAGER không được thấy bảng thống kê theo các cửa hàng khác (phải rỗng)");
        assertTrue(result.getPendingOnlineOrders().isEmpty(),
                "STORE_MANAGER không được thấy danh sách đơn trực tuyến chờ xác nhận (phải rỗng)");
    }

    @Test
    @DisplayName("Phạm vi truy cập của STORE_MANAGER: Ném AccessDeniedException khi cố tình truyền storeId khác")
    void testStoreScope_StoreManager_AccessDeniedOnDifferentStore() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(true);
        when(storeScopeService.getAssignedStoreId()).thenReturn(1L);

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 10);

        assertThrows(AccessDeniedException.class, () -> {
            reportService.getDashboardData(start, end, 2L);
        }, "Phải ném AccessDeniedException khi STORE_MANAGER cố tình truyền storeId khác");
    }

    @Test
    @DisplayName("Cảnh báo tồn kho thấp: Truy vấn đúng ngưỡng cấu hình app.inventory.low-stock-threshold")
    void testLowStockThreshold_PassedCorrectly() {
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);
        when(reportRepository.getCompletedOrderSummary(any(), any(), any()))
                .thenReturn(new CompletedOrderSummaryRow(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(reportRepository.getProcessingOrderSummary(any(), any(), any(), any()))
                .thenReturn(new ProcessingOrderSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.countCancelledOrders(any(), any(), any())).thenReturn(0L);
        when(reportRepository.getTodayCompletedSummary(any(), any(), any()))
                .thenReturn(new TodayCompletedSummaryRow(0L, BigDecimal.ZERO));
        when(reportRepository.getDailyRevenue(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getStoreRevenueAggregates(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getPaymentMethodRevenue(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getOrderStatusCounts(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.getTopSellingProducts(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.findLatestPendingOnlineOrders(any())).thenReturn(Collections.emptyList());
        when(storeRepository.findByStatusOrderByNameAsc(any())).thenReturn(Collections.emptyList());

        Product product = Product.builder().name("Giày Sneaker").build();
        ProductVariant variant = ProductVariant.builder().sku("SKU-01").product(product).color("Đen").size("42").build();
        Inventory inv = Inventory.builder().id(10L).store(sampleStore).variant(variant).quantityOnHand(3).reorderLevel(5).build();

        when(reportRepository.findLowStockInventories(isNull(), eq(5), any(Pageable.class)))
                .thenReturn(List.of(inv));

        DashboardReportDTO result = reportService.getDashboardData(LocalDate.now().minusDays(5), LocalDate.now(), null);

        assertEquals(1, result.getLowStockAlerts().size(), "Phải có 1 bản ghi cảnh báo tồn thấp");
        assertEquals("SKU-01", result.getLowStockAlerts().get(0).getSku());
        assertEquals(3, result.getLowStockAlerts().get(0).getQuantityOnHand());
        assertEquals(5, result.getLowStockThreshold());
    }
}
