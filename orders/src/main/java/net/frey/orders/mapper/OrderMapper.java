package net.frey.orders.mapper;

import net.frey.orders.data.entity.OrderEntity;
import net.frey.orders.model.Order;
import net.frey.orders.model.OrderItem;

public class OrderMapper {
    private OrderMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Order toRo(OrderEntity orderEntity) {
        var orderItems = orderEntity.getProducts().stream()
                .map(orderItem -> new OrderItem(orderItem.getProduct().getId(), orderItem.getQuantity()))
                .toList();

        return new Order(orderEntity.getId(), orderEntity.getCustomer().getId(), orderItems, orderEntity.getStatus());
    }
}
