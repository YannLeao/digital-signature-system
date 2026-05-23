# CURRENT_TASK.md — Tarefa Atual

## Status

Concluido.

## Tarefa ativa

### SETUP-02 — Inicializar projeto React + Vite + Tailwind

## Camada

Frontend

## Prioridade

Alta

## Objetivo

Criar a estrutura inicial do frontend usando React, Vite, TypeScript e TailwindCSS.

## Contexto

O backend inicial já foi criado. Agora o projeto precisa da base frontend para permitir as próximas telas, rotas protegidas, integração com API e fluxos de autenticação.

## Checklist de implementação

- [x] Criar projeto React com Vite e TypeScript dentro de `frontend/`.
- [x] Instalar e configurar TailwindCSS.
- [x] Instalar dependências base:
  - [x] `react-router-dom`
  - [x] `axios`
  - [x] `react-hook-form`
  - [x] `zod`
  - [x] `@hookform/resolvers`
- [x] Criar estrutura de pastas:
  - [x] `src/pages/`
  - [x] `src/components/`
  - [x] `src/hooks/`
  - [x] `src/services/`
  - [x] `src/utils/`
  - [x] `src/contexts/`
  - [x] `src/routes/`
- [x] Criar `.env.example` com `VITE_API_BASE_URL`.
- [x] Configurar proxy do Vite para o backend em desenvolvimento.
- [x] Criar layout base inicial.
- [x] Criar roteamento inicial.
- [x] Criar componente `PrivateRoute`.
- [x] Fazer rota protegida redirecionar para `/login` quando não houver token.

## Critérios de aceitação

- [x] Projeto roda com `npm run dev` sem erros.
- [x] Tailwind está funcional com classe utilitária visível.
- [x] Estrutura de pastas foi criada.
- [x] `.env.example` existe.
- [x] Rota protegida redireciona para `/login` quando não há token.
- [x] Nenhuma funcionalidade futura foi implementada fora do escopo.

## Fora de escopo

Não implementar ainda:

- Tela final de login
- Tela final de cadastro
- Integração real com autenticação backend
- JWT completo
- 2FA
- Passkeys
- Assinatura de PDFs
- Dashboard completo

## Prompt recomendado para o agente

Implemente completamente a tarefa SETUP-02 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, `docs/architecture/frontend.md` 
e este arquivo. Não implemente funcionalidades fora do escopo. Ao final, informe os arquivos criados/alterados e como 
validar com `npm run dev`.
