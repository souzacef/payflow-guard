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

## Notes
- Spring Boot auto-configures DB when JPA is present
- Security adds login page automatically
- SecurityFilterChain controls endpoint access
- APIs usually return JSON
- Versioned endpoints use paths like /api/v1
- Security rules can expose specific routes publicly