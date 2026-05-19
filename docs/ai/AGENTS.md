# AGENTS.md — Guia para Agentes de IA

Este arquivo define as regras globais que qualquer agente de IA deve seguir ao trabalhar neste repositório.

## Objetivo

Ajudar no desenvolvimento incremental do projeto sem quebrar arquitetura, escopo ou decisões técnicas já definidas.

## Regra principal

Sempre leia primeiro:

1. `docs/ai/CONTEXT.md`
2. `docs/ai/CURRENT_TASK.md`
3. Arquivos específicos referenciados pela tarefa atual

Não avance para tarefas futuras sem autorização explícita.

## Stack oficial

### Backend
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

### Frontend
- React
- Vite
- TypeScript
- TailwindCSS
- React Router
- Axios
- React Hook Form
- Zod

## Estrutura esperada do repositório

```text
.
├── backend/
├── frontend/
├── docs/
│   ├── ai/
│   ├── architecture/
│   ├── product/
│   ├── security/
│   └── standards/
├── README.md
├── .gitignore
└── ROADMAP.md
```

## Regras gerais

- Não implementar funcionalidades fora da tarefa ativa.
- Não adicionar dependências sem necessidade clara.
- Não alterar decisões arquiteturais sem registrar justificativa.
- Não antecipar épicos futuros.
- Não criar código mockado como solução final.
- Não expor segredos, tokens, chaves privadas ou variáveis sensíveis.

## Regras de desenvolvimento

- Código deve ser simples, incremental e testável.
- Priorizar clareza antes de abstrações prematuras.
- Toda implementação deve respeitar os critérios de aceitação da tarefa atual.
- Após concluir uma tarefa, atualizar `docs/ai/CURRENT_TASK.md` e, se necessário, o roadmap.

## Regras de backend

Consulte também:

- `docs/architecture/backend.md`
- `docs/security/security-overview.md`
- `docs/standards/api.md`

## Regras de frontend

Consulte também:

- `docs/architecture/frontend.md`
- `docs/standards/coding.md`

## Quando estiver em dúvida

1. Não invente requisito.
2. Não implemente fora do escopo.
3. Explique a dúvida no final da resposta.
4. Sugira o menor próximo passo seguro.
