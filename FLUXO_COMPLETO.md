# 🚀 Fluxo Completo: Angular → Order Service → Redis → Kafka

## Status dos Serviços ✅

```
Angular Frontend:        http://localhost:4200/pedido
Order Service (API):     http://localhost:8081/orders
Redis (Armazenamento):   localhost:6379
Kafka (Broker):          localhost:9092
```

## Fluxo de Funcionamento

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Angular clica em "Enviar Pedido"                             │
│    POST http://localhost:4081/orders                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Order Service recebe e processa                              │
│    - Gera UUID para o pedido                                    │
│    - Calcula o total                                            │
│    - Salva no REDIS                                             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. Publica evento no Kafka                                      │
│    Topic: orders-created                                        │
│    Topic: orders-delivered (quando marca como entregue)        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Próximo Microserviço (Entrega) consome do Kafka             │
│    e começa a processar a entrega                               │
└─────────────────────────────────────────────────────────────────┘
```

## Exemplo de Request (Angular → Order Service)

```json
POST http://localhost:8081/orders
Content-Type: application/json

{
  "customerId": "CUSTOMER_001",
  "items": [
    {
      "name": "Pizza Margherita",
      "price": 45.50,
      "quantity": 2
    },
    {
      "name": "Pizza Calabresa",
      "price": 52.00,
      "quantity": 1
    }
  ],
  "total": 143.00
}
```

## Resposta (Order Service → Angular)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUSTOMER_001",
  "items": [
    {
      "name": "Pizza Margherita",
      "price": 45.50,
      "quantity": 2
    },
    {
      "name": "Pizza Calabresa",
      "price": 52.00,
      "quantity": 1
    }
  ],
  "total": 143.00,
  "status": "CREATED"
}
```

## Dentro do Order Service

### 1. Recebe da API (OrderController)
```
POST /orders → createOrder()
```

### 2. Processa (PlaceOrderService)
- Gera UUID: `UUID.randomUUID()`
- Status: `CREATED`
- Calcula total
- Salva no Redis via `OrderRedisRepository`
- Publica no Kafka via `KafkaOrderProducer`

### 3. Armazenamento (Redis)
```
Key: order:550e8400-e29b-41d4-a716-446655440000
Value: { JSON do pedido }
```

### 4. Publicação (Kafka)
```
Topic: orders-created
Message: { JSON do pedido }
```

## Verificar Dados no Redis

```bash
# Conectar ao Redis CLI
docker exec -it redis-pizza redis-cli

# Ver todas as chaves
KEYS *

# Ver um pedido específico
GET order:550e8400-e29b-41d4-a716-446655440000

# Ver quantos pedidos tem
DBSIZE
```

## Monitorar Kafka

```bash
# Ver tópicos criados
docker exec ifood-pizza-pedido-kafka-1 kafka-topics --list --bootstrap-server localhost:9092

# Consumir mensagens em tempo real
docker exec ifood-pizza-pedido-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders-created \
  --from-beginning
```

## Config no Angular (Order Service)

Certifique-se que o `OrderService` tem a **porta corrigida para 8081**:

```typescript
@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8081/orders';  // ✅ PORTA 8081

  createOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order);
  }
}
```

## Checklist para Testar

- [ ] Docker Desktop está rodando
- [ ] Containers ativos: `docker-compose ps`
  - [ ] order-service (8081)
  - [ ] redis-pizza (6379)
  - [ ] kafka (9092)
  - [ ] zookeeper (2181)
- [ ] Angular rodando: `ng serve` (porta 4200)
- [ ] Angular OrderService com porta **8081**
- [ ] Testar health: `curl http://localhost:8081/orders/health`

## Como Testar

### Via PowerShell
```powershell
$body = @{
  customerId = "CUSTOMER_TESTE_001"
  items = @(@{ name = "Pizza"; price = 50; quantity = 1 })
  total = 50
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/orders" `
  -Method POST `
  -Body $body `
  -ContentType "application/json"
```

### Via cURL (Git Bash)
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUSTOMER_001",
    "items": [{"name": "Pizza", "price": 50, "quantity": 1}],
    "total": 50
  }'
```

### Via Angular Button
```typescript
enviarPedido(): void {
  const order = {
    customerId: this.getCustomerId(),
    items: this.cart,
    total: this.total
  };
  
  this.orderService.createOrder(order).subscribe({
    next: (response) => {
      console.log('✅ Pedido salvo no Redis:', response.id);
      // Pedido está em: Redis
      // Evento publicado em: Kafka topic 'orders-created'
    }
  });
}
```

## Próximos Passos

1. **Microserviço de Entrega** consume `orders-created` do Kafka
2. **Microserviço de Entrega** atualiza status em seu próprio banco
3. Quando entrega realizada, publica `orders-delivered` 
4. **Order Service** recebe e atualiza pedido no Redis
