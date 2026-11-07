package com.dahdt.ecommerce.modules.promotion.strategy;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.promotion.entity.DiscountType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry quản lý và phân phối các DiscountStrategy theo DiscountType.
 * Tự động thu thập các bean triển khai DiscountStrategy thông qua Spring Dependency Injection.
 */
@Component
public class DiscountStrategyRegistry {

    private final Map<DiscountType, DiscountStrategy> strategies = new EnumMap<>(DiscountType.class);

    public DiscountStrategyRegistry(List<DiscountStrategy> strategyList) {
        if (strategyList != null) {
            for (DiscountStrategy strategy : strategyList) {
                strategies.put(strategy.getSupportedType(), strategy);
            }
        }
    }

    /**
     * Lấy Strategy tính giảm giá tương ứng với loại giảm giá của voucher.
     *
     * @param type Loại giảm giá (PERCENT hoặc FIXED)
     * @return DiscountStrategy tương ứng
     * @throws BadRequestException nếu không tìm thấy Strategy hỗ trợ
     */
    public DiscountStrategy getStrategy(DiscountType type) {
        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new BadRequestException("Không tìm thấy chiến lược giảm giá cho loại: " + type);
        }
        return strategy;
    }
}

