package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE / FACTORY (OrderStateFactory)
 * ============================================================================
 * Vai trò: Factory ánh xạ từ enum OrderStatus sang đối tượng State tương ứng.
 * Lý do sử dụng:
 * 1. Tập trung hóa việc khởi tạo và quản lý vòng đời của các đối tượng State.
 * 2. Cung cấp điểm truy cập thống nhất (Single Point of Access) để lấy đối tượng State
 *    từ mã enum lưu trong cơ sở dữ liệu.
 * 3. Dễ dàng bổ sung thêm State mới vào Spring Context mà không phải can thiệp sửa đổi
 *    các lớp nghiệp vụ khác.
 * ============================================================================
 */
@Component
public class OrderStateFactory {

    private final Map<OrderStatus, OrderState> stateMap = new EnumMap<>(OrderStatus.class);

    public OrderStateFactory(List<OrderState> states) {
        for (OrderState state : states) {
            stateMap.put(state.getStatus(), state);
        }
    }

    /**
     * Lấy đối tượng OrderState tương ứng với mã OrderStatus.
     *
     * @param status Mã trạng thái đơn hàng
     * @return Đối tượng OrderState tương ứng
     * @throws BadRequestException nếu không tìm thấy State cho mã trạng thái
     */
    public OrderState getState(OrderStatus status) {
        if (status == null) {
            throw new BadRequestException("Trạng thái đơn hàng không được để trống.");
        }
        OrderState state = stateMap.get(status);
        if (state == null) {
            throw new BadRequestException("Không tìm thấy đối tượng xử lý trạng thái cho: " + status);
        }
        return state;
    }
}

