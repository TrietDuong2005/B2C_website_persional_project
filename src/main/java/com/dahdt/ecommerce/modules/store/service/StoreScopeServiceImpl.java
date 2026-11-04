package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Cài đặt thành phần kiểm soát phạm vi truy cập dữ liệu chi nhánh.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreScopeServiceImpl implements StoreScopeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId()).orElse(userDetails.getUser());
        }

        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Employee getCurrentEmployee() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return employeeRepository.findByUserId(user.getId()).orElse(null);
    }

    @Override
    public boolean isCurrentAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @Override
    public boolean isCurrentStoreManager() {
        return hasRole("ROLE_STORE_MANAGER");
    }

    private boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (roleName.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Long getAssignedStoreId() {
        Employee employee = getCurrentEmployee();
        return (employee != null && employee.getStore() != null) ? employee.getStore().getId() : null;
    }

    @Override
    public Store getAssignedStore() {
        Employee employee = getCurrentEmployee();
        return (employee != null) ? employee.getStore() : null;
    }

    @Override
    public void validateStoreAccess(Long targetStoreId) throws AccessDeniedException {
        if (isCurrentAdmin()) {
            // ADMIN có toàn quyền trên mọi chi nhánh
            return;
        }

        if (isCurrentStoreManager()) {
            Employee employee = getCurrentEmployee();
            if (employee == null || employee.getStore() == null) {
                log.warn("Tài khoản Quản lý chưa được liên kết với chi nhánh nào trong bảng employees.");
                throw new AccessDeniedException("Tài khoản của bạn chưa được liên kết với bất kỳ chi nhánh nào!");
            }
            if (targetStoreId == null || !targetStoreId.equals(employee.getStore().getId())) {
                log.warn("Truy cập trái phép: Manager của store [{}] đang cố truy cập store [{}]",
                        employee.getStore().getId(), targetStoreId);
                throw new AccessDeniedException("Bạn chỉ có quyền truy cập dữ liệu của chi nhánh được phân công (" +
                        employee.getStore().getName() + ")!");
            }
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này!");
    }

    @Override
    public Long resolveStoreId(Long requestedStoreId) throws AccessDeniedException {
        if (isCurrentStoreManager()) {
            Employee employee = getCurrentEmployee();
            if (employee == null || employee.getStore() == null) {
                throw new AccessDeniedException("Tài khoản của bạn chưa được phân công chi nhánh công tác!");
            }
            Long assignedId = employee.getStore().getId();
            if (requestedStoreId != null && !requestedStoreId.equals(assignedId)) {
                log.warn("STORE_MANAGER cố tình truy cập cửa hàng khác qua URL. Yêu cầu: {}, Cửa hàng gán: {}",
                        requestedStoreId, assignedId);
                throw new AccessDeniedException("Bạn chỉ có quyền truy cập chi nhánh được phân công!");
            }
            return assignedId;
        }

        if (isCurrentAdmin()) {
            if (requestedStoreId != null) {
                if (!storeRepository.existsById(requestedStoreId)) {
                    throw new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + requestedStoreId);
                }
                return requestedStoreId;
            }
            // Mặc định chọn chi nhánh đầu tiên nếu ADMIN chưa chọn
            List<Store> activeStores = storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE);
            if (!activeStores.isEmpty()) {
                return activeStores.get(0).getId();
            }
            List<Store> allStores = storeRepository.findAll();
            return allStores.isEmpty() ? null : allStores.get(0).getId();
        }

        throw new AccessDeniedException("Bạn không có quyền xem thông tin tồn kho hoặc chi nhánh!");
    }
}

