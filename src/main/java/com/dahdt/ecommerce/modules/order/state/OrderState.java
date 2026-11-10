package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;

import java.util.Set;

/**
 * ============================================================================
 * DESIGN PATTERN: STATE (Hành vi - Behavioral Pattern)
 * ============================================================================
 * Vai trò: Giao diện trạng thái chung (State Interface).
 * Lý do sử dụng:
 * 1. Đóng gói các quy tắc chuyển đổi trạng thái vòng đời đơn hàng vào từng lớp trạng
 *    thái cụ thể, thay vì dùng các chuỗi if/else hoặc switch/case phức tạp, dàn trải.
 * 2. Mỗi trạng thái tự định nghĩa danh sách các trạng thái kế tiếp hợp lệ, khả năng
 *    được hủy đơn và kiểm tra tính hợp lệ của việc chuyển đổi.
 * 3. Tuân thủ nguyên lý Single Responsibility (SRP) và Open/Closed (OCP): dễ dàng
 *    bổ sung thêm trạng thái hoặc thay đổi luồng nghiệp vụ mà không làm ảnh hưởng
 *    đến các trạng thái khác.
 * ============================================================================
 */
public interface OrderState {

    /**
     * Lấy mã trạng thái đơn hàng tương ứng với State này.
     */
    OrderStatus getStatus();

    /**
     * Lấy tập hợp các trạng thái đích hợp lệ có thể chuyển tới từ trạng thái hiện tại.
     */
    Set<OrderStatus> getValidNextStatuses();

    /**
     * Kiểm tra đơn hàng ở trạng thái hiện tại có được phép hủy hay không.
     * Theo quy tắc nghiệp vụ: Đơn ONLINE chỉ được hủy khi PENDING, CONFIRMED hoặc PACKING.
     */
    boolean canCancel();

    /**
     * Kiểm tra tính hợp lệ của việc chuyển đổi từ trạng thái hiện tại sang trạng thái đích.
     * Ném BadRequestException với thông báo tiếng Việt rõ ràng nếu bước chuyển không hợp lệ.
     *
     * @param targetStatus Trạng thái đích muốn chuyển tới
     * @throws BadRequestException nếu việc chuyển đổi vi phạm quy tắc vòng đời
     */
    default void validateTransition(OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new BadRequestException("Trạng thái đích không được để trống.");
        }
        if (targetStatus == getStatus()) {
            throw new BadRequestException("Đơn hàng hiện đã ở trạng thái [" + getStatus() + "].");
        }
        if (targetStatus == OrderStatus.CANCELLED) {
            if (!canCancel()) {
                throw new BadRequestException("Đơn hàng đang ở trạng thái [" + getStatus() +
                        "] không thể hủy. Chỉ có thể hủy đơn khi đang ở trạng thái Chờ xác nhận, Đã xác nhận hoặc Đang đóng gói.");
            }
            return;
        }
        if (!getValidNextStatuses().contains(targetStatus)) {
            throw new BadRequestException("Không thể chuyển trạng thái đơn hàng từ [" + getStatus() +
                    "] sang [" + targetStatus + "]. Vui lòng tuân thủ quy trình tuần tự của vòng đời đơn.");
        }
    }
}

