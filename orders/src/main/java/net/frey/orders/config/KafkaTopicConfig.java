package net.frey.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.order-topic}")
    private String orderPlacedTopicName;

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(orderPlacedTopicName).partitions(3).replicas(1).build();
    }
}
