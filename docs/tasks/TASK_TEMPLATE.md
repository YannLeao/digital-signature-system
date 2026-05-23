# CURRENT_TASK.md — Tarefa Atual

## Status

Status.

## Tarefa ativa

### Referência da tarefa. exemplo: SEC-02 — Backend: HTTP Security Headers obrigatórios

## Camada

Ex.: Backend / Segurança

## Prioridade

Ex.: Alta

## Objetivo

...

## Contexto

O projeto já possui:

- ...

Esta tarefa fortalece a camada HTTP da aplicação antes da evolução completa de:

- ...

## Objetivo funcional

...

## Checklist de implementação

- [ ] ...

### Estrutura recomendada

Ex.:

```text
config/
├── SecurityConfig.java
├── CorsConfig.java
└── SecurityHeadersConfig.java (opcional)
```

A implementação pode variar, desde que mantenha clareza arquitetural.

### Regras

* [ ] ...

## Variáveis de ambiente

...

## Testes manuais recomendados

### curl
Ex.: 

Executar:

```bash
curl -I http://localhost:8080/api/v1/health
```

## Critérios de aceitação

* [ ] ...

## Cuidados importantes

...

## Fora de escopo

Não implementar ainda:

* ...

## Arquivos relevantes

Ex.:

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`

## Prompt recomendado para o agente

...

Ex.:

Ao final:

* informe os arquivos criados/alterados
* explique como validar os headers com curl
* explique como validar CSP no navegador
* explique possíveis impactos no frontend
* explique como validar que o frontend continua funcional
