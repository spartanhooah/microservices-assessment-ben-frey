package net.frey.orders

import net.frey.orders.data.entity.CustomerEntity
import net.frey.orders.data.entity.ProductEntity
import net.frey.orders.model.Customer
import net.frey.orders.model.Product

class TestData {
    static def customerEntity() {
        new CustomerEntity(
            firstName: "Sally",
            lastName: "Ride",
            email: "sally@ride.org",
            streetAddress: "1234 Main",
            city: "New York",
            state: "NY",
            zip: "10010",
            phoneNumber: "(123) 456-7890"
        )
    }

    static def productEntity() {
        new ProductEntity(
            name: "Computer",
            price: new BigDecimal("1000.00")
        )
    }
}
