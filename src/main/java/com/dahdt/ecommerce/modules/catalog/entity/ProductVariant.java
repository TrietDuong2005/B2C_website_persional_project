package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Biến thể sản phẩm (bảng product_variants).
 * Đơn vị lưu kho và bán hàng thực tế (SKU).
 * Mỗi biến thể có màu sắc (color), kích cỡ (size), giá bán, giá vốn và mã vạch riêng.
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    /** Khóa chính biến thể */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sản phẩm cha của biến thể */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Mã quản lý đơn vị lưu kho duy nhất (SKU - Stock Keeping Unit) */
    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    /** Mã vạch sản phẩm để quét nhanh bằng máy quét mã vạch tại quầy POS */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /** Màu sắc của biến thể (ví dụ: Đen, Trắng, Xanh Navy...) */
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    /** Kích cỡ của biến thể (ví dụ: S, M, L, XL, 39, 40...) */
    @Column(name = "size", nullable = false, length = 50)
    private String size;

    /** Giá bán niêm yết của biến thể (phải > 0) */
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    /** Giá vốn sản phẩm phục vụ thống kê lợi nhuận (>= 0) */
    @Column(name = "cost_price", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    /** Trọng lượng đóng gói tính theo gram để tính phí vận chuyển */
    @Column(name = "weight_gram", nullable = false)
    @Builder.Default
    private Integer weightGram = 0;

    /** Trạng thái kinh doanh biến thể (ACTIVE: đang bán, INACTIVE: tạm ngừng) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductVariantStatus status = ProductVariantStatus.ACTIVE;

    /** Bộ sưu tập ảnh riêng của biến thể (nếu có, ví dụ ảnh chụp theo màu này) */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Kiểm tra xem biến thể có thể đưa vào kinh doanh bán hàng hay không.
     * Điều kiện: Biến thể có status = ACTIVE và sản phẩm cha có status = ACTIVE.
     */
    public boolean isSellable() {
        return ProductVariantStatus.ACTIVE.equals(this.status)
                && this.product != null
                && ProductStatus.ACTIVE.equals(this.product.getStatus());
    }
}

