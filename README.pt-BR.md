# PayFlow Guard 💳🛡️

[🇺🇸 English Version](README.md)

PayFlow Guard é uma API backend para gerenciamento de merchants e pagamentos com foco em segurança, escalabilidade e arquitetura antifraude.

Desenvolvido com Java 21 e Spring Boot, o projeto simula um backend de processamento de pagamentos do mundo real, incluindo autenticação, gestão de merchants, ciclo de vida de pagamentos, reembolsos e webhooks.

---

## 🎯 Objetivo

Este projeto simula um backend de pagamentos semelhante aos utilizados por fintechs.

Foco em:

* fluxos de transação seguros
* isolamento de dados multi-tenant
* design de APIs escaláveis
* princípios de arquitetura limpa
* processamento baseado em máquina de estados

---

## 🚀 Funcionalidades

### 🔐 Autenticação e Segurança

* Autenticação via JWT
* Senhas com hashing BCrypt
* Login e registro seguros
* Endpoints protegidos
* Isolamento de dados por usuário
* Controle de acesso por papel (USER / ADMIN)

---

### 🏪 Gestão de Merchants

* Criar, atualizar e remover merchants
* Paginação, filtros e ordenação
* Status do merchant (ACTIVE / INACTIVE)
* Acesso limitado ao dono dos dados

---

### 💳 Sistema de Pagamentos

* Criação de pagamentos vinculados a merchants
* Ciclo de vida completo:

PENDING → AUTHORIZED → CAPTURED → REFUNDED / FAILED

* Validação rigorosa de transições
* Controle administrativo de status
* Captura automática via scheduler

---

### 🧪 Detecção de Fraude

* Validação automática na criação
* Regras implementadas:

  * Bloqueio por valor alto
  * Bloqueio por frequência de transações
* Motivo da fraude armazenado e retornado

---

### 🔁 Idempotência (nível produção)

* Evita pagamentos duplicados
* Uso do header `Idempotency-Key`
* Mesma requisição = mesma resposta
* Escopo por merchant

---

### 💸 Sistema de Reembolsos

* Suporte a reembolsos parciais
* Múltiplos reembolsos por pagamento
* Controle de:

  * valor total reembolsado
  * registros individuais

---

### 📜 Histórico de Reembolsos

GET /api/v1/payments/{id}/refunds

Retorna a linha do tempo completa de reembolsos.

---

### ⚙️ Captura Automática

Processo agendado:

AUTHORIZED → CAPTURED

Com geração de audit log e webhook.

---

### 📡 Webhooks

* Evento: `payment.status.updated`
* Armazena tentativas de envio
* Retry automático em caso de falha

---

### 🧾 Auditoria

Registra:

* mudanças de status
* reembolsos
* overrides
* operações automáticas

---

### 📊 Design da API

* Endpoints REST (`/api/v1/...`)
* DTOs limpos
* Tratamento global de exceções
* Estrutura consistente de erro
* Uso correto de códigos HTTP

---

### 🧪 Testes

* Testes de integração:

  * Idempotência
  * Histórico de reembolsos

---

## 🔒 Destaques de Segurança

* Autenticação stateless com JWT
* Senhas com BCrypt
* Filtros do Spring Security
* Isolamento de dados no nível de query

---

## 🧱 Stack Tecnológica

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker)
* JWT
* Maven
* Swagger / OpenAPI

---

## 🏗️ Arquitetura

Controller → Service → Repository → Database

* **Controller** → camada HTTP
* **Service** → regras de negócio
* **Repository** → acesso a dados
* **DTOs** → contratos da API

### Cross-cutting

* Segurança
* Auditoria
* Webhooks
* Scheduler

---

## 🔑 Fluxo de Autenticação

1. Registro:

POST /api/v1/auth/register

2. Login:

POST /api/v1/auth/login

3. Uso do token:

Authorization: Bearer <token>

---

## 📦 Exemplos de Endpoints

### Criar pagamento

POST /api/v1/payments
Header: Idempotency-Key

### Atualizar status

PATCH /api/v1/payments/{id}/status

### Reembolso

POST /api/v1/payments/{id}/refund

### Histórico de reembolsos

GET /api/v1/payments/{id}/refunds

---

## ⚙️ Executando localmente

### 1. Clonar

git clone https://github.com/souzacef/payflow-guard.git
cd payflow-guard

### 2. Subir banco

docker compose up -d

### 3. Rodar aplicação

./mvnw spring-boot:run

### 4. Swagger

http://localhost:8080/swagger-ui/index.html

---

## 🧪 Fluxo de teste

1. Registrar usuário
2. Fazer login
3. Criar merchant
4. Criar pagamento
5. Autorizar pagamento
6. Aguardar captura automática
7. Realizar reembolsos
8. Consultar histórico

---

## 📸 API Preview

![Swagger Overview](docs/swagger-overview.png)
![Swagger Endpoints](docs/swagger-endpoints.png)
![Swagger Request](docs/swagger-request.png)

---

## 📌 Roadmap

* [x] Ciclo de pagamento
* [x] Antifraude
* [x] Controle de acesso
* [x] Webhooks
* [x] Reembolsos
* [x] Idempotência
* [ ] Integração com gateway real
* [ ] Conversão de moeda
* [ ] Regras antifraude avançadas

---

## 👨‍💻 Autor

Carlos Eduardo Freire de Souza
Desenvolvedor Backend (Java, APIs, IA)

GitHub: https://github.com/souzacef
LinkedIn: https://linkedin.com/in/carlosefsouza

---

## 🧠 Reflexão final

Pagamentos não são apenas transações.

São **máquinas de estado com consequências**.
