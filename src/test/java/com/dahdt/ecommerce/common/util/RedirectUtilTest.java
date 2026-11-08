package com.dahdt.ecommerce.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử bảo mật tiện ích RedirectUtil (Chống lỗ hổng Open Redirect)")
class RedirectUtilTest {

    @Test
    @DisplayName("isValidRedirect(): URL ngoài chứa http://evil.com bị từ chối")
    void testExternalHttpUrl_shouldBeInvalid() {
        assertFalse(RedirectUtil.isValidRedirect("http://evil.com"));
        assertFalse(RedirectUtil.isValidRedirect("http://evil.com/login"));
    }

    @Test
    @DisplayName("isValidRedirect(): URL bắt đầu bằng //evil.com (protocol-relative) bị từ chối")
    void testProtocolRelativeUrl_shouldBeInvalid() {
        assertFalse(RedirectUtil.isValidRedirect("//evil.com"));
        assertFalse(RedirectUtil.isValidRedirect("//evil.com/test"));
    }

    @Test
    @DisplayName("isValidRedirect(): URL chứa gạch chéo ngược /\\evil.com bị từ chối")
    void testBackslashUrl_shouldBeInvalid() {
        assertFalse(RedirectUtil.isValidRedirect("/\\evil.com"));
        assertFalse(RedirectUtil.isValidRedirect("\\evil.com"));
        assertFalse(RedirectUtil.isValidRedirect("/test\\path"));
    }

    @Test
    @DisplayName("isValidRedirect(): URL nội bộ hợp lệ kèm tham số tiếng Việt đã mã hóa")
    void testValidInternalUrls_shouldBeValid() {
        assertTrue(RedirectUtil.isValidRedirect("/shop/products/abc?color=%C4%90en"));
        assertTrue(RedirectUtil.isValidRedirect("/shop/products"));
        assertTrue(RedirectUtil.isValidRedirect("/shop/products?category=giay&sort=price_asc"));
        assertTrue(RedirectUtil.isValidRedirect("/"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://attacker.org",
            "javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "/path\r\nInjected-Header: bad",
            "/path\nInjected: bad",
            "",
            "   "
    })
    @DisplayName("isValidRedirect(): Các dạng URL độc hại hoặc không hợp lệ khác đều bị từ chối")
    void testMaliciousOrEmptyUrls_shouldBeInvalid(String input) {
        assertFalse(RedirectUtil.isValidRedirect(input));
    }

    @Test
    @DisplayName("isValidRedirect(): Giá trị null trả về false")
    void testNullUrl_shouldBeInvalid() {
        assertFalse(RedirectUtil.isValidRedirect(null));
    }

    @Test
    @DisplayName("isValidRedirect(): Từ chối đường dẫn bắt đầu bằng /login, /logout, /error để tránh vòng lặp")
    void testLoopProneUrls_shouldBeInvalid() {
        assertFalse(RedirectUtil.isValidRedirect("/login"));
        assertFalse(RedirectUtil.isValidRedirect("/login?redirect=/login"));
        assertFalse(RedirectUtil.isValidRedirect("/logout"));
        assertFalse(RedirectUtil.isValidRedirect("/logout?redirect=/shop"));
        assertFalse(RedirectUtil.isValidRedirect("/error"));
        assertFalse(RedirectUtil.isValidRedirect("/error?status=500"));
    }

    @Test
    @DisplayName("getSafeRedirect(): Trả về đúng URL khi hợp lệ và fallback về mặc định khi không hợp lệ")
    void testGetSafeRedirect() {
        assertEquals("/shop/products", RedirectUtil.getSafeRedirect("/shop/products", "/"));
        assertEquals("/", RedirectUtil.getSafeRedirect("http://evil.com", "/"));
        assertEquals("/shop/products", RedirectUtil.getSafeRedirect("//evil.com", "/shop/products"));
        assertEquals("/", RedirectUtil.getSafeRedirect("/login?redirect=/login", "/"));
        assertEquals("/", RedirectUtil.getSafeRedirect("/login", "/"));
        assertEquals("/", RedirectUtil.getSafeRedirect("/logout", "/"));
        assertEquals("/", RedirectUtil.getSafeRedirect("/error", "/"));
    }
}

