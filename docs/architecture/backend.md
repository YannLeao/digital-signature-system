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
├── event/
├── exception/
├── listener/
├── mapper/
├── repository/
├── security/
├── service/
└── validation/
```

## Responsabilidades

### controller

Recebe requisicoes HTTP, valida entrada basica e delega para services.

Nao deve conter regra de negocio.

### service

Contem regras de negocio e orquestracao de casos de uso.

### repository

Acesso ao banco usando Spring Data JPA.

### domain

Entidades e modelos centrais do dominio.

### dto

Objetos de entrada e saida da API.

DTOs nao devem ser entidades JPA.

### event

Eventos de dominio/aplicacao para sinalizar acontecimentos sensiveis sem acoplar diretamente o caso de uso ao efeito colateral.

Eventos nao devem carregar segredos, tokens, chaves criptograficas ou payloads sensiveis desnecessarios.

### listener

Listeners de eventos internos, como notificacoes por e-mail.

Falhas em listeners nao devem quebrar o fluxo principal de autenticacao quando o evento for apenas notificacao.

### config

Configuracoes de framework, seguranca, CORS, beans e propriedades.

### exception

Excecoes customizadas e tratamento global de erros.

### mapper

Conversoes entre entidades, DTOs e modelos internos.

### security

Componentes de seguranca, tokens, criptografia, cookies e filtros de autenticacao.

### validation

Validacoes reutilizaveis de entrada e regras auxiliares de validacao.

## Regras

- Usar constructor injection.
- Evitar field injection com `@Autowired`.
- Nao acessar repository diretamente em controllers.
- Nao expor entidades JPA como resposta da API.
- Nao hardcodar configuracoes sensiveis.
- Usar variaveis de ambiente para configuracoes externas.
- Usar Flyway para evolucao de schema.
- Nao alterar banco manualmente fora de migration.

## Variaveis de ambiente

Variaveis obrigatorias devem ser validadas no boot da aplicacao.

A aplicacao deve recusar iniciar se uma variavel obrigatoria estiver ausente.

## Banco de dados

- PostgreSQL como banco principal.
- Flyway para versionamento de schema.
- Migrations devem seguir o padrao:

```text
V{numero}__{descricao}.sql
```

Exemplo:

```text
V1__create_users_table.sql
```
