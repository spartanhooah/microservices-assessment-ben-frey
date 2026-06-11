package net.frey.orders.service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import net.frey.orders.config.TenantContext;
import net.frey.orders.data.entity.OrderEntity;
import net.frey.orders.data.repository.CustomerRepository;
import net.frey.orders.data.repository.OrderRepository;
import net.frey.orders.data.repository.ProductRepository;
import net.frey.orders.mapper.OrderMapper;
import net.frey.orders.model.Order;
import net.frey.orders.model.OrderEvent;
import net.frey.orders.model.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final String orderTopic;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            KafkaTemplate<String, OrderEvent> kafkaTemplate,
            @Value("${app.kafka.order-topic}") String orderTopic) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderTopic = orderTopic;
    }

    public Order createOrder(Order order) {
        var entity = new OrderEntity();
        var customer = customerRepository
                .findById(order.customerId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Customer with ID + " + order.customerId() + " not found"));
        entity.setCustomer(customer);
        entity.setStatus(order.status());
        order.products().forEach(product -> {
            var productEntity = productRepository
                    .findById(product.productId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Product with ID + " + product.productId() + " not found"));
            entity.addProduct(productEntity, product.quantity());
        });

        var savedEntity = orderRepository.save(entity);

        var outgoingOrder = OrderMapper.toRo(savedEntity);
        sendOrderEvent(savedEntity);

        return outgoingOrder;
    }

    public Order updateStatus(UUID orderId, OrderStatus status) {
        var entity = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID + " + orderId + " not found"));

        if (entity.getStatus() == status) {
            log.warn("Order {} already has status {}", orderId, status);

            return OrderMapper.toRo(entity);
        } else if (entity.getStatus() == OrderStatus.SHIPPED || entity.getStatus() == OrderStatus.DELIVERED) {
            log.error("Too late to cancel order {}!", orderId);

            return OrderMapper.toRo(entity);
        }

        entity.setStatus(status);
        orderRepository.save(entity);

        Order outgoingOrder = OrderMapper.toRo(entity);

        sendOrderEvent(entity);

        return outgoingOrder;
    }

    private void sendOrderEvent(OrderEntity savedEntity) {
        var event = new OrderEvent(
                savedEntity.getId(),
                savedEntity.getCustomer().getId(),
                savedEntity.getCustomer().getEmail(),
                savedEntity.getCustomer().getFirstName() + " "
                        + savedEntity.getCustomer().getLastName(),
                savedEntity.getProducts().size(),
                savedEntity.getStatus().toString());

        try {
            kafkaTemplate
                    .send(orderTopic, TenantContext.getCurrentTenant(), event)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send order creation event for order {}.", savedEntity.getId(), e);
            throw new RuntimeException(e);
        }
    }
}
