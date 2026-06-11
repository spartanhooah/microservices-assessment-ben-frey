package net.frey.orders.model;

import java.util.UUID;

public record Customer(
        UUID id,
        String firstName,
        String lastName,
        String emailAddress,
        String streetAddress,
        String city,
        String state,
        String zip,
        String phoneNumber) {}
