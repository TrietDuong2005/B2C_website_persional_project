package com.dahdt.ecommerce.modules.cart.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.cart.dto.CartItemResponseDTO;
import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import com.dahdt.ecommerce.modules.cart.mapper.CartMapper;
import com.dahdt.ecommerce.modules.cart.repository.CartItemRepository;
import com.dahdt.ecommerce.modules.cart.repository.CartRepository;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ giỏ hàng trực tuyến (CartService).
 * Tuân thủ quy tắc kiến trúc phân lớp, rich domain model và kiểm soát bảo mật theo quyền sở hữu.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final OnlineAvailabilityService onlineAvailabilityService;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(Long userId) {
        if (userId == null) {
            return CartResponseDTO.builder().isEmpty(true).build();
        }

        Optional<Cart> optionalCart = cartRepository.findByUserIdAndStatusWithDetails(userId, CartStatus.ACTIVE);
        if (optionalCart.isEmpty() || optionalCart.get().isEmpty()) {
            return CartResponseDTO.builder()
                    .id(optionalCart.map(Cart::getId).orElse(null))
                    .isEmpty(true)
                    .subtotal(BigDecimal.ZERO)
                    .totalQuantity(0)
                    .build();
        }

        Cart cart = optionalCart.get();
        List<CartItem> items = cart.getItems();

        // Thu thập toàn bộ variantId để truy vấn tồn kho 1 lần duy nhất (chống N+1)
        List<Long> variantIds = items.stream()
                .map(item -> item.getVariant().getId())
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Integer> stockMap = onlineAvailabilityService.getAvailableQuantities(variantIds);

        boolean hasProblem = false;
        List<CartItemResponseDTO> itemDtos = new ArrayList<>();

        for (CartItem item : items) {
            CartItemResponseDTO itemDto = cartMapper.toItemDto(item);
            ProductVariant variant = item.getVariant();

            // Ảnh đại diện món hàng: ưu tiên ảnh riêng của biến thể, nếu không có lấy thumbnail sản phẩm
            String imgUrl = null;
            List<ProductImage> varImages = productImageRepository.findByVariantIdOrderBySortOrderAscIdAsc(variant.getId());
            if (!varImages.isEmpty()) {
                imgUrl = varImages.get(0).getUrl();
            } else if (variant.getProduct() != null) {
                imgUrl = variant.getProduct().getThumbnailUrl();
            }
            if (imgUrl == null || imgUrl.isBlank()) {
                imgUrl = "/images/placeholder-shoe.svg";
            }
            itemDto.setImageUrl(imgUrl);

            // Giá luôn tính theo Entity (lấy từ DB của biến thể)
            itemDto.setUnitPrice(variant.getPrice());
            itemDto.setLineTotal(item.lineTotal());

            // Tồn kho và cảnh báo
            int stock = stockMap.getOrDefault(variant.getId(), 0);
            itemDto.setAvailableStock(stock);

            boolean outOfStock = (stock <= 0);
            boolean stockExceeded = (!outOfStock && item.getQuantity() > stock);
            boolean unavailable = !variant.isSellable();

            itemDto.setOutOfStock(outOfStock);
            itemDto.setStockExceeded(stockExceeded);
            itemDto.setUnavailable(unavailable);

            if (unavailable) {
                itemDto.setWarningMessage("Sản phẩm hiện tạm ngừng kinh doanh.");
                hasProblem = true;
            } else if (outOfStock) {
                itemDto.setWarningMessage("Sản phẩm hiện đã hết hàng trong kho online.");
                hasProblem = true;
            } else if (stockExceeded) {
                itemDto.setWarningMessage("Số lượng trong giỏ (" + item.getQuantity() + " đôi) vượt quá tồn kho (chỉ còn " + stock + " đôi).");
                hasProblem = true;
            }

            itemDtos.add(itemDto);
        }

        return CartResponseDTO.builder()
                .id(cart.getId())
                .items(itemDtos)
                .subtotal(cart.subtotal())
                .totalQuantity(cart.getTotalQuantity())
                .isEmpty(false)
                .hasProblem(hasProblem)
                .build();
    }

    @Override
    public void addItem(Long userId, Long variantId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Số lượng thêm vào giỏ phải lớn hơn hoặc bằng 1.");
        }
        if (quantity > Cart.MAX_PER_LINE) {
            throw new BadRequestException("Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + Cart.MAX_PER_LINE + " đôi.");
        }

        // 1. Kiểm tra biến thể tồn tại và đang ACTIVE của sản phẩm ACTIVE
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm có ID: " + variantId));

        if (!variant.isSellable()) {
            throw new BadRequestException("Sản phẩm hoặc biến thể này hiện không khả dụng để kinh doanh.");
        }

        // 2. Lấy hoặc tạo giỏ hàng ACTIVE cho người dùng
        Cart cart = getOrCreateActiveCart(userId);

        // 3. Tính toán số lượng dự kiến sau khi cộng dồn
        int currentQtyInCart = cart.getItems().stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getId().equals(variantId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        int targetQuantity = currentQtyInCart + quantity;
        if (targetQuantity > Cart.MAX_PER_LINE) {
            throw new BadRequestException(
                    "Số lượng tối đa cho mỗi dòng sản phẩm là " + Cart.MAX_PER_LINE +
                            " đôi (hiện có: " + currentQtyInCart + ", thêm: " + quantity + " = " + targetQuantity + ")."
            );
        }

        // 4. Kiểm tra tồn kho tại kho xuất hàng online
        int availableStock = onlineAvailabilityService.getAvailableQuantity(variantId);
        if (availableStock <= 0) {
            throw new InsufficientStockException("Sản phẩm này hiện đã hết hàng trong kho.");
        }
        if (targetQuantity > availableStock) {
            throw new InsufficientStockException(
                    "Số lượng yêu cầu (" + targetQuantity + " đôi) vượt quá số lượng tồn kho online hiện có (" + availableStock + " đôi)."
            );
        }

        // 5. Thêm/cộng dồn vào giỏ qua phương thức nghiệp vụ của Entity
        cart.addItem(variant, quantity);
        cartRepository.save(cart);
    }

    @Override
    public void updateItemQuantity(Long userId, Long itemId, int quantity) {
        CartItem cartItem = cartItemRepository.findByIdWithDetails(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng có ID: " + itemId));

        // Kiểm tra bảo mật: mỗi người chỉ được sửa dòng thuộc giỏ của chính mình
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa đổi giỏ hàng của người khác.");
        }

        // Nếu số lượng <= 0: thực hiện xóa dòng khỏi giỏ
        if (quantity <= 0) {
            Cart cart = cartItem.getCart();
            cart.removeItem(itemId);
            cartItemRepository.delete(cartItem);
            return;
        }

        if (quantity > Cart.MAX_PER_LINE) {
            throw new BadRequestException("Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + Cart.MAX_PER_LINE + " đôi.");
        }

        // Kiểm tra tồn kho khả dụng online
        int availableStock = onlineAvailabilityService.getAvailableQuantity(cartItem.getVariant().getId());
        if (quantity > availableStock) {
            throw new InsufficientStockException(
                    "Số lượng cập nhật (" + quantity + " đôi) vượt quá số tồn kho online hiện có (" + availableStock + " đôi)."
            );
        }

        cartItem.updateQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findByIdWithDetails(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng có ID: " + itemId));

        // Kiểm tra bảo mật: mỗi người chỉ được xóa dòng thuộc giỏ của chính mình
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa sản phẩm trong giỏ hàng của người khác.");
        }

        Cart cart = cartItem.getCart();
        cart.removeItem(itemId);
        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {
        cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public int countActiveCartItems(Long userId) {
        if (userId == null) {
            return 0;
        }
        return cartRepository.countActiveCartItemQuantity(userId);
    }

    // =========================================================================
    // HÀM NỘI BỘ
    // =========================================================================

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findByUserIdAndStatusWithDetails(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có ID: " + userId));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}

