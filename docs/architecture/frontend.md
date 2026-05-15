# Arquitetura Frontend

## Stack

- React
- Vite
- TypeScript
- TailwindCSS
- React Router
- Axios
- React Hook Form
- Zod

## Estrutura de pastas

```text
frontend/src/
├── components/
├── contexts/
├── hooks/
├── pages/
├── routes/
├── services/
├── utils/
├── App.tsx
└── main.tsx
```

## Responsabilidades

### components

Componentes reutilizáveis e visuais.

### pages

Telas completas da aplicação.

### routes

Configuração de rotas públicas, privadas e redirecionamentos.

### services

Clientes HTTP, integração com API e serviços externos.

### hooks

Hooks reutilizáveis de estado e comportamento.

### contexts

Contextos globais, como autenticação e preferências do usuário.

### utils

Funções auxiliares puras.

## Regras

- Usar TypeScript.
- Evitar lógica de API diretamente dentro de componentes.
- Centralizar chamadas HTTP em `services/`.
- Usar `react-hook-form` + `zod` para formulários.
- Usar Tailwind para estilização.
- Rotas privadas devem passar por `PrivateRoute`.
- Variáveis de ambiente frontend devem usar prefixo `VITE_`.

## Ambiente

Criar `.env.example` com:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Proxy de desenvolvimento

Configurar proxy do Vite para facilitar comunicação com o backend local durante desenvolvimento.

## Fora de escopo no setup inicial

Não implementar telas finais complexas antes dos épicos correspondentes.
