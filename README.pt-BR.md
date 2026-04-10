# PayFlow Guard 💳🛡️

[🇺🇸 English Version](README.md)

PayFlow Guard é uma API backend para gerenciamento de merchants e pagamentos com foco em segurança, escalabilidade e arquitetura antifraude.

Desenvolvido com Java 21 e Spring Boot, o projeto simula um backend de processamento de pagamentos do mundo real, incluindo autenticação, gestão de merchants, ciclo de vida de pagamentos, reembolsos, idempotência, logs de auditoria, captura automática e entrega de webhooks com retry.

---

## 🎯 Objetivo

Este projeto foi criado para simular um backend de pagamentos semelhante aos utilizados por fintechs.

O foco está em:

* fluxos de transação seguros
* isolamento de dados multi-tenant
* design de APIs escaláveis
* princípios de arquitetura limpa
* processamento orientado por estados

---

## 🚀 Funcionalidades

### 🔐 Autenticação e Segurança

* Autenticação baseada em JWT
* Hash de senha com BCrypt
* Login e registro seguros
* Endpoints protegidos com validação de token
* Isolamento de dados por usuário
* Controle de acesso por papéis (USER / ADMIN)

### 🏪 Gestão de Merchants

* Criar, atualizar e excluir merchants
* Paginação, filtros e ordenação
* Gerenciamento de status (ACTIVE / INACTIVE)
* Acesso escopado por usuário

### 💳 Sistema de Pagamentos

* Criação de pagamentos vinculados a merchants
* Ciclo de vida completo:
  * `PENDING → AUTHORIZED → CAPTURED → REFUNDED / FAILED`
* Validação rigorosa de transições
* Atualizações de status controladas por administrador
* Captura automática via scheduler

### 🧪 Detecção de Fraude

* Validação automática de fraude na criação do pagamento
* Regras implementadas:
  * bloqueio por valor alto
  * bloqueio por frequência de transações
* Motivo da fraude armazenado e retornado pela API

### 🔁 Idempotência

* Evita criação duplicada de pagamentos
* Utiliza o header `Idempotency-Key`
* A mesma requisição retorna o mesmo pagamento
* Escopo por merchant

### 💸 Sistema de Reembolsos

* Suporte a reembolsos parciais
* Múltiplos reembolsos por pagamento
* Controle agregado do total reembolsado
* Persistência de registros individuais de reembolso

### 📜 Histórico de Reembolsos

* `GET /api/v1/payments/{id}/refunds`
* Linha do tempo completa de reembolsos por pagamento

### ⚙️ Captura Automática

* Processo agendado:
  * `AUTHORIZED → CAPTURED`
* Gera audit log e webhook

### 📡 Webhooks

* Evento: `payment.status.updated`
* Entrega HTTP real
* Rastreamento de tentativas
* Retry automático em falhas

### 🧾 Logs de Auditoria

Registram:

* mudanças de status
* reembolsos
* overrides
* operações automáticas

### 📊 Design da API

* Endpoints REST (`/api/v1/...`)
* DTOs limpos para request/response
* Tratamento global de exceções
* Estrutura consistente de erros
* Uso correto de códigos HTTP

### 🧪 Testes

* Testes de integração:
  * idempotência
  * histórico de reembolsos
  * transições do ciclo de vida

---

## 🔐 Modelo de Papéis e Acesso

Novos usuários são criados com o papel `USER` por padrão.

Algumas operações são restritas a `ADMIN`, como:

* atualizações de status de pagamento
* overrides de status
* reembolsos
* inspeção de eventos de webhook e fluxos operacionais

No ambiente atual de desenvolvimento, privilégios administrativos podem ser concedidos diretamente no PostgreSQL:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'user@test.com';
```

Isso mantém o fluxo da aplicação simples, mas ainda permite testar cenários administrativos localmente.

---

## 🔒 Destaques de Segurança

* Autenticação stateless com JWT
* Senhas armazenadas com BCrypt
* Proteção de endpoints via filtros do Spring Security
* Isolamento de dados por usuário no nível de query

---

## 🧱 Stack Tecnológica

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker)
* JWT
* Maven
* Swagger / OpenAPI (springdoc)

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```text
Controller → Service → Repository → Database
```

* **Controller**: recebe e responde requisições HTTP
* **Service**: concentra regras de negócio e validações
* **Repository**: acesso a dados via JPA
* **DTOs**: contratos limpos da API

### Decisões de Design

* Uso de DTOs para desacoplar API e persistência
* Modelagem com enums para valores controlados
* Tratamento centralizado de exceções com `@RestControllerAdvice`
* Autenticação stateless com JWT
* Isolamento de dados por usuário no nível de query

### Diagrama Conceitual de Arquitetura

![Arquitetura do PayFlow Guard](docs/architecture-diagram.png)

---

## 🧠 Arquitetura Explicada

O sistema segue uma arquitetura em camadas com separação clara de responsabilidades:

```text
Controller → Service → Repository → Database
```

### 🔄 Fluxo da Requisição

1. A requisição chega ao **Controller**
2. O controller valida e encaminha para a **camada de Service**
3. O service:
   * aplica regras de negócio
   * valida transições de estado
   * executa checagens antifraude
   * garante idempotência
4. O service interage com a **camada de Repository**
5. O repository persiste ou consulta dados no **Banco de Dados**
6. A resposta é mapeada para um DTO e devolvida ao cliente

### ⚙️ Exemplo: Criação de Pagamento

```text
Cliente
  ↓
