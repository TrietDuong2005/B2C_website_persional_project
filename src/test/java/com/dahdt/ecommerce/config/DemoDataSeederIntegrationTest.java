package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp bắt đúng lỗi LazyInitializationException ngoài Hibernate Session (DemoDataSeederIntegrationTest).
 * Ràng buộc:
 * - Chạy trên kết nối CSDL SQL Server thật của môi trường DEV (profile 'dev').
 * - Chạy HOÀN TOÀN NGOÀI transaction (KHÔNG có @Transactional trên class hay method) để tái hiện chính xác
 *   môi trường chạy thực tế của ApplicationRunner lúc ứng dụng khởi động.
 * - Khẳng định hàm nạp biến thể đủ điều kiện bán (loadSellableVariants) nạp sẵn Product bằng JOIN FETCH,
 *   tuyệt đối không ném org.hibernate.LazyInitializationException khi gọi isSellable() hoặc getProduct().
 * - Đặt @SpringBootTest(properties = "app.demo-data.enabled=false") để seeder không tự ý nạp dữ liệu trong lúc test.
 * - Tuân thủ quy tắc 17 của brief: Mọi assertion đều có thông điệp mô tả kèm giá trị thật.
 */
@SpringBootTest(properties = "app.demo-data.enabled=false")
@ActiveProfiles("dev")
@DisplayName("Kiểm thử tích hợp chống LazyInitializationException ngoài Session (DemoDataSeederIntegrationTest)")
class DemoDataSeederIntegrationTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Test
    @DisplayName("Hàm loadSellableVariants nạp sẵn Product bằng JOIN FETCH, chạy ngoài Transaction không ném LazyInitializationException")
    void testLoadSellableVariants_OutsideTransaction_DoesNotThrowLazyInitializationException() {
        // Thực thi gọi hàm đọc dữ liệu hoàn toàn ngoài Hibernate Session / Transaction
        List<ProductVariant> variants = demoDataSeeder.loadSellableVariants();

        // Sử dụng Assumptions: nếu cơ sở dữ liệu dev chưa áp dụng migration V29 thì bỏ qua an toàn
        Assumptions.assumeTrue(variants != null && !variants.isEmpty(),
                "Bỏ qua kiểm thử nếu cơ sở dữ liệu chưa có dữ liệu mẫu catalog từ migration V29");

        assertNotNull(variants, "Danh sách biến thể sản phẩm trả về không được null");
        assertFalse(variants.isEmpty(), "Danh sách biến thể sản phẩm đủ điều kiện bán không được rỗng (kích thước thực tế: " + variants.size() + ")");

        for (ProductVariant v : variants) {
            // 1. Kiểm tra trạng thái của biến thể
            assertEquals(ProductVariantStatus.ACTIVE, v.getStatus(),
                    "Biến thể [" + v.getSku() + "] phải có trạng thái ACTIVE nhưng thực tế là: " + v.getStatus());

            // 2. Kiểm tra thực thể Product cha đã được nạp sẵn (không bị proxy chưa khởi tạo ngoài Session)
            assertNotNull(v.getProduct(),
                    "Sản phẩm cha của biến thể [" + v.getSku() + "] không được null");
            assertNotNull(v.getProduct().getName(),
                    "Tên sản phẩm cha của SKU [" + v.getSku() + "] không được null (nạp thành công ngoài Session)");
            assertEquals(ProductStatus.ACTIVE, v.getProduct().getStatus(),
                    "Sản phẩm cha [" + v.getProduct().getName() + "] của SKU [" + v.getSku() + "] phải có trạng thái ACTIVE");

            // 3. Khẳng định phương thức isSellable() thực thi trơn tru mà không ném LazyInitializationException
            boolean sellable = assertDoesNotThrow(v::isSellable,
                    "Gọi v.isSellable() cho SKU [" + v.getSku() + "] ngoài Session không được ném LazyInitializationException");
            assertTrue(sellable,
                    "Biến thể [" + v.getSku() + "] phải đủ điều kiện bán (isSellable = true)");
        }
    }

    @Test
    @DisplayName("Khẳng định seeder bị vô hiệu hóa trong môi trường kiểm thử (app.demo-data.enabled=false)")
    void testDemoDataSeeder_DisabledInTestEnvironment() {
        // Thuộc tính demoDataEnabled được cấu hình là false trong test
        assertFalse(demoDataSeeder.isDemoDataEnabled(),
                "demoDataEnabled của DemoDataSeeder phải là false trong môi trường kiểm thử");

        // Khi gọi run(), seeder phải kết thúc ngay lập tức và không ném lỗi
        assertDoesNotThrow(() -> demoDataSeeder.run(null),
                "Gọi demoDataSeeder.run() trong môi trường test phải kết thúc an toàn và không ném ngoại lệ");
    }
}

