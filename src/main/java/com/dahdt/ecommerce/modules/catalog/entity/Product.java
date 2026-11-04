package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Sản phẩm cha (bảng products).
 * Đại diện cho một mẫu sản phẩm chung (ví dụ: "Áo sơ mi nam Oxford"),
 * gom nhóm các biến thể màu sắc và kích cỡ cụ thể ở bảng product_variants.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    /** Khóa chính sản phẩm */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Danh mục mà sản phẩm này trực thuộc (bắt buộc) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Thương hiệu của sản phẩm (có thể null nếu sản phẩm không có thương hiệu cụ thể) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** Tên sản phẩm mẫu */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Đường dẫn SEO duy nhất của sản phẩm */
    @Column(name = "slug", nullable = false, length = 300, unique = true)
    private String slug;

    /** Mô tả chi tiết về sản phẩm, chất liệu, hướng dẫn sử dụng */
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    /** Đường dẫn URL hình ảnh đại diện (thumbnail) của sản phẩm */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** Trạng thái hiển thị (ACTIVE: đang bán, HIDDEN: ẩn) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /** Danh sách các biến thể bán hàng (màu sắc, kích cỡ, SKU) */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    /** Bộ sưu tập hình ảnh chi tiết của sản phẩm */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Thêm biến thể mới vào sản phẩm cha và đồng bộ quan hệ 2 chiều.
     */
    public void addVariant(ProductVariant variant) {
        if (this.variants == null) {
            this.variants = new ArrayList<>();
        }
        this.variants.add(variant);
        variant.setProduct(this);
    }

    /**
     * Xóa biến thể khỏi sản phẩm cha.
     */
    public void removeVariant(ProductVariant variant) {
        if (this.variants != null) {
            this.variants.remove(variant);
            variant.setProduct(null);
        }
    }

    /**
     * Thêm hình ảnh mới vào bộ sưu tập ảnh sản phẩm.
     */
    public void addImage(ProductImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
        image.setProduct(this);
    }

    /**
     * Xóa hình ảnh khỏi sản phẩm.
     */
    public void removeImage(ProductImage image) {
        if (this.images != null) {
            this.images.remove(image);
            image.setProduct(null);
        }
    }

    /**
     * Kiểm tra sản phẩm cha có đang ở trạng thái kinh doanh hay không.
     */
    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(this.status);
    }
}

