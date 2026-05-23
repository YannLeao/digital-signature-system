# CURRENT_TASK.md — Tarefa Atual

## Status

Concluido.

## Tarefa ativa

### SETUP-04 — Configurar padrão global de erros da API

## Camada

Backend

## Prioridade

Média

## Objetivo

Implementar um padrão único de respostas de erro para toda a API, evitando vazamento de detalhes internos e facilitando o consumo pelo frontend.

## Contexto

O backend já possui estrutura base, variáveis de ambiente validadas, PostgreSQL e Flyway configurados. Agora a API precisa retornar erros de forma consistente antes de avançar para autenticação e regras de negócio.

## Checklist de implementação

- [x] Criar pacote/estrutura para tratamento de erros, se ainda não existir.
- [x] Criar DTO padrão de erro da API.
- [x] Criar DTO para erros de validação de campos.
- [x] Criar enum ou classe de códigos de erro por domínio:
  - [x] `AUTH_XXX`
  - [x] `VAL_XXX`
  - [x] `DOC_XXX`
  - [x] `SEC_XXX`
  - [x] `SYS_XXX`
- [x] Implementar `GlobalExceptionHandler` com `@ControllerAdvice` ou `@RestControllerAdvice`.
- [x] Tratar erros de validação do Bean Validation.
- [x] Tratar exceções de recurso não encontrado.
- [x] Tratar exceções de regra de negócio.
- [x] Tratar exceções genéricas com resposta segura.
- [x] Garantir timestamp em UTC ISO-8601.
- [x] Garantir que stack trace nunca apareça na resposta.
- [x] Garantir que mensagens internas, SQL ou detalhes sensíveis não sejam expostos.
- [x] Documentar os códigos e padrões de erros em API_EXCEPTIONS.md na raíz do projeto

## Formato padrão de erro

```json
{
  "code": "VAL_001",
  "message": "Dados inválidos.",
  "timestamp": "2026-05-16T12:00:00Z"
}
````

## Formato para erro de validação

```json
{
  "code": "VAL_001",
  "message": "Dados inválidos.",
  "timestamp": "2026-05-16T12:00:00Z",
  "fields": [
    {
      "field": "email",
      "message": "E-mail inválido."
    }
  ]
}
```

## Códigos iniciais recomendados

* `VAL_001` — Dados inválidos.
* `VAL_002` — Parâmetro inválido.
* `AUTH_001` — Credenciais inválidas.
* `AUTH_002` — Acesso não autorizado.
* `AUTH_003` — Token inválido ou revogado.
* `SEC_001` — Requisição bloqueada por política de segurança.
* `DOC_001` — Documento inválido.
* `SYS_001` — Erro interno inesperado.

## Status HTTP esperados

* `400 Bad Request` para validação ou entrada inválida.
* `401 Unauthorized` para ausência/falha de autenticação.
* `403 Forbidden` para acesso negado.
* `404 Not Found` para recurso inexistente.
* `409 Conflict` para conflito de estado ou duplicidade.
* `429 Too Many Requests` para rate limiting futuro.
* `500 Internal Server Error` para falhas inesperadas.

## Critérios de aceitação

* [x] Toda exceção tratada retorna JSON no formato padrão.
* [x] Erros de validação listam campos inválidos.
* [x] Status HTTP está correto para cada tipo de erro.
* [x] Stack traces não aparecem nas respostas.
* [x] Queries SQL não aparecem nas respostas.
* [x] Erros genéricos retornam `SYS_001`.
* [x] A estrutura está pronta para ser reutilizada nos próximos épicos.
* [x] Todos os códigos e padrões de erros estão documentados em API_EXCEPTIONS.md na raíz do projeto

## Fora de escopo

Não implementar ainda:

* Login.
* Cadastro.
* JWT.
* Rate limiting.
* Regras completas de autenticação.
* Integração frontend com erros.
* Internacionalização de mensagens.
* Observabilidade avançada.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/standards/api.md`
* `docs/security/security-overview.md`

## Prompt recomendado para o agente

Implemente completamente a tarefa SETUP-04 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, `docs/architecture/backend.md`, `docs/standards/api.md` e este arquivo.

Crie um tratamento global de erros seguro, consistente e reutilizável. Não implemente autenticação, JWT, login ou cadastro ainda.

Ao final, informe os arquivos criados/alterados e explique como testar respostas de validação, recurso não encontrado e erro genérico.
