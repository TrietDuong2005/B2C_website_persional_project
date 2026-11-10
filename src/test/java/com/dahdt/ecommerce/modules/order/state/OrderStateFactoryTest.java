package com.dahdt.ecommerce.modules.order.state;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử OrderStateFactory: Ánh xạ mã enum OrderStatus sang đối tượng State")
class OrderStateFactoryTest {

    private OrderStateFactory factory;

    @BeforeEach
    void setUp() {
        List<OrderState> states = List.of(
                new PendingState(),
                new ConfirmedState(),
                new PackingState(),
                new ShippingState(),
                new CompletedState(),
                new CancelledState()
        );
        factory = new OrderStateFactory(states);
    }

    @Test
    @DisplayName("getState: Ánh xạ đầy đủ và chính xác tất cả 6 mã OrderStatus")
    void testGetState_Success() {
        assertInstanceOf(PendingState.class, factory.getState(OrderStatus.PENDING), "PENDING phải ra PendingState");
        assertInstanceOf(ConfirmedState.class, factory.getState(OrderStatus.CONFIRMED), "CONFIRMED phải ra ConfirmedState");
        assertInstanceOf(PackingState.class, factory.getState(OrderStatus.PACKING), "PACKING phải ra PackingState");
        assertInstanceOf(ShippingState.class, factory.getState(OrderStatus.SHIPPING), "SHIPPING phải ra ShippingState");
        assertInstanceOf(CompletedState.class, factory.getState(OrderStatus.COMPLETED), "COMPLETED phải ra CompletedState");
        assertInstanceOf(CancelledState.class, factory.getState(OrderStatus.CANCELLED), "CANCELLED phải ra CancelledState");
    }

    @Test
    @DisplayName("getState: Ném BadRequestException khi truyền null")
    void testGetState_Null() {
        assertThrows(BadRequestException.class, () -> factory.getState(null), "Null status phải ném ngoại lệ");
    }
}

