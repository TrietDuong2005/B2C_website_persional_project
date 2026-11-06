package com.dahdt.ecommerce.modules.inventory.fulfillment;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử MainWarehouseFulfillmentResolver (Nguồn cung ứng online)")
class MainWarehouseFulfillmentResolverTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MainWarehouseFulfillmentResolver fulfillmentResolver;

    private Store warehouse;

    @BeforeEach
    void setUp() {
        warehouse = Store.builder()
                .id(1L)
                .code("WH-TONG")
                .name("Kho tổng Triết Store")
                .type(StoreType.WAREHOUSE)
                .status(StoreStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("resolveDefaultSource() chọn đúng kho WAREHOUSE đang hoạt động có ID nhỏ nhất")
    void resolveDefaultSource_shouldReturnActiveWarehouse() {
        when(storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE))
                .thenReturn(Optional.of(warehouse));

        Store resolved = fulfillmentResolver.resolveDefaultSource();

        assertNotNull(resolved);
        assertEquals(1L, resolved.getId());
        assertEquals("WH-TONG", resolved.getCode());
        assertEquals(StoreType.WAREHOUSE, resolved.getType());
        verify(storeRepository, times(1)).findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
    }

    @Test
    @DisplayName("resolveDefaultSource() ném lỗi nghiệp vụ rõ ràng khi không có kho tổng nào hoạt động")
    void resolveDefaultSource_whenNoActiveWarehouse_shouldThrowResourceNotFoundException() {
        when(storeRepository.findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> fulfillmentResolver.resolveDefaultSource());

        assertTrue(ex.getMessage().contains("Không tìm thấy kho tổng"));
        verify(storeRepository, times(1)).findFirstByTypeAndStatusOrderByIdAsc(StoreType.WAREHOUSE, StoreStatus.ACTIVE);
    }
}

