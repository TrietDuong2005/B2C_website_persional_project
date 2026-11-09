package com.dahdt.ecommerce.modules.order.factory;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bộ sinh mã đơn hàng duy nhất cho toàn hệ thống theo từng kênh bán hàng.
 * Định dạng:
 * - Đơn Online: Tiền tố DH + yyyyMMdd + 4 chữ số ngẫu nhiên (ví dụ: DH202611081234).
 * - Đơn POS:    Tiền tố HD + yyyyMMdd + 4 chữ số ngẫu nhiên (ví dụ: HD202611085678).
 * Dễ đọc, thân thiện với người dùng và hỗ trợ cơ chế thử lại (retry) khi phát hiện trùng mã trong CSDL.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCodeGenerator {

    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_RETRIES = 10;

    /**
     * Sinh mã đơn hàng duy nhất mặc định cho kênh online (giữ nguyên tương thích mã cũ).
     *
     * @return Chuỗi mã đơn hàng dạng DH + yyyyMMdd + 4 số ngẫu nhiên
     */
    public String generateUniqueOrderCode() {
        return generateUniqueOrderCode(OrderChannel.ONLINE);
    }

    /**
     * Sinh mã đơn hàng duy nhất theo kênh bán hàng chỉ định:
     * - OrderChannel.POS    -> Tiền tố "HD"
     * - OrderChannel.ONLINE -> Tiền tố "DH"
     *
     * @param channel Kênh bán hàng (ONLINE hoặc POS)
     * @return Chuỗi mã đơn hàng duy nhất
     */
    public String generateUniqueOrderCode(OrderChannel channel) {
        String prefix = (channel == OrderChannel.POS) ? "HD" : "DH";
        String datePart = LocalDate.now().format(DATE_FORMATTER);

        for (int i = 0; i < MAX_RETRIES; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidateCode = prefix + datePart + randomNum;

            if (!orderRepository.existsByOrderCode(candidateCode)) {
                return candidateCode;
            }
            log.warn("Mã đơn hàng [{}] bị trùng lặp, tiến hành thử lại lần {}/{}", candidateCode, i + 1, MAX_RETRIES);
        }

        // Trường hợp cực hiếm khi 10 lần đều trùng, sử dụng thêm timestamp mili-giây
        int fallbackRandom = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + datePart + (System.currentTimeMillis() % 10000) + fallbackRandom;
    }
}

