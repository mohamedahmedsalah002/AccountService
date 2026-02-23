# Account Service

Account management microservice for VRBank system. Handles account creation, updates, balance inquiries, and publishes account events to Kafka.

---

## Features

- **Account Management**: Create, read, update, close accounts
- **Business Validation**: 
  - Maximum 5 active accounts per user
  - Minimum balance requirement for SAVINGS accounts ($10)
  - Cannot close account with positive balance
  - Cannot reopen closed accounts
- **Kafka Integration**:
  - **Consumes**: `user.created` (auto-creates default CHECKING account for new users)
  - **Produces**: `account.created` (notifies other services when account is created)
- **Account Types**: CHECKING, SAVINGS, BUSINESS
- **Account Status**: ACTIVE, FROZEN, CLOSED

---

## REST API Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/accounts` | Create account | 201 |
| GET | `/api/accounts/{id}` | Get account by ID | 200 |
| GET | `/api/accounts/user/{userId}` | Get all accounts for user | 200 |
| PUT | `/api/accounts/{id}` | Update account | 200 |
| DELETE | `/api/accounts/{id}` | Close account | 204 |
| GET | `/api/accounts/{id}/balance` | Get account balance | 200 |

---

## Entity Structure

### Account
```java
{
  "accountId": 1,
  "accountNumber": "12345678901234567890",
  "userId": "uuid",
  "balance": 1000.00,
  "accountType": "CHECKING",
  "status": "ACTIVE",
  "createdAt": "2026-02-12T10:00:00",
  "updatedAt": "2026-02-12T10:00:00"
}
```

---

## Kafka Events

### Consumes

**Topic**: `user.created`
- Automatically creates a default CHECKING account with $0 balance when a new user registers

### Produces

**Topic**: `account.created`
```json
{
  "eventId": "uuid",
  "eventType": "ACCOUNT_CREATED",
  "timestamp": "2026-02-12T10:00:00",
  "serviceName": "account-service",
  "data": {
    "accountId": 1,
    "accountNumber": "12345678901234567890",
    "userId": "uuid",
    "accountType": "CHECKING",
    "balance": 0.00,
    "createdAt": "2026-02-12T10:00:00"
  }
}
```

---

## Configuration

**Port**: 8083  
**Database**: H2 (dev), MySQL (production)  
**Kafka**: localhost:9092

### application.properties
```properties
server.port=8083
spring.datasource.url=jdbc:h2:mem:accountdb
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=account-service-group
```

---

## Business Rules

1. **Account Limit**: Users can have maximum 5 active accounts
2. **Minimum Balance**: SAVINGS accounts require minimum $10 initial balance
3. **Close Account**: Can only close accounts with $0 balance
4. **Reopen Account**: Closed accounts cannot be reopened
5. **Auto-Creation**: New users automatically get a CHECKING account

---

## How to Run

### Prerequisites
- Java 21
- Kafka running on localhost:9092
- VirtualBank service running (for user.created events)

### Start the service
```bash
cd /Users/mo/Downloads/Account-Service
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

---

## Testing Flow

### 1. Register a new user in VirtualBank
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@test.com",
    "password": "password123"
  }'
```

**Result**: Account Service automatically creates a CHECKING account for alice

### 2. Get user's accounts
```bash
curl http://localhost:8083/api/accounts/user/{userId}
```

### 3. Create additional account
```bash
curl -X POST http://localhost:8083/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "uuid",
    "accountType": "SAVINGS",
    "initialBalance": 100.00
  }'
```

### 4. Check balance
```bash
curl http://localhost:8083/api/accounts/1/balance
```

### 5. Close account
```bash
curl -X DELETE http://localhost:8083/api/accounts/1
```

---

## Architecture

```
VirtualBank (8081)
     |
     |--user.created--> Account Service (8083)
                             |
                             |--account.created--> Notification Service (8082)
                             |
                        Kafka (9092)
```

---

## Tests

- **AccountServiceTest**: Unit tests for business logic
  - Account creation with validation
  - Account limit enforcement
  - Minimum balance validation
  - Close account rules
- **UserEventConsumerTest**: Kafka consumer tests
  - Auto-create account on user.created
  - Error handling

---

## Error Handling

- `IllegalStateException`: Account limit reached, cannot close account with balance
- `IllegalArgumentException`: Minimum balance not met
- `RuntimeException`: Account not found

---

## Next Steps

- Add JWT authentication (copy from Transaction service)
- Add account balance update endpoints (deposit/withdraw)
- Add account statement generation
- Add account freeze/unfreeze functionality
- Integrate with Transaction service for balance updates
