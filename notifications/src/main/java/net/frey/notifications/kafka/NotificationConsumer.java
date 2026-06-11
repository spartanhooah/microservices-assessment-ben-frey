package net.frey.notifications.kafka;

import lombok.extern.slf4j.Slf4j;
import net.frey.notifications.model.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {
    @KafkaListener(topics = "orders.topic")
    public void consume(OrderEvent payload) {
        log.info("Sending notification to {}'s email {} that their order of {} items is in status {}",
            payload.customerName(), payload.customerEmail(), payload.productCount(), payload.status());
    }
}
