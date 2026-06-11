package net.frey.notifications.model;

import java.util.UUID;

public record OrderEvent(
    UUID orderId,
    UUID customerId,
    String customerEmail,
    String customerName,
    int productCount,
    String status
) {}
