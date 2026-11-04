package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ quản lý Nhân sự chuỗi bán lẻ (EmployeeService).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public Page<EmployeeResponseDTO> getEmployeesByStore(Long storeId, String keyword, Pageable pageable) {
        String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Employee> employeePage = employeeRepository.searchEmployeesByStore(storeId, query, pageable);
        return employeePage.map(employeeMapper::toDto);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployeesByStore(Long storeId) {
        List<Employee> employees = employeeRepository.findByStoreIdWithDetails(storeId);
        return employeeMapper.toDtoList(employees);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        // 1. Kiểm tra tài khoản người dùng đã tồn tại trong hệ thống chưa
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người dùng với email: " + email));

        // 2. Kiểm tra xem user này đã là nhân viên của một chi nhánh nào chưa (ràng buộc 1 user chỉ thuộc 1 store)
        if (employeeRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Tài khoản [" + email + "] đã được phân công nhân sự tại một chi nhánh trong hệ thống!");
        }

        // 3. Kiểm tra chi nhánh tồn tại
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getStoreId()));

        // 4. Tạo hồ sơ nhân viên
        Employee employee = Employee.builder()
                .user(user)
                .store(store)
                .position(dto.getPosition())
                .status(EmployeeStatus.ACTIVE)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // 5. Cập nhật Role hệ thống tương ứng cho User (MANAGER -> STORE_MANAGER, CASHIER -> CASHIER)
        syncRoleForPosition(user, dto.getPosition());

        log.info("Đã thêm nhân viên [{}] ({}) vào chi nhánh [{}] với chức vụ [{}]",
                user.getEmail(), (user.getProfile() != null ? user.getProfile().getFullName() : ""),
                store.getCode(), dto.getPosition());

        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + dto.getId()));

        // Cập nhật chi nhánh nếu có chuyển công tác
        if (!employee.getStore().getId().equals(dto.getStoreId())) {
            Store newStore = storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getStoreId()));
            employee.transferStore(newStore);
        }

        // Cập nhật chức vụ và đồng bộ Role hệ thống nếu có thay đổi
        if (!employee.getPosition().equals(dto.getPosition())) {
            employee.changePosition(dto.getPosition());
            syncRoleForPosition(employee.getUser(), dto.getPosition());
        }

        // Cập nhật trạng thái làm việc nếu có truyền
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Đã cập nhật hồ sơ nhân viên ID [{}] - chức vụ [{}]", updated.getId(), updated.getPosition());
        return employeeMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void lockEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        log.info("Đã khóa hồ sơ nhân viên ID [{}] ({})", id, employee.getUser().getEmail());
    }

    @Override
    @Transactional
    public void unlockEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        log.info("Đã mở khóa hồ sơ nhân viên ID [{}] ({})", id, employee.getUser().getEmail());
    }

    /**
     * Đồng bộ vai trò (Role) trong bảng user_roles khớp với chức vụ tại chi nhánh:
     * - MANAGER: Thêm role STORE_MANAGER, gỡ role CASHIER
     * - CASHIER: Thêm role CASHIER, gỡ role STORE_MANAGER
     * - STAFF: Gỡ cả role STORE_MANAGER và CASHIER
     */
    private void syncRoleForPosition(User user, EmployeePosition position) {
        Role storeManagerRole = roleRepository.findByCode("STORE_MANAGER").orElse(null);
        Role cashierRole = roleRepository.findByCode("CASHIER").orElse(null);

        boolean changed = false;

        switch (position) {
            case MANAGER -> {
                if (storeManagerRole != null && !user.getRoles().contains(storeManagerRole)) {
                    user.getRoles().add(storeManagerRole);
                    changed = true;
                }
                if (cashierRole != null && user.getRoles().contains(cashierRole)) {
                    user.getRoles().remove(cashierRole);
                    changed = true;
                }
            }
            case CASHIER -> {
                if (cashierRole != null && !user.getRoles().contains(cashierRole)) {
                    user.getRoles().add(cashierRole);
                    changed = true;
                }
                if (storeManagerRole != null && user.getRoles().contains(storeManagerRole)) {
                    user.getRoles().remove(storeManagerRole);
                    changed = true;
                }
            }
            case STAFF -> {
                if (storeManagerRole != null && user.getRoles().contains(storeManagerRole)) {
                    user.getRoles().remove(storeManagerRole);
                    changed = true;
                }
                if (cashierRole != null && user.getRoles().contains(cashierRole)) {
                    user.getRoles().remove(cashierRole);
                    changed = true;
                }
            }
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}

