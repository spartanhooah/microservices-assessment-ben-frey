package net.frey.orders.mapper

import net.frey.orders.TestData
import net.frey.orders.model.Customer
import spock.lang.Specification

class CustomerMapperTest extends Specification {
    def "Correctly maps RO to Entity"() {
        given:
        def customerRo = new Customer(
            null,
            "Sally",
            "Ride",
            "sally@ride.org",
            "1234 Main",
            "New York",
            "NY",
            "10010",
            "(123) 456-7890"
        )

        when:
        def entity = CustomerMapper.toEntity(customerRo)

        then:
        entity.firstName == "Sally"
        entity.lastName == "Ride"
        entity.email == "sally@ride.org"
        entity.streetAddress == "1234 Main"
        entity.city == "New York"
        entity.state == "NY"
        entity.zip == "10010"
        entity.phoneNumber == "(123) 456-7890"
    }

    def "Correctly maps Entity to RO"() {
        given:
        def customerEntity = TestData.customerEntity()

        when:
        def customer = CustomerMapper.toRo(customerEntity)

        then:
        customer.firstName() == "Sally"
        customer.lastName() == "Ride"
        customer.emailAddress() == "sally@ride.org"
        customer.streetAddress() == "1234 Main"
        customer.city() == "New York"
        customer.state() == "NY"
        customer.zip() == "10010"
        customer.phoneNumber() == "(123) 456-7890"
    }
}
