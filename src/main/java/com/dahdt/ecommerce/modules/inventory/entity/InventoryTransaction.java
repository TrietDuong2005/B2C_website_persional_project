package com.dahdt.ecommerce.modules.inventory.entity;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity Sổ cái giao dịch kho (bảng inventory_transactions).
 * Bảng nhật ký biến động kho theo cơ chế chỉ thêm (Append-Only / Audit Log), tuyệt đối không sửa hay xóa.
 * Khớp hoàn toàn với cấu trúc migration V16.
 */
@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    /** Khóa chính bản ghi giao dịch kho */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Biến thể sản phẩm (SKU) có biến động số lượng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /** Chi nhánh cửa hàng hoặc kho xảy ra giao dịch */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Loại nghiệp vụ kho (IMPORT, SALE, CANCEL, TRANSFER_IN, TRANSFER_OUT, ADJUST) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private InventoryTransactionType type;

    /** Số lượng biến động (+ tăng, - giảm) */
    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    /** Số lượng tồn thực tế ngay sau khi hoàn thành giao dịch (phải >= 0) */
    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    /** Loại chứng từ tham chiếu (ví dụ: MANUAL, ORDER, TRANSFER) */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /** Mã ID của chứng từ tham chiếu (ví dụ: order_id, stock_transfer_id) */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Ghi chú lý do biến động kho (bắt buộc khi ADJUST) */
    @Column(name = "note", length = 500)
    private String note;

    /** Người thực hiện giao dịch (nhân viên hoặc admin đang đăng nhập) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Thời điểm phát sinh giao dịch */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Tự động gán thời điểm tạo giao dịch khi INSERT */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

