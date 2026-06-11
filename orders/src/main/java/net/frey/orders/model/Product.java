package net.frey.orders.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(UUID id, String name, BigDecimal price) {}
