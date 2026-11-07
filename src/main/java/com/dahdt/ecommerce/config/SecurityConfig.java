package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.common.response.ApiResponse;
import com.dahdt.ecommerce.common.util.RedirectUtil;
import com.dahdt.ecommerce.config.security.CustomUserDetailsService;
import com.dahdt.ecommerce.config.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.util.Set;

/**
 * Cấu hình bảo mật Spring Security với 2 SecurityFilterChain:
 * 1. Chain @Order(1): Áp dụng cho "/api/**", cơ chế Stateless + JWT, trả JSON ApiResponse cho lỗi 401, 403.
 * 2. Chain @Order(2): Áp dụng cho các trang web còn lại, cơ chế Form Login + Session, CSRF bật, phân quyền URL.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Tắt việc Spring Boot tự động đăng ký JwtAuthenticationFilter vào servlet container toàn cục.
     * Nhờ đó filter này CHỈ chạy trong SecurityFilterChain dành cho "/api/**".
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Handler chuyển hướng người dùng sau khi đăng nhập form thành công:
     * - ADMIN / STORE_MANAGER -> /admin (hoặc URL admin đang truy cập trước đó)
     * - CASHIER -> /pos
     * - CUSTOMER: quay lại trang đang xem trước đó (SavedRequest), nếu không có thì về "/"
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {
        RequestCache requestCache = new HttpSessionRequestCache();
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            SavedRequest savedRequest = requestCache.getRequest(request, response);

            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_STORE_MANAGER")) {
                if (savedRequest != null && savedRequest.getRedirectUrl() != null && savedRequest.getRedirectUrl().contains("/admin")) {
                    response.sendRedirect(savedRequest.getRedirectUrl());
                } else {
                    response.sendRedirect("/admin");
                }
            } else if (roles.contains("ROLE_CASHIER")) {
                response.sendRedirect("/pos");
            } else {
                // Khách hàng cá nhân (CUSTOMER):
                // 1. Nếu có tham số redirect hợp lệ từ form, chuyển hướng đến URL đó
                String redirectParam = request.getParameter("redirect");
                if (RedirectUtil.isValidRedirect(redirectParam)) {
                    response.sendRedirect(redirectParam.trim());
                } else if (savedRequest != null && savedRequest.getRedirectUrl() != null && !savedRequest.getRedirectUrl().isBlank()) {
                    // 2. Giữ nguyên hành vi SavedRequest hiện có (ví dụ khi bị chặn chuyển hướng từ /cart)
                    response.sendRedirect(savedRequest.getRedirectUrl());
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }

    // =========================================================================
    // CHAIN 1: REST API (/api/**) - Stateless, JWT, trả JSON ApiResponse
    // =========================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                // Tắt CSRF cho REST API (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Quản lý session: không tạo session trên server cho API
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Endpoint đăng nhập / làm mới token công khai
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Mọi API khác thuộc /api/** đều cần xác thực bằng JWT
                        .anyRequest().authenticated()
                )
                // Xử lý lỗi trả về JSON ApiResponse theo đúng chuẩn hệ thống
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            ApiResponse<Void> apiResponse = ApiResponse.error("Chưa xác thực hoặc token không hợp lệ", HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ApiResponse<Void> apiResponse = ApiResponse.error("Bạn không có quyền truy cập tài nguyên này", HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                )
                // Thêm JWT Filter trước UsernamePasswordAuthenticationFilter trong chain API
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================================================================
    // CHAIN 2: Giao diện Web - Form login, Session, CSRF bật, phân quyền URL
    // =========================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Tài nguyên tĩnh, tài liệu Swagger và trang lỗi
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Trang đăng nhập web công khai
                        .requestMatchers("/login").permitAll()

                        // Trang xem sản phẩm / trang chủ shop công khai
                        .requestMatchers("/", "/shop/**", "/products/**", "/categories/**").permitAll()

                        // Phân quyền URL theo mục 9 của brief:
                        // Admin: /admin/** -> ADMIN, STORE_MANAGER
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        // POS: /pos/** -> CASHIER, STORE_MANAGER, ADMIN
                        .requestMatchers("/pos/**").hasAnyRole("CASHIER", "STORE_MANAGER", "ADMIN")

                        // Khách hàng cá nhân: giỏ hàng, checkout, tài khoản -> CUSTOMER
                        .requestMatchers("/cart", "/cart/**", "/checkout", "/checkout/**", "/account", "/account/**").hasRole("CUSTOMER")

                        // Các trang web còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        // Khi người dùng bị cấm truy cập (ví dụ CUSTOMER vào /admin), chuyển hướng đến /error/403
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }
}
