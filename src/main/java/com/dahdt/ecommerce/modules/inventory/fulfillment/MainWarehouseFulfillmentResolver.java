package com.dahdt.ecommerce.modules.inventory.fulfillment;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Triển khai mặc định của FulfillmentSourceResolver:
 * Chọn kho có type = WAREHOUSE đang hoạt động (ACTIVE) với ID nhỏ nhất trong cơ sở dữ liệu.
 * Nếu không có kho nào thỏa mãn, ném lỗi nghiệp vụ rõ ràng (ResourceNotFoundException).
 */
@Component
@RequiredArgsConstructor
public class MainWarehouseFulfillmentResolver implements FulfillmentSourceResolver {

    private final StoreRepository storeRepository;

    @Override
    public Store resolveDefaultSource() {
        return storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy kho tổng (WAREHOUSE) nào đang hoạt động để làm nguồn cung ứng online."
                ));
    }
}

