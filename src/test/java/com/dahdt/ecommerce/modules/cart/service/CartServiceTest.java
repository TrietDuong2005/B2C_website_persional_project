package com.dahdt.ecommerce.modules.cart.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import com.dahdt.ecommerce.modules.cart.mapper.CartMapper;
import com.dahdt.ecommerce.modules.cart.repository.CartItemRepository;
import com.dahdt.ecommerce.modules.cart.repository.CartRepository;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ CartService (Giỏ hàng trực tuyến)")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OnlineAvailabilityService onlineAvailabilityService;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product activeProduct;
    private ProductVariant activeVariant;
    private ProductVariant inactiveVariant;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("customer@dahdt.com").build();

        cart = Cart.builder()
                .id(100L)
                .user(user)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        activeProduct = Product.builder()
                .id(10L)
                .name("Nike Air Force 1")
                .status(ProductStatus.ACTIVE)
                .build();

        activeVariant = ProductVariant.builder()
                .id(201L)
                .product(activeProduct)
                .sku("NK-AF1-TRANG-42")
                .color("Trắng")
                .size("42")
                .price(new BigDecimal("2500000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        inactiveVariant = ProductVariant.builder()
                .id(202L)
                .product(activeProduct)
                .sku("NK-AF1-DEN-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("2500000"))
                .status(ProductVariantStatus.INACTIVE) // Tạm ngừng kinh doanh
                .build();
    }

    @Test
    @DisplayName("addItem(): Biến thể không ACTIVE bị chặn ném BadRequestException")
    void addItem_inactiveVariant_shouldThrowBadRequestException() {
        when(productVariantRepository.findById(202L)).thenReturn(Optional.of(inactiveVariant));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cartService.addItem(1L, 202L, 1));

        assertTrue(ex.getMessage().contains("không khả dụng"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem(): Biến thể của sản phẩm HIDDEN bị chặn ném BadRequestException")
    void addItem_hiddenProductVariant_shouldThrowBadRequestException() {
        Product hiddenProduct = Product.builder()
                .id(20L)
                .name("Sản phẩm ẩn")
                .status(ProductStatus.HIDDEN)
                .build();

        ProductVariant variantOfHidden = ProductVariant.builder()
                .id(203L)
                .product(hiddenProduct)
                .status(ProductVariantStatus.ACTIVE)
                .build();

        when(productVariantRepository.findById(203L)).thenReturn(Optional.of(variantOfHidden));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cartService.addItem(1L, 203L, 1));

        assertTrue(ex.getMessage().contains("không khả dụng"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem(): Vượt quá tồn kho online bị chặn ném InsufficientStockException")
    void addItem_stockExceeded_shouldThrowInsufficientStockException() {
        when(productVariantRepository.findById(201L)).thenReturn(Optional.of(activeVariant));
        when(cartRepository.findByUserIdAndStatusWithDetails(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        // Tồn kho online chỉ còn 3 đôi
        when(onlineAvailabilityService.getAvailableQuantity(201L)).thenReturn(3);

        // Khách yêu cầu thêm 4 đôi -> vượt tồn
        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(1L, 201L, 4));

        assertTrue(ex.getMessage().contains("vượt quá"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem(): Cộng dồn với số lượng đã có trong giỏ vượt quá tồn kho bị chặn")
    void addItem_accumulatedStockExceeded_shouldThrowInsufficientStockException() {
        // Đã có sẵn 2 đôi trong giỏ
        cart.addItem(activeVariant, 2);

        when(productVariantRepository.findById(201L)).thenReturn(Optional.of(activeVariant));
        when(cartRepository.findByUserIdAndStatusWithDetails(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        // Tồn kho online hiện chỉ còn 3 đôi
        when(onlineAvailabilityService.getAvailableQuantity(201L)).thenReturn(3);

        // Khách thêm tiếp 2 đôi (tổng sẽ là 4 đôi > tồn 3 đôi)
        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(1L, 201L, 2));

        assertTrue(ex.getMessage().contains("vượt quá"));
        assertEquals(2, cart.getItems().get(0).getQuantity(), "Số lượng không bị thay đổi khi lỗi");
    }

    @Test
    @DisplayName("updateItemQuantity(): Người khác không sửa được dòng giỏ hàng của mình (ném AccessDeniedException)")
    void updateItemQuantity_differentUser_shouldThrowAccessDeniedException() {
        CartItem cartItem = CartItem.builder()
                .id(1001L)
                .cart(cart) // Thuộc về user id = 1
                .variant(activeVariant)
                .quantity(2)
                .build();

        when(cartItemRepository.findByIdWithDetails(1001L)).thenReturn(Optional.of(cartItem));

        // Người dùng ID 999 cố tình sửa giỏ của người dùng ID 1
        assertThrows(AccessDeniedException.class,
                () -> cartService.updateItemQuantity(999L, 1001L, 3));

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeItem(): Người khác không xoá được dòng giỏ hàng của mình (ném AccessDeniedException)")
    void removeItem_differentUser_shouldThrowAccessDeniedException() {
        CartItem cartItem = CartItem.builder()
                .id(1001L)
                .cart(cart) // Thuộc về user id = 1
                .variant(activeVariant)
                .quantity(2)
                .build();

        when(cartItemRepository.findByIdWithDetails(1001L)).thenReturn(Optional.of(cartItem));

        // Người dùng ID 999 cố tình xoá dòng giỏ hàng của người dùng ID 1
        assertThrows(AccessDeniedException.class,
                () -> cartService.removeItem(999L, 1001L));

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Đơn giá và thành tiền luôn lấy từ DB (ProductVariant.price), không nhận từ client")
    void priceAlwaysComesFromDatabase() {
        // Biến thể trong DB có giá niêm yết 2.500.000 đ
        cart.addItem(activeVariant, 2);

        CartItem item = cart.getItems().get(0);
        assertEquals(new BigDecimal("2500000"), item.getVariant().getPrice());
        // lineTotal = 2 * 2.500.000 = 5.000.000 đ
        assertEquals(new BigDecimal("5000000"), item.lineTotal());
        assertEquals(new BigDecimal("5000000"), cart.subtotal());
    }
}

