package com.dahdt.ecommerce.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tiện ích tạo slug thân thiện SEO từ chuỗi văn bản tiếng Việt.
 * Bỏ dấu tiếng Việt, thay khoảng trắng và ký tự đặc biệt bằng dấu gạch ngang "-".
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-+");

    private SlugUtil() {
        // Utility class không cho khởi tạo
    }

    /**
     * Chuyển đổi tên tiếng Việt thành slug SEO chuẩn.
     * Ví dụ: "Áo thun nam Cotton Compact (Coolmate)" -> "ao-thun-nam-cotton-compact-coolmate"
     * "Biti's Hunter X" -> "bitis-hunter-x"
     *
     * @param input Chuỗi văn bản gốc
     * @return Slug chuẩn hóa, chữ thường, nối bằng dấu '-'
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.trim();

        // 1. Thay thế ký tự đ/Đ đặc trưng của tiếng Việt trước khi tách dấu
        normalized = normalized.replace("đ", "d").replace("Đ", "D");

        // 2. Xóa các dấu nháy đơn, nháy kép trước khi tách từ (để Biti's -> Bitis -> bitis)
        normalized = normalized.replaceAll("['’`\"]", "");

        // 3. Chuẩn hóa NFD để tách các dấu thanh ra khỏi nguyên âm, sau đó xóa dấu
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 4. Chuyển sang chữ thường
        normalized = normalized.toLowerCase(Locale.ROOT);

        // 5. Thay thế khoảng trắng và ký tự gạch dưới bằng dấu gạch ngang
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");
        normalized = normalized.replace("_", "-");

        // 6. Loại bỏ tất cả ký tự không phải chữ cái latin, số hoặc dấu gạch ngang
        normalized = NON_LATIN.matcher(normalized).replaceAll("-");

        // 7. Gom nhiều dấu gạch ngang liên tiếp thành 1 dấu
        normalized = MULTI_HYPHEN.matcher(normalized).replaceAll("-");

        // 8. Cắt bỏ dấu gạch ngang ở đầu và cuối chuỗi nếu có
        if (normalized.startsWith("-")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("-")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
