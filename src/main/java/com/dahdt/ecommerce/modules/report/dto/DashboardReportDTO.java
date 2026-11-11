package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO tổng hợp chỉ đọc chứa toàn bộ dữ liệu phục vụ hiển thị Dashboard quản trị.
 * Được sinh bởi ReportService và đưa trực tiếp vào Spring Model.
 */
@Getter
@Builder
public class DashboardReportDTO {

    private final LocalDate filterStartDate;
    private final LocalDate filterEndDate;
    private final Long filterStoreId;
    private final String filterStoreName;
    private final boolean storeManager;

    private final DashboardSummaryDTO summary;
    private final List<DailyRevenueDTO> dailyRevenues;
    private final List<StoreRevenueDTO> storeRevenues;
    private final List<PaymentMethodRevenueDTO> paymentMethodRevenues;
    private final List<OrderStatusCountDTO> orderStatusCounts;
    private final List<TopSellingProductDTO> topSellingProducts;
    private final List<LowStockAlertDTO> lowStockAlerts;
    private final List<PendingOnlineOrderDTO> pendingOnlineOrders;
    private final int lowStockThreshold;
}

