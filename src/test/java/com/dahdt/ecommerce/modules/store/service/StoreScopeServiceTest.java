package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử thành phần phân quyền StoreScopeService")
class StoreScopeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreScopeServiceImpl storeScopeService;

    private User managerUser;
    private User adminUser;
    private Store storeQ1;
    private Store storeQ3;
    private Employee managerEmployee;

    @BeforeEach
    void setUp() {
        storeQ1 = Store.builder().id(1L).code("STORE-Q1").name("Chi nhánh Q1").build();
        storeQ3 = Store.builder().id(2L).code("STORE-Q3").name("Chi nhánh Q3").build();

        Role managerRole = Role.builder().id(2L).code("STORE_MANAGER").name("Quản lý cửa hàng").build();
        Role adminRole = Role.builder().id(1L).code("ADMIN").name("Quản trị viên").build();

        managerUser = User.builder()
                .id(10L)
                .email("manager@dahdt.com")
                .roles(Set.of(managerRole))
                .build();

        adminUser = User.builder()
                .id(1L)
                .email("admin@dahdt.com")
                .roles(Set.of(adminRole))
                .build();

        managerEmployee = Employee.builder()
                .id(100L)
                .user(managerUser)
                .store(storeQ1)
                .position(EmployeePosition.MANAGER)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user, String roleName) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(new SimpleGrantedAuthority(roleName))
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("STORE_MANAGER truy cập đúng cửa hàng của mình (STORE-Q1) thì được phép")
    void validateStoreAccess_storeManager_assignedStore_shouldPass() {
        authenticateAs(managerUser, "ROLE_STORE_MANAGER");
        when(userRepository.findById(10L)).thenReturn(Optional.of(managerUser));
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(managerEmployee));

        assertDoesNotThrow(() -> storeScopeService.validateStoreAccess(1L));
    }

    @Test
    @DisplayName("STORE_MANAGER cố tình truy cập cửa hàng khác (STORE-Q3) thì bị chặn với AccessDeniedException")
    void validateStoreAccess_storeManager_otherStore_shouldThrowAccessDeniedException() {
        authenticateAs(managerUser, "ROLE_STORE_MANAGER");
        when(userRepository.findById(10L)).thenReturn(Optional.of(managerUser));
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(managerEmployee));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> storeScopeService.validateStoreAccess(2L));
        assertTrue(ex.getMessage().contains("phân công"));
    }

    @Test
    @DisplayName("ADMIN có quyền truy cập bất kỳ cửa hàng nào trong hệ thống")
    void validateStoreAccess_admin_anyStore_shouldPass() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        assertDoesNotThrow(() -> storeScopeService.validateStoreAccess(1L));
        assertDoesNotThrow(() -> storeScopeService.validateStoreAccess(2L));
        assertDoesNotThrow(() -> storeScopeService.validateStoreAccess(999L));
    }

    @Test
    @DisplayName("resolveStoreId: STORE_MANAGER cố tình truyền ID cửa hàng khác trên URL thì bị chặn")
    void resolveStoreId_storeManager_otherStoreRequested_shouldThrowAccessDeniedException() {
        authenticateAs(managerUser, "ROLE_STORE_MANAGER");
        when(userRepository.findById(10L)).thenReturn(Optional.of(managerUser));
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(managerEmployee));

        assertThrows(AccessDeniedException.class, () -> storeScopeService.resolveStoreId(2L));
    }
}