PaymentController
  ↓
PaymentService
  ↓
FraudCheckService
  ↓
Validação de idempotência
  ↓
PaymentRepository
  ↓
Banco de Dados
  ↓
PaymentResponse
```

### 🔁 Exemplo: Atualização do Ciclo de Vida

```text
Cliente
  ↓
PaymentController
  ↓
PaymentService
  ↓
Validar transição
  ↓
Salvar pagamento
  ↓
Audit log
  ↓
Webhook
  ↓
Response
```

### 💸 Exemplo: Fluxo de Reembolso

```text
Cliente
  ↓
PaymentController
  ↓
PaymentService
  ↓
Validar regras de reembolso
  ↓
Criar registro de reembolso
  ↓
Atualizar total reembolsado
  ↓
Salvar pagamento
  ↓
Audit log
  ↓
Webhook
  ↓
Response
```

### 📡 Exemplo: Captura Automática

```text
Scheduler
  ↓
PaymentAutoCaptureService
  ↓
Buscar pagamentos AUTHORIZED
  ↓
Atualizar para CAPTURED
  ↓
Audit log
  ↓
Webhook
```

### 🧩 Princípios de Design

* **Separação de responsabilidades**
  Cada camada tem uma responsabilidade clara

* **Design orientado a estados**
  O ciclo de vida do pagamento é controlado por transições válidas

* **Mentalidade idempotency-first**
  Evita operações financeiras duplicadas

* **Auditabilidade**
  Toda ação crítica é rastreável

* **Resiliência**
  Webhooks são reenviados em caso de falha

---

## 🔑 Fluxo de Autenticação

1. Usuário se registra:

```text
POST /api/v1/auth/register
```

2. Usuário faz login:

```text
POST /api/v1/auth/login
```

3. A API retorna o token JWT

4. O token é utilizado nas requisições:

```text
Authorization: Bearer <token>
```

---

## 📦 Exemplos de Endpoints

### Obter usuário autenticado

```text
GET /api/v1/auth/me
```

### Criar merchant

```text
POST /api/v1/merchants
```

### Listar merchants com paginação

```text
GET /api/v1/merchants?page=0&size=20&sort=id,asc
```

### Atualizar status do merchant

```text
PATCH /api/v1/merchants/{id}/status
```

### Criar pagamento

```text
POST /api/v1/payments
Header: Idempotency-Key
```

### Atualizar status do pagamento

```text
PATCH /api/v1/payments/{id}/status
```

### Reembolsar pagamento

```text
POST /api/v1/payments/{id}/refund
```

### Histórico de reembolsos

```text
GET /api/v1/payments/{id}/refunds
```

---

## 📥 Exemplo de Resposta

```json
{
  "id": 1,
  "businessName": "Minha Loja",
  "email": "loja@email.com",
  "status": "ACTIVE",
  "createdAt": "2026-03-24T10:30:00Z",
  "updatedAt": "2026-03-24T10:30:00Z"
}
```

---

## ⚙️ Executando Localmente

### 1. Clonar o repositório

```bash
git clone https://github.com/souzacef/payflow-guard.git
cd payflow-guard
```

### 2. Iniciar PostgreSQL

```bash
docker compose up -d
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

### 4. Acessar Swagger UI

http://localhost:8080/swagger-ui/index.html

---

## 🧪 Exemplo de Fluxo de Teste

1. Registrar usuário
2. Fazer login e obter JWT
3. Autorizar no Swagger
4. Criar merchant
5. Criar pagamento
6. Mover pagamento para `AUTHORIZED`
7. Aguardar captura automática
8. Realizar reembolsos parciais
9. Consultar histórico de reembolsos

---

## 📡 Comportamento de Entrega de Webhooks

Eventos de webhook são persistidos e rastreados.

O sistema suporta:

* entrega HTTP real
* retry automático para falhas
* rastreamento de códigos de resposta HTTP
* armazenamento de detalhes de erro para observabilidade

Se a URL de destino for inválida ou estiver inacessível, o evento é marcado como falho em vez de ser perdido silenciosamente.

---

## 📸 API Preview

![Swagger Overview](docs/swagger-overview.png)
![Swagger Endpoints](docs/swagger-endpoints.png)
![Swagger Request](docs/swagger-request.png)

---

## 📌 Roadmap

* [x] Ciclo de vida de pagamentos
* [x] Regras de fraude
* [x] Controle de acesso por papéis
* [x] Webhooks com retry
* [x] Sistema de reembolsos com histórico
* [x] Idempotência
* [x] Captura automática
* [x] Testes de integração
* [ ] Integração com gateway de pagamento real
* [ ] Conversão cambial / FX
* [ ] Regras antifraude avançadas

---

## 👨‍💻 Autor

Carlos Eduardo Freire de Souza  
Desenvolvedor Backend focado em Java, APIs e sistemas backend escaláveis

GitHub: https://github.com/souzacef  
LinkedIn: https://linkedin.com/in/carlosefsouza

---

## 💡 Observações

Este projeto foi construído como peça de portfólio com foco em:

* padrões reais de backend
* arquitetura limpa
* comportamento inspirado em produção
* regras de negócio orientadas a estado

---

## 🧠 Reflexão Final

Pagamentos não são apenas transações.

São **máquinas de estado com consequências**.
