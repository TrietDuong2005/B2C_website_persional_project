package com.dahdt.ecommerce.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử SlugUtil - Tiện ích tạo SEO slug tiếng Việt")
class SlugUtilTest {

    @Test
    @DisplayName("Chuyển chuỗi tiếng Việt có dấu thành slug không dấu và nối bằng dấu gạch ngang")
    void testToSlugWithVietnameseDiacritics() {
        String input = "Áo thun nam Cotton Compact Coolmate";
        String expected = "ao-thun-nam-cotton-compact-coolmate";
        assertEquals(expected, SlugUtil.toSlug(input));
    }

    @Test
    @DisplayName("Xử lý ký tự đặc biệt 'đ' và 'Đ' của tiếng Việt")
    void testToSlugWithSpecialVietnameseCharD() {
        String input = "Đồng hồ điện tử Casio cổ điển";
        String expected = "dong-ho-dien-tu-casio-co-dien";
        assertEquals(expected, SlugUtil.toSlug(input));
    }

    @Test
    @DisplayName("Loại bỏ ký tự đặc biệt, dấu câu, dấu ngoặc và ký hiệu")
    void testToSlugWithSpecialCharacters() {
        String input = "Giày sneaker Biti's Hunter X (Special Edition!) & Phụ kiện 2026 #Hot";
        String expected = "giay-sneaker-bitis-hunter-x-special-edition-phu-kien-2026-hot";
        assertEquals(expected, SlugUtil.toSlug(input));
    }

    @Test
    @DisplayName("Gom nhiều khoảng trắng và gạch ngang liên tiếp thành 1 dấu gạch ngang duy nhất")
    void testToSlugWithMultipleSpacesAndDashes() {
        String input = "   Quần   jean ---  nam   Slim   Fit   ";
        String expected = "quan-jean-nam-slim-fit";
        assertEquals(expected, SlugUtil.toSlug(input));
    }

    @Test
    @DisplayName("Trả về chuỗi rỗng khi chuỗi đầu vào là null hoặc chỉ chứa khoảng trắng")
    void testToSlugWithNullOrEmpty() {
        assertEquals("", SlugUtil.toSlug(null));
        assertEquals("", SlugUtil.toSlug(""));
        assertEquals("", SlugUtil.toSlug("    "));
    }
}

