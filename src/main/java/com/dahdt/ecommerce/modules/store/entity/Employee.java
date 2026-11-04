package com.dahdt.ecommerce.modules.store.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity Nhân viên chuỗi (bảng employees).
 * Liên kết một tài khoản người dùng (users) với một cửa hàng cụ thể (stores) và chức vụ đảm nhiệm.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    /** Khóa chính hồ sơ nhân viên */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tài khoản người dùng được gán hồ sơ nhân viên (quan hệ 1-1 duy nhất) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Chi nhánh cửa hàng hoặc kho mà nhân viên đang công tác */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Vị trí chức vụ: MANAGER, CASHIER, STAFF */
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 20)
    @Builder.Default
    private EmployeePosition position = EmployeePosition.STAFF;

    /** Trạng thái làm việc (ACTIVE: đang làm, INACTIVE: đã khóa/nghỉ việc) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    // =========================================================================
    // LUẬT NGHIỆP VỤ & PHƯƠNG THỨC TIỆN ÍCH (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Kiểm tra nhân viên có đang ở trạng thái làm việc tích cực hay không.
     */
    public boolean isActive() {
        return EmployeeStatus.ACTIVE.equals(this.status);
    }

    /**
     * Kiểm tra nhân viên có phải Quản lý cửa hàng hay không.
     */
    public boolean isManager() {
        return EmployeePosition.MANAGER.equals(this.position);
    }

    /**
     * Kiểm tra nhân viên có phải Thu ngân quầy hay không.
     */
    public boolean isCashier() {
        return EmployeePosition.CASHIER.equals(this.position);
    }

    /**
     * Cập nhật vị trí chức vụ mới cho nhân viên.
     */
    public void changePosition(EmployeePosition newPosition) {
        if (newPosition != null) {
            this.position = newPosition;
        }
    }

    /**
     * Điều chuyển nhân viên sang công tác tại chi nhánh khác.
     */
    public void transferStore(Store newStore) {
        if (newStore != null) {
            this.store = newStore;
        }
    }
}

