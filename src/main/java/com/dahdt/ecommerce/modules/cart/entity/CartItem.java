package com.dahdt.ecommerce.modules.cart.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity Chi tiết món hàng trong giỏ (bảng cart_items).
 * Khớp chính xác với migration V18:
 * - Không lưu giá; đơn giá và giá trị luôn được tính toán trực tiếp từ bảng biến thể.
 * - Ràng buộc UNIQUE (cart_id, variant_id).
 */
@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "UQ_cart_items_cart_variant", columnNames = {"cart_id", "variant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Tính thành tiền của dòng sản phẩm: đơn giá biến thể * số lượng.
     * Giá luôn lấy từ DB của biến thể, không tin giá từ client.
     */
    public BigDecimal lineTotal() {
        if (variant == null || variant.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        int qty = (this.quantity != null && this.quantity > 0) ? this.quantity : 0;
        return variant.getPrice().multiply(BigDecimal.valueOf(qty));
    }

    /**
     * Cập nhật số lượng cho dòng sản phẩm có kiểm tra giới hạn 1..MAX_PER_LINE.
     */
    public void updateQuantity(int newQty) {
        if (newQty < 1) {
            throw new BadRequestException("Số lượng sản phẩm trong giỏ phải lớn hơn hoặc bằng 1.");
        }
        if (newQty > Cart.MAX_PER_LINE) {
            throw new BadRequestException("Số lượng cho mỗi sản phẩm trong giỏ không được vượt quá " + Cart.MAX_PER_LINE + " đôi.");
        }
        this.quantity = newQty;
    }
}

