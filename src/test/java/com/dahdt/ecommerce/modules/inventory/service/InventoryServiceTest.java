package com.dahdt.ecommerce.modules.inventory.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.dto.InventoryAdjustRequestDTO;
import com.dahdt.ecommerce.modules.inventory.dto.InventoryImportRequestDTO;
import com.dahdt.ecommerce.modules.inventory.dto.InventoryResponseDTO;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.mapper.InventoryMapper;
import com.dahdt.ecommerce.modules.inventory.mapper.InventoryTransactionMapper;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ InventoryService (Nhập kho, Điều chỉnh & Sổ cái)")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Store testStore;
    private ProductVariant testVariant;
    private Inventory testInventory;
    private User testUser;

    @BeforeEach
    void setUp() {
        testStore = Store.builder()
                .id(1L)
                .code("STORE-Q1")
                .name("Triết Store - Quận 1")
                .build();

        Product product = Product.builder().id(10L).name("Giày Sneaker Triết").build();
        testVariant = ProductVariant.builder()
                .id(100L)
                .product(product)
                .sku("SNK-BLK-40")
                .color("Đen")
                .size("40")
                .build();

        testInventory = Inventory.builder()
                .id(500L)
                .store(testStore)
                .variant(testVariant)
                .quantityOnHand(10)
                .reorderLevel(5)
                .version(0L)
                .build();

        testUser = User.builder()
                .id(1L)
                .email("admin@dahdt.com")
                .build();
    }

    @Test
    @DisplayName("Nhập kho (IMPORT): cập nhật số lượng tồn và ghi đúng 1 dòng sổ cái với quantity_change và quantity_after chính xác")
    void importStock_shouldUpdateInventoryAndRecordTransaction() {
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(new InventoryResponseDTO());

        InventoryImportRequestDTO importDTO = InventoryImportRequestDTO.builder()
                .storeId(1L)
                .variantId(100L)
                .quantity(15)
                .note("Nhập đợt 1")
                .build();

        inventoryService.importStock(importDTO, 1L);

        // 1. Kiểm tra tồn kho sau khi tăng: 10 + 15 = 25
        assertEquals(25, testInventory.getQuantityOnHand());
        verify(inventoryRepository, times(1)).save(testInventory);

        // 2. Kiểm tra ghi đúng MỘT dòng giao dịch sổ cái kho
        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());

        InventoryTransaction tx = txCaptor.getValue();
        assertEquals(InventoryTransactionType.IMPORT, tx.getType());
        assertEquals(15, tx.getQuantityChange());
        assertEquals(25, tx.getQuantityAfter());
        assertEquals("MANUAL", tx.getReferenceType());
        assertEquals("Nhập đợt 1", tx.getNote());
        assertEquals(testUser, tx.getCreatedBy());
    }

    @Test
    @DisplayName("Điều chỉnh kho (ADJUST) giảm tồn: ghi nhận biến động âm và tồn sau chính xác")
    void adjustStock_negativeChange_shouldDeductAndRecordTransaction() {
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(new InventoryResponseDTO());

        InventoryAdjustRequestDTO adjustDTO = InventoryAdjustRequestDTO.builder()
                .storeId(1L)
                .variantId(100L)
                .quantityChange(-3)
                .note("Hàng hỏng rách đế")
                .build();

        inventoryService.adjustStock(adjustDTO, 1L);

        // 1. Tồn sau khi giảm: 10 - 3 = 7
        assertEquals(7, testInventory.getQuantityOnHand());

        // 2. Sổ kho ghi nhận biến động -3, tồn sau 7
        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());

        InventoryTransaction tx = txCaptor.getValue();
        assertEquals(InventoryTransactionType.ADJUST, tx.getType());
        assertEquals(-3, tx.getQuantityChange());
        assertEquals(7, tx.getQuantityAfter());
        assertEquals("MANUAL", tx.getReferenceType());
        assertEquals("Hàng hỏng rách đế", tx.getNote());
    }

    @Test
    @DisplayName("Điều chỉnh kho (ADJUST) thiếu lý do (note null hoặc rỗng) thì bị chặn với BadRequestException")
    void adjustStock_withoutNote_shouldThrowBadRequestException() {
        InventoryAdjustRequestDTO adjustDTO1 = InventoryAdjustRequestDTO.builder()
                .storeId(1L)
                .variantId(100L)
                .quantityChange(5)
                .note(null)
                .build();

        assertThrows(BadRequestException.class, () -> inventoryService.adjustStock(adjustDTO1, 1L));

        InventoryAdjustRequestDTO adjustDTO2 = InventoryAdjustRequestDTO.builder()
                .storeId(1L)
                .variantId(100L)
                .quantityChange(5)
                .note("   ")
                .build();

        assertThrows(BadRequestException.class, () -> inventoryService.adjustStock(adjustDTO2, 1L));

        verify(inventoryRepository, never()).save(any());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Điều chỉnh kho (ADJUST) giảm quá số tồn hiện có thì bị ném InsufficientStockException")
    void adjustStock_exceedingQuantity_shouldThrowInsufficientStockException() {
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 100L)).thenReturn(Optional.of(testInventory));

        InventoryAdjustRequestDTO adjustDTO = InventoryAdjustRequestDTO.builder()
                .storeId(1L)
                .variantId(100L)
                .quantityChange(-15) // Hiện tại chỉ có 10
                .note("Kiểm kê thất thoát")
                .build();

        assertThrows(InsufficientStockException.class, () -> inventoryService.adjustStock(adjustDTO, 1L));

        assertEquals(10, testInventory.getQuantityOnHand());
        verify(inventoryTransactionRepository, never()).save(any());
    }
}

