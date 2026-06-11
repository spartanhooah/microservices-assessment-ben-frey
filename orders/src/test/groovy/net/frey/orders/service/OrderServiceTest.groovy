package net.frey.orders.service

import net.frey.orders.TestData
import net.frey.orders.data.entity.CustomerEntity
import net.frey.orders.data.entity.OrderEntity
import net.frey.orders.data.entity.ProductEntity
import net.frey.orders.data.repository.CustomerRepository
import net.frey.orders.data.repository.OrderRepository
import net.frey.orders.data.repository.ProductRepository
import net.frey.orders.model.Order
import net.frey.orders.model.OrderEvent
import net.frey.orders.model.OrderItem
import net.frey.orders.model.OrderStatus
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static java.util.UUID.randomUUID

class OrderServiceTest extends Specification {
    def orderRepository = Mock(OrderRepository)
    def customerRepository = Mock(CustomerRepository)
    def productRepository = Mock(ProductRepository)
    def kafkaTemplate = Mock(KafkaTemplate)
    def orderTopic = "test-topic"

    def testSubject = new OrderService(
        orderRepository,
        customerRepository,
        productRepository,
        kafkaTemplate,
        orderTopic
    )

    def "Successfully create an order"() {
        given:
        def orderEntity = buildOrderEntity()
        def order = new Order(
            randomUUID(),
            randomUUID(),
            [
                new OrderItem(
                    randomUUID(),
                    1
                ),
                new OrderItem(
                    randomUUID(),
                    1
                )
            ],
            OrderStatus.NEW
        )

        when:
        def result = testSubject.createOrder(order)

        then:
        1 * customerRepository.findById(_ as UUID) >> Optional.of(orderEntity.getCustomer())
        2 * productRepository.findById(_ as UUID) >> Optional.of(TestData.productEntity())
        1 * orderRepository.save(_ as OrderEntity) >> orderEntity
        1 * kafkaTemplate.send(_ as String, _, _ as OrderEvent) >> CompletableFuture.completedFuture(null)

        result.products().size() == 2
    }

    def "Successfully update an order's status"() {
        given:
        def orderEntity = buildOrderEntity()

        when:
        def result = testSubject.updateStatus(orderEntity.id, OrderStatus.DELIVERED)

        then:
        1 * orderRepository.findById(_ as UUID) >> Optional.of(orderEntity)
        1 * kafkaTemplate.send(_ as String, _, _ as OrderEvent) >> CompletableFuture.completedFuture(null)

        result.products().size() == 2
    }

    def "Status cannot be updated to the same status"() {
        given:
        def status = OrderStatus.NEW
        def orderEntity = buildOrderEntity()

        when:
        def result = testSubject.updateStatus(orderEntity.id, status)

        then:
        1 * orderRepository.findById(_ as UUID) >> Optional.of(orderEntity)
        0 * kafkaTemplate.send(_ as String, _, _ as OrderEvent) >> CompletableFuture.completedFuture(null)

        result.status() == status
    }

    def "Cannot cancel order if status is shipped or delivered"() {
        given:
        def status = orderStatus
        def orderEntity = buildOrderEntity(status)

        when:
        def result = testSubject.updateStatus(orderEntity.id, status)

        then:
        1 * orderRepository.findById(_ as UUID) >> Optional.of(orderEntity)
        0 * kafkaTemplate.send(_ as String, _, _ as OrderEvent) >> CompletableFuture.completedFuture(null)

        result.status() == status

        where:
        orderStatus << [OrderStatus.SHIPPED, OrderStatus.DELIVERED]
    }

    static def buildOrderEntity(status = OrderStatus.NEW) {
        def customerEntity = TestData.customerEntity()
        def orderEntity = new OrderEntity(
            id: randomUUID(),
            customer: customerEntity,
            status: status
        )
        orderEntity.addProduct(
            new ProductEntity(
                name: "Computer",
                price: new BigDecimal("1000.00")),
            1)
        orderEntity.addProduct(
            new ProductEntity(
                name: "Desk",
                price: new BigDecimal("200.00")),
            1)

        orderEntity
    }
}
