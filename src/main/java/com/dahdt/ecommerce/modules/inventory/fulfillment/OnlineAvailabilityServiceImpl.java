package com.dahdt.ecommerce.modules.inventory.fulfillment;

import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ tính toán khả dụng tồn kho online (OnlineAvailabilityService).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnlineAvailabilityServiceImpl implements OnlineAvailabilityService {

    private final FulfillmentSourceResolver fulfillmentSourceResolver;
    private final InventoryRepository inventoryRepository;

    @Override
    public Store getSourceWarehouse() {
        return fulfillmentSourceResolver.resolveDefaultSource();
    }

    @Override
    public int getAvailableQuantity(Long variantId) {
        if (variantId == null) {
            return 0;
        }
        Store sourceWarehouse = getSourceWarehouse();
        return inventoryRepository.findByStoreIdAndVariantId(sourceWarehouse.getId(), variantId)
                .map(Inventory::getQuantityOnHand)
                .filter(qty -> qty != null && qty > 0)
                .orElse(0);
    }

    @Override
    public Map<Long, Integer> getAvailableQuantities(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Store sourceWarehouse = getSourceWarehouse();
        List<Inventory> inventories = inventoryRepository.findByStoreIdAndVariantIdIn(sourceWarehouse.getId(), variantIds);

        Map<Long, Integer> resultMap = inventories.stream()
                .filter(inv -> inv.getVariant() != null && inv.getVariant().getId() != null)
                .collect(Collectors.toMap(
                        inv -> inv.getVariant().getId(),
                        inv -> (inv.getQuantityOnHand() != null && inv.getQuantityOnHand() > 0) ? inv.getQuantityOnHand() : 0,
                        (existing, replacement) -> existing
                ));

        // Điền 0 cho các biến thể không có bản ghi tồn kho
        for (Long id : variantIds) {
            if (id != null) {
                resultMap.putIfAbsent(id, 0);
            }
        }

        return resultMap;
    }

    @Override
    public boolean isAvailable(Long variantId, int requestedQty) {
        if (requestedQty <= 0) {
            return false;
        }
        return getAvailableQuantity(variantId) >= requestedQty;
    }
}

