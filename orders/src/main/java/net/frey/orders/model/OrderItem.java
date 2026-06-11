package net.frey.orders.model;

import java.util.UUID;

public record OrderItem(UUID productId, Integer quantity) {}
