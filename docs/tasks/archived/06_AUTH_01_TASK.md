# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### AUTH-01 — Endpoint de cadastro de usuário com Argon2id

## Camada

Backend

## Resultado

AUTH-01 implementada com endpoint `POST /api/v1/auth/register`, validação server-side de e-mail e senha forte, hash Argon2id com baseline OWASP (`memory=65536`, `iterations=3`, `parallelism=4`), persistência segura apenas do hash e erros padronizados pelo `GlobalExceptionHandler`.

## Prioridade

Alta

## Objetivo

Implementar o primeiro fluxo real de autenticação do sistema: cadastro seguro de usuários com política forte de senha e armazenamento utilizando Argon2id.

## Contexto

A infraestrutura base do backend já foi concluída:

- estrutura Spring Boot
- PostgreSQL + Flyway
- tratamento global de erros
- versionamento `/api/v1`
- health endpoint
- documentação de execução do projeto

Agora o projeto entra oficialmente no primeiro módulo crítico de segurança: autenticação.

Esta implementação servirá como base para:

- login
- JWT
- sessões
- TOTP
- passkeys
- auditoria
- assinatura digital

Portanto, o fluxo deve ser implementado com foco em segurança, clareza e extensibilidade.

## Objetivo funcional

Criar endpoint:

```text
POST /api/v1/auth/register
````

Responsável por:

* validar entrada
* validar política de senha
* verificar unicidade do e-mail
* gerar hash Argon2id
* persistir usuário no banco
* retornar resposta segura

## Checklist de implementação

### Estrutura e domínio

* [ ] Criar estrutura de autenticação caso ainda não exista:

    * [ ] `controller/auth`
    * [ ] `service/auth`
    * [ ] `dto/auth`
* [ ] Criar entidade de usuário caso ainda não exista.
* [ ] Garantir que a entidade esteja alinhada com a migration `users`.

### Endpoint

* [ ] Criar endpoint:

    * [ ] `POST /api/v1/auth/register`
* [ ] Definir request DTO.
* [ ] Definir response DTO.
* [ ] Usar Bean Validation (`@Valid`).

### Campos mínimos esperados

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

### Política de senha

A senha deve obrigatoriamente possuir:

* [ ] mínimo de 12 caracteres
* [ ] letra maiúscula
* [ ] letra minúscula
* [ ] número
* [ ] símbolo especial

### Validação de senha

* [ ] Implementar validação server-side.
* [ ] Não confiar apenas no frontend futuramente.
* [ ] Rejeitar senhas fracas com `400 Bad Request`.
* [ ] Retornar código:

    * [ ] `VAL_001`

### E-mail

* [ ] Validar formato do e-mail.
* [ ] Garantir unicidade do e-mail no banco.
* [ ] Retornar erro seguro para duplicidade.

### Argon2id

* [ ] Implementar hash usando Argon2id.
* [ ] Utilizar biblioteca adequada do ecossistema Spring/Java.
* [ ] Confirmar que o hash armazenado inicia com:

    * [ ] `$argon2id$`

### Configuração mínima esperada do Argon2id

Seguir baseline OWASP mínima:

* [ ] memory: `65536`
* [ ] iterations: `3`
* [ ] parallelism: `4`

### Restrições de segurança

É proibido utilizar:

* [ ] bcrypt
* [ ] MD5
* [ ] SHA-1
* [ ] SHA-256 simples
* [ ] senha sem salt
* [ ] criptografia reversível para senha

### Persistência

* [ ] Persistir usuário no PostgreSQL.
* [ ] Salvar apenas o hash da senha.
* [ ] Nunca salvar senha em texto claro.
* [ ] Nunca logar senha.
* [ ] Nunca retornar senha na API.

### Campos mínimos esperados na entidade

* [ ] `id`
* [ ] `email`
* [ ] `password_hash`
* [ ] `created_at`
* [ ] `updated_at`
* [ ] `failed_attempts`
* [ ] `locked_until`

### Resposta esperada

Exemplo seguro:

```json
{
  "message": "Usuário registrado com sucesso."
}
```

### Tratamento de erros

* [ ] Integrar com GlobalExceptionHandler já existente.
* [ ] Retornar erros padronizados.
* [ ] Não expor detalhes internos.
* [ ] Não expor stack trace.
* [ ] Não expor SQL.

## Boas práticas obrigatórias

### Entidade

* [ ] Evitar expor entidade diretamente na API.
* [ ] Usar DTOs para entrada e saída.

### Service

* [ ] Controller não deve conter regra de negócio.
* [ ] Hash deve ser gerado na camada service.

### Repository

* [ ] Repository apenas acessa banco.
* [ ] Não colocar regra de negócio em repository.

### Segurança

* [ ] Nunca comparar senha manualmente usando equals futuramente.
* [ ] Preparar estrutura para autenticação futura.
* [ ] Não adicionar JWT ainda.
* [ ] Não adicionar login ainda.

## Critérios de aceitação

* [ ] Usuário válido consegue se registrar.
* [ ] Senha fraca retorna `400`.
* [ ] E-mail inválido retorna `400`.
* [ ] E-mail duplicado retorna erro controlado.
* [ ] Hash salvo inicia com `$argon2id$`.
* [ ] Nenhuma senha aparece em logs.
* [ ] Nenhuma senha aparece na resposta.
* [ ] Endpoint segue padrão `/api/v1`.
* [ ] Erros seguem padrão global da API.

## Testes recomendados

* [ ] Cadastro válido.
* [ ] Senha curta.
* [ ] Senha sem símbolo.
* [ ] Senha sem número.
* [ ] Senha sem maiúscula.
* [ ] E-mail inválido.
* [ ] E-mail duplicado.
* [ ] Verificar hash salvo no banco.
* [ ] Verificar que senha original nunca aparece persistida.

## Fora de escopo

Não implementar ainda:

* Login.
* JWT.
* Refresh Token.
* Sessões.
* TOTP.
* Passkeys.
* Rate limiting.
* Bloqueio por tentativas.
* Auditoria.
* Envio de e-mail.
* Frontend de autenticação.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`

## Prompt recomendado para o agente

Implemente completamente a tarefa AUTH-01 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, `docs/architecture/backend.md`, `docs/security/security-overview.md`, `docs/standards/api.md` e este arquivo.

Implemente um endpoint seguro de cadastro de usuário usando Argon2id seguindo baseline mínima OWASP.

Não implemente login, JWT, sessões, TOTP, passkeys ou qualquer autenticação além do cadastro.

Ao final:

* informe os arquivos criados/alterados
* explique como testar o endpoint
* explique como validar o hash Argon2id salvo no banco
* explique como validar os erros de senha fraca
