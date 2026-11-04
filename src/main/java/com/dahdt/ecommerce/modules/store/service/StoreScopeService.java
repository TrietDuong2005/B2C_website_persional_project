package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.springframework.security.access.AccessDeniedException;

/**
 * Thành phần dùng chung quản lý phạm vi truy cập dữ liệu chi nhánh (Store Scoping).
 * Phục vụ phân quyền theo mục 9 của Project Brief:
 * - ADMIN: Toàn quyền truy cập mọi cửa hàng và kho trong hệ thống.
 * - STORE_MANAGER / CASHIER: Chỉ được phép xem và thao tác trên đúng cửa hàng được phân công
 *   (truy xuất từ bảng employees). Mọi hành vi cố tình đổi tham số trên URL để truy cập chi nhánh
 *   khác đều bị chặn với mã 403 (AccessDeniedException).
 *
 * Tái sử dụng xuyên suốt các ngày tiếp theo (Kho, Bán hàng POS, Đơn hàng đa kênh).
 */
public interface StoreScopeService {

    /**
     * Lấy thực thể User của người dùng đang đăng nhập trong phiên làm việc hiện tại.
     * Trả về null nếu chưa đăng nhập.
     */
    User getCurrentUser();

    /**
     * Lấy hồ sơ nhân viên (Employee) tương ứng với tài khoản người dùng đang đăng nhập.
     * Trả về null nếu người dùng không phải là nhân viên chuỗi (ví dụ: ADMIN thuần túy hoặc CUSTOMER).
     */
    Employee getCurrentEmployee();

    /**
     * Kiểm tra người dùng hiện tại có vai trò Quản trị viên (ROLE_ADMIN) hay không.
     */
    boolean isCurrentAdmin();

    /**
     * Kiểm tra người dùng hiện tại có vai trò Quản lý cửa hàng (ROLE_STORE_MANAGER) hay không.
     */
    boolean isCurrentStoreManager();

    /**
     * Lấy ID cửa hàng được phân công cho nhân viên/quản lý hiện tại.
     * Đối với ADMIN, trả về null nếu không ép buộc cửa hàng.
     */
    Long getAssignedStoreId();

    /**
     * Lấy thực thể cửa hàng được phân công cho nhân viên/quản lý hiện tại.
     */
    Store getAssignedStore();

    /**
     * Kiểm tra quyền truy cập vào một chi nhánh cụ thể:
     * - Nếu là ADMIN: luôn cho phép truy cập.
     * - Nếu là STORE_MANAGER: chi nhánh đích phải trùng khớp với chi nhánh được phân công trong bảng employees.
     *   Nếu khác, ném AccessDeniedException để Spring Security kích hoạt trang 403.
     *
     * @param targetStoreId Mã ID chi nhánh cần truy cập hoặc thao tác
     * @throws AccessDeniedException nếu người dùng không có quyền truy cập chi nhánh này
     */
    void validateStoreAccess(Long targetStoreId) throws AccessDeniedException;

    /**
     * Giải quyết ID chi nhánh hợp lệ cho ngữ cảnh hiện tại:
     * - Với STORE_MANAGER: luôn trả về ID chi nhánh mình phụ trách (bị chặn nếu cố tình truyền ID khác).
     * - Với ADMIN: trả về requestedStoreId nếu có, hoặc ID chi nhánh đầu tiên nếu chưa chọn.
     *
     * @param requestedStoreId ID chi nhánh do request gửi lên (có thể null)
     * @return ID chi nhánh hợp lệ
     */
    Long resolveStoreId(Long requestedStoreId) throws AccessDeniedException;
}

