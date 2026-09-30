package com.dahdt.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình bảo mật cho ứng dụng.
 *
 * Hiện tại cấu hình ở mức tối thiểu (cho phép tất cả request đi qua)
 * để project có thể khởi chạy và test được.
 *
 * Sẽ được cấu hình chi tiết hơn ở giai đoạn 1 (module auth):
 * - Thêm JWT filter
 * - Cấu hình endpoint nào cần đăng nhập, endpoint nào public
 * - Cấu hình CORS cho frontend
 */
@Configuration          // Đánh dấu đây là class cấu hình Spring
@EnableWebSecurity      // Kích hoạt Spring Security
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì dùng JWT (stateless), không dùng session/cookie
                .csrf(AbstractHttpConfigurer::disable)

                // Không tạo session trên server (stateless — mỗi request tự xác thực bằng JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Tạm thời cho phép tất cả request đi qua (chưa có JWT)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll());

        return http.build();
    }
}
