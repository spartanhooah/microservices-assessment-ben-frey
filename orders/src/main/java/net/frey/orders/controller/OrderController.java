package net.frey.orders.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.frey.orders.model.Order;
import net.frey.orders.model.OrderStatus;
import net.frey.orders.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        var savedOrder = orderService.createOrder(order);

        return ResponseEntity.ok(savedOrder);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable UUID orderId, @RequestBody OrderStatus status) {
        var updatedOrder = orderService.updateStatus(orderId, status);

        return ResponseEntity.ok(updatedOrder);
    }
}
