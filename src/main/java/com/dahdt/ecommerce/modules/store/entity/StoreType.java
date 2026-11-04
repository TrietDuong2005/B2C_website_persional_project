package com.dahdt.ecommerce.modules.store.entity;

/**
 * Loại điểm hoạt động của chuỗi bán lẻ:
 * - STORE: Điểm bán lẻ trực tiếp (cửa hàng có quầy POS và kho phụ)
 * - WAREHOUSE: Kho bãi trung chuyển / Kho tổng tập trung
 */
public enum StoreType {
    STORE,
    WAREHOUSE
}

