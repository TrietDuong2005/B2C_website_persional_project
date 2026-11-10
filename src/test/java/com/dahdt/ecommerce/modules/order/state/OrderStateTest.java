package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử State Pattern: Vòng đời và quy tắc chuyển đổi trạng thái đơn hàng")
class OrderStateTest {

    private final PendingState pendingState = new PendingState();
    private final ConfirmedState confirmedState = new ConfirmedState();
    private final PackingState packingState = new PackingState();
    private final ShippingState shippingState = new ShippingState();
    private final CompletedState completedState = new CompletedState();
    private final CancelledState cancelledState = new CancelledState();

    // =========================================================================
    // 1. PENDING STATE
    // =========================================================================
    @Test
    @DisplayName("PendingState: Trạng thái hợp lệ, có thể hủy và chuyển sang CONFIRMED")
    void testPendingState() {
        assertEquals(OrderStatus.PENDING, pendingState.getStatus(), "Status phải là PENDING");
        assertTrue(pendingState.canCancel(), "Đơn PENDING phải được phép hủy");
        assertEquals(Set.of(OrderStatus.CONFIRMED), pendingState.getValidNextStatuses(), "PENDING chỉ chuyển sang CONFIRMED");

        // Chuyển hợp lệ
        assertDoesNotThrow(() -> pendingState.validateTransition(OrderStatus.CONFIRMED));
        assertDoesNotThrow(() -> pendingState.validateTransition(OrderStatus.CANCELLED));

        // Chuyển không hợp lệ (nhảy cóc sang SHIPPING, COMPLETED hoặc quay về chính nó)
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> pendingState.validateTransition(OrderStatus.SHIPPING));
        assertTrue(ex1.getMessage().contains("Không thể chuyển trạng thái"), "Thông báo lỗi phải rõ ràng: " + ex1.getMessage());

        assertThrows(BadRequestException.class, () -> pendingState.validateTransition(OrderStatus.COMPLETED));
        assertThrows(BadRequestException.class, () -> pendingState.validateTransition(OrderStatus.PENDING));
        assertThrows(BadRequestException.class, () -> pendingState.validateTransition(null));
    }

    // =========================================================================
    // 2. CONFIRMED STATE
    // =========================================================================
    @Test
    @DisplayName("ConfirmedState: Trạng thái hợp lệ, có thể hủy và chuyển sang PACKING")
    void testConfirmedState() {
        assertEquals(OrderStatus.CONFIRMED, confirmedState.getStatus(), "Status phải là CONFIRMED");
        assertTrue(confirmedState.canCancel(), "Đơn CONFIRMED phải được phép hủy");
        assertEquals(Set.of(OrderStatus.PACKING), confirmedState.getValidNextStatuses(), "CONFIRMED chỉ chuyển sang PACKING");

        assertDoesNotThrow(() -> confirmedState.validateTransition(OrderStatus.PACKING));
        assertDoesNotThrow(() -> confirmedState.validateTransition(OrderStatus.CANCELLED));

        assertThrows(BadRequestException.class, () -> confirmedState.validateTransition(OrderStatus.PENDING), "Không được quay lui về PENDING");
        assertThrows(BadRequestException.class, () -> confirmedState.validateTransition(OrderStatus.COMPLETED), "Không được nhảy cóc sang COMPLETED");
    }

    // =========================================================================
    // 3. PACKING STATE
    // =========================================================================
    @Test
    @DisplayName("PackingState: Trạng thái hợp lệ, có thể hủy và chuyển sang SHIPPING")
    void testPackingState() {
        assertEquals(OrderStatus.PACKING, packingState.getStatus(), "Status phải là PACKING");
        assertTrue(packingState.canCancel(), "Đơn PACKING phải được phép hủy");
        assertEquals(Set.of(OrderStatus.SHIPPING), packingState.getValidNextStatuses(), "PACKING chỉ chuyển sang SHIPPING");

        assertDoesNotThrow(() -> packingState.validateTransition(OrderStatus.SHIPPING));
        assertDoesNotThrow(() -> packingState.validateTransition(OrderStatus.CANCELLED));

        assertThrows(BadRequestException.class, () -> packingState.validateTransition(OrderStatus.CONFIRMED), "Không được quay lui về CONFIRMED");
        assertThrows(BadRequestException.class, () -> packingState.validateTransition(OrderStatus.COMPLETED), "Không được nhảy cóc sang COMPLETED");
    }

    // =========================================================================
    // 4. SHIPPING STATE
    // =========================================================================
    @Test
    @DisplayName("ShippingState: Không thể hủy đơn, chỉ có thể chuyển sang COMPLETED")
    void testShippingState() {
        assertEquals(OrderStatus.SHIPPING, shippingState.getStatus(), "Status phải là SHIPPING");
        assertFalse(shippingState.canCancel(), "Đơn đang SHIPPING tuyệt đối không được phép hủy");
        assertEquals(Set.of(OrderStatus.COMPLETED), shippingState.getValidNextStatuses(), "SHIPPING chỉ chuyển sang COMPLETED");

        assertDoesNotThrow(() -> shippingState.validateTransition(OrderStatus.COMPLETED));

        BadRequestException exCancel = assertThrows(BadRequestException.class, () -> shippingState.validateTransition(OrderStatus.CANCELLED));
        assertTrue(exCancel.getMessage().contains("không thể hủy"), "Thông báo lỗi hủy đơn đang giao: " + exCancel.getMessage());

        assertThrows(BadRequestException.class, () -> shippingState.validateTransition(OrderStatus.PACKING), "Không được quay lui về PACKING");
    }

    // =========================================================================
    // 5. COMPLETED STATE (Terminal)
    // =========================================================================
    @Test
    @DisplayName("CompletedState: Trạng thái kết thúc, không thể hủy và không chuyển tiếp")
    void testCompletedState() {
        assertEquals(OrderStatus.COMPLETED, completedState.getStatus(), "Status phải là COMPLETED");
        assertFalse(completedState.canCancel(), "Đơn COMPLETED không được phép hủy");
        assertTrue(completedState.getValidNextStatuses().isEmpty(), "COMPLETED là trạng thái kết thúc, danh sách kế tiếp rỗng");

        assertThrows(BadRequestException.class, () -> completedState.validateTransition(OrderStatus.CANCELLED));
        assertThrows(BadRequestException.class, () -> completedState.validateTransition(OrderStatus.SHIPPING));
    }

    // =========================================================================
    // 6. CANCELLED STATE (Terminal)
    // =========================================================================
    @Test
    @DisplayName("CancelledState: Trạng thái kết thúc, không thể hủy lại và không chuyển tiếp")
    void testCancelledState() {
        assertEquals(OrderStatus.CANCELLED, cancelledState.getStatus(), "Status phải là CANCELLED");
        assertFalse(cancelledState.canCancel(), "Đơn CANCELLED không thể hủy lại");
        assertTrue(cancelledState.getValidNextStatuses().isEmpty(), "CANCELLED là trạng thái kết thúc");

        assertThrows(BadRequestException.class, () -> cancelledState.validateTransition(OrderStatus.PENDING));
        assertThrows(BadRequestException.class, () -> cancelledState.validateTransition(OrderStatus.CONFIRMED));
    }
}

