package com.dahdt.ecommerce.modules.inventory.entity;

import com.dahdt.ecommerce.modules.inventory.dto.InventoryTransactionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho enum InventoryTransactionType và nhãn tiếng Việt trên DTO (InventoryTransactionTypeTest).
 * Ràng buộc:
 * - Kiểm tra đủ mọi giá trị của enum (IMPORT, SALE, CANCEL, TRANSFER_IN, TRANSFER_OUT, ADJUST).
 * - Khẳng định nhãn hiển thị tiếng Việt đồng bộ và chính xác.
 * - Tuân thủ quy tắc 17: Mọi assert đều có thông điệp mô tả kèm giá trị thật.
 */
@DisplayName("Kiểm thử đơn vị: Ánh xạ nhãn tiếng Việt loại giao dịch kho (InventoryTransactionTypeTest)")
class InventoryTransactionTypeTest {

    @Test
    @DisplayName("1. Khẳng định số lượng giá trị enum khớp chính xác 6 loại giao dịch của CSDL")
    void testEnumValuesCount() {
        InventoryTransactionType[] types = InventoryTransactionType.values();
        assertEquals(6, types.length,
                "Enum InventoryTransactionType phải có đúng 6 giá trị theo CSDL. Kích thước thực tế: " + types.length);
    }

    @ParameterizedTest(name = "Kiểm tra nhãn hiển thị tiếng Việt của loại: {0}")
    @EnumSource(InventoryTransactionType.class)
    @DisplayName("2. Mọi giá trị enum đều có displayName tiếng Việt hợp lệ, không null và không rỗng")
    void testAllEnumValuesHaveNonEmptyDisplayName(InventoryTransactionType type) {
        String displayName = type.getDisplayName();
        assertNotNull(displayName,
                "displayName của " + type.name() + " không được null");
        assertFalse(displayName.isBlank(),
                "displayName của " + type.name() + " không được để trống. Giá trị thực tế: '" + displayName + "'");
    }

    @Test
    @DisplayName("3. Khẳng định chính xác nhãn tiếng Việt của từng loại giao dịch kho")
    void testExactDisplayNames() {
        assertEquals("Nhập kho", InventoryTransactionType.IMPORT.getDisplayName(),
                "Nhãn của IMPORT phải là 'Nhập kho'. Thực tế: " + InventoryTransactionType.IMPORT.getDisplayName());

        assertEquals("Bán hàng", InventoryTransactionType.SALE.getDisplayName(),
                "Nhãn của SALE phải là 'Bán hàng'. Thực tế: " + InventoryTransactionType.SALE.getDisplayName());

        assertEquals("Hoàn kho (hủy đơn)", InventoryTransactionType.CANCEL.getDisplayName(),
                "Nhãn của CANCEL phải là 'Hoàn kho (hủy đơn)'. Thực tế: " + InventoryTransactionType.CANCEL.getDisplayName());

        assertEquals("Chuyển kho đến", InventoryTransactionType.TRANSFER_IN.getDisplayName(),
                "Nhãn của TRANSFER_IN phải là 'Chuyển kho đến'. Thực tế: " + InventoryTransactionType.TRANSFER_IN.getDisplayName());

        assertEquals("Chuyển kho đi", InventoryTransactionType.TRANSFER_OUT.getDisplayName(),
                "Nhãn của TRANSFER_OUT phải là 'Chuyển kho đi'. Thực tế: " + InventoryTransactionType.TRANSFER_OUT.getDisplayName());

        assertEquals("Điều chỉnh", InventoryTransactionType.ADJUST.getDisplayName(),
                "Nhãn của ADJUST phải là 'Điều chỉnh'. Thực tế: " + InventoryTransactionType.ADJUST.getDisplayName());
    }

    @Test
    @DisplayName("4. InventoryTransactionResponseDTO: getTypeLabel() đồng bộ với getDisplayName() của enum")
    void testDtoTypeLabelSynchronization() {
        for (InventoryTransactionType type : InventoryTransactionType.values()) {
            InventoryTransactionResponseDTO dto = InventoryTransactionResponseDTO.builder()
                    .type(type)
                    .build();

            assertEquals(type.getDisplayName(), dto.getTypeLabel(),
                    "DTO getTypeLabel() cho loại " + type.name() + " phải khớp với getDisplayName(). Thực tế: " + dto.getTypeLabel());
        }

        // Trường hợp type là null
        InventoryTransactionResponseDTO nullTypeDto = new InventoryTransactionResponseDTO();
        assertEquals("Khác", nullTypeDto.getTypeLabel(),
                "DTO getTypeLabel() khi type null phải là 'Khác'. Thực tế: " + nullTypeDto.getTypeLabel());
    }
}

