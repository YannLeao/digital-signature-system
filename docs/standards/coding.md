# Coding Standards

## Geral

- Priorizar clareza.
- Evitar overengineering.
- Implementar de forma incremental.
- Não misturar responsabilidades.
- Não criar abstrações antes de necessidade real.

## Backend

- Usar constructor injection.
- Evitar `@Autowired` em atributos.
- Controllers não têm regra de negócio.
- Services não devem conhecer detalhes de HTTP.
- DTOs não devem ser entidades JPA.
- Exceptions devem ser tratadas centralmente.
- Configurações sensíveis via variável de ambiente.

## Frontend

- Usar TypeScript.
- Componentes devem ser pequenos e legíveis.
- Chamadas HTTP devem ficar em `services/`.
- Formulários devem usar `react-hook-form` + `zod`.
- Estado global só quando necessário.
- Evitar lógica complexa diretamente no JSX.

## Commits

Usar Conventional Commits.

Exemplos:

```text
feat(frontend): initialize vite react app
fix(auth): return generic invalid credentials message
refactor(backend): move environment validation to config
chore(docs): split roadmap into modular docs
```
