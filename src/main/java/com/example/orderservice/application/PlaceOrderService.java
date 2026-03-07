package com.example.orderservice.application;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.in.PlaceOrderUseCase;
import com.example.orderservice.ports.out.OrderRepository;
import com.example.orderservice.adapters.out.KafkaOrderProducer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final KafkaOrderProducer kafkaProducer;

    public PlaceOrderService(OrderRepository orderRepository,
                             KafkaOrderProducer kafkaProducer) {
        this.orderRepository = orderRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Order placeOrder(Order order) {
        // Gera ID e status
        order.setId(UUID.randomUUID());
        order.setStatus(Order.OrderStatus.CREATED);

        // Calcula total do pedido
        double total = order.getItems()
                            .stream()
                            .mapToDouble(item -> item.getPrice() * item.getQuantity())
                            .sum();
        order.setTotal(total);

        // Salva no repositório (Postgres ou Redis)
        Order savedOrder = orderRepository.save(order);

        // Publica evento Kafka
        kafkaProducer.publishOrderCreated(savedOrder);

        return savedOrder;
    }
}