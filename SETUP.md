# Sistema Completo de Pedidos - Pizza iFood

## Arquitetura

Este projeto implementa um sistema hexagonal (ports & adapters) com:

### Backend (Java/Spring Boot)
- **OrderServiceApplication**: Aplicação principal Spring Boot
- **OrderController**: Adapter de entrada (REST)
- **PlaceOrderService**: Serviço de aplicação para criar pedidos
- **UpdateOrderStatusService**: Serviço de aplicação para atualizar status
- **GetOrderService**: Serviço de aplicação para recuperar pedidos
- **KafkaOrderProducer**: Adapter de saída (Kafka)
- **InMemoryOrderRepository**: Adapter de persistência (em memória)

### Frontend (Angular)
- **PedidoComponent**: Componente standalone que gerencia pedidos
- **OrderService**: Serviço que comunica com a API REST
- Template HTML com exibição do carrinho e ações

## Fluxo de Funcionamento

### 1. Enviar Pedido
1. Usuário clica em "Enviar Pedido" no Angular
2. Angular envia POST para `http://localhost:8080/orders`
3. Spring cria Order com ID UUID
4. PlaceOrderService calcula o total
5. Salva em InMemoryOrderRepository
6. Publica evento em `orders-created` no Kafka
7. Retorna Order com ID para Angular
8. Angular exibe o ID do pedido

### 2. Entregar Pedido
1. Usuário clica em "Entregar Pedido"
2. Angular envia PUT para `http://localhost:8080/orders/{id}/status`
3. UpdateOrderStatusService atualiza para DELIVERED
4. Publica evento em `orders-delivered` no Kafka (para microserviço de entrega)
5. Retorna Order atualizado

## Endpoints da API

```
POST   /orders                    - Criar novo pedido
GET    /orders/{id}              - Obter pedido por ID
PUT    /orders/{id}/status       - Atualizar status do pedido
```

## Configuração Required

### Backend
```properties
# application.properties
spring.application.name=order-service
server.port=8080

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Frontend
Adicione ao seu `app.config.ts` ou module imports:

```typescript
import { HttpClientModule } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    // ... outras configurações
    provideHttpClient(),
  ]
};
```

## Dependências Maven

O `pom.xml` já contém:
- Spring Boot Starter Web
- Spring Boot Starter Kafka
- Spring Boot Starter Validation
- Lombok
- Jackson (JSON)

## Estado do Pedido

```
CREATED     → Pedido criado
CONFIRMED   → Pedido confirmado
DELIVERED   → Pedido entregue
CANCELLED   → Pedido cancelado
```

## Próximos Passos

1. **Persistência em BD**: Substituir InMemoryOrderRepository por JPA/Hibernate
2. **Microserviço de Entrega**: Criar um serviço que consome eventos de `orders-delivered`
3. **Segurança**: Adicionar Spring Security e autenticação
4. **Validação**: Melhorar validação com Jakarta Validation annotations
5. **Tratamento de Erros**: Implementar GlobalExceptionHandler
6. **Logs**: Adicionar logging estruturado

## Como Executar

### Backend
```bash
mvn clean compile
mvn spring-boot:run
```

### Frontend
```bash
ng serve
```

Acesse: `http://localhost:4200`

## Tópicos Kafka

- `orders-created`: Publicado quando um pedido é criado
- `orders-delivered`: Publicado quando um pedido é entregue
