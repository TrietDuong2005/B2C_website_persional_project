package com.dahdt.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Class khởi chạy chính của ứng dụng Spring Boot.
 * Khi chạy lệnh "mvn spring-boot:run", Spring Boot sẽ tìm class này
 * để khởi động toàn bộ ứng dụng (web server, kết nối DB, load cấu hình...).
 */
@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
