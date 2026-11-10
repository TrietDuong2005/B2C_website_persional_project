package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * DESIGN PATTERN: OBSERVER (Concrete Observer / Listener)
 * ============================================================================
 * Vai trò: Lắng nghe sự kiện OrderCancelledEvent để thực hiện hoàn kho và ghi sổ cái kho.
 * Lý do sử dụng:
 * 1. Chạy ĐỒNG BỘ (@EventListener) trong CÙNG TRANSACTION với thao tác chuyển trạng
 *    thái CANCELLED của OrderStatusService: Đảm bảo tính toàn vẹn ACID, nếu hoàn kho
 *    xảy ra lỗi thì toàn bộ thao tác hủy đơn sẽ rollback hoàn toàn.
 * 2. Tự động cộng lại số lượng vào đúng kho của đơn (store_id) và ghi một dòng sổ kho
 *    loại CANCEL (theo CHECK constraint của V16), số lượng dương (+), reference_type
 *    'ORDER', reference_id là id đơn, created_by là người hủy, note có mã đơn và lý do.
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledListener {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    /**
     * Xử lý hoàn kho và ghi sổ cái kho khi đơn hàng bị hủy.
     * Chạy đồng bộ trong cùng transaction của OrderStatusService.changeStatus.
     *
     * @param event Sự kiện OrderCancelledEvent chứa thông tin đơn hàng bị hủy
     */
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = event.getOrder();
        User actorUser = event.getActorUser();
        String cancelReason = event.getCancelReason();

        log.info("Bắt đầu xử lý hoàn kho cho đơn hàng bị hủy: Mã [{}] tại chi nhánh [{}]",
                order.getOrderCode(), order.getStore().getCode());

        if (order.getItems() == null || order.getItems().isEmpty()) {
            log.warn("Đơn hàng [{}] không có mặt hàng nào để hoàn kho.", order.getOrderCode());
            return;
        }

        for (OrderItem item : order.getItems()) {
            if (item.getVariant() == null) {
                log.warn("Bỏ qua mặt hàng [{}] trong đơn [{}] vì không liên kết biến thể sản phẩm.",
                        item.getProductName(), order.getOrderCode());
                continue;
            }

            Long variantId = item.getVariant().getId();
            Long storeId = order.getStore().getId();
            int qtyToRestore = item.getQuantity();

            // 1. Tìm bản ghi tồn kho tương ứng tại chi nhánh xuất kho của đơn
            Inventory inventory = inventoryRepository.findByStoreIdAndVariantId(storeId, variantId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy bản ghi tồn kho cho sản phẩm [" + item.getProductName() +
                                    " - SKU: " + item.getSku() + "] tại chi nhánh [" + order.getStore().getName() + "]"
                    ));

            // 2. Tăng số lượng tồn kho (hoàn kho) thông qua phương thức nghiệp vụ của chính Entity
            inventory.increase(qtyToRestore);

            // 3. Lưu và flush ngay để áp dụng kiểm tra khóa lạc quan @Version trên inventories
            Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);

            // 4. Ghi một dòng vào sổ cái giao dịch kho (Audit Log - Append-Only)
            // Loại CANCEL, số lượng dương (+), reference_type 'ORDER', reference_id là id đơn
            String txNote = "Hủy đơn hàng " + order.getOrderCode() +
                    (cancelReason != null && !cancelReason.isBlank() ? " - Lý do: " + cancelReason.trim() : "");

            InventoryTransaction tx = InventoryTransaction.builder()
                    .variant(item.getVariant())
                    .store(order.getStore())
                    .type(InventoryTransactionType.CANCEL)
                    .quantityChange(qtyToRestore)
                    .quantityAfter(savedInventory.getQuantityOnHand())
                    .referenceType("ORDER")
                    .referenceId(order.getId())
                    .note(txNote)
                    .createdBy(actorUser)
                    .createdAt(LocalDateTime.now())
                    .build();

            inventoryTransactionRepository.save(tx);

            log.info("Đã hoàn kho thành công: SKU [{}], Chi nhánh [{}], Số lượng [+{}], Tồn sau hoàn: [{}]",
                    item.getSku(), order.getStore().getCode(), qtyToRestore, savedInventory.getQuantityOnHand());
        }
    }
}

