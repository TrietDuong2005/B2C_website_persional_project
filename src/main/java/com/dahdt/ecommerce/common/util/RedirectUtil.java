package com.dahdt.ecommerce.common.util;

/**
 * Tiện ích kiểm tra và chuẩn hóa đường dẫn chuyển hướng (Redirect URL),
 * ngăn chặn lỗ hổng bảo mật Open Redirect (CWE-601) và HTTP Response Splitting.
 */
public final class RedirectUtil {

    private RedirectUtil() {
        // Utility class, không khởi tạo
    }

    /**
     * Kiểm tra một đường dẫn có phải là đường dẫn nội bộ an toàn hay không.
     * Quy tắc bảo mật bắt buộc:
     * 1. Không rỗng (not null, not blank).
     * 2. Bắt đầu bằng đúng một ký tự "/" (bắt đầu bằng "/", nhưng không bắt đầu bằng "//").
     * 3. Không chứa schema "://" (chặn http://evil.com, https://evil.com, javascript://...).
     * 4. Không chứa dấu gạch chéo ngược "\" (chặn /\evil.com hoặc \evil.com có thể bị trình duyệt chuẩn hóa thành //).
     * 5. Không chứa ký tự xuống dòng (\r, \n) (chặn HTTP Response Splitting / Header Injection).
     *
     * @param url Đường dẫn cần kiểm tra
     * @return true nếu là đường dẫn nội bộ hợp lệ, false nếu không an toàn
     */
    public static boolean isValidRedirect(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String trimmed = url.trim();

        // Phải bắt đầu bằng đúng một dấu "/"
        if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
            return false;
        }

        // Không chứa schema url (://)
        if (trimmed.contains("://")) {
            return false;
        }

        // Không chứa dấu gạch chéo ngược (\)
        if (trimmed.contains("\\")) {
            return false;
        }

        // Không chứa ký tự xuống dòng (\r, \n)
        if (trimmed.contains("\r") || trimmed.contains("\n")) {
            return false;
        }

        return true;
    }

    /**
     * Lấy đường dẫn chuyển hướng an toàn. Nếu URL hợp lệ thì giữ nguyên, ngược lại trả về defaultUrl (mặc định "/").
     *
     * @param url Đường dẫn yêu cầu chuyển hướng
     * @param defaultUrl Đường dẫn dự phòng an toàn
     * @return URL an toàn
     */
    public static String getSafeRedirect(String url, String defaultUrl) {
        if (isValidRedirect(url)) {
            return url.trim();
        }
        return (defaultUrl != null && !defaultUrl.isBlank()) ? defaultUrl : "/";
    }
}

