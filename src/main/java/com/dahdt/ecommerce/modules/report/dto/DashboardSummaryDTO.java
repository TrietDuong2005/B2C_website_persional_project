package com.dahdt.ecommerce.modules.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chỉ đọc chứa các chỉ số tổng quan (Thẻ KPI / Metric Cards) trên Dashboard quản trị.
 *
 * ĐỊNH NGHĨA SỐ LIỆU:
 * - totalRevenue: Tổng total_amount của các đơn hàng COMPLETED có completed_at trong kỳ.
 * - completedOrderCount: Số đơn hàng COMPLETED có completed_at trong kỳ.
 * - averageOrderValue: Giá trị trung bình mỗi đơn = totalRevenue / completedOrderCount (0 nếu chưa có đơn).
 * - posRevenue: Tổng doanh thu từ kênh bán tại quầy (POS) hoàn tất trong kỳ.
 * - onlineRevenue: Tổng doanh thu từ kênh bán trực tuyến (Shop) hoàn tất trong kỳ.
 * - processingOrderCount: Số lượng đơn hàng đang xử lý (PENDING, CONFIRMED, PACKING, SHIPPING) đặt trong kỳ.
 * - expectedRevenue: Tổng total_amount của các đơn hàng đang xử lý đặt trong kỳ.
 * - cancelledOrderCount: Số lượng đơn hàng ĐÃ HỦY (CANCELLED) đặt trong kỳ.
 * - todayRevenue: Doanh thu của các đơn hàng COMPLETED có completed_at trong ngày hôm nay.
 * - todayCompletedOrderCount: Số đơn hàng hoàn tất trong ngày hôm nay.
 */
@Getter
@Builder
public class DashboardSummaryDTO {

    private final BigDecimal totalRevenue;
    private final long completedOrderCount;
    private final BigDecimal averageOrderValue;
    private final BigDecimal posRevenue;
    private final BigDecimal onlineRevenue;
    private final long processingOrderCount;
    private final BigDecimal expectedRevenue;
    private final long cancelledOrderCount;
    private final BigDecimal todayRevenue;
    private final long todayCompletedOrderCount;
}

