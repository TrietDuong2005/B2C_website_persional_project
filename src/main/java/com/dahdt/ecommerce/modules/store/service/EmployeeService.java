package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.modules.store.dto.EmployeeRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeResponseDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Nhân sự chuỗi bán lẻ (EmployeeService).
 */
public interface EmployeeService {

    /**
     * Lấy danh sách nhân viên của một chi nhánh (kèm tìm kiếm và phân trang).
     */
    Page<EmployeeResponseDTO> getEmployeesByStore(Long storeId, String keyword, Pageable pageable);

    /**
     * Lấy toàn bộ danh sách nhân viên của một chi nhánh.
     */
    List<EmployeeResponseDTO> getAllEmployeesByStore(Long storeId);

    /**
     * Lấy chi tiết thông tin hồ sơ nhân viên theo ID.
     */
    EmployeeResponseDTO getEmployeeById(Long id);

    /**
     * Thêm mới nhân viên vào chi nhánh:
     * - Tìm kiếm tài khoản đã tồn tại qua email
     * - Kiểm tra user chưa thuộc chi nhánh nào khác
     * - Tạo bản ghi Employee
     * - Tự động cập nhật Role tương ứng (MANAGER -> STORE_MANAGER, CASHIER -> CASHIER)
     */
    EmployeeResponseDTO addEmployee(EmployeeRequestDTO dto);

    /**
     * Cập nhật vị trí chức vụ, điều chuyển chi nhánh hoặc trạng thái của nhân viên.
     * Tự động điều chỉnh Role hệ thống tương ứng khi thay đổi chức vụ.
     */
    EmployeeResponseDTO updateEmployee(EmployeeUpdateDTO dto);

    /**
     * Khóa nhân viên (ngưng hoạt động / thôi việc).
     */
    void lockEmployee(Long id);

    /**
     * Mở khóa nhân viên (tiếp tục làm việc).
     */
    void unlockEmployee(Long id);
}

