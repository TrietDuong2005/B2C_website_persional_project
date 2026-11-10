package com.dahdt.ecommerce.modules.order.mapper;

import com.dahdt.ecommerce.modules.order.dto.*;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderItem;
import com.dahdt.ecommerce.modules.order.entity.OrderStatusHistory;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper chuyển đổi đối tượng Order và các thành phần liên quan sang DTO.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(target = "customerName", expression = "java(mapCustomerName(order))")
    @Mapping(target = "cashierName", expression = "java(mapCashierName(order))")
    @Mapping(target = "totalQuantity", expression = "java(order.getTotalQuantity())")
    @Mapping(target = "paymentMethod", expression = "java(mapPaymentMethod(order))")
    @Mapping(target = "paymentStatus", expression = "java(mapPaymentStatus(order))")
    OrderResponseDTO toResponseDTO(Order order);

    List<OrderResponseDTO> toResponseDTOList(List<Order> orders);

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(target = "customerName", expression = "java(mapCustomerName(order))")
    @Mapping(target = "cashierName", expression = "java(mapCashierName(order))")
    @Mapping(target = "totalQuantity", expression = "java(order.getTotalQuantity())")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payments", source = "payments")
    @Mapping(target = "statusHistories", source = "statusHistories")
    OrderDetailResponseDTO toDetailDTO(Order order);

    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(target = "imageUrl", ignore = true)
    OrderItemResponseDTO toItemDTO(OrderItem item);

    List<OrderItemResponseDTO> toItemDTOList(List<OrderItem> items);

    PaymentResponseDTO toPaymentDTO(Payment payment);

    List<PaymentResponseDTO> toPaymentDTOList(List<Payment> payments);

    @Mapping(target = "changedByName", expression = "java(mapChangedByName(history))")
    OrderStatusHistoryResponseDTO toStatusHistoryDTO(OrderStatusHistory history);

    List<OrderStatusHistoryResponseDTO> toStatusHistoryDTOList(List<OrderStatusHistory> histories);

    default String mapChangedByName(OrderStatusHistory history) {
        if (history == null || history.getChangedBy() == null) {
            return "Hệ thống";
        }
        if (history.getChangedBy().getProfile() != null &&
                history.getChangedBy().getProfile().getFullName() != null &&
                !history.getChangedBy().getProfile().getFullName().isBlank()) {
            return history.getChangedBy().getProfile().getFullName();
        }
        return history.getChangedBy().getEmail();
    }

    default PaymentMethod mapPaymentMethod(Order order) {
        if (order == null || order.getPayments() == null || order.getPayments().isEmpty()) {
            return null;
        }
        return order.getPayments().get(0).getMethod();
    }

    default PaymentStatus mapPaymentStatus(Order order) {
        if (order == null || order.getPayments() == null || order.getPayments().isEmpty()) {
            return null;
        }
        return order.getPayments().get(0).getStatus();
    }

    default String mapCustomerName(Order order) {
        if (order == null || order.getCustomer() == null) {
            return null;
        }
        if (order.getCustomer().getProfile() != null &&
                order.getCustomer().getProfile().getFullName() != null &&
                !order.getCustomer().getProfile().getFullName().isBlank()) {
            return order.getCustomer().getProfile().getFullName();
        }
        return order.getCustomer().getEmail();
    }

    default String mapCashierName(Order order) {
        if (order == null || order.getCashier() == null) {
            return null;
        }
        if (order.getCashier().getProfile() != null &&
                order.getCashier().getProfile().getFullName() != null &&
                !order.getCashier().getProfile().getFullName().isBlank()) {
            return order.getCashier().getProfile().getFullName();
        }
        return order.getCashier().getEmail();
    }
}
