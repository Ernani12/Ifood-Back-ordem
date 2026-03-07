package com.example.orderservice.application;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.in.UpdateOrderStatusUseCase;
import com.example.orderservice.ports.out.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final com.example.orderservice.adapters.out.KafkaOrderProducer producer;

    public UpdateOrderStatusService(OrderRepository orderRepository, 
                                    com.example.orderservice.adapters.out.KafkaOrderProducer producer) {
        this.orderRepository = orderRepository;
        this.producer = producer;
    }

    @Override
    public Order updateStatus(UUID orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        
        // Publicar evento de status atualizado
        if (status == Order.OrderStatus.DELIVERED) {
            producer.publishOrderDelivered(updated);
        }
        
        return updated;
    }
}
