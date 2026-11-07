package com.dahdt.ecommerce.modules.order.factory;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bộ sinh mã đơn hàng duy nhất cho toàn hệ thống.
 * Định dạng: DH + yyyyMMdd + 4 chữ số ngẫu nhiên (ví dụ: DH202611071234).
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
     * Sinh mã đơn hàng duy nhất, kiểm tra chống trùng lặp trong CSDL.
     *
     * @return Chuỗi mã đơn hàng dạng DH + yyyyMMdd + 4 số ngẫu nhiên
     */
    public String generateUniqueOrderCode() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);

        for (int i = 0; i < MAX_RETRIES; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidateCode = "DH" + datePart + randomNum;

            if (!orderRepository.existsByOrderCode(candidateCode)) {
                return candidateCode;
            }
            log.warn("Mã đơn hàng [{}] bị trùng lặp, tiến hành thử lại lần {}/{}", candidateCode, i + 1, MAX_RETRIES);
        }

        // Trường hợp cực hiếm khi 10 lần đều trùng, sử dụng thêm timestamp mili-giây
        int fallbackRandom = ThreadLocalRandom.current().nextInt(100, 1000);
        return "DH" + datePart + (System.currentTimeMillis() % 10000) + fallbackRandom;
    }
}

