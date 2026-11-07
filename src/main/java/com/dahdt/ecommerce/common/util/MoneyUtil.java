package com.dahdt.ecommerce.common.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Helper định dạng và tính toán tiền tệ Việt Nam Đồng (VNĐ) dùng chung cho toàn bộ hệ thống.
 * Đảm bảo hiển thị thống nhất theo định dạng "1.290.000 đ", tránh định dạng rải rác.
 * Có thể gọi trực tiếp trong code Java hoặc trong Thymeleaf qua {@code @money.format(amount)}.
 *
 * Tiền VND làm tròn về số nguyên (scale 0); DB lưu DECIMAL(18,2) nên giá trị đọc lên có scale 2.
 * LUÔN so sánh bằng compareTo, không dùng equals.
 */
@Component("money")
public class MoneyUtil {

    private static final DecimalFormatSymbols SYMBOLS;

    static {
        SYMBOLS = new DecimalFormatSymbols(new Locale("vi", "VN"));
        SYMBOLS.setGroupingSeparator('.');
    }

    /**
     * Làm tròn số tiền về số nguyên đồng (HALF_UP).
     * Tiền VND làm tròn về số nguyên (scale 0); DB lưu DECIMAL(18,2) nên giá trị đọc lên có scale 2.
     * LUÔN so sánh bằng compareTo, không dùng equals.
     *
     * @param amount Số tiền cần làm tròn
     * @return Số tiền đã làm tròn về số nguyên đồng (scale 0)
     */
    public static BigDecimal roundMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Định dạng số tiền kiểu BigDecimal sang chuỗi hiển thị VNĐ.
     * Ví dụ: 1290000 -> "1.290.000 đ"
     *
     * @param amount Số tiền cần định dạng (nếu null trả về "0 đ")
     * @return Chuỗi định dạng kèm đơn vị "đ"
     */
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }
        DecimalFormat df = new DecimalFormat("#,###", SYMBOLS);
        return df.format(amount) + " đ";
    }

    /**
     * Định dạng số tiền kiểu Long sang chuỗi hiển thị VNĐ.
     */
    public static String format(Long amount) {
        if (amount == null) {
            return "0 đ";
        }
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * Định dạng số tiền kiểu Integer sang chuỗi hiển thị VNĐ.
     */
    public static String format(Integer amount) {
        if (amount == null) {
            return "0 đ";
        }
        return format(BigDecimal.valueOf(amount));
    }
}

