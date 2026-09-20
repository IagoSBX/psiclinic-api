# PsiClinic API

API REST para gerenciamento de uma clínica de psicologia: cadastro de pacientes, psicólogos e agendamento de sessões de terapia.

## Descrição do projeto

A API permite:

- Cadastrar, consultar, atualizar e remover **pacientes** (com validação de CPF único).
- Cadastrar, consultar, atualizar e remover **psicólogos** (com validação de CRP único).
- Agendar, consultar, atualizar status e remover **sessões**, com bloqueio automático de conflito de horário para o mesmo psicólogo (considerando a duração de cada sessão).
- Filtrar sessões por psicólogo, por paciente ou por período (data início/fim), com paginação.

## Stack

- Java 17
- Spring Boot 3.3.x (Spring Web, Spring Data JPA, Spring Validation)
- Hibernate / JPA
- MySQL 8
- Maven
- Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito (testes unitários)
- Docker / docker-compose (MySQL local)

## Arquitetura

```
com.psiclinic
├── controller    # Endpoints REST
├── service       # Regras de negócio
├── repository    # Interfaces JpaRepository
├── model         # Entidades JPA
├── dto           # Records de request/response
├── mapper        # Conversão entidade <-> DTO
├── exception     # Exceções customizadas + @RestControllerAdvice
└── config        # Configurações (OpenAPI, etc.)
```

## Como rodar localmente

### Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker e Docker Compose

### 1. Subir o banco de dados MySQL

```bash
cp .env.example .env
docker compose up -d
```

Isso sobe um MySQL 8 na porta `3306` com o banco `psiclinic` já criado (usuário/senha definidos no `.env`).

### 2. Rodar a aplicação

```bash
export $(cat .env | xargs)   # carrega as variáveis de ambiente no shell (opcional)
mvn spring-boot:run
```

Por padrão a aplicação sobe com o profile `dev` (`SPRING_PROFILES_ACTIVE=dev`), lendo as credenciais do banco via variáveis de ambiente:

| Variável | Padrão (dev) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do MySQL |
| `DB_PORT` | `3306` | Porta do MySQL |
| `DB_NAME` | `psiclinic` | Nome do banco |
| `DB_USER` | `psiclinic` | Usuário do banco |
| `DB_PASSWORD` | `psiclinic` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta da aplicação |

Para produção, ative o profile `prod` (`SPRING_PROFILES_ACTIVE=prod`) e informe `DB_HOST`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` (sem valores padrão, obrigatórios).

### 3. Acessar a documentação (Swagger UI)

Com a aplicação rodando:

```
http://localhost:8080/swagger-ui.html
```

### 4. Rodar os testes

```bash
mvn test
```

## Endpoints

### Pacientes

| Método | Rota | Descrição |
|---|---|---|
| POST | `/pacientes` | Cadastrar paciente |
| GET | `/pacientes` | Listar todos os pacientes |
| GET | `/pacientes/{id}` | Buscar paciente por id |
| PUT | `/pacientes/{id}` | Atualizar paciente |
| DELETE | `/pacientes/{id}` | Remover paciente |

**Exemplo de request — POST /pacientes**

```json
{
  "nome": "João da Silva",
  "cpf": "12345678900",
  "dataNascimento": "1990-05-14",
  "telefone": "11999999999",
  "email": "joao.silva@email.com",
  "endereco": "Rua das Flores, 123 - São Paulo/SP"
}
```

**Exemplo de response — 201 Created**

```json
{
  "id": 1,
  "nome": "João da Silva",
  "cpf": "12345678900",
  "dataNascimento": "1990-05-14",
  "telefone": "11999999999",
  "email": "joao.silva@email.com",
  "endereco": "Rua das Flores, 123 - São Paulo/SP",
  "dataCadastro": "2026-09-20T10:30:00"
}
```

### Psicólogos

| Método | Rota | Descrição |
|---|---|---|
| POST | `/psicologos` | Cadastrar psicólogo |
| GET | `/psicologos` | Listar todos os psicólogos |
| GET | `/psicologos/{id}` | Buscar psicólogo por id |
| PUT | `/psicologos/{id}` | Atualizar psicólogo |
| DELETE | `/psicologos/{id}` | Remover psicólogo |

**Exemplo de request — POST /psicologos**

```json
{
  "nome": "Dra. Maria Souza",
  "crp": "06/123456",
  "especialidade": "Terapia Cognitivo-Comportamental",
  "email": "maria.souza@email.com",
  "telefone": "11988888888"
}
```

**Exemplo de response — 201 Created**

```json
{
  "id": 1,
  "nome": "Dra. Maria Souza",
  "crp": "06/123456",
  "especialidade": "Terapia Cognitivo-Comportamental",
  "email": "maria.souza@email.com",
  "telefone": "11988888888"
}
```

### Sessões

| Método | Rota | Descrição |
|---|---|---|
| POST | `/sessoes` | Agendar sessão |
| GET | `/sessoes` | Listar sessões (paginado) |
| GET | `/sessoes?dataInicio=&dataFim=` | Listar sessões em um período (paginado) |
| GET | `/sessoes/{id}` | Buscar sessão por id |
| PUT | `/sessoes/{id}/status` | Atualizar status da sessão |
| DELETE | `/sessoes/{id}` | Remover sessão |
| GET | `/sessoes/psicologo/{id}` | Listar sessões de um psicólogo (paginado) |
| GET | `/sessoes/paciente/{id}` | Listar sessões de um paciente (paginado) |

**Exemplo de request — POST /sessoes**

```json
{
  "pacienteId": 1,
  "psicologoId": 1,
  "dataHora": "2026-10-01T14:00:00",
  "duracaoMinutos": 50,
  "valor": 180.00,
  "observacoes": "Primeira sessão de avaliação"
}
```

**Exemplo de response — 201 Created**

```json
{
  "id": 1,
  "paciente": { "id": 1, "nome": "João da Silva" },
  "psicologo": { "id": 1, "nome": "Dra. Maria Souza" },
  "dataHora": "2026-10-01T14:00:00",
  "duracaoMinutos": 50,
  "status": "AGENDADA",
  "valor": 180.00,
  "observacoes": "Primeira sessão de avaliação"
}
```

**Exemplo de conflito de horário — 409 Conflict**

```json
{
  "timestamp": "2026-09-20T10:35:00",
  "status": 409,
  "error": "Conflict",
  "message": "O psicólogo já possui uma sessão agendada que conflita com o horário informado",
  "details": []
}
```

**Exemplo de request — PUT /sessoes/{id}/status**

```json
{
  "status": "REALIZADA"
}
```

Valores possíveis para `status`: `AGENDADA`, `REALIZADA`, `CANCELADA`, `FALTOU`.

### Filtro por período

```
GET /sessoes?dataInicio=2026-10-01T00:00:00&dataFim=2026-10-31T23:59:59&page=0&size=20
```

## Tratamento de erros

Erros são retornados em um formato padronizado pelo `@RestControllerAdvice`:

| Situação | Status HTTP |
|---|---|
| Recurso não encontrado | 404 Not Found |
| CPF/CRP duplicado | 409 Conflict |
| Conflito de horário na sessão | 409 Conflict |
| Erro de validação (Bean Validation) | 400 Bad Request |
| Erro inesperado | 500 Internal Server Error |

## Licença

Projeto de uso interno/educacional.
# psiclinic-api
