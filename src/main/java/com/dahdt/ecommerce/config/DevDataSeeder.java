package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import com.dahdt.ecommerce.modules.user.repository.RoleRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Seeder dữ liệu mẫu cho môi trường DEV (chỉ kích hoạt với profile 'dev').
 * Tự động tạo và tự sửa chuẩn dev cho đúng 5 tài khoản thử nghiệm:
 * 1. ADMIN (admin@dahdt.com)
 * 2. STORE_MANAGER (manager@dahdt.com - gắn với chi nhánh STORE-Q1)
 * 3. CASHIER (cashier@dahdt.com - gắn với chi nhánh STORE-Q1)
 * 4. CUSTOMER (customer@dahdt.com)
 * 5. CUSTOMER 2 (customer2@dahdt.com - khách hàng thứ hai kiểm thử phân quyền đơn hàng)
 *
 * Lưu ý: Bảng employees được thao tác trực tiếp bằng JdbcTemplate để không phải tạo Entity mới.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public static final String DEFAULT_PASSWORD = "password123";

    /**
     * Danh sách DUY NHẤT 5 email dev được phép tự động sửa chuẩn dev.
     * Tuyệt đối không đụng tới tài khoản ngoài danh sách này.
     */
    public static final Set<String> DEV_EMAILS = Set.of(
            "admin@dahdt.com",
            "manager@dahdt.com",
            "cashier@dahdt.com",
            "customer@dahdt.com",
            "customer2@dahdt.com"
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== BẮT ĐẦU CHẠY DEV DATA SEEDER ==========");

        // 1. Tài khoản Quản trị viên (ADMIN)
        User admin = createOrUpdateUser(
                "admin@dahdt.com",
                "0901000001",
                "Quản trị viên Hệ thống",
                "ADMIN"
        );

        // 2. Tài khoản Quản lý cửa hàng (STORE_MANAGER)
        User manager = createOrUpdateUser(
                "manager@dahdt.com",
                "0901000002",
                "Quản lý Chi nhánh Q1",
                "STORE_MANAGER"
        );
        if (manager != null) {
            linkEmployeeToStore(manager.getId(), "STORE-Q1", "MANAGER");
        }

        // 3. Tài khoản Thu ngân (CASHIER)
        User cashier = createOrUpdateUser(
                "cashier@dahdt.com",
                "0901000003",
                "Thu ngân Chi nhánh Q1",
                "CASHIER"
        );
        if (cashier != null) {
            linkEmployeeToStore(cashier.getId(), "STORE-Q1", "CASHIER");
        }

        // 4. Tài khoản Khách hàng 1 (CUSTOMER)
        createOrUpdateUser(
                "customer@dahdt.com",
                "0901000004",
                "Khách hàng Thân thiết",
                "CUSTOMER"
        );

        // 5. Tài khoản Khách hàng 2 (CUSTOMER)
        createOrUpdateUser(
                "customer2@dahdt.com",
                "0901000005",
                "Khách hàng Thử nghiệm 2",
                "CUSTOMER"
        );

        log.info("========== HOÀN THÀNH DEV DATA SEEDER ==========");
    }

    User createOrUpdateUser(String email, String phone, String fullName, String roleCode) {
        // Ràng buộc nghiêm ngặt: TUYỆT ĐỐI chỉ đụng tới 5 email dev
        if (!DEV_EMAILS.contains(email)) {
            log.warn("Email [{}] không thuộc danh sách tài khoản dev, bỏ qua hoàn toàn.", email);
            return null;
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vai trò: " + roleCode));

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            boolean modified = false;

            // 1. Kiểm tra và sửa mật khẩu khớp chuẩn dev
            if (user.getPassword() == null || !passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                log.info("Đã đặt lại mật khẩu dev cho tài khoản [{}]", email);
                modified = true;
            }

            // 2. Kiểm tra và sửa trạng thái hoạt động
            if (!"active".equalsIgnoreCase(user.getStatus())) {
                user.setStatus("active");
                log.info("Đã khôi phục trạng thái hoạt động 'active' cho tài khoản [{}]", email);
                modified = true;
            }
            if (user.isDeleted()) {
                user.setDeleted(false);
                user.setDeletedAt(null);
                log.info("Đã khôi phục trạng thái chưa xóa cho tài khoản [{}]", email);
                modified = true;
            }

            // 3. Kiểm tra và bổ sung đúng vai trò mong đợi
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
                log.info("Đã bổ sung vai trò [{}] cho tài khoản [{}]", roleCode, email);
                modified = true;
            }

            // 4. Kiểm tra và bổ sung user_profiles
            if (user.getProfile() == null) {
                UserProfile profile = UserProfile.builder()
                        .user(user)
                        .fullName(fullName)
                        .build();
                user.setProfile(profile);
                log.info("Đã tạo hồ sơ user_profile cho tài khoản [{}]", email);
                modified = true;
            } else if (user.getProfile().getFullName() == null || user.getProfile().getFullName().isBlank()) {
                user.getProfile().setFullName(fullName);
                log.info("Đã cập nhật họ tên trong user_profile cho tài khoản [{}]", email);
                modified = true;
            }

            if (modified) {
                user = userRepository.save(user);
            }
            return user;
        } else {
            log.info("Tạo mới tài khoản dev [{}] với vai trò [{}]...", email, roleCode);
            Set<Role> roles = new HashSet<>();
            roles.add(role);

            User user = User.builder()
                    .email(email)
                    .phone(phone)
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .status("active")
                    .emailVerifiedAt(LocalDateTime.now())
                    .phoneVerifiedAt(LocalDateTime.now())
                    .roles(roles)
                    .build();

            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .fullName(fullName)
                    .build();
            user.setProfile(profile);

            return userRepository.save(user);
        }
    }

    private void linkEmployeeToStore(Long userId, String storeCode, String position) {
        if (userId == null) {
            return;
        }
        try {
            Long storeId = jdbcTemplate.queryForObject(
                    "SELECT id FROM stores WHERE code = ?",
                    Long.class,
                    storeCode
            );
            if (storeId == null) {
                log.warn("Không tìm thấy cửa hàng với mã [{}], bỏ qua gắn employee.", storeCode);
                return;
            }

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM employees WHERE user_id = ?",
                    Integer.class,
                    userId
            );

            if (count == null || count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO employees (user_id, store_id, position, status, created_at, updated_at) " +
                                "VALUES (?, ?, ?, 'ACTIVE', GETDATE(), GETDATE())",
                        userId, storeId, position
                );
                log.info("Đã gắn user_id [{}] vào cửa hàng [{}] với vị trí [{}].", userId, storeCode, position);
            } else {
                log.info("User_id [{}] đã được gán hồ sơ employee.", userId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi liên kết nhân viên vào cửa hàng [{}]: {}", storeCode, e.getMessage());
        }
    }
}
