package com.dahdt.ecommerce.modules.order.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity Chi tiết mặt hàng trong đơn (bảng order_items).
 * Khớp chính xác với migration V20.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Lưu bản sao ảnh chụp dữ liệu (Snapshot) tại thời điểm mua: tên sản phẩm, mã SKU,
 * màu sắc, kích cỡ, đơn giá và thành tiền, nhằm bảo toàn tính toàn vẹn lịch sử giao dịch.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Đơn hàng chứa dòng sản phẩm này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Biến thể sản phẩm gốc (có thể null nếu biến thể sau này bị xóa trong danh mục) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    /** Snapshot: Tên sản phẩm tại thời điểm mua */
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    /** Snapshot: Mã SKU sản phẩm */
    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    /** Snapshot: Màu sắc */
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    /** Snapshot: Kích cỡ */
    @Column(name = "size", nullable = false, length = 50)
    private String size;

    /** Snapshot: Đơn giá bán tại thời điểm mua */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    /** Số lượng mua */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Snapshot: Thành tiền dòng = unit_price * quantity */
    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal;
}

