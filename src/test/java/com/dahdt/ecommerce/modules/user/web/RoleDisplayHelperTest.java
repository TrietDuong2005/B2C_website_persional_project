package com.dahdt.ecommerce.modules.user.web;

import com.dahdt.ecommerce.modules.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Helper hiển thị vai trò người dùng (RoleDisplayHelper)")
class RoleDisplayHelperTest {

    private RoleDisplayHelper helper;

    @BeforeEach
    void setUp() {
        helper = new RoleDisplayHelper();
    }

    @Test
    @DisplayName("Chỉ CUSTOMER cho kết quả rỗng (không hiển thị nhãn vai trò trên Shop)")
    void testOnlyCustomer_ReturnsEmpty() {
        // Kiểm thử với chuỗi mã vai trò
        assertEquals("", helper.getStaffRoleLabel("CUSTOMER"));
        assertEquals("", helper.getStaffRoleLabel("ROLE_CUSTOMER"));

        // Kiểm thử với đối tượng Role Entity
        Role customerRole = Role.builder().code("CUSTOMER").name("Khách hàng").build();
        assertEquals("", helper.getStaffRoleLabel(Set.of(customerRole)));

        // Kiểm thử với Authentication (Spring Security)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "customer@dahdt.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        assertEquals("", helper.getStaffRoleLabel(auth));
        assertFalse(helper.hasStaffRole(auth));
    }

    @Test
    @DisplayName("ADMIN cho nhãn quản trị")
    void testAdmin_ReturnsAdminLabel() {
        // Kiểm thử với mã vai trò
        assertEquals("Quản trị viên", helper.getStaffRoleLabel("ADMIN"));
        assertEquals("Quản trị viên", helper.getStaffRoleLabel("ROLE_ADMIN"));

        // Kiểm thử với Authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@dahdt.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        String label = helper.getStaffRoleLabel(auth);
        assertEquals("Quản trị viên", label);
        assertTrue(label.contains("Quản trị"));
        assertTrue(helper.hasStaffRole(auth));
    }

    @Test
    @DisplayName("CUSTOMER cùng CASHIER chỉ cho nhãn thu ngân (không hiển thị CUSTOMER kèm theo)")
    void testCustomerAndCashier_ReturnsCashierLabelOnly() {
        // Kiểm thử với chuỗi vai trò
        assertEquals("Thu ngân", helper.getStaffRoleLabel("CUSTOMER", "CASHIER"));
        assertEquals("Thu ngân", helper.getStaffRoleLabel("ROLE_CUSTOMER", "ROLE_CASHIER"));

        // Kiểm thử với Authentication chứa đồng thời 2 quyền
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "cashier@dahdt.com",
                "N/A",
                List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                        new SimpleGrantedAuthority("ROLE_CASHIER")
                )
        );
        String label = helper.getStaffRoleLabel(auth);
        assertEquals("Thu ngân", label);
        assertFalse(label.contains("CUSTOMER"));
        assertFalse(label.contains("Khách hàng"));
        assertTrue(helper.hasStaffRole(auth));
    }

    @Test
    @DisplayName("STORE_MANAGER cho nhãn quản lý cửa hàng")
    void testStoreManager_ReturnsStoreManagerLabel() {
        assertEquals("Quản lý cửa hàng", helper.getStaffRoleLabel("STORE_MANAGER"));
        assertEquals("Quản lý cửa hàng", helper.getStaffRoleLabel("ROLE_STORE_MANAGER"));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "manager@dahdt.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_STORE_MANAGER"))
        );
        assertEquals("Quản lý cửa hàng", helper.getStaffRoleLabel(auth));
        assertTrue(helper.hasStaffRole(auth));
    }

    @Test
    @DisplayName("Trường hợp null hoặc rỗng trả về chuỗi rỗng")
    void testNullOrEmpty_ReturnsEmpty() {
        assertEquals("", helper.getStaffRoleLabel((Authentication) null));
        assertEquals("", helper.getStaffRoleLabel((String[]) null));
        assertEquals("", helper.getStaffRoleLabel(Collections.emptyList()));
        assertFalse(helper.hasStaffRole((Authentication) null));
    }
}

