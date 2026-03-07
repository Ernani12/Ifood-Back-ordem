package com.example.orderservice.adapters.in;

import com.example.orderservice.application.RedisCartService;
import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.in.PlaceOrderUseCase;
import com.example.orderservice.ports.in.GetOrderUseCase;
import com.example.orderservice.ports.in.UpdateOrderStatusUseCase;

import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200",maxAge = 3600,
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}

)
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final RedisCartService redisCartService;

    public OrderController(
            PlaceOrderUseCase placeOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase,
            RedisCartService redisCartService
    ) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.redisCartService = redisCartService;
    }

    /**
     * Cria o pedido final, verificando primeiro se existe um carrinho temporário no Redis.
     * Se houver, usa o carrinho do Redis e limpa a chave.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
    if (order.getCustomerId() != null) {
        Order tempOrder = redisCartService.getCart(order.getCustomerId());
        if (tempOrder != null && tempOrder.getItems() != null) {
            // Em vez de substituir o objeto todo, garanta que os itens e o cliente estão lá
            order.setItems(tempOrder.getItems());
            order.setTotal(tempOrder.getTotal());
            redisCartService.deleteCart(order.getCustomerId()); 
        }
    }
        Order created = placeOrderUseCase.placeOrder(order);
        return ResponseEntity.ok(created);
    }

    /**
     * Salva o carrinho temporariamente no Redis por 3 minutos
     */
    @PostMapping("/temporary")
    public ResponseEntity<Map<String, String>> saveTemporaryOrder(@RequestBody Order order) {
    if (order.getCustomerId() != null) {
        redisCartService.saveCartTemporarily(order.getCustomerId(), order);
    }

    Map<String, String> response = new HashMap<>();
    response.put("message", "Carrinho salvo com sucesso!");
    return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return getOrderUseCase.getorderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable UUID id,
                                                   @RequestBody StatusUpdateRequest request) {
        Order updated = updateOrderStatusUseCase.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    public static class StatusUpdateRequest {
        private Order.OrderStatus status;

        public Order.OrderStatus getStatus() {
            return status;
        }

        public void setStatus(Order.OrderStatus status) {
            this.status = status;
        }
    }

  @GetMapping("/temporary/exists/{customerId}")
public ResponseEntity<Boolean> checkTemporaryCart(@PathVariable String customerId) {

    boolean exists = redisCartService.cartExists(customerId);

    return ResponseEntity.ok(exists);
}


    @GetMapping("/temporary/{customerId}")
    public ResponseEntity<Order> getTemporaryCart(@PathVariable String customerId) {

    Order order = redisCartService.getCart(customerId);

    if (order == null) {
        return ResponseEntity.ok(new Order()); // retorna objeto vazio
    }

    return ResponseEntity.ok(order);
    }
}