package com.dahdt.ecommerce.modules.user.mapper;

import com.dahdt.ecommerce.modules.user.dto.AddressRequestDTO;
import com.dahdt.ecommerce.modules.user.entity.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử AddressMapper (Ánh xạ DTO sang Entity và cờ mặc định)")
class AddressMapperTest {

    private final AddressMapper addressMapper = Mappers.getMapper(AddressMapper.class);

    @Test
    @DisplayName("toEntity(): Tạo địa chỉ với cờ mặc định = true thì entity có cờ mặc định = true")
    void testToEntity_withIsDefaultTrue_shouldMapCorrectly() {
        AddressRequestDTO dto = new AddressRequestDTO();
        dto.setRecipientName("Nguyễn Văn A");
        dto.setPhone("0901234567");
        dto.setProvince("TP. Hồ Chí Minh");
        dto.setDistrict("Quận 1");
        dto.setWard("Phường Bến Nghé");
        dto.setDetailAddress("123 Lê Lợi");
        dto.setDefault(true);
        dto.setType("shipping");
        dto.setLatitude(new BigDecimal("10.7768890"));
        dto.setLongitude(new BigDecimal("106.7008060"));

        Address entity = addressMapper.toEntity(dto);

        assertNotNull(entity);
        assertTrue(entity.isDefault(), "Entity phải nhận đúng cờ isDefault = true từ DTO");
        assertEquals("Nguyễn Văn A", entity.getRecipientName());
        assertEquals("0901234567", entity.getPhone());
        assertEquals("TP. Hồ Chí Minh", entity.getProvince());
        assertEquals("Quận 1", entity.getDistrict());
        assertEquals("Phường Bến Nghé", entity.getWard());
        assertEquals("123 Lê Lợi", entity.getDetailAddress());
        assertEquals("shipping", entity.getType());
        assertEquals(new BigDecimal("10.7768890"), entity.getLatitude());
        assertEquals(new BigDecimal("106.7008060"), entity.getLongitude());
    }

    @Test
    @DisplayName("updateEntityFromDto(): Cập nhật địa chỉ với cờ mặc định = true thì entity có cờ mặc định = true")
    void testUpdateEntityFromDto_withIsDefaultTrue_shouldMapCorrectly() {
        AddressRequestDTO dto = new AddressRequestDTO();
        dto.setRecipientName("Trần Thị B");
        dto.setDefault(true);

        Address entity = new Address();
        entity.setDefault(false);

        addressMapper.updateEntityFromDto(dto, entity);

        assertTrue(entity.isDefault(), "Entity phải cập nhật đúng cờ isDefault = true");
        assertEquals("Trần Thị B", entity.getRecipientName());
    }
}

