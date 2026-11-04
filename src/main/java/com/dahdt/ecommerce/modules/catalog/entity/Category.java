package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Danh mục sản phẩm (bảng categories).
 * Mô hình cây phân cấp cha - con tự tham chiếu thông qua cột parent_id.
 * Kế thừa BaseEntity để tự động quản lý created_at và updated_at.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    /** Khóa chính danh mục, tự tăng theo Identity của SQL Server */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Danh mục cha trực tiếp (null nếu là danh mục gốc cấp cao nhất) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /** Danh sách các danh mục con trực thuộc */
    @OneToMany(mappedBy = "parent")
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    /** Tên danh mục hiển thị (ví dụ: Thời trang nam, Áo thun) */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Đường dẫn SEO duy nhất của danh mục */
    @Column(name = "slug", nullable = false, length = 150, unique = true)
    private String slug;

    /** Thứ tự sắp xếp hiển thị trên giao diện menu (số nhỏ đứng trước) */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /** Trạng thái kích hoạt (true: hiển thị, false: ẩn) */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Danh sách sản phẩm thuộc danh mục này */
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Kiểm tra xem danh mục có phải là danh mục gốc (root) hay không.
     */
    public boolean isRoot() {
        return this.parent == null;
    }

    /**
     * Kiểm tra xem danh mục hiện tại có danh mục con trực thuộc hay không.
     */
    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    /**
     * Thêm danh mục con vào danh mục hiện tại và thiết lập quan hệ 2 chiều.
     */
    public void addChild(Category child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }
}

