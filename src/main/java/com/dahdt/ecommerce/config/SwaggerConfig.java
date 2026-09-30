package com.dahdt.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger / OpenAPI — tạo trang web tài liệu API tự động.
 *
 * Sau khi chạy ứng dụng, truy cập:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * Swagger UI cho phép bạn xem và TEST thử tất cả API ngay trên trình duyệt
 * mà không cần cài Postman hay viết frontend.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce B2C API")
                        .version("1.0.0")
                        .description("API hệ thống thương mại điện tử B2C — Spring Boot MVC + SQL Server")
                        .contact(new Contact()
                                .name("DA_HDT Team")));
    }
}
