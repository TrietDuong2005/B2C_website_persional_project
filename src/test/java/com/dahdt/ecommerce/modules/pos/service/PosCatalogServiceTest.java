package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosProductSearchResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử nghiệp vụ tra cứu sản phẩm POS (PosCatalogService)")
class PosCatalogServiceTest {

    private EmployeeRepository employeeRepository;
    private ProductVariantRepository productVariantRepository;
    private InventoryRepository inventoryRepository;
    private PosCatalogService posCatalogService;

    private User cashierUser;
    private Store cashierStore;
    private Employee cashierEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository = Mockito.mock(EmployeeRepository.class);
        productVariantRepository = Mockito.mock(ProductVariantRepository.class);
        inventoryRepository = Mockito.mock(InventoryRepository.class);

        posCatalogService = new PosCatalogServiceImpl(
                employeeRepository,
                productVariantRepository,
                inventoryRepository
        );

        cashierUser = User.builder().id(10L).email("cashier@trietstore.vn").build();
        cashierStore = Store.builder().id(2L).code("STORE-Q1").name("Triết Store - Quận 1").build();
        cashierEmployee = Employee.builder()
                .id(1L)
                .user(cashierUser)
                .store(cashierStore)
                .position(EmployeePosition.CASHIER)
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("search: Tìm kiếm thành công theo SKU khớp chính xác, trả đúng tồn kho chi nhánh thu ngân")
    void testSearch_BySku_Success() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));

        Product shoe = Product.builder().id(1L).name("Giày Sneaker Nam").status(ProductStatus.ACTIVE).build();
        ProductVariant variant = ProductVariant.builder()
                .id(101L)
                .product(shoe)
                .sku("SNK-01-42")
                .barcode("8930000000101")
                .color("Trắng")
                .size("42")
                .price(new BigDecimal("1200000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();

        when(productVariantRepository.searchActiveVariantsForPos(eq("SNK-01-42"), any(Pageable.class)))
                .thenReturn(List.of(variant));

        Inventory storeInventory = Inventory.builder()
                .id(501L)
                .store(cashierStore)
                .variant(variant)
                .quantityOnHand(15)
                .build();

        when(inventoryRepository.findByStoreIdAndVariantIdIn(eq(2L), anyCollection()))
                .thenReturn(List.of(storeInventory));

        List<PosProductSearchResponseDTO> results = posCatalogService.search(10L, "SNK-01-42");

        assertNotNull(results);
        assertEquals(1, results.size());
        PosProductSearchResponseDTO dto = results.get(0);
        assertEquals(101L, dto.getVariantId());
        assertEquals("SNK-01-42", dto.getSku());
        assertEquals("8930000000101", dto.getBarcode());
        assertEquals("Giày Sneaker Nam", dto.getProductName());
        assertEquals("Trắng", dto.getColor());
        assertEquals("42", dto.getSize());
        assertEquals(0, new BigDecimal("1200000").compareTo(dto.getUnitPrice()));
        assertEquals(15, dto.getQuantityOnHand());
    }

    @Test
    @DisplayName("search: Giới hạn tối đa 20 dòng và không lỗi khi danh sách rỗng")
    void testSearch_Limit20Rows() {
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));

        List<ProductVariant> mockList = new ArrayList<>();
        Product shoe = Product.builder().id(1L).name("Giày Thể Thao").status(ProductStatus.ACTIVE).build();
        for (long i = 1; i <= 20; i++) {
            mockList.add(ProductVariant.builder()
                    .id(i)
                    .product(shoe)
                    .sku("SKU-" + i)
                    .barcode("BARCODE-" + i)
                    .color("Đen")
                    .size("40")
                    .price(new BigDecimal("500000"))
                    .status(ProductVariantStatus.ACTIVE)
                    .build());
        }

        when(productVariantRepository.searchActiveVariantsForPos(eq("Giày"), any(Pageable.class)))
                .thenReturn(mockList);
        when(inventoryRepository.findByStoreIdAndVariantIdIn(eq(2L), anyCollection()))
                .thenReturn(Collections.emptyList());

        List<PosProductSearchResponseDTO> results = posCatalogService.search(10L, "Giày");

        assertNotNull(results);
        assertEquals(20, results.size());
        assertEquals(0, results.get(0).getQuantityOnHand()); // Không có inventory thì tồn = 0
    }

    @Test
    @DisplayName("search: Tài khoản không có hồ sơ nhân viên -> ném BadRequestException")
    void testSearch_NoEmployeeProfile_ThrowsException() {
        when(employeeRepository.findByUserId(99L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posCatalogService.search(99L, "sneaker"));
        assertTrue(ex.getMessage().contains("Tài khoản không thuộc cửa hàng nào"));
    }

    @Test
    @DisplayName("search: Nhân viên bị khóa (INACTIVE) -> ném BadRequestException")
    void testSearch_InactiveEmployee_ThrowsException() {
        cashierEmployee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(cashierEmployee));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> posCatalogService.search(10L, "sneaker"));
        assertTrue(ex.getMessage().contains("bị khóa"));
    }
}

