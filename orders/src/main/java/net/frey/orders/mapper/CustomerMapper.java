package net.frey.orders.mapper;

import net.frey.orders.data.entity.CustomerEntity;
import net.frey.orders.model.Customer;

public class CustomerMapper {
    private CustomerMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static CustomerEntity toEntity(Customer customer) {
        var entity = new CustomerEntity();

        entity.setFirstName(customer.firstName());
        entity.setLastName(customer.lastName());
        entity.setEmail(customer.emailAddress());
        entity.setStreetAddress(customer.streetAddress());
        entity.setCity(customer.city());
        entity.setState(customer.state());
        entity.setZip(customer.zip());
        entity.setPhoneNumber(customer.phoneNumber());

        return entity;
    }

    public static Customer toRo(CustomerEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getStreetAddress(),
                entity.getCity(),
                entity.getState(),
                entity.getZip(),
                entity.getPhoneNumber());
    }
}
