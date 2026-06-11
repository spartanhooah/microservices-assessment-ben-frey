package net.frey.orders.model;

import java.util.List;
import java.util.UUID;

public record Order(UUID id, UUID customerId, List<OrderItem> products, OrderStatus status) {}
