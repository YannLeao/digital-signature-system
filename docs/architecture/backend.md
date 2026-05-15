# Arquitetura Backend

## Stack

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- Lombok
- MapStruct
- PostgreSQL
- Flyway

## Estrutura de pacotes

```text
backend/src/main/java/.../
├── config/
├── controller/
├── domain/
├── dto/
├── exception/
├── repository/
├── service/
└── mapper/
```

## Responsabilidades

### controller

Recebe requisições HTTP, valida entrada básica e delega para services.

Não deve conter regra de negócio.

### service

Contém regras de negócio e orquestração de casos de uso.

### repository

Acesso ao banco usando Spring Data JPA.

### domain

Entidades e modelos centrais do domínio.

### dto

Objetos de entrada e saída da API.

DTOs não devem ser entidades JPA.

### config

Configurações de framework, segurança, CORS, beans e propriedades.

### exception

Exceções customizadas e tratamento global de erros.

### mapper

Conversões entre entidades, DTOs e modelos internos.

## Regras

- Usar constructor injection.
- Evitar field injection com `@Autowired`.
- Não acessar repository diretamente em controllers.
- Não expor entidades JPA como resposta da API.
- Não hardcodar configurações sensíveis.
- Usar variáveis de ambiente para configurações externas.
- Usar Flyway para evolução de schema.
- Não alterar banco manualmente fora de migration.

## Variáveis de ambiente

Variáveis obrigatórias devem ser validadas no boot da aplicação.

A aplicação deve recusar iniciar se uma variável obrigatória estiver ausente.

## Banco de dados

- PostgreSQL como banco principal.
- Flyway para versionamento de schema.
- Migrations devem seguir o padrão:

```text
V{numero}__{descricao}.sql
```

Exemplo:

```text
V1__create_users_table.sql
```
