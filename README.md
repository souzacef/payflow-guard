# PayFlow Guard

PayFlow Guard is a backend API for merchant and payment management with a focus on security, scalability, and fraud-aware architecture.

Built with Java 21 and Spring Boot, the project models a real-world payment processing backend, including authentication, merchant management, and secure multi-tenant data handling.

---

## 🎯 Purpose

This project was designed to simulate a real-world payment backend similar to systems used by fintech companies like Worldpay.

It focuses on:

* secure transaction flows
* multi-tenant data isolation
* scalable API design
* clean architecture principles

---

## 🚀 Features

### 🔐 Authentication & Security

* JWT-based authentication
* BCrypt password hashing
* Secure login and registration
* Protected endpoints with token validation
* Multi-tenant data isolation (users only access their own data)
* Role-based access control (USER / ADMIN)


### 🏪 Merchant Management

* Create, update, delete merchants
* Pagination, filtering, and sorting
* Merchant status management (ACTIVE / INACTIVE)
* User-scoped data access

### 📊 API Design

* RESTful endpoints (`/api/v1/...`)
* Clean request/response DTOs
* Global exception handling
* Consistent error response structure

### 🕒 Audit & Metadata

* Automatic `createdAt` and `updatedAt` timestamps
* Hibernate-managed lifecycle fields

### 📄 Documentation

* Swagger / OpenAPI integration
* JWT authentication support in Swagger UI

---

## 🔒 Security Highlights

* Stateless authentication using JWT
* Passwords stored using BCrypt hashing
* Endpoint protection via Spring Security filters
* User-level data isolation enforced at query level

---

## 🧱 Tech Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker)
* JWT (authentication)
* Maven
* Swagger / OpenAPI (springdoc)

---

## 🏗️ Architecture

The project follows a layered architecture:

```
Controller → Service → Repository → Database
```

* **Controller**: Handles HTTP requests/responses
* **Service**: Business logic and validation
* **Repository**: Data access (JPA)
* **DTOs**: Clean API contracts (no entity leakage)

### Key Design Decisions

* Use of DTOs to decouple API from persistence layer
* Enum modeling for controlled domain values
* Centralized exception handling with `@RestControllerAdvice`
* Stateless authentication with JWT
* Per-user data isolation enforced at query level

---

## 🔑 Authentication Flow

1. User registers:

```
POST /api/v1/auth/register
```

2. User logs in:

```
POST /api/v1/auth/login
```

3. API returns JWT token

4. Token is used in requests:

```
Authorization: Bearer <token>
```

---

## 📦 Example Endpoints

### Get current user

```
GET /api/v1/auth/me
```

### Create merchant

```
POST /api/v1/merchants
```

### Get merchants (with pagination)

```
GET /api/v1/merchants?page=0&size=20&sort=id,asc
```

### Update merchant status

```
PATCH /api/v1/merchants/{id}/status
```

---

## 📥 Example Response

```json
{
  "id": 1,
  "businessName": "My Store",
  "email": "store@email.com",
  "status": "ACTIVE",
  "createdAt": "2026-03-24T10:30:00Z",
  "updatedAt": "2026-03-24T10:30:00Z"
}
```

---

## ⚙️ Running Locally

### 1. Clone repository

```bash
git clone https://github.com/souzacef/payflow-guard.git
cd payflow-guard
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Access Swagger UI

http://localhost:8080/swagger-ui/index.html

---

## 🧪 Example Test Flow

1. Register user
2. Login and get JWT
3. Authorize in Swagger
4. Create merchant
5. Retrieve merchant list
6. Update merchant status

---

## 📸 API Preview

![Swagger Overview](docs/swagger-overview.png)
![Swagger Overview](docs/swagger-endpoints.png)
![Swagger Overview](docs/swagger-request.png)

---

## 📌 Roadmap

* [ ] Payment processing module
* [ ] Fraud detection rules
* [ ] Role-based access (ADMIN / USER)
* [ ] Webhooks
* [ ] Integration with external APIs (FX, payment gateways)
* [ ] Automated tests (unit + integration)

---

## 👨‍💻 Author

Carlos Eduardo Freire de Souza
Backend Developer focused on Java, APIs, and AI integration

GitHub: https://github.com/souzacef
LinkedIn: https://linkedin.com/in/carlosefsouza

---

## 💡 Notes

This project was built as a portfolio piece with focus on:

* real-world backend patterns
* clean architecture
* production-like features
* interview readiness
