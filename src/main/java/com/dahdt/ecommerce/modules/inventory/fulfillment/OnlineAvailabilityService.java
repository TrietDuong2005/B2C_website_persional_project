package com.dahdt.ecommerce.modules.inventory.fulfillment;

import com.dahdt.ecommerce.modules.store.entity.Store;

import java.util.Collection;
import java.util.Map;

/**
 * Service tính toán số lượng tồn kho khả dụng cho kênh bán lẻ trực tuyến (Shop).
 * Dựa trên nguồn kho cung ứng được giải quyết bởi FulfillmentSourceResolver.
 */
public interface OnlineAvailabilityService {

    /**
     * Lấy kho nguồn mặc định dùng cho kênh online.
     */
    Store getSourceWarehouse();

    /**
     * Lấy số lượng tồn khả dụng của một biến thể sản phẩm tại kho nguồn online.
     *
     * @param variantId ID của biến thể sản phẩm (SKU)
     * @return Số lượng tồn khả dụng (>= 0)
     */
    int getAvailableQuantity(Long variantId);

    /**
     * Lấy số lượng tồn khả dụng cho nhiều biến thể cùng lúc trong một câu truy vấn (chống N+1).
     *
     * @param variantIds Danh sách ID các biến thể sản phẩm
     * @return Map ánh xạ từ variantId sang số lượng tồn khả dụng
     */
    Map<Long, Integer> getAvailableQuantities(Collection<Long> variantIds);

    /**
     * Kiểm tra xem biến thể có đủ số lượng yêu cầu tại kho online hay không.
     *
     * @param variantId ID của biến thể sản phẩm
     * @param requestedQty Số lượng muốn mua
     * @return true nếu tồn kho >= requestedQty, ngược lại false
     */
    boolean isAvailable(Long variantId, int requestedQty);
}

