package net.frey.orders.mapper

import net.frey.orders.data.entity.CustomerEntity
import net.frey.orders.data.entity.OrderEntity
import net.frey.orders.data.entity.OrderItemEntity
import net.frey.orders.data.entity.ProductEntity
import net.frey.orders.model.Customer
import net.frey.orders.model.Order
import net.frey.orders.model.Product
import org.apache.commons.lang3.tuple.Pair
import spock.lang.Specification

class OrderMapperTest extends Specification {
    def "Correctly maps RO to Entity"() {
        given:
        def orderRo = new Order(
                new Customer(
                        "Sally",
                        "Ride",
                        "sally@ride.org",
                        "1234 Main",
                        "New York",
                        "NY",
                        "10010",
                        "(123) 456-7890"
                ),
                [
                        Pair.of(
                                new Product(
                                        "Computer",
                                        new BigDecimal("1000.00")
                                ),
                                1
                        ),
                        Pair.of(
                                new Product(
                                        "Desk",
                                        new BigDecimal("200.00")
                                ),
                                1
                        )
                ]
        )

        when:
        def entity = OrderMapper.toEntity(orderRo, customer)

        then:
        def products = entity.products
        products.size() == 2
        products[1].quantity == 1
    }

    def "Correctly maps Entity to RO"() {
        given:
        def orderEntity = new OrderEntity(
            customer: new CustomerEntity(
                firstName: "Sally",
                lastName: "Ride",
                email: "sally@ride.org",
                streetAddress: "1234 Main",
                city: "New York",
                state: "NY",
                zip: "10010",
                phoneNumber: "(123) 456-7890"
            ),
            products: [
                new OrderItemEntity(
                    product: new ProductEntity(
                        name: "Computer",
                        price: new BigDecimal("1000.00")
                    ),
                    quantity: 1
                ),
                new OrderItemEntity(
                    product: new ProductEntity(
                        name: "Desk",
                        price: new BigDecimal("200.00")
                    ),
                    quantity: 1
                )
            ]
        )

        when:
        def order = OrderMapper.toRo(orderEntity)

        then:
        def products = order.products()
        products.size() == 2
        products[0].right == 1
    }
}
