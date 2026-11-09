package com.dahdt.ecommerce.modules.order.event;

import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử lắng nghe sự kiện đặt đơn trừ tồn kho (OrderPlacedListener)")
class OrderPlacedListenerTest {

    private InventoryRepository inventoryRepository;
    private InventoryTransactionRepository inventoryTransactionRepository;
    private OrderPlacedListener listener;

    private Store store;
    private User customer;
    private ProductVariant variant;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        inventoryTransactionRepository = mock(InventoryTransactionRepository.class);
        listener = new OrderPlacedListener(inventoryRepository, inventoryTransactionRepository);

        store = Store.builder().id(1L).code("WH-TONG").name("Kho Tổng").build();
        customer = User.builder().id(10L).email("khach@trietstore.vn").build();

        variant = ProductVariant.builder()
                .id(100L)
                .sku("SNK-01")
                .color("Đen")
                .size("42")
                .price(new BigDecimal("500000"))
                .build();

        inventory = Inventory.builder()
                .id(50L)
                .store(store)
                .variant(variant)
                .quantityOnHand(20)
                .version(1L)
                .build();
    }

    @Test
    @DisplayName("onOrderPlaced: Trừ kho thành công và ghi sổ kho SALE với thông tin chuẩn xác")
    void testOnOrderPlaced_Success() {
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.saveAndFlush(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem item = OrderItem.builder()
                .variant(variant)
                .productName("Giày Thể Thao")
                .sku("SNK-01")
                .quantity(3)
                .build();

        Order order = Order.builder()
                .id(200L)
                .orderCode("DH202611070001")
                .store(store)
                .customer(customer)
                .items(List.of(item))
                .build();

        OrderPlacedEvent event = new OrderPlacedEvent(this, order);
        listener.onOrderPlaced(event);

        // Kiểm tra số lượng tồn đã bị trừ 3: 20 - 3 = 17
        assertEquals(17, inventory.getQuantityOnHand());
        verify(inventoryRepository, times(1)).saveAndFlush(inventory);

        // Kiểm tra ghi sổ cái giao dịch kho (Audit Log)
        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());

        InventoryTransaction tx = txCaptor.getValue();
        assertNotNull(tx);
        assertEquals(variant, tx.getVariant());
        assertEquals(store, tx.getStore());
        assertEquals(InventoryTransactionType.SALE, tx.getType());
        assertEquals(-3, tx.getQuantityChange());
        assertEquals(17, tx.getQuantityAfter());
        assertEquals("ORDER", tx.getReferenceType());
        assertEquals(200L, tx.getReferenceId());
        assertTrue(tx.getNote().contains("DH202611070001"));
        assertEquals(customer, tx.getCreatedBy());
        assertNotNull(tx.getCreatedAt());
    }

    @Test
    @DisplayName("onOrderPlaced: Ném InsufficientStockException khi tồn kho không đủ")
    void testOnOrderPlaced_InsufficientStock_ThrowsException() {
        inventory.setQuantityOnHand(2); // Chỉ còn 2 đôi
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(inventory));

        OrderItem item = OrderItem.builder()
                .variant(variant)
                .productName("Giày Thể Thao")
                .sku("SNK-01")
                .quantity(5) // Mua 5 đôi
                .build();

        Order order = Order.builder()
                .id(200L)
                .orderCode("DH202611070001")
                .store(store)
                .customer(customer)
                .items(List.of(item))
                .build();

        OrderPlacedEvent event = new OrderPlacedEvent(this, order);

        assertThrows(InsufficientStockException.class, () -> listener.onOrderPlaced(event));
        // Không được lưu sổ cái kho khi bị lỗi
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("onOrderPlaced: Đơn POS thì created_by là thu ngân và note có chữ Bán tại quầy kèm mã đơn")
    void testOnOrderPlaced_PosOrder_CreatedByCashier() {
        User cashier = User.builder().id(30L).email("cashier@trietstore.vn").build();
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.saveAndFlush(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem item = OrderItem.builder()
                .variant(variant)
                .productName("Giày Thể Thao")
                .sku("SNK-01")
                .quantity(2)
                .build();

        Order posOrder = Order.builder()
                .id(300L)
                .orderCode("HD202611080001")
                .channel(OrderChannel.POS)
                .store(store)
                .cashier(cashier)
                .customer(null)
                .items(List.of(item))
                .build();

        OrderPlacedEvent event = new OrderPlacedEvent(this, posOrder);
        listener.onOrderPlaced(event);

        assertEquals(18, inventory.getQuantityOnHand());

        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());

        InventoryTransaction tx = txCaptor.getValue();
        assertNotNull(tx);
        assertEquals(cashier, tx.getCreatedBy(), "CreatedBy của dòng sổ kho cho đơn POS phải là thu ngân (cashier)");
        assertTrue(tx.getNote().contains("Bán tại quầy"), "Note sổ kho đơn POS phải chứa 'Bán tại quầy'");
        assertTrue(tx.getNote().contains("HD202611080001"), "Note sổ kho đơn POS phải chứa mã đơn hàng");
    }
}

