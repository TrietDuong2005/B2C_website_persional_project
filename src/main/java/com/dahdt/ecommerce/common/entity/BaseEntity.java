package com.dahdt.ecommerce.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base entity chứa các trường audit dùng chung cho tất cả bảng.
 * Mọi entity khác chỉ cần "extends BaseEntity" để tự động có created_at, updated_at.
 *
 * Sử dụng @PrePersist và @PreUpdate để tự động gán thời gian khi tạo/sửa bản ghi,
 * không cần set thủ công trong service.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    /** Thời điểm tạo bản ghi — tự động gán, không cho sửa */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật gần nhất — tự động gán mỗi khi save */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Tự động gán thời gian khi tạo bản ghi mới (INSERT) */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Tự động cập nhật thời gian khi sửa bản ghi (UPDATE) */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
