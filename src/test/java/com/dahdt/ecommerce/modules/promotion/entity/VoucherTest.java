package com.dahdt.ecommerce.modules.promotion.entity;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử quy tắc kiểm tra hiệu lực Voucher (Domain Entity)")
class VoucherTest {

    @Test
    @DisplayName("Voucher hợp lệ: không ném ngoại lệ khi thỏa mãn đầy đủ điều kiện")
    void testValidateApplicable_Valid() {
        Voucher voucher = Voucher.builder()
                .code("SUMMER2026")
                .name("Hè 2026")
                .status(VoucherStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(5))
                .endAt(LocalDateTime.now().plusDays(10))
                .usageLimit(100)
                .usedCount(10)
                .minOrderValue(new BigDecimal("300000"))
                .build();

        assertDoesNotThrow(() -> voucher.validateApplicable(new BigDecimal("500000"), LocalDateTime.now()));
    }

    @Test
    @DisplayName("Voucher không hoạt động: ném BadRequestException")
    void testValidateApplicable_InactiveStatus() {
        Voucher voucher = Voucher.builder()
                .code("INACTIVE10")
                .status(VoucherStatus.INACTIVE)
                .startAt(LocalDateTime.now().minusDays(5))
                .endAt(LocalDateTime.now().plusDays(10))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> voucher.validateApplicable(new BigDecimal("500000"), LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("không hoạt động"));
    }

    @Test
    @DisplayName("Voucher chưa bắt đầu: ném BadRequestException")
    void testValidateApplicable_NotStartedYet() {
        Voucher voucher = Voucher.builder()
                .code("FUTURE_VOUCHER")
                .status(VoucherStatus.ACTIVE)
                .startAt(LocalDateTime.now().plusDays(2))
                .endAt(LocalDateTime.now().plusDays(10))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> voucher.validateApplicable(new BigDecimal("500000"), LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("chưa đến thời gian"));
    }

    @Test
    @DisplayName("Voucher đã hết hạn: ném BadRequestException")
    void testValidateApplicable_Expired() {
        Voucher voucher = Voucher.builder()
                .code("OLD2025")
                .status(VoucherStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(30))
                .endAt(LocalDateTime.now().minusDays(1))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> voucher.validateApplicable(new BigDecimal("500000"), LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("đã hết hạn"));
    }

    @Test
    @DisplayName("Voucher đã hết lượt dùng: ném BadRequestException khi usedCount >= usageLimit")
    void testValidateApplicable_UsageLimitReached() {
        Voucher voucher = Voucher.builder()
                .code("LIMITED10")
                .status(VoucherStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(5))
                .endAt(LocalDateTime.now().plusDays(5))
                .usageLimit(50)
                .usedCount(50)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> voucher.validateApplicable(new BigDecimal("500000"), LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("hết lượt"));
    }

    @Test
    @DisplayName("Chưa đạt giá trị đơn tối thiểu: ném BadRequestException")
    void testValidateApplicable_BelowMinOrderValue() {
        Voucher voucher = Voucher.builder()
                .code("MIN500K")
                .status(VoucherStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(5))
                .endAt(LocalDateTime.now().plusDays(5))
                .minOrderValue(new BigDecimal("500000"))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> voucher.validateApplicable(new BigDecimal("450000"), LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("chưa đạt giá trị tối thiểu"));
    }
}

