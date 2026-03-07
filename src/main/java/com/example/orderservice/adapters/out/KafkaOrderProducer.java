package com.example.orderservice.adapters.out;

import com.example.orderservice.domain.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderProducer {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public KafkaOrderProducer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Order order) {
        kafkaTemplate.send("orders-created", order);
    }

    public void publishOrderDelivered(Order order) {
        kafkaTemplate.send("orders-delivered", order);
    }
}