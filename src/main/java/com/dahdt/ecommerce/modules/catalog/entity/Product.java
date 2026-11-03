package com.dahdt.ecommerce.modules.catalog.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "draft";

    @Column(name = "product_type", nullable = false, length = 50)
    @Builder.Default
    private String productType = "variant";

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
