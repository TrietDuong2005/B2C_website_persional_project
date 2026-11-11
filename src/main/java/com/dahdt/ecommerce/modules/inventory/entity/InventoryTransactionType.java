package com.dahdt.ecommerce.modules.inventory.entity;

import lombok.Getter;

/**
 * Loại giao dịch ghi sổ kho (Audit Log):
 * Khớp hoàn toàn với CHECK trong migration V16:
 * - IMPORT: Nhập kho (nhập hàng mới, nhập từ nhà máy)
 * - SALE: Bán hàng (xuất bán hàng cho đơn Online hoặc quầy POS)
 * - CANCEL: Hoàn kho (hủy đơn) (hoàn hàng về kho do đơn hàng bị hủy)
 * - TRANSFER_IN: Chuyển kho đến (nhập chuyển kho đến từ chi nhánh khác)
 * - TRANSFER_OUT: Chuyển kho đi (xuất chuyển kho đi sang chi nhánh khác)
 * - ADJUST: Điều chỉnh (điều chỉnh kho sau kiểm kê, bắt buộc kèm lý do)
 */
@Getter
public enum InventoryTransactionType {
    IMPORT("Nhập kho"),
    SALE("Bán hàng"),
    CANCEL("Hoàn kho (hủy đơn)"),
    TRANSFER_IN("Chuyển kho đến"),
    TRANSFER_OUT("Chuyển kho đi"),
    ADJUST("Điều chỉnh");

    private final String displayName;

    InventoryTransactionType(String displayName) {
        this.displayName = displayName;
    }
}

