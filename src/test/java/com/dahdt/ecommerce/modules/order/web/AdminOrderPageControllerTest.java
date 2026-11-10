package com.dahdt.ecommerce.modules.order.web;

import com.dahdt.ecommerce.modules.order.dto.OrderDetailResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.entity.OrderChannel;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.order.state.*;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Kiểm thử Controller: Quản lý đơn hàng trên Admin (AdminOrderPageController)")
class AdminOrderPageControllerTest {

    private MockMvc mockMvc;
    private OrderStatusService orderStatusService;
    private OrderStateFactory orderStateFactory;
    private StoreScopeService storeScopeService;
    private StoreRepository storeRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        orderStatusService = mock(OrderStatusService.class);
        storeScopeService = mock(StoreScopeService.class);
        storeRepository = mock(StoreRepository.class);

        List<OrderState> states = List.of(
                new PendingState(),
                new ConfirmedState(),
                new PackingState(),
                new ShippingState(),
                new CompletedState(),
                new CancelledState()
        );
        orderStateFactory = new OrderStateFactory(states);

        AdminOrderPageController controller = new AdminOrderPageController(
                orderStatusService,
                orderStateFactory,
                storeScopeService,
                storeRepository
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        adminUser = User.builder().id(1L).email("admin@dahdt.com").build();
        when(storeScopeService.getCurrentUser()).thenReturn(adminUser);
        when(storeScopeService.isCurrentAdmin()).thenReturn(true);
        when(storeScopeService.isCurrentStoreManager()).thenReturn(false);
    }

    @Test
    @DisplayName("GET /admin/orders: Trả về trang admin/order/order-list với danh sách đơn")
    void testListOrders_Success() throws Exception {
        OrderResponseDTO dto = OrderResponseDTO.builder()
                .id(1L)
                .orderCode("DH001")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("500000"))
                .build();

        when(orderStatusService.getOrdersForAdmin(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));
        when(storeRepository.findByStatusOrderByNameAsc(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order/order-list"))
                .andExpect(model().attributeExists("orderPage"))
                .andExpect(model().attributeExists("allStatuses"))
                .andExpect(model().attributeExists("allChannels"));
    }

    @Test
    @DisplayName("GET /admin/orders/{id}: Trả về trang admin/order/order-detail với chi tiết đơn")
    void testViewOrderDetail_Success() throws Exception {
        OrderDetailResponseDTO dto = OrderDetailResponseDTO.builder()
                .id(10L)
                .orderCode("DH010")
                .channel(OrderChannel.ONLINE)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("800000"))
                .build();

        when(orderStatusService.getOrderDetailForAdmin(10L)).thenReturn(dto);

        mockMvc.perform(get("/admin/orders/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order/order-detail"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("canChangeStatus", true))
                .andExpect(model().attribute("canCancel", true));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/status: Chuyển trạng thái và redirect với flash message")
    void testChangeOrderStatus_Redirect() throws Exception {
        doNothing().when(orderStatusService).changeStatus(eq(10L), eq(OrderStatus.CONFIRMED), eq(1L), any());

        mockMvc.perform(post("/admin/orders/10/status")
                        .param("targetStatus", "CONFIRMED")
                        .param("note", "Duyệt đơn"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(orderStatusService, times(1)).changeStatus(eq(10L), eq(OrderStatus.CONFIRMED), eq(1L), eq("Duyệt đơn"));
    }

    @Test
    @DisplayName("POST /admin/orders/{id}/cancel: Hủy đơn và redirect với flash message")
    void testCancelOrder_Redirect() throws Exception {
        doNothing().when(orderStatusService).changeStatus(eq(10L), eq(OrderStatus.CANCELLED), eq(1L), any());

        mockMvc.perform(post("/admin/orders/10/cancel")
                        .param("reason", "Hết hàng kho"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(orderStatusService, times(1)).changeStatus(eq(10L), eq(OrderStatus.CANCELLED), eq(1L), eq("Hết hàng kho"));
    }
}

