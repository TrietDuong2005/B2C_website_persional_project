package com.dahdt.ecommerce.modules.inventory.entity;

/**
 * Loại giao dịch ghi sổ kho (Audit Log):
 * Khớp hoàn toàn với CHECK trong migration V16:
 * - IMPORT: Nhập kho (nhập hàng mới, nhập từ nhà máy)
 * - SALE: Xuất bán hàng (đơn Online hoặc quầy POS)
 * - CANCEL: Hoàn hàng về kho do hủy đơn
 * - TRANSFER_IN: Nhập chuyển kho đến từ chi nhánh khác
 * - TRANSFER_OUT: Xuất chuyển kho đi sang chi nhánh khác
 * - ADJUST: Điều chỉnh kho sau kiểm kê (tăng hoặc giảm, bắt buộc kèm lý do)
 */
public enum InventoryTransactionType {
    IMPORT,
    SALE,
    CANCEL,
    TRANSFER_IN,
    TRANSFER_OUT,
    ADJUST
}

