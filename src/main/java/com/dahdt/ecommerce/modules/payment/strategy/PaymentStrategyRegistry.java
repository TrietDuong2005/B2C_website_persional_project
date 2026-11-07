package com.dahdt.ecommerce.modules.payment.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry quản lý và phân phối các PaymentStrategy theo PaymentMethod.
 * Tự động đăng ký các bean PaymentStrategy thông qua cơ chế Dependency Injection của Spring Boot.
 * Thiết kế mở để trong Ngày 8-9 chỉ cần bổ sung các triển khai CashPaymentStrategy, CardPaymentStrategy, QrPaymentStrategy.
 */
@Component
public class PaymentStrategyRegistry {

    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentStrategyRegistry(List<PaymentStrategy> strategyList) {
        if (strategyList != null) {
            for (PaymentStrategy strategy : strategyList) {
                strategies.put(strategy.getSupportedMethod(), strategy);
            }
        }
    }

    /**
     * Lấy chiến lược thanh toán phù hợp cho đơn hàng trực tuyến.
     * Kiểm tra chặt chẽ: nếu phương thức không tồn tại hoặc không được hỗ trợ online thì ném lỗi nghiệp vụ.
     *
     * @param method Phương thức thanh toán được khách chọn
     * @return Chiến lược thanh toán tương ứng
     * @throws BadRequestException nếu phương thức không hợp lệ hoặc không hỗ trợ online
     */
    public PaymentStrategy getOnlineStrategy(PaymentMethod method) {
        if (method == null) {
            throw new BadRequestException("Vui lòng chọn phương thức thanh toán.");
        }

        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null || !strategy.isSupportedOnline()) {
            throw new BadRequestException("Phương thức thanh toán [" + method + "] không được hỗ trợ cho đơn hàng online.");
        }

        return strategy;
    }

    /**
     * Lấy chiến lược thanh toán tổng quát theo phương thức (dùng chung cho cả POS sau này).
     *
     * @param method Phương thức thanh toán
     * @return Chiến lược thanh toán tương ứng
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            throw new BadRequestException("Phương thức thanh toán không được để trống.");
        }
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new BadRequestException("Phương thức thanh toán [" + method + "] chưa được cấu hình xử lý.");
        }
        return strategy;
    }
}

