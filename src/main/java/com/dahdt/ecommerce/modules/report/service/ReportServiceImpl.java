package com.dahdt.ecommerce.modules.report.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.util.MoneyUtil;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.web.OrderDisplayHelper;
import com.dahdt.ecommerce.modules.report.dto.*;
import com.dahdt.ecommerce.modules.report.dto.projection.*;
import com.dahdt.ecommerce.modules.report.repository.ReportRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Cài đặt nghiệp vụ Báo cáo & Thống kê cho trang Dashboard quản trị.
 *
 * TUÂN THỦ CÁC QUY TẮC:
 * - So sánh tiền bằng compareTo.
 * - An toàn kiểu tuyệt đối (Type-safe): Dùng projection DTO với SELECT new, loại bỏ hoàn toàn Object[]
 *   và việc ép kiểu thủ công.
 * - Tránh N+1 query bằng JPQL tổng hợp và projection DTO.
 * - Phân quyền Store Scoping: STORE_MANAGER chỉ xem được cửa hàng mình phụ trách.
 * - Điền đầy đủ các ngày trong kỳ (ngày không có đơn có số đơn = 0 và doanh thu = 0).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final StoreRepository storeRepository;
    private final StoreScopeService storeScopeService;
    private final OrderDisplayHelper orderDisplayHelper;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    @Value("${app.dashboard.low-stock-max-rows:20}")
    private int lowStockMaxRows;

    @Value("${app.dashboard.top-products-limit:10}")
    private int topProductsLimit;

    @Value("${app.dashboard.pending-orders-limit:5}")
    private int pendingOrdersLimit;

    @Value("${app.dashboard.default-days-range:30}")
    private int defaultDaysRange;

    @Override
    public DashboardReportDTO getDashboardData(LocalDate startDate, LocalDate endDate, Long requestedStoreId) {
        // 1. Chuẩn hóa khoảng ngày lọc (mặc định 30 ngày gần nhất tính đến hôm nay)
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();
        LocalDate effectiveStartDate = (startDate != null)
                ? startDate
                : effectiveEndDate.minusDays(defaultDaysRange - 1);

        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            throw new BadRequestException("Ngày bắt đầu không được lớn hơn ngày kết thúc!");
        }

        LocalDateTime startDateTime = effectiveStartDate.atStartOfDay();
        LocalDateTime endDateTime = effectiveEndDate.atTime(LocalTime.MAX);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 2. Xác định phạm vi chi nhánh (Store Scoping)
        boolean isStoreManager = storeScopeService.isCurrentStoreManager();
        Long effectiveStoreId;
        String filterStoreName = "Toàn hệ thống";

        if (isStoreManager) {
            effectiveStoreId = storeScopeService.getAssignedStoreId();
            if (effectiveStoreId == null) {
                log.warn("Tài khoản STORE_MANAGER chưa được phân công chi nhánh trong bảng employees.");
                throw new AccessDeniedException("Tài khoản của bạn chưa được phân công chi nhánh!");
            }
            if (requestedStoreId != null && !requestedStoreId.equals(effectiveStoreId)) {
                log.warn("STORE_MANAGER cố tình truy cập cửa hàng khác: requestedStoreId={}, assignedStoreId={}",
                        requestedStoreId, effectiveStoreId);
                throw new AccessDeniedException("Bạn chỉ có quyền truy cập dữ liệu của chi nhánh được phân công!");
            }
            Store assignedStore = storeScopeService.getAssignedStore();
            if (assignedStore != null) {
                filterStoreName = assignedStore.getName();
            }
        } else {
            effectiveStoreId = requestedStoreId;
            if (effectiveStoreId != null) {
                Store selectedStore = storeRepository.findById(effectiveStoreId).orElse(null);
                if (selectedStore != null) {
                    filterStoreName = selectedStore.getName();
                }
            }
        }

        // 3. Thẻ chỉ số tổng quan (KPI Cards) - An toàn kiểu với CompletedOrderSummaryRow
        DashboardSummaryDTO summary = calculateSummaryMetrics(
                startDateTime, endDateTime, todayStart, todayEnd, effectiveStoreId, isStoreManager);

        // 4. Doanh thu theo từng ngày trong kỳ (Điền 0 cho ngày trống) - An toàn kiểu với DailyRevenueRow
        List<DailyRevenueDTO> dailyRevenues = calculateDailyRevenues(
                effectiveStartDate, effectiveEndDate, startDateTime, endDateTime, effectiveStoreId);

        // 5. Doanh thu theo cửa hàng (Chỉ hiển thị cho ADMIN) - An toàn kiểu với StoreRevenueRow
        List<StoreRevenueDTO> storeRevenues = isStoreManager
                ? Collections.emptyList()
                : calculateStoreRevenues(startDateTime, endDateTime, effectiveStoreId, summary.getTotalRevenue());

        // 6. Doanh thu theo phương thức thanh toán - An toàn kiểu với PaymentMethodRevenueRow
        List<PaymentMethodRevenueDTO> paymentMethodRevenues = calculatePaymentMethodRevenues(
                startDateTime, endDateTime, effectiveStoreId, summary.getTotalRevenue());

        // 7. Số đơn theo trạng thái (đầy đủ 6 trạng thái) - An toàn kiểu với OrderStatusCountRow
        List<OrderStatusCountDTO> orderStatusCounts = calculateOrderStatusCounts(
                startDateTime, endDateTime, effectiveStoreId);

        // 8. Top sản phẩm bán chạy trong kỳ (từ đơn COMPLETED) - An toàn kiểu với TopSellingProductRow
        List<TopSellingProductDTO> topSellingProducts = calculateTopSellingProducts(
                startDateTime, endDateTime, effectiveStoreId);

        // 9. Cảnh báo tồn kho thấp (tồn <= threshold, tối đa lowStockMaxRows dòng)
        List<LowStockAlertDTO> lowStockAlerts = calculateLowStockAlerts(effectiveStoreId);

        // 10. 5 đơn hàng trực tuyến mới nhất đang chờ xác nhận (chỉ ADMIN)
        List<PendingOnlineOrderDTO> pendingOnlineOrders = isStoreManager
                ? Collections.emptyList()
                : calculatePendingOnlineOrders();

        return DashboardReportDTO.builder()
                .filterStartDate(effectiveStartDate)
                .filterEndDate(effectiveEndDate)
                .filterStoreId(effectiveStoreId)
                .filterStoreName(filterStoreName)
                .storeManager(isStoreManager)
                .summary(summary)
                .dailyRevenues(dailyRevenues)
                .storeRevenues(storeRevenues)
                .paymentMethodRevenues(paymentMethodRevenues)
                .orderStatusCounts(orderStatusCounts)
                .topSellingProducts(topSellingProducts)
                .lowStockAlerts(lowStockAlerts)
                .pendingOnlineOrders(pendingOnlineOrders)
                .lowStockThreshold(lowStockThreshold)
                .build();
    }

    private DashboardSummaryDTO calculateSummaryMetrics(
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            LocalDateTime todayStart, LocalDateTime todayEnd,
            Long effectiveStoreId, boolean isStoreManager) {

        // Thống kê đơn hoàn thành trong kỳ (DTO typed)
        CompletedOrderSummaryRow comp = reportRepository.getCompletedOrderSummary(startDateTime, endDateTime, effectiveStoreId);
        long completedCount = (comp != null) ? comp.getCompletedCount() : 0L;
        BigDecimal totalRevenue = (comp != null) ? MoneyUtil.roundMoney(comp.getTotalRevenue()) : BigDecimal.ZERO;
        BigDecimal posRevenue = (comp != null) ? MoneyUtil.roundMoney(comp.getPosRevenue()) : BigDecimal.ZERO;
        BigDecimal onlineRevenue = (comp != null) ? MoneyUtil.roundMoney(comp.getOnlineRevenue()) : BigDecimal.ZERO;

        // Nếu là STORE_MANAGER, chỉ quản lý quầy POS của chi nhánh mình, doanh thu online luôn bằng 0
        if (isStoreManager) {
            onlineRevenue = BigDecimal.ZERO;
        }

        // Giá trị trung bình mỗi đơn = totalRevenue / completedCount
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (completedCount > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(completedCount), 0, RoundingMode.HALF_UP);
        }

        // Đơn hàng đang xử lý (PENDING, CONFIRMED, PACKING, SHIPPING) (DTO typed)
        List<OrderStatus> processingStatuses = List.of(
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PACKING, OrderStatus.SHIPPING);
        ProcessingOrderSummaryRow proc = reportRepository.getProcessingOrderSummary(processingStatuses, startDateTime, endDateTime, effectiveStoreId);
        long processingCount = (proc != null) ? proc.getProcessingCount() : 0L;
        BigDecimal expectedRevenue = (proc != null) ? MoneyUtil.roundMoney(proc.getExpectedRevenue()) : BigDecimal.ZERO;

        // Số đơn hủy
        long cancelledCount = reportRepository.countCancelledOrders(startDateTime, endDateTime, effectiveStoreId);

        // Số liệu hôm nay (DTO typed)
        TodayCompletedSummaryRow today = reportRepository.getTodayCompletedSummary(todayStart, todayEnd, effectiveStoreId);
        long todayCount = (today != null) ? today.getTodayCount() : 0L;
        BigDecimal todayRevenue = (today != null) ? MoneyUtil.roundMoney(today.getTodayRevenue()) : BigDecimal.ZERO;

        return DashboardSummaryDTO.builder()
                .totalRevenue(totalRevenue)
                .completedOrderCount(completedCount)
                .averageOrderValue(averageOrderValue)
                .posRevenue(posRevenue)
                .onlineRevenue(onlineRevenue)
                .processingOrderCount(processingCount)
                .expectedRevenue(expectedRevenue)
                .cancelledOrderCount(cancelledCount)
                .todayRevenue(todayRevenue)
                .todayCompletedOrderCount(todayCount)
                .build();
    }

    private List<DailyRevenueDTO> calculateDailyRevenues(
            LocalDate startDate, LocalDate endDate,
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            Long effectiveStoreId) {

        // Khởi tạo bản đồ chứa toàn bộ các ngày trong kỳ với giá trị mặc định bằng 0
        Map<LocalDate, DailyRevenueDTO> dailyMap = new LinkedHashMap<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            dailyMap.put(cur, DailyRevenueDTO.builder()
                    .date(cur)
                    .orderCount(0L)
                    .revenue(BigDecimal.ZERO)
                    .build());
            cur = cur.plusDays(1);
        }

        // Truy vấn dữ liệu thực tế từ CSDL thông qua Projection DTO có kiểu rõ ràng
        List<DailyRevenueRow> dbRows = reportRepository.getDailyRevenue(startDateTime, endDateTime, effectiveStoreId);
        if (dbRows != null) {
            for (DailyRevenueRow row : dbRows) {
                if (row == null || row.getDate() == null) continue;
                LocalDate rowDate = row.getDate();
                if (dailyMap.containsKey(rowDate)) {
                    dailyMap.put(rowDate, DailyRevenueDTO.builder()
                            .date(rowDate)
                            .orderCount(row.getOrderCount())
                            .revenue(MoneyUtil.roundMoney(row.getRevenue()))
                            .build());
                }
            }
        }

        return new ArrayList<>(dailyMap.values());
    }

    private List<StoreRevenueDTO> calculateStoreRevenues(
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            Long effectiveStoreId, BigDecimal totalRevenue) {

        List<Store> activeStores = (effectiveStoreId != null)
                ? storeRepository.findById(effectiveStoreId).map(List::of).orElse(Collections.emptyList())
                : storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE);

        Map<Long, StoreAgg> aggMap = new HashMap<>();
        List<StoreRevenueRow> dbRows = reportRepository.getStoreRevenueAggregates(startDateTime, endDateTime, effectiveStoreId);
        if (dbRows != null) {
            for (StoreRevenueRow row : dbRows) {
                if (row == null || row.getStoreId() == null) continue;
                aggMap.put(row.getStoreId(), new StoreAgg(row.getOrderCount(), MoneyUtil.roundMoney(row.getRevenue())));
            }
        }

        List<StoreRevenueDTO> list = new ArrayList<>();
        for (Store store : activeStores) {
            StoreAgg agg = aggMap.getOrDefault(store.getId(), new StoreAgg(0L, BigDecimal.ZERO));
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0 && agg.revenue.compareTo(BigDecimal.ZERO) > 0) {
                percentage = agg.revenue.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 1, RoundingMode.HALF_UP);
            }

            String typeLabel = (store.getType() != null)
                    ? (store.getType().name().equals("WAREHOUSE") ? "Kho tổng" : "Cửa hàng bán lẻ")
                    : "Cửa hàng";

            list.add(StoreRevenueDTO.builder()
                    .storeId(store.getId())
                    .storeCode(store.getCode())
                    .storeName(store.getName())
                    .storeType(typeLabel)
                    .orderCount(agg.count)
                    .revenue(agg.revenue)
                    .percentage(percentage)
                    .build());
        }

        // Sắp xếp theo doanh thu giảm dần, sau đó theo tên cửa hàng
        list.sort((a, b) -> {
            int cmp = b.getRevenue().compareTo(a.getRevenue());
            if (cmp != 0) return cmp;
            return a.getStoreName().compareToIgnoreCase(b.getStoreName());
        });

        return list;
    }

    private List<PaymentMethodRevenueDTO> calculatePaymentMethodRevenues(
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            Long effectiveStoreId, BigDecimal totalRevenue) {

        List<PaymentMethodRevenueRow> rows = reportRepository.getPaymentMethodRevenue(startDateTime, endDateTime, effectiveStoreId);
        List<PaymentMethodRevenueDTO> list = new ArrayList<>();

        if (rows != null) {
            for (PaymentMethodRevenueRow row : rows) {
                if (row == null || row.getMethod() == null) continue;
                BigDecimal amount = MoneyUtil.roundMoney(row.getTotalAmount());

                BigDecimal percentage = BigDecimal.ZERO;
                if (totalRevenue.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = amount.multiply(BigDecimal.valueOf(100))
                            .divide(totalRevenue, 1, RoundingMode.HALF_UP);
                }

                list.add(PaymentMethodRevenueDTO.builder()
                        .method(row.getMethod())
                        .methodName(orderDisplayHelper.getPaymentMethodLabel(row.getMethod()))
                        .transactionCount(row.getTransactionCount())
                        .totalAmount(amount)
                        .percentage(percentage)
                        .build());
            }
        }

        return list;
    }

    private List<OrderStatusCountDTO> calculateOrderStatusCounts(
            LocalDateTime startDateTime, LocalDateTime endDateTime, Long effectiveStoreId) {

        List<OrderStatusCountRow> rows = reportRepository.getOrderStatusCounts(startDateTime, endDateTime, effectiveStoreId);
        Map<OrderStatus, StatusAgg> statusMap = new EnumMap<>(OrderStatus.class);

        long totalOrdersAll = 0L;
        if (rows != null) {
            for (OrderStatusCountRow row : rows) {
                if (row == null || row.getStatus() == null) continue;
                statusMap.put(row.getStatus(), new StatusAgg(row.getOrderCount(), MoneyUtil.roundMoney(row.getTotalAmount())));
                totalOrdersAll += row.getOrderCount();
            }
        }

        List<OrderStatusCountDTO> list = new ArrayList<>();
        for (OrderStatus st : OrderStatus.values()) {
            StatusAgg agg = statusMap.getOrDefault(st, new StatusAgg(0L, BigDecimal.ZERO));
            BigDecimal pct = BigDecimal.ZERO;
            if (totalOrdersAll > 0 && agg.count > 0) {
                pct = BigDecimal.valueOf(agg.count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalOrdersAll), 1, RoundingMode.HALF_UP);
            }

            list.add(OrderStatusCountDTO.builder()
                    .status(st)
                    .statusLabel(orderDisplayHelper.getStatusLabel(st))
                    .badgeClass(orderDisplayHelper.getStatusBadgeClass(st))
                    .orderCount(agg.count)
                    .totalAmount(agg.totalAmount)
                    .percentage(pct)
                    .build());
        }

        return list;
    }

    private List<TopSellingProductDTO> calculateTopSellingProducts(
            LocalDateTime startDateTime, LocalDateTime endDateTime, Long effectiveStoreId) {

        List<TopSellingProductRow> rows = reportRepository.getTopSellingProducts(
                startDateTime, endDateTime, effectiveStoreId, PageRequest.of(0, topProductsLimit));

        List<TopSellingProductDTO> list = new ArrayList<>();
        if (rows != null) {
            for (TopSellingProductRow row : rows) {
                if (row == null) continue;

                list.add(TopSellingProductDTO.builder()
                        .sku(row.getSku())
                        .productName(row.getProductName())
                        .color(row.getColor())
                        .size(row.getSize())
                        .quantitySold(row.getQuantitySold())
                        .totalRevenue(MoneyUtil.roundMoney(row.getTotalRevenue()))
                        .build());
            }
        }

        return list;
    }

    private List<LowStockAlertDTO> calculateLowStockAlerts(Long effectiveStoreId) {
        List<Inventory> inventories = reportRepository.findLowStockInventories(
                effectiveStoreId, lowStockThreshold, PageRequest.of(0, lowStockMaxRows));

        List<LowStockAlertDTO> list = new ArrayList<>();
        for (Inventory inv : inventories) {
            list.add(LowStockAlertDTO.builder()
                    .inventoryId(inv.getId())
                    .storeName(inv.getStore().getName())
                    .sku(inv.getVariant().getSku())
                    .productName(inv.getVariant().getProduct().getName())
                    .color(inv.getVariant().getColor())
                    .size(inv.getVariant().getSize())
                    .quantityOnHand(inv.getQuantityOnHand())
                    .reorderLevel(inv.getReorderLevel())
                    .outOfStock(inv.getQuantityOnHand() <= 0)
                    .build());
        }

        return list;
    }

    private List<PendingOnlineOrderDTO> calculatePendingOnlineOrders() {
        List<Order> orders = reportRepository.findLatestPendingOnlineOrders(
                PageRequest.of(0, pendingOrdersLimit));

        List<PendingOnlineOrderDTO> list = new ArrayList<>();
        for (Order o : orders) {
            String shipName = (o.getShipName() != null && !o.getShipName().isBlank())
                    ? o.getShipName()
                    : ((o.getCustomer() != null && o.getCustomer().getProfile() != null)
                    ? o.getCustomer().getProfile().getFullName() : "Khách trực tuyến");

            list.add(PendingOnlineOrderDTO.builder()
                    .orderId(o.getId())
                    .orderCode(o.getOrderCode())
                    .shipName(shipName)
                    .shipPhone((o.getShipPhone() != null) ? o.getShipPhone() : "-")
                    .placedAt(o.getPlacedAt())
                    .totalAmount(MoneyUtil.roundMoney(o.getTotalAmount()))
                    .build());
        }

        return list;
    }

    private record StoreAgg(long count, BigDecimal revenue) {}
    private record StatusAgg(long count, BigDecimal totalAmount) {}
}
