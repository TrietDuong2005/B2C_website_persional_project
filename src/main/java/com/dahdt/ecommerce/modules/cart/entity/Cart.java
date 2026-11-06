package com.dahdt.ecommerce.modules.cart.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Entity Giỏ hàng trực tuyến (bảng carts).
 * Khớp chính xác với migration V17:
 * - Lưu thông tin phiên giỏ hàng của khách hàng đã đăng nhập.
 * - Trạng thái: ACTIVE, ABANDONED, CONVERTED.
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    /** Giới hạn số lượng tối đa cho mỗi dòng sản phẩm trong giỏ */
    public static final int MAX_PER_LINE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Thêm biến thể vào giỏ:
     * - Nếu biến thể đã có trong giỏ: cộng dồn số lượng.
     * - Số lượng mỗi dòng sau cộng dồn phải từ 1 đến MAX_PER_LINE (10).
     *
     * @param variant Biến thể sản phẩm cần thêm
     * @param qty Số lượng thêm vào (phải >= 1)
     * @return CartItem đã được thêm hoặc cộng dồn
     */
    public CartItem addItem(ProductVariant variant, int qty) {
        if (qty < 1) {
            throw new BadRequestException("Số lượng thêm vào giỏ phải lớn hơn hoặc bằng 1.");
        }
        if (qty > MAX_PER_LINE) {
            throw new BadRequestException("Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + MAX_PER_LINE + " đôi.");
        }

        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        Optional<CartItem> existingItem = this.items.stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + qty;
            if (newQty > MAX_PER_LINE) {
                throw new BadRequestException(
                        "Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + MAX_PER_LINE +
                                " đôi (hiện có: " + item.getQuantity() + ", thêm: " + qty + " = " + newQty + ")."
                );
            }
            item.setQuantity(newQty);
            return item;
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(this)
                    .variant(variant)
                    .quantity(qty)
                    .build();
            this.items.add(newItem);
            return newItem;
        }
    }

    /**
     * Cập nhật số lượng của một dòng sản phẩm theo ID dòng (CartItem.id).
     *
     * @param itemId ID của CartItem
     * @param newQty Số lượng mới (1..MAX_PER_LINE; nếu <= 0 sẽ xóa dòng)
     */
    public void updateItemQuantity(Long itemId, int newQty) {
        if (this.items == null) {
            return;
        }
        if (newQty <= 0) {
            removeItem(itemId);
            return;
        }
        if (newQty > MAX_PER_LINE) {
            throw new BadRequestException("Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + MAX_PER_LINE + " đôi.");
        }

        this.items.stream()
                .filter(item -> item.getId() != null && item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(newQty));
    }

    /**
     * Xóa một dòng khỏi giỏ theo CartItem ID.
     */
    public void removeItem(Long itemId) {
        if (this.items != null && itemId != null) {
            this.items.removeIf(item -> item.getId() != null && item.getId().equals(itemId));
        }
    }

    /**
     * Tính tạm tính của toàn bộ giỏ hàng (tổng lineTotal của các dòng).
     */
    public BigDecimal subtotal() {
        if (isEmpty()) {
            return BigDecimal.ZERO;
        }
        return this.items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Kiểm tra giỏ hàng có rỗng hay không.
     */
    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    /**
     * Lấy tổng số lượng sản phẩm (đôi giày) có trong giỏ hàng.
     */
    public int getTotalQuantity() {
        if (isEmpty()) {
            return 0;
        }
        return this.items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }
}

