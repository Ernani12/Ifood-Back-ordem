package com.example.orderservice.adapters.in;

import com.example.orderservice.adapters.out.KafkaOrderProducer;
import com.example.orderservice.application.RedisCartService;
import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.in.PlaceOrderUseCase;
import com.example.orderservice.ports.in.GetOrderUseCase;
import com.example.orderservice.ports.in.UpdateOrderStatusUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600,
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final RedisCartService redisCartService;
    private final KafkaOrderProducer kafkaOrderProducer;

    public OrderController(
            PlaceOrderUseCase placeOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase,
            RedisCartService redisCartService,
            KafkaOrderProducer kafkaOrderProducer
    ) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.redisCartService = redisCartService;
        this.kafkaOrderProducer = kafkaOrderProducer;
    }

    /** Cria o pedido final, pega do Redis se houver e envia para Kafka */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        if (order.getCustomerId() != null) {
            Order tempOrder = redisCartService.getCart(order.getCustomerId());
            if (tempOrder != null && tempOrder.getItems() != null) {
                order.setItems(tempOrder.getItems());
                order.setTotal(tempOrder.getTotal());
                redisCartService.deleteCart(order.getCustomerId());
            }
        }

        Order created = placeOrderUseCase.placeOrder(order);

        // Envia para Kafka
        kafkaOrderProducer.publishOrderCreated(created);

        return ResponseEntity.ok(created);
    }

    /** Salva o carrinho temporariamente no Redis */
    @PostMapping("/temporary")
    public ResponseEntity<Map<String, String>> saveTemporaryOrder(@RequestBody Order order) {
        if (order.getCustomerId() != null) {
            redisCartService.saveCartTemporarily(order.getCustomerId(), order);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Carrinho salvo com sucesso!");
        return ResponseEntity.ok(response);
    }

    /** Health check */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running!");
    }

    /** Consulta pedido por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return getOrderUseCase.getorderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Atualiza status do pedido */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable UUID id,
                                                   @RequestBody StatusUpdateRequest request) {
        Order updated = updateOrderStatusUseCase.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    public static class StatusUpdateRequest {
        private Order.OrderStatus status;
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
    }

    /** Verifica se há carrinho temporário */
    @GetMapping("/temporary/exists/{customerId}")
    public ResponseEntity<Boolean> checkTemporaryCart(@PathVariable String customerId) {
        return ResponseEntity.ok(redisCartService.cartExists(customerId));
    }

    /** Retorna carrinho temporário */
    @GetMapping("/temporary/{customerId}")
    public ResponseEntity<Order> getTemporaryCart(@PathVariable String customerId) {
        Order order = redisCartService.getCart(customerId);
        return ResponseEntity.ok(order != null ? order : new Order());
    }
}