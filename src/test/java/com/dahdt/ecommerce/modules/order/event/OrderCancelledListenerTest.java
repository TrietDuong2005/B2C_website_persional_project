package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Observer: Lắng nghe sự kiện hủy đơn hoàn kho (OrderCancelledListener)")
class OrderCancelledListenerTest {

    private InventoryRepository inventoryRepository;
    private InventoryTransactionRepository inventoryTransactionRepository;
    private OrderCancelledListener listener;

    private Store store;
    private User adminUser;
    private ProductVariant variant;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        inventoryTransactionRepository = mock(InventoryTransactionRepository.class);
        listener = new OrderCancelledListener(inventoryRepository, inventoryTransactionRepository);

        store = Store.builder().id(1L).code("WH-TONG").name("Kho Tổng").build();
        adminUser = User.builder().id(99L).email("admin@dahdt.com").build();

        variant = ProductVariant.builder()
                .id(100L)
                .sku("SNK-NIKE-42")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("1200000"))
                .build();

        inventory = Inventory.builder()
                .id(10L)
                .store(store)
                .variant(variant)
                .quantityOnHand(15)
                .version(2L)
                .build();
    }

    @Test
    @DisplayName("onOrderCancelled: Cộng lại tồn kho đúng số lượng và ghi sổ kho CANCEL với created_by chuẩn")
    void testOnOrderCancelled_Success() {
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.saveAndFlush(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem item = OrderItem.builder()
                .variant(variant)
                .productName("Giày Sneaker Nike Air")
                .sku("SNK-NIKE-42")
                .quantity(3)
                .build();

        Order order = Order.builder()
                .id(500L)
                .orderCode("DH20261110001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.CANCELLED)
                .store(store)
                .items(List.of(item))
                .build();

        OrderCancelledEvent event = new OrderCancelledEvent(this, order, adminUser, "Khách hàng đổi ý");

        // Gọi listener
        listener.onOrderCancelled(event);

        // 1. Kiểm tra tồn kho tăng đúng 3 (15 + 3 = 18)
        assertEquals(18, inventory.getQuantityOnHand(), "Tồn kho sau khi hoàn phải là 15 + 3 = 18 đôi");
        verify(inventoryRepository, times(1)).saveAndFlush(inventory);

        // 2. Kiểm tra ghi sổ cái giao dịch kho (Audit Log)
        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());

        InventoryTransaction tx = txCaptor.getValue();
        assertNotNull(tx, "Giao dịch kho không được null");
        assertEquals(InventoryTransactionType.CANCEL, tx.getType(), "Loại giao dịch phải là CANCEL");
        assertEquals(3, tx.getQuantityChange(), "Số lượng biến động phải là DƯƠNG 3 (+3)");
        assertEquals(18, tx.getQuantityAfter(), "Số lượng tồn sau biến động phải là 18");
        assertEquals("ORDER", tx.getReferenceType(), "Loại chứng từ tham chiếu phải là ORDER");
        assertEquals(500L, tx.getReferenceId(), "ID tham chiếu phải là orderId 500");
        assertEquals(adminUser, tx.getCreatedBy(), "Người tạo giao dịch phải là adminUser");
        assertTrue(tx.getNote().contains("DH20261110001"), "Ghi chú phải chứa mã đơn hàng: " + tx.getNote());
        assertTrue(tx.getNote().contains("Khách hàng đổi ý"), "Ghi chú phải chứa lý do hủy: " + tx.getNote());
    }

    @Test
    @DisplayName("onOrderCancelled: Đơn không có mặt hàng thì bỏ qua an toàn không lỗi")
    void testOnOrderCancelled_EmptyItems() {
        Order order = Order.builder()
                .id(501L)
                .orderCode("DH20261110002")
                .store(store)
                .items(List.of())
                .build();

        OrderCancelledEvent event = new OrderCancelledEvent(this, order, adminUser, "Hủy đơn rỗng");
        assertDoesNotThrow(() -> listener.onOrderCancelled(event));

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(inventoryTransactionRepository);
    }
}

