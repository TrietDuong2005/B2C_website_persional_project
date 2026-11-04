package com.dahdt.ecommerce.modules.inventory.entity;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ Domain Entity - Inventory (Tồn kho)")
class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .quantityOnHand(10)
                .reorderLevel(5)
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("increase() với số lượng hợp lệ (> 0) thì tăng số lượng tồn chính xác")
    void increase_withValidQuantity_shouldIncreaseQuantity() {
        inventory.increase(5);
        assertEquals(15, inventory.getQuantityOnHand());
    }

    @Test
    @DisplayName("increase() với số lượng <= 0 thì ném BadRequestException")
    void increase_withZeroOrNegativeQuantity_shouldThrowBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> inventory.increase(0));
        assertTrue(ex1.getMessage().contains("lớn hơn 0"));

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> inventory.increase(-3));
        assertTrue(ex2.getMessage().contains("lớn hơn 0"));
    }

    @Test
    @DisplayName("deduct() với số lượng hợp lệ và <= tồn kho thì giảm số lượng tồn chính xác")
    void deduct_withValidQuantity_shouldDeductQuantity() {
        inventory.deduct(4);
        assertEquals(6, inventory.getQuantityOnHand());

        inventory.deduct(6);
        assertEquals(0, inventory.getQuantityOnHand());
    }

    @Test
    @DisplayName("deduct() với số lượng <= 0 thì ném BadRequestException")
    void deduct_withZeroOrNegativeQuantity_shouldThrowBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> inventory.deduct(0));
        assertTrue(ex1.getMessage().contains("lớn hơn 0"));

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> inventory.deduct(-5));
        assertTrue(ex2.getMessage().contains("lớn hơn 0"));
    }

    @Test
    @DisplayName("deduct() vượt quá số lượng tồn kho thì bị chặn và ném InsufficientStockException")
    void deduct_exceedingQuantity_shouldThrowInsufficientStockException() {
        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> inventory.deduct(11));
        assertTrue(ex.getMessage().contains("không đủ"));
        assertEquals(10, inventory.getQuantityOnHand(), "Tồn kho không được thay đổi khi trừ thất bại");
    }

    @Test
    @DisplayName("isOutOfStock() trả về true khi tồn kho = 0 hoặc null, false khi tồn > 0")
    void isOutOfStock_shouldWorkCorrectly() {
        assertFalse(inventory.isOutOfStock(), "Tồn = 10 thì không thể out of stock");

        inventory.setQuantityOnHand(0);
        assertTrue(inventory.isOutOfStock(), "Tồn = 0 thì out of stock");

        inventory.setQuantityOnHand(null);
        assertTrue(inventory.isOutOfStock(), "Tồn = null thì out of stock");
    }

    @Test
    @DisplayName("isLow() trả về true khi 0 < tồn <= reorder_level, false trong các trường hợp khác")
    void isLow_shouldWorkCorrectly() {
        // Tồn = 10, reorderLevel = 5 -> false
        assertFalse(inventory.isLow());

        // Tồn = 5, reorderLevel = 5 -> true (chạm ngưỡng)
        inventory.setQuantityOnHand(5);
        assertTrue(inventory.isLow());

        // Tồn = 1, reorderLevel = 5 -> true
        inventory.setQuantityOnHand(1);
        assertTrue(inventory.isLow());

        // Tồn = 0 -> false (vì lúc này đã là HẾT HÀNG, không phải sắp hết)
        inventory.setQuantityOnHand(0);
        assertFalse(inventory.isLow());
    }
}

