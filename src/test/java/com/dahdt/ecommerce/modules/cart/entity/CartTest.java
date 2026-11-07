package com.dahdt.ecommerce.modules.cart.entity;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ Domain Entity - Cart & CartItem (Giỏ hàng)")
class CartTest {

    private Cart cart;
    private ProductVariant variant1;
    private ProductVariant variant2;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id(1L)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        Product product = Product.builder()
                .id(10L)
                .name("Giày Sneaker Nam")
                .status(ProductStatus.ACTIVE)
                .build();

        variant1 = ProductVariant.builder()
                .id(101L)
                .product(product)
                .sku("SNK-DEN-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("1200000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        variant2 = ProductVariant.builder()
                .id(102L)
                .product(product)
                .sku("SNK-TRANG-41")
                .color("Trắng")
                .size("41")
                .price(new BigDecimal("1500000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("addItem() cộng dồn số lượng chính xác khi biến thể đã có trong giỏ")
    void addItem_shouldAccumulateQuantity_whenVariantAlreadyExists() {
        cart.addItem(variant1, 2);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());

        // Thêm tiếp 3 đôi cùng biến thể -> cộng dồn thành 5
        cart.addItem(variant1, 3);
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("addItem() giới hạn số lượng mỗi dòng từ 1 đến 10, vượt quá 10 bị chặn")
    void addItem_shouldEnforceQuantityLimit_from1To10() {
        // Số lượng < 1 bị chặn
        BadRequestException exZero = assertThrows(BadRequestException.class, () -> cart.addItem(variant1, 0));
        assertTrue(exZero.getMessage().contains("lớn hơn hoặc bằng 1"));

        // Thêm ban đầu 8 đôi thành công
        cart.addItem(variant1, 8);
        assertEquals(8, cart.getItems().get(0).getQuantity());

        // Cộng dồn thêm 3 đôi (tổng 11 > MAX_PER_LINE = 10) -> ném BadRequestException
        BadRequestException exExceeded = assertThrows(BadRequestException.class, () -> cart.addItem(variant1, 3));
        assertTrue(exExceeded.getMessage().contains("không được vượt quá 10 đôi"));
        assertEquals(8, cart.getItems().get(0).getQuantity(), "Số lượng trong giỏ không bị thay đổi khi lỗi");

        // Thêm một lần > 10 đôi cũng bị chặn
        BadRequestException exOver10 = assertThrows(BadRequestException.class, () -> cart.addItem(variant2, 11));
        assertTrue(exOver10.getMessage().contains("không được vượt quá 10 đôi"));
    }

    @Test
    @DisplayName("removeItem() xóa đúng dòng theo ID")
    void removeItem_shouldRemoveItemById() {
        CartItem item1 = cart.addItem(variant1, 2);
        item1.setId(1001L);
        CartItem item2 = cart.addItem(variant2, 1);
        item2.setId(1002L);

        assertEquals(2, cart.getItems().size());

        // Xóa item1
        cart.removeItem(1001L);
        assertEquals(1, cart.getItems().size());
        assertEquals(102L, cart.getItems().get(0).getVariant().getId());
    }

    @Test
    @DisplayName("subtotal() tính tổng tiền chính xác từ các dòng")
    void subtotal_shouldCalculateTotalCorrectly() {
        // Giỏ trống: subtotal = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(cart.subtotal()));
        assertTrue(cart.isEmpty());

        // item1: 2 * 1.200.000 = 2.400.000
        cart.addItem(variant1, 2);
        // item2: 3 * 1.500.000 = 4.500.000
        cart.addItem(variant2, 3);

        // Tổng: 2.400.000 + 4.500.000 = 6.900.000
        assertEquals(0, new BigDecimal("6900000").compareTo(cart.subtotal()));
        assertEquals(5, cart.getTotalQuantity());
        assertFalse(cart.isEmpty());
    }
}

