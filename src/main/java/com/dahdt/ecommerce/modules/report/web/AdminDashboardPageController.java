package com.dahdt.ecommerce.modules.report.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.report.dto.DashboardReportDTO;
import com.dahdt.ecommerce.modules.report.service.ReportService;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Controller phục vụ trang chủ Admin và Bảng điều khiển Quản trị (Dashboard dạng bảng số).
 *
 * QUY ƯỚC ĐẶT TÊN & ĐIỀU HƯỚNG:
 * - Có tiền tố Admin: AdminDashboardPageController theo quy ước mục 5 của Project Brief.
 * - Hỗ trợ cả 2 URL: /admin (trang chủ admin mặc định) và /admin/dashboard.
 * - Phân quyền: Cả ADMIN và STORE_MANAGER đều truy cập được; STORE_MANAGER bị giới hạn nghiêm ngặt
 *   dữ liệu theo StoreScopeService (ném AccessDeniedException nếu cố tình đổi ID cửa hàng khác).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardPageController {

    private final ReportService reportService;
    private final StoreRepository storeRepository;
    private final StoreScopeService storeScopeService;

    /**
     * Hiển thị Dashboard quản trị với các thẻ chỉ số và bảng dữ liệu dạng số.
     * Hỗ trợ lọc theo khoảng ngày và chi nhánh.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "storeId", required = false) Long storeId,
            Model model) {

        boolean isStoreManager = storeScopeService.isCurrentStoreManager();
        boolean isAdmin = storeScopeService.isCurrentAdmin();

        // Kiểm tra phân quyền: Quản lý cửa hàng không được phép xem cửa hàng khác (quy tắc 14)
        if (isStoreManager) {
            Long assignedStoreId = storeScopeService.getAssignedStoreId();
            if (storeId != null && !storeId.equals(assignedStoreId)) {
                log.warn("STORE_MANAGER cố tình truy cập cửa hàng ID [{}] khác với cửa hàng được phân công [{}]",
                        storeId, assignedStoreId);
                storeScopeService.validateStoreAccess(storeId);
            }
        }

        // Bắt lỗi ngày bắt đầu lớn hơn ngày kết thúc: hiển thị thông báo thân thiện, không sập trang
        String errorMessage = null;
        LocalDate queryStartDate = startDate;
        LocalDate queryEndDate = endDate;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            errorMessage = "Ngày bắt đầu (" + startDate + ") không được lớn hơn ngày kết thúc (" + endDate + "). Đang hiển thị dữ liệu mặc định 30 ngày gần nhất.";
            queryStartDate = null;
            queryEndDate = null;
        }

        DashboardReportDTO report;
        try {
            report = reportService.getDashboardData(queryStartDate, queryEndDate, storeId);
        } catch (BadRequestException bre) {
            errorMessage = bre.getMessage();
            report = reportService.getDashboardData(null, null, storeId);
        }

        // Nạp thông tin người dùng đang đăng nhập cho header banner
        User currentUser = storeScopeService.getCurrentUser();
        if (currentUser != null) {
            String fullName = (currentUser.getProfile() != null && currentUser.getProfile().getFullName() != null)
                    ? currentUser.getProfile().getFullName()
                    : currentUser.getEmail();
            model.addAttribute("currentUserFullName", fullName);
            model.addAttribute("currentUserEmail", currentUser.getEmail());
            model.addAttribute("currentUserRoles", currentUser.getRoles());
        }

        model.addAttribute("report", report);
        model.addAttribute("startDate", report.getFilterStartDate());
        model.addAttribute("endDate", report.getFilterEndDate());
        model.addAttribute("selectedStoreId", report.getFilterStoreId());
        model.addAttribute("isStoreManager", isStoreManager);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("errorMessage", errorMessage);

        if (!isStoreManager) {
            model.addAttribute("stores", storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE));
        } else {
            model.addAttribute("currentStore", storeScopeService.getAssignedStore());
        }

        return "admin/dashboard";
    }
}

