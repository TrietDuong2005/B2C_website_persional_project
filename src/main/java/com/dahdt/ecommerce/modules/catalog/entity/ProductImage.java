package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity Hình ảnh sản phẩm (bảng product_images).
 * Lưu trữ ảnh của sản phẩm chung hoặc gắn riêng cho từng biến thể cụ thể (ví dụ ảnh theo màu).
 */
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

    /** Khóa chính bản ghi ảnh */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sản phẩm cha mà hình ảnh này thuộc về */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Biến thể cụ thể mà ảnh này đại diện (có thể null nếu là ảnh chụp toàn bộ sản phẩm hoặc gắn theo màu) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    /** Màu sắc của sản phẩm mà ảnh này đại diện (NULL nếu là ảnh gắn biến thể cụ thể hoặc ảnh chung cho sản phẩm) */
    @Column(name = "color", length = 50)
    private String color;

    /** Đường dẫn URL trực tiếp của hình ảnh */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /** Thứ tự sắp xếp hiển thị trong bộ sưu tập (số nhỏ xếp trước) */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /** Đánh dấu xem ảnh này có phải là ảnh đại diện thu nhỏ (thumbnail) hay không */
    @Column(name = "is_thumbnail", nullable = false)
    @Builder.Default
    private Boolean isThumbnail = false;
}

