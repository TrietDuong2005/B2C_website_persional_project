package com.dahdt.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test cơ bản kiểm tra ứng dụng có khởi động được không.
 * Annotation @SpringBootTest sẽ load toàn bộ Spring context.
 *
 * Lưu ý: Test này cần kết nối được tới SQL Server để pass.
 * Nếu chưa cấu hình DB, có thể bỏ qua test này khi build bằng:
 *   mvn clean install -DskipTests
 */
@SpringBootTest
@ActiveProfiles("dev")
class EcommerceApplicationTests {

    @Test
    void contextLoads() {
        // Test rỗng — chỉ kiểm tra Spring context load thành công, không lỗi cấu hình
    }
}
