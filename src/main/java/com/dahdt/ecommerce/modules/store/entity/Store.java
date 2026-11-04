package com.dahdt.ecommerce.modules.store.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Điểm bán lẻ hoặc Kho tổng (bảng stores).
 * Quản lý toàn bộ chi nhánh bán hàng và kho bãi của thương hiệu trong chuỗi.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    /** Khóa chính chi nhánh */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã định danh chi nhánh duy nhất (ví dụ: STORE-Q1, WH-TONG) */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /** Tên cửa hàng hoặc kho (ví dụ: Triết Store - Quận 1) */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Loại điểm: STORE (Điểm bán lẻ) hoặc WAREHOUSE (Kho bãi) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private StoreType type = StoreType.STORE;

    /** Tỉnh / Thành phố */
    @Column(name = "province", nullable = false, length = 100)
    private String province;

    /** Quận / Huyện */
    @Column(name = "district", nullable = false, length = 100)
    private String district;

    /** Phường / Xã */
    @Column(name = "ward", nullable = false, length = 100)
    private String ward;

    /** Địa chỉ chi tiết (số nhà, tên đường) */
    @Column(name = "detail_address", nullable = false, length = 500)
    private String detailAddress;

    /** Tọa độ GPS vĩ độ (Latitude: -90 đến 90) */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    /** Tọa độ GPS kinh độ (Longitude: -180 đến 180) */
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Số điện thoại liên hệ cửa hàng */
    @Column(name = "phone", length = 20)
    private String phone;

    /** Trạng thái hoạt động (ACTIVE: đang mở, INACTIVE: ngừng hoạt động) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StoreStatus status = StoreStatus.ACTIVE;

    /** Danh sách nhân viên làm việc tại chi nhánh này */
    @OneToMany(mappedBy = "store")
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    // =========================================================================
    // LUẬT NGHIỆP VỤ & PHƯƠNG THỨC TIỆN ÍCH (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Kiểm tra chi nhánh có đang mở cửa hoạt động hay không.
     */
    public boolean isActive() {
        return StoreStatus.ACTIVE.equals(this.status);
    }

    /**
     * Kiểm tra điểm này có phải là kho tổng hay không.
     */
    public boolean isWarehouse() {
        return StoreType.WAREHOUSE.equals(this.type);
    }

    /**
     * Kiểm tra điểm này có phải là cửa hàng bán lẻ trực tiếp hay không.
     */
    public boolean isStore() {
        return StoreType.STORE.equals(this.type);
    }

    /**
     * Lấy chuỗi địa chỉ đầy đủ để hiển thị.
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (detailAddress != null && !detailAddress.isBlank()) {
            sb.append(detailAddress);
        }
        if (ward != null && !ward.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(ward);
        }
        if (district != null && !district.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(district);
        }
        if (province != null && !province.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(province);
        }
        return sb.toString();
    }
}

