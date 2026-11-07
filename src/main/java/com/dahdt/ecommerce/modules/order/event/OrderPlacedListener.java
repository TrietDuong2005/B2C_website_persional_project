package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * DESIGN PATTERN: OBSERVER (Concrete Observer / Listener)
 * ============================================================================
 * Vai trò: Lắng nghe sự kiện OrderPlacedEvent để thực hiện trừ tồn kho và ghi sổ kho.
 * Lý do sử dụng:
 * 1. Chạy ĐỒNG BỘ (@EventListener) trong CÙNG TRANSACTION với thao tác đặt hàng:
 *    Nếu bất kỳ biến thể nào không đủ tồn kho hoặc xảy ra xung đột khóa lạc quan
 *    (Optimistic Locking trên cột version của bảng inventories), exception sẽ
 *    lan truyền ra ngoài khiến toàn bộ giao dịch đặt đơn rollback triệt để (ACID).
 * 2. Đảm bảo tính nhất quán giữa đơn hàng và số liệu kho: Không bao giờ xảy ra
 *    tình trạng đơn hàng đã tạo nhưng kho chưa trừ hoặc ghi nhận sai lệch.
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedListener {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    /**
     * Xử lý trừ kho và ghi sổ cái giao dịch kho ngay khi đơn hàng được tạo.
     * Chạy đồng bộ trong cùng transaction của OrderService.placeOrder.
     *
     * @param event Sự kiện OrderPlacedEvent chứa thông tin đơn hàng
     */
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.getOrder();
        log.info("Bắt đầu xử lý trừ kho đồng bộ cho đơn hàng [{}] tại chi nhánh [{}]",
                order.getOrderCode(), order.getStore().getCode());

        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            if (item.getVariant() == null) {
                continue;
            }

            Long variantId = item.getVariant().getId();
            Long storeId = order.getStore().getId();
            int qtyToDeduct = item.getQuantity();

            // 1. Tìm bản ghi tồn kho tương ứng tại kho xuất của đơn
            Inventory inventory = inventoryRepository.findByStoreIdAndVariantId(storeId, variantId)
                    .orElseThrow(() -> new InsufficientStockException(
                            "Không tìm thấy bản ghi tồn kho cho sản phẩm [" + item.getProductName() +
                                    " - SKU: " + item.getSku() + "] tại chi nhánh [" + order.getStore().getName() + "]"
                    ));

            // 2. Trừ tồn kho thông qua phương thức nghiệp vụ của chính Entity Inventory
            // (Ném InsufficientStockException nếu số lượng tồn không đủ, gây rollback toàn bộ)
            inventory.deduct(qtyToDeduct);

            // 3. Lưu và flush ngay để kích hoạt kiểm tra khóa lạc quan (@Version)
            Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);

            // 4. Ghi một dòng vào sổ cái giao dịch kho (Audit Log - Append-Only)
            InventoryTransaction tx = InventoryTransaction.builder()
                    .variant(item.getVariant())
                    .store(order.getStore())
                    .type(InventoryTransactionType.SALE)
                    .quantityChange(-qtyToDeduct)
                    .quantityAfter(savedInventory.getQuantityOnHand())
                    .referenceType("ORDER")
                    .referenceId(order.getId())
                    .note("Xuất kho cho đơn hàng " + order.getOrderCode())
                    .createdBy(order.getCustomer())
                    .createdAt(LocalDateTime.now())
                    .build();

            inventoryTransactionRepository.save(tx);

            log.info("Đã trừ tồn kho thành công: SKU [{}], Chi nhánh [{}], Số lượng [-{}], Tồn sau: [{}]",
                    item.getSku(), order.getStore().getCode(), qtyToDeduct, savedInventory.getQuantityOnHand());
        }
    }
}

