package com.dahdt.ecommerce.modules.inventory.entity;

import com.dahdt.ecommerce.common.entity.BaseEntity;
import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity Tồn kho theo từng chi nhánh (bảng inventories).
 * Quản lý số lượng tồn thực tế của từng biến thể sản phẩm (SKU) tại từng cửa hàng hoặc kho.
 * Kế thừa BaseEntity để tự động quản lý createdAt và updatedAt.
 * Áp dụng cơ chế Khóa lạc quan (@Version) chống hiện tượng bán vượt tồn khi nhiều giao dịch đồng thời.
 */
@Entity
@Table(name = "inventories",
        uniqueConstraints = @UniqueConstraint(name = "UQ_inventories_variant_store", columnNames = {"variant_id", "store_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    /** Khóa chính bản ghi tồn kho */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Biến thể sản phẩm (SKU) đang quản lý tồn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /** Chi nhánh cửa hàng hoặc kho bãi đang lưu giữ hàng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Số lượng tồn thực tế khả dụng (phải >= 0) */
    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    /** Ngưỡng cảnh báo sắp hết hàng (mặc định 5 đôi) */
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 5;

    /** Phiên bản phục vụ cơ chế khóa lạc quan (Optimistic Locking) */
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    // =========================================================================
    // LUẬT NGHIỆP VỤ ĐẶT TRONG ENTITY (RICH DOMAIN MODEL)
    // =========================================================================

    /**
     * Tăng số lượng hàng tồn kho (ví dụ khi Nhập kho, Chuyển kho vào, Hoàn đơn hủy).
     *
     * @param qty Số lượng cần tăng (bắt buộc phải > 0)
     * @throws BadRequestException nếu số lượng tăng <= 0
     */
    public void increase(int qty) {
        if (qty <= 0) {
            throw new BadRequestException("Số lượng tăng kho phải lớn hơn 0 (nhận được: " + qty + ")");
        }
        int current = (this.quantityOnHand != null) ? this.quantityOnHand : 0;
        this.quantityOnHand = current + qty;
    }

    /**
     * Giảm số lượng hàng tồn kho (ví dụ khi Xuất bán tại quầy POS, Xuất đơn Online, Xuất chuyển kho).
     * Tuyệt đối không cho phép tồn kho âm.
     *
     * @param qty Số lượng cần giảm (bắt buộc phải > 0)
     * @throws BadRequestException nếu số lượng giảm <= 0
     * @throws InsufficientStockException nếu số lượng yêu cầu vượt quá số tồn hiện có
     */
    public void deduct(int qty) {
        if (qty <= 0) {
            throw new BadRequestException("Số lượng xuất kho phải lớn hơn 0 (nhận được: " + qty + ")");
        }
        int current = (this.quantityOnHand != null) ? this.quantityOnHand : 0;
        if (current < qty) {
            throw new InsufficientStockException(
                    "Số lượng tồn kho không đủ (hiện có: " + current + ", yêu cầu xuất: " + qty + ")"
            );
        }
        this.quantityOnHand = current - qty;
    }

    /**
     * Kiểm tra sản phẩm tại cửa hàng này đã hết hàng hay chưa (tồn <= 0).
     */
    public boolean isOutOfStock() {
        return this.quantityOnHand == null || this.quantityOnHand <= 0;
    }

    /**
     * Kiểm tra sản phẩm tại cửa hàng có ở mức cảnh báo sắp hết hàng hay không.
     * Điều kiện: Tồn kho lớn hơn 0 và nhỏ hơn hoặc bằng reorder_level.
     */
    public boolean isLow() {
        int threshold = (this.reorderLevel != null) ? this.reorderLevel : 5;
        return this.quantityOnHand != null && this.quantityOnHand > 0 && this.quantityOnHand <= threshold;
    }

    /**
     * Xác định mã trạng thái tồn kho: OUT_OF_STOCK (Hết), LOW_STOCK (Sắp hết), IN_STOCK (Đủ).
     */
    public String getStockStatusCode() {
        if (isOutOfStock()) {
            return "OUT_OF_STOCK";
        } else if (isLow()) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }
}

