package com.dahdt.ecommerce.modules.report.service;

import com.dahdt.ecommerce.modules.report.dto.DashboardReportDTO;

import java.time.LocalDate;

/**
 * Service nghiệp vụ báo cáo thống kê Dashboard quản trị.
 *
 * ĐỊNH NGHĨA SỐ LIỆU:
 * - DOANH THU = tổng total_amount của các đơn hàng có trạng thái COMPLETED
 *   và có completed_at nằm trong kỳ báo cáo (từ startDate 00:00:00 đến endDate 23:59:59.999999999).
 *   Đơn POS hoàn thành khi bán; đơn online hoàn thành khi giao xong.
 * - Đơn CANCELLED không được tính vào doanh thu.
 * - Đơn đang xử lý (PENDING, CONFIRMED, PACKING, SHIPPING) được tính riêng là "doanh thu dự kiến".
 *
 * PHÂN QUYỀN TRUY CẬP (Store Scoping):
 * - ADMIN: Toàn quyền xem số liệu toàn chuỗi (requestedStoreId = null) hoặc lọc theo từng cửa hàng cụ thể.
 * - STORE_MANAGER: Bị giới hạn nghiêm ngặt qua StoreScopeService, chỉ thấy số liệu cửa hàng mình phụ trách.
 *   Vì đơn hàng trực tuyến (Shop) thuộc kho tổng, STORE_MANAGER chỉ thấy các đơn POS của cửa hàng mình.
 */
public interface ReportService {

    /**
     * Lấy toàn bộ dữ liệu báo cáo thống kê cho trang Dashboard.
     *
     * @param startDate Ngày bắt đầu khoảng thời gian lọc (mặc định 30 ngày gần nhất nếu null)
     * @param endDate Ngày kết thúc khoảng thời gian lọc (mặc định hôm nay nếu null)
     * @param requestedStoreId ID cửa hàng do client yêu cầu lọc (chỉ ADMIN có quyền chỉ định tự do)
     * @return DTO chỉ đọc chứa toàn bộ các thẻ chỉ số và bảng dữ liệu số
     */
    DashboardReportDTO getDashboardData(LocalDate startDate, LocalDate endDate, Long requestedStoreId);
}

