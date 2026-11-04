package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Thương hiệu sản phẩm (bảng brands).
 * Kế thừa BaseEntity để tự động quản lý created_at và updated_at.
 */
@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    /** Khóa chính thương hiệu */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên thương hiệu (ví dụ: Coolmate, Routine, Biti's...) */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Đường dẫn SEO duy nhất của thương hiệu */
    @Column(name = "slug", nullable = false, length = 150, unique = true)
    private String slug;

    /** Trạng thái kích hoạt (true: đang hợp tác, false: tạm ẩn) */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Danh sách các sản phẩm thuộc thương hiệu này */
    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}

