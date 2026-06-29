# Purchase app

## Run the application

```
docker compose up
mvn spring-boot:run
```

## Examples

### Create a purchase

```
curl --location 'http://localhost:8080/purchases' \
--header 'x-idempotency-key: 8e404d74-7d0a-498c-92e9-72d73a4cba92' \
--header 'Content-Type: application/json' \
--data '{
    "description": "Any value",
    "transactionDate": "2026-05-03T10:15:30-03:00",
    "amount": "10.99"
}'
```

#### Response
```
{
    "id": "fcf9e76e-79b0-48a4-82e0-93da2ad4b43f",
    "description": "Any value",
    "transactionDate": "2026-05-03T13:15:30Z",
    "amount": 10.99,
    "convertedAmount": 10.99,
    "exchangeRate": 1.00 # american dollar as default
}
```

### Get a purchase

```
curl --location 'http://localhost:8080/purchases/7d2c203d-6ab1-4a80-8246-af6fa7ceb959?currency=Mexico-Peso'
```

#### Response

```
{
    "id": "7d2c203d-6ab1-4a80-8246-af6fa7ceb959",
    "description": "Any value",
    "transactionDate": "2026-05-03T13:15:30Z",
    "amount": 10.99,
    "convertedAmount": 198.13,
    "exchangeRate": 18.028
}
```