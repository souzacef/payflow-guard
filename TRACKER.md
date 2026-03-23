# PayFlow Guard Tracker

## Goal
Build a portfolio-ready payment processing backend with fraud analysis using Java and Spring Boot.

## Milestones
- [ ] Foundation
- [ ] Authentication
- [ ] Merchant and Customer Management
- [ ] Payment Processing
- [ ] Fraud Analysis
- [ ] Webhooks and FX
- [ ] Tests and Documentation

## Current Focus
- [x] Check installed tools
- [x] Create GitHub repository
- [x] Install Java 21
- [x] Install Maven
- [x] Create Spring Boot project
- [x] Run application locally
- [x] Create first endpoint

## Done
- [x] Repository created
- [x] Git confirmed
- [x] VS Code confirmed
- [x] Docker confirmed
- [x] Spring Boot application starts without database
- [x] First /health endpoint created
- [x] Spring Security default login understood
- [x] /health endpoint made public
- [x] First security configuration created
- [x] Create package structure
- [x] Convert /health to JSON response
- [x] Create /api/v1/test endpoint
- [x] Allow public access to test endpoints
- [x] Create reusable ApiResponse class
- [x] Update test endpoint to use ApiResponse
- [x] Create root endpoint
- [x] Allow root endpoint in security
- [x] Create merchant module
- [x] Create Merchant entity class
- [x] Create MerchantResponse DTO
- [x] Create sample merchant endpoint
- [x] Create MerchantService
- [x] Move business logic from controller to service
- [x] Controller now uses dependency injection
- [x] Disable form login for API development
- [x] Disable CSRF temporarily for POST testing
- [x] Enable public merchant endpoints
- [x] Fix security so validation errors return through /error
- [x] Valid merchant request returns 200
- [x] Invalid merchant request returns 400
- [x] Create ErrorResponse model
- [x] Implement global exception handler
- [x] Customize validation error responses
- [x] Create Docker Compose file for PostgreSQL
- [x] Start PostgreSQL container
- [x] Configure Spring datasource in application.yml
- [x] Convert Merchant into JPA entity
- [x] Create MerchantRepository
- [x] Persist merchants to PostgreSQL
- [x] Add GET /api/v1/merchants
- [x] Add GET /api/v1/merchants/{id}
- [x] Retrieve data from PostgreSQL
- [x] Add DELETE /api/v1/merchants/{id}
- [x] Return 204 when deletion succeeds
- [x] Return 404 when deleting a missing merchant

## Notes
- Spring Boot auto-configures DB when JPA is present
- Security adds login page automatically
- SecurityFilterChain controls endpoint access
- APIs usually return JSON
- Versioned endpoints use paths like /api/v1
- Security rules can expose specific routes publicly

## Day 2

- [x] POST /api/v1/merchants
- [x] Validation with @Valid
- [x] Global exception handler
- [x] PostgreSQL integration (Docker)
- [x] JPA entity + repository
- [x] GET all merchants
- [x] GET merchant by id
- [x] 404 handling (ResourceNotFoundException)
- [x] DELETE merchant

## Day 3

- [x] Add PUT /api/v1/merchants/{id}
- [x] Update merchant data in database
- [x] Handle not found on update
- [x] Add pagination to GET /merchants
- [x] Use Pageable and Page<T>
- [x] Customize pagination response
- [x] Hide internal Spring Page structure
- [x] Add filtering by email
- [x] Add filtering by businessName
- [x] Add sorting support
- [x] Add user entity
- [x] Fix reserved table name (users)
- [x] Implement register/login
- [x] Generate JWT tokens

## Day 4

- [x] Use JWT token in Authorization header
- [x] Access protected endpoints
- [x] Return 401 without token
- [x] Add BCrypt password hashing
- [x] Hash password on register
- [x] Validate hashed password on login
- [x] Create UnauthorizedException
- [x] Handle login errors with 401
- [x] Return clean error response for invalid credentials
- [x] Add JWT authentication
- [x] Protect endpoints with token
- [x] Associate merchants with users
- [x] Enforce tenant isolation (user-specific data)
- [x] Add GET /auth/me endpoint
- [x] Retrieve authenticated user from SecurityContext
- [x] Add validation to auth requests
- [x] Prevent duplicate email registration
- [x] Return 409 on duplicate email
- [x] Return 400 on invalid input
- [x] Add springdoc OpenAPI dependency
- [x] Add OpenAPI metadata config
- [x] Expose Swagger UI and api-docs endpoints
- [x] Add Swagger/OpenAPI documentation
- [x] Add JWT auth support in Swagger UI