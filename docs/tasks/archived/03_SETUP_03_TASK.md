# CURRENT_TASK.md — Tarefa Atual

## Status

Concluido.

## Tarefa ativa

### SETUP-03 — Configurar PostgreSQL e migrações com Flyway

## Camada

Backend

## Prioridade

Alta

## Objetivo

Configurar o backend para usar PostgreSQL como banco principal e Flyway como ferramenta oficial de versionamento de schema.

## Contexto

O backend Spring Boot já foi inicializado e a estrutura base do projeto existe. Agora o projeto precisa deixar de 
depender de configuração genérica/local e passar a usar PostgreSQL com variáveis de ambiente, além de migrations 
versionadas com Flyway.

Esta tarefa é fundacional para autenticação, usuários, auditoria, tokens e demais módulos persistentes.

## Checklist de implementação

- [x] Adicionar dependências necessárias para PostgreSQL e Flyway.
- [x] Configurar conexão com PostgreSQL usando variáveis de ambiente.
- [x] Garantir que nenhuma credencial fique hardcoded.
- [x] Atualizar `.env.example` com variáveis do banco:
  - [x] `DB_HOST`
  - [x] `DB_PORT`
  - [x] `DB_NAME`
  - [x] `DB_USERNAME`
  - [x] `DB_PASSWORD`
- [x] Configurar `application.properties` ou `application.yml` para usar essas variáveis.
- [x] Configurar Flyway para executar migrations automaticamente no startup.
- [x] Criar pasta padrão de migrations:
  - [x] `src/main/resources/db/migration/`
- [x] Criar migration `V1__create_users_table.sql`.
- [x] Criar migration `V2__create_audit_log_table.sql`.
- [x] Documentar o padrão de nomenclatura das migrations.
- [x] Garantir que o Hibernate não crie/alterar schema automaticamente em ambiente de desenvolvimento.
- [x] Configurar `spring.jpa.hibernate.ddl-auto=validate` ou equivalente.
- [x] Validar que a aplicação sobe com banco válido.
- [x] Validar que as tabelas são criadas pelo Flyway.
- [x] Validar que a aplicação falha caso o schema esteja divergente.

## Estrutura esperada das migrations

### V1__create_users_table.sql

Deve criar a tabela `users` com campos mínimos:

- `id`
- `email`
- `password_hash`
- `created_at`
- `updated_at`
- `failed_attempts`
- `locked_until`

Boas práticas:

- `id` como UUID.
- `email` único e obrigatório.
- `password_hash` obrigatório.
- `created_at` obrigatório.
- `updated_at` obrigatório.
- `failed_attempts` com valor padrão `0`.
- `locked_until` nullable.

### V2__create_audit_log_table.sql

Deve criar a tabela `audit_log` com campos mínimos:

- `id`
- `user_id`
- `timestamp_utc`
- `ip`
- `user_agent`
- `action`
- `result`
- `metadata`

Boas práticas:

- `id` como UUID.
- `user_id` nullable para permitir eventos sem usuário autenticado.
- `metadata` como `JSONB`.
- Não criar endpoints de update/delete para essa tabela.

## Critérios de aceitação

- [x] Flyway executa migrations automaticamente na inicialização.
- [x] Tabelas `users` e `audit_log` são criadas corretamente no PostgreSQL.
- [x] Nenhuma tabela foi criada manualmente no banco.
- [x] O schema é controlado apenas por migrations.
- [x] O Hibernate não altera schema automaticamente.
- [x] A aplicação falha se o banco estiver indisponível ou schema estiver inconsistente.
- [x] `.env.example` documenta todas as variáveis necessárias.
- [x] Não há usuário, senha, host ou nome do banco hardcoded no código.

## Fora de escopo

Não implementar ainda:

- Cadastro real de usuário.
- Login.
- Hash Argon2id.
- JWT.
- Refresh token.
- Endpoints de auditoria.
- Regras completas de autenticação.
- Docker Compose, caso não esteja previsto neste card.
- Interface frontend para autenticação.

## Arquivos relevantes

Consulte antes de implementar:

- `docs/ai/AGENTS.md`
- `docs/ai/CONTEXT.md`
- `docs/architecture/backend.md`
- `docs/architecture/database.md`
- `docs/security/security-overview.md`
- `docs/product/roadmap.md`
