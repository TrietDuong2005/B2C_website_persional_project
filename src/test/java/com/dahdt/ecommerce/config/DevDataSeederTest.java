package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import com.dahdt.ecommerce.modules.user.repository.RoleRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Seeder dữ liệu mẫu dev (DevDataSeederTest)")
class DevDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DevDataSeeder devDataSeeder;

    @Test
    @DisplayName("1. Tài khoản dev đã đúng chuẩn thì KHÔNG bị đụng (không save, không encode lại)")
    void testExistingCorrectUser_NotModified() {
        Role role = Role.builder().id(1L).code("CUSTOMER").name("Khách hàng").build();
        User user = User.builder()
                .id(100L)
                .email("customer@dahdt.com")
                .phone("0901000004")
                .password("$2a$10$validDevHash")
                .status("active")
                .isDeleted(false)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        UserProfile profile = UserProfile.builder()
                .id(100L)
                .user(user)
                .fullName("Khách hàng Thân thiết")
                .build();
        user.setProfile(profile);

        when(roleRepository.findByCode("CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.findByEmail("customer@dahdt.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$validDevHash")).thenReturn(true);

        User result = devDataSeeder.createOrUpdateUser(
                "customer@dahdt.com",
                "0901000004",
                "Khách hàng Thân thiết",
                "CUSTOMER"
        );

        assertNotNull(result);
        // Xác nhận tài khoản đúng chuẩn thì không gọi save hay encode lại
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("2. Tài khoản sai hash thì được sửa hash chuẩn dev")
    void testExistingUser_WrongPasswordHash_PasswordIsReset() {
        Role role = Role.builder().id(2L).code("CUSTOMER").name("Khách hàng").build();
        User user = User.builder()
                .id(101L)
                .email("customer2@dahdt.com")
                .phone("0901000005")
                .password("$2a$10$wrongHashFromManualSql")
                .status("active")
                .isDeleted(false)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        UserProfile profile = UserProfile.builder()
                .id(101L)
                .user(user)
                .fullName("Khách hàng Thử nghiệm 2")
                .build();
        user.setProfile(profile);

        when(roleRepository.findByCode("CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.findByEmail("customer2@dahdt.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$wrongHashFromManualSql")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$newlyEncodedDevPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = devDataSeeder.createOrUpdateUser(
                "customer2@dahdt.com",
                "0901000005",
                "Khách hàng Thử nghiệm 2",
                "CUSTOMER"
        );

        assertNotNull(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$10$newlyEncodedDevPassword", captor.getValue().getPassword());
        assertEquals("customer2@dahdt.com", captor.getValue().getEmail());
    }

    @Test
    @DisplayName("3. Thiếu user_profiles thì được tạo mới hồ sơ")
    void testExistingUser_MissingProfile_ProfileIsCreated() {
        Role role = Role.builder().id(3L).code("CUSTOMER").name("Khách hàng").build();
        User user = User.builder()
                .id(102L)
                .email("customer2@dahdt.com")
                .phone("0901000005")
                .password("$2a$10$validDevHash")
                .status("active")
                .isDeleted(false)
                .roles(new HashSet<>(Set.of(role)))
                .profile(null) // Thiếu hồ sơ profile
                .build();

        when(roleRepository.findByCode("CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.findByEmail("customer2@dahdt.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$validDevHash")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = devDataSeeder.createOrUpdateUser(
                "customer2@dahdt.com",
                "0901000005",
                "Khách hàng Thử nghiệm 2",
                "CUSTOMER"
        );

        assertNotNull(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotNull(captor.getValue().getProfile());
        assertEquals("Khách hàng Thử nghiệm 2", captor.getValue().getProfile().getFullName());
        assertEquals(captor.getValue(), captor.getValue().getProfile().getUser());
    }

    @Test
    @DisplayName("4. Email ngoài danh sách dev TUYỆT ĐỐI không bị đụng")
    void testNonDevEmail_NeverTouched() {
        String nonDevEmail = "nv.test@example.com";

        User result = devDataSeeder.createOrUpdateUser(
                nonDevEmail,
                "0999999999",
                "Nhân viên thử nghiệm ngoài",
                "CASHIER"
        );

        assertNull(result);
        // Đảm bảo không truy vấn DB và không lưu bất cứ gì
        verify(userRepository, never()).findByEmail(nonDevEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByCode(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("5. Phương thức run() chỉ quét đúng 5 email dev và bỏ qua các email khác")
    void testRun_OnlyProcessesDevEmails() {
        Role role = Role.builder().id(99L).code("CUSTOMER").name("Vai trò mẫu").build();
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        devDataSeeder.run();

        // Kiểm tra đúng 5 lần tìm theo email tương ứng 5 tài khoản dev
        verify(userRepository, times(5)).findByEmail(anyString());
        for (String devEmail : DevDataSeeder.DEV_EMAILS) {
            verify(userRepository).findByEmail(devEmail);
        }
        // Đảm bảo không gọi email ngoài danh sách dev
        verify(userRepository, never()).findByEmail("nv.test@example.com");
    }
}

