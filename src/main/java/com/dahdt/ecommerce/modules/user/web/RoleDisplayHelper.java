package com.dahdt.ecommerce.modules.user.web;

import com.dahdt.ecommerce.modules.user.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper hỗ trợ chọn nhãn vai trò hiển thị cho người dùng trên giao diện.
 * Quy tắc nghiệp vụ (Ngày 7A-FIX):
 * - Trên giao diện Shop (navbar, trang chủ), khách hàng (CUSTOMER) KHÔNG thấy nhãn vai trò (trả về chuỗi rỗng "").
 * - Tài khoản có vai trò nhân viên (ADMIN, STORE_MANAGER, CASHIER) hiển thị nhãn vai trò nhân viên.
 * - Nếu người dùng có nhiều vai trò (ví dụ CUSTOMER cùng CASHIER), chỉ hiển thị vai trò nhân viên (ưu tiên ADMIN > STORE_MANAGER > CASHIER),
 *   không hiển thị nhãn CUSTOMER kèm theo.
 * Có thể gọi trực tiếp trong Thymeleaf: {@code @roleDisplay.getStaffRoleLabel(#authentication)}.
 */
@Component("roleDisplay")
public class RoleDisplayHelper {

    public static final String LABEL_ADMIN = "Quản trị viên";
    public static final String LABEL_STORE_MANAGER = "Quản lý cửa hàng";
    public static final String LABEL_CASHIER = "Thu ngân";

    /**
     * Lấy nhãn vai trò nhân viên từ Authentication của Spring Security.
     * Trả về chuỗi rỗng ("") nếu chưa xác thực hoặc chỉ có vai trò CUSTOMER.
     */
    public String getStaffRoleLabel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        return getStaffRoleLabel(authentication.getAuthorities());
    }

    /**
     * Lấy nhãn vai trò nhân viên từ tập hợp quyền (authorities) hoặc đối tượng Role.
     */
    public String getStaffRoleLabel(Collection<?> authoritiesOrRoles) {
        if (authoritiesOrRoles == null || authoritiesOrRoles.isEmpty()) {
            return "";
        }
        Set<String> codes = extractRoleCodes(authoritiesOrRoles);
        return getStaffRoleLabelFromCodes(codes);
    }

    /**
     * Lấy nhãn vai trò nhân viên từ danh sách mã vai trò (varargs).
     */
    public String getStaffRoleLabel(String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return "";
        }
        return getStaffRoleLabelFromCodes(Arrays.asList(roleCodes));
    }

    /**
     * Kiểm tra người dùng có vai trò nhân viên hay không.
     */
    public boolean hasStaffRole(Authentication authentication) {
        return !getStaffRoleLabel(authentication).isEmpty();
    }

    /**
     * Kiểm tra danh sách quyền có vai trò nhân viên hay không.
     */
    public boolean hasStaffRole(Collection<?> authoritiesOrRoles) {
        return !getStaffRoleLabel(authoritiesOrRoles).isEmpty();
    }

    private String getStaffRoleLabelFromCodes(Collection<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return "";
        }

        Set<String> normalized = roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(code -> code.startsWith("ROLE_") ? code.substring(5) : code)
                .collect(Collectors.toSet());

        // Thứ tự ưu tiên nhân viên: ADMIN > STORE_MANAGER > CASHIER
        // Khách hàng (CUSTOMER) bị bỏ qua
        if (normalized.contains("ADMIN")) {
            return LABEL_ADMIN;
        }
        if (normalized.contains("STORE_MANAGER")) {
            return LABEL_STORE_MANAGER;
        }
        if (normalized.contains("CASHIER")) {
            return LABEL_CASHIER;
        }

        return "";
    }

    private Set<String> extractRoleCodes(Collection<?> items) {
        Set<String> codes = new HashSet<>();
        for (Object item : items) {
            if (item == null) {
                continue;
            }
            if (item instanceof GrantedAuthority ga) {
                codes.add(ga.getAuthority());
            } else if (item instanceof Role role) {
                codes.add(role.getCode());
            } else if (item instanceof String str) {
                codes.add(str);
            }
        }
        return codes;
    }
}

