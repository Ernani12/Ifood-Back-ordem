Aqui está o comando para testar o POST de pedido:

# PowerShell

$body = @{
  customerId = "CUSTOMER_TEST_001"
  items = @(
    @{
      name = "Pizza Margherita"
      price = 45.50
      quantity = 2
    }
  )
  total = 91.00
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/orders" `
  -Method POST `
  -Body $body `
  -ContentType "application/json" `
  -UseBasicParsing | Select-Object @{Name="Status"; Expression={$_.StatusCode}}, @{Name="Response"; Expression={$_.Content}}


# Ou via cURL (se tiver Git Bash):

curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUSTOMER_TEST_001",
    "items": [{
      "name": "Pizza Margherita",
      "price": 45.50,
      "quantity": 2
    }],
    "total": 91.00
  }'

# Resultado esperado:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUSTOMER_TEST_001",
  "items": [
    {
      "name": "Pizza Margherita",
      "price": 45.50,
      "quantity": 2
    }
  ],
  "total": 91.00,
  "status": "CREATED"
}
