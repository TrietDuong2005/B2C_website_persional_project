package com.dahdt.ecommerce.modules.inventory.fulfillment;

import com.dahdt.ecommerce.modules.store.entity.Store;

/**
 * Interface Strategy xác định nguồn chi nhánh / kho hàng cung ứng cho đơn hàng trực tuyến (Shop Online).
 * Tuân thủ Quy tắc 9 (Mục 10) trong Project Brief.
 */
public interface FulfillmentSourceResolver {

    /**
     * Xác định chi nhánh / kho mặc định làm nguồn xuất hàng cho kênh online.
     *
     * @return Thực thể Store đại diện cho điểm cung ứng
     */
    Store resolveDefaultSource();
}

