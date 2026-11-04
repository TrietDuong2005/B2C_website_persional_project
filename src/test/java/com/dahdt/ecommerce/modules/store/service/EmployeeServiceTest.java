package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.store.dto.EmployeeRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeResponseDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeUpdateDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.mapper.EmployeeMapper;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.RoleRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ EmployeeService (Nhân sự & Đồng bộ Role)")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private User testUser;
    private Store testStore;
    private Role roleCustomer;
    private Role roleStoreManager;
    private Role roleCashier;

    @BeforeEach
    void setUp() {
        roleCustomer = Role.builder().id(1L).code("CUSTOMER").name("Khách hàng").build();
        roleStoreManager = Role.builder().id(2L).code("STORE_MANAGER").name("Quản lý cửa hàng").build();
        roleCashier = Role.builder().id(3L).code("CASHIER").name("Thu ngân").build();

        Set<Role> roles = new HashSet<>();
        roles.add(roleCustomer);

        testUser = User.builder()
                .id(50L)
                .email("user@dahdt.com")
                .roles(roles)
                .build();

        testStore = Store.builder()
                .id(1L)
                .code("STORE-Q1")
                .name("Triết Store - Quận 1")
                .build();
    }

    @Test
    @DisplayName("Thêm nhân viên: Một user không thể thuộc hai cửa hàng (ném BadRequestException nếu đã là employee)")
    void addEmployee_userAlreadyBelongsToStore_shouldThrowBadRequestException() {
        when(userRepository.findByEmail("user@dahdt.com")).thenReturn(Optional.of(testUser));
        when(employeeRepository.existsByUserId(50L)).thenReturn(true);

        EmployeeRequestDTO requestDTO = EmployeeRequestDTO.builder()
                .storeId(1L)
                .email("user@dahdt.com")
                .position(EmployeePosition.CASHIER)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> employeeService.addEmployee(requestDTO));
        assertTrue(ex.getMessage().contains("đã được phân công"));

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thêm nhân viên chức vụ CASHIER: tạo bản ghi Employee và gán thêm role CASHIER cho user")
    void addEmployee_cashier_shouldAssignCashierRole() {
        when(userRepository.findByEmail("user@dahdt.com")).thenReturn(Optional.of(testUser));
        when(employeeRepository.existsByUserId(50L)).thenReturn(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(roleRepository.findByCode("CASHIER")).thenReturn(Optional.of(roleCashier));
        when(roleRepository.findByCode("STORE_MANAGER")).thenReturn(Optional.of(roleStoreManager));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeMapper.toDto(any())).thenReturn(new EmployeeResponseDTO());

        EmployeeRequestDTO requestDTO = EmployeeRequestDTO.builder()
                .storeId(1L)
                .email("user@dahdt.com")
                .position(EmployeePosition.CASHIER)
                .build();

        employeeService.addEmployee(requestDTO);

        // Kiểm tra User đã có role CASHIER
        assertTrue(testUser.getRoles().contains(roleCashier));
        assertFalse(testUser.getRoles().contains(roleStoreManager));
        verify(userRepository, times(1)).save(testUser);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Đổi chức vụ từ MANAGER sang CASHIER: gỡ role STORE_MANAGER và thêm role CASHIER")
    void updateEmployee_managerToCashier_shouldUpdateRolesCorrectly() {
        Set<Role> roles = new HashSet<>();
        roles.add(roleCustomer);
        roles.add(roleStoreManager);
        testUser.setRoles(roles);

        Employee employee = Employee.builder()
                .id(100L)
                .user(testUser)
                .store(testStore)
                .position(EmployeePosition.MANAGER)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(roleRepository.findByCode("STORE_MANAGER")).thenReturn(Optional.of(roleStoreManager));
        when(roleRepository.findByCode("CASHIER")).thenReturn(Optional.of(roleCashier));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeMapper.toDto(any())).thenReturn(new EmployeeResponseDTO());

        EmployeeUpdateDTO updateDTO = EmployeeUpdateDTO.builder()
                .id(100L)
                .storeId(1L)
                .position(EmployeePosition.CASHIER)
                .build();

        employeeService.updateEmployee(updateDTO);

        assertEquals(EmployeePosition.CASHIER, employee.getPosition());
        assertTrue(testUser.getRoles().contains(roleCashier), "Phải có role CASHIER");
        assertFalse(testUser.getRoles().contains(roleStoreManager), "Role STORE_MANAGER phải bị gỡ");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Đổi chức vụ sang STAFF: gỡ cả role STORE_MANAGER và CASHIER")
    void updateEmployee_toStaff_shouldRemoveBothRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(roleCustomer);
        roles.add(roleStoreManager);
        testUser.setRoles(roles);

        Employee employee = Employee.builder()
                .id(100L)
                .user(testUser)
                .store(testStore)
                .position(EmployeePosition.MANAGER)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(roleRepository.findByCode("STORE_MANAGER")).thenReturn(Optional.of(roleStoreManager));
        when(roleRepository.findByCode("CASHIER")).thenReturn(Optional.of(roleCashier));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeMapper.toDto(any())).thenReturn(new EmployeeResponseDTO());

        EmployeeUpdateDTO updateDTO = EmployeeUpdateDTO.builder()
                .id(100L)
                .storeId(1L)
                .position(EmployeePosition.STAFF)
                .build();

        employeeService.updateEmployee(updateDTO);

        assertEquals(EmployeePosition.STAFF, employee.getPosition());
        assertFalse(testUser.getRoles().contains(roleStoreManager), "Role STORE_MANAGER phải bị gỡ");
        assertFalse(testUser.getRoles().contains(roleCashier), "Role CASHIER phải bị gỡ");
        assertTrue(testUser.getRoles().contains(roleCustomer), "Role CUSTOMER vẫn được giữ");
        verify(userRepository, times(1)).save(testUser);
    }
}

