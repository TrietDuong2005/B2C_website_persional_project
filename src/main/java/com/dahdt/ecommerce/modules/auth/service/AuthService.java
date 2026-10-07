package com.dahdt.ecommerce.modules.auth.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.config.security.JwtTokenProvider;
import com.dahdt.ecommerce.modules.auth.dto.LoginRequestDTO;
import com.dahdt.ecommerce.modules.auth.dto.LoginResponseDTO;
import com.dahdt.ecommerce.modules.auth.dto.RegisterRequestDTO;
import com.dahdt.ecommerce.modules.auth.entity.AuthToken;
import com.dahdt.ecommerce.modules.auth.repository.AuthTokenRepository;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.entity.UserProfile;
import com.dahdt.ecommerce.modules.user.repository.RoleRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Đăng ký tài khoản mới cho khách hàng
     */
    @Transactional
    public void register(RegisterRequestDTO request) {
        // Kiểm tra email hoặc sđt đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Số điện thoại đã được sử dụng");
        }

        // Tạo User entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("active"); // Mặc định active (hoặc pending nếu có gửi OTP)

        // Lấy Role CUSTOMER mặc định từ DB
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER chưa được tạo trong DB (cần chạy migration V6)"));
        user.getRoles().add(customerRole);

        // Tạo UserProfile đính kèm
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        
        user.setProfile(profile);

        // Lưu xuống DB (CascadeType.ALL sẽ tự lưu profile)
        userRepository.save(user);
    }

    /**
     * Đăng nhập và cấp phát Access Token + Refresh Token
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Xác thực bằng Spring Security Auth Manager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Lưu thông tin đăng nhập vào Context (tùy chọn)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lấy thông tin User hiện tại
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Sinh token
        String accessToken = tokenProvider.generateAccessToken(userDetails.getId());
        String refreshToken = tokenProvider.generateRefreshToken(userDetails.getId());

        // Lưu refresh token vào database
        AuthToken authToken = new AuthToken();
        authToken.setUser(userDetails.getUser());
        authToken.setTokenHash(refreshToken); // Thực tế có thể hash bằng SHA-256 rồi mới lưu
        authToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // khớp với app.jwt.refresh-token-expiration
        authTokenRepository.save(authToken);

        // Trả kết quả
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userDetails.getId())
                .build();
    }
}
