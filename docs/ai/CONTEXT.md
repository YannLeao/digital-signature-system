# CONTEXT.md — Contexto Geral do Projeto

Este arquivo resume o estado geral do projeto para agentes de IA e desenvolvedores.

## Visão geral

O projeto é uma plataforma web fullstack com foco em autenticação segura, assinatura digital de PDFs, verificação pública de autenticidade, auditoria e proteção avançada de sessões.

O sistema será desenvolvido em um monorepositório simples, contendo:

- Backend em Spring Boot
- Frontend em React + Vite + Tailwind
- Banco PostgreSQL
- Migrações com Flyway

## Estado atual

O projeto ainda está nas fases iniciais de setup.

A primeira etapa de inicialização do backend já foi executada com sucesso.

A próxima etapa de foco é a inicialização do frontend com React, Vite e TailwindCSS.

## Escopo de alto nível

O produto contempla os seguintes grandes blocos:

1. Setup e infraestrutura base
2. Autenticação com login e senha
3. Autenticação com passkeys/WebAuthn
4. Autenticação de dois fatores com TOTP
5. Tokens JWT e refresh tokens seguros
6. CORS e headers de segurança
7. Assinatura digital de PDFs
8. Verificação pública de autenticidade
9. Sessões, auditoria e notificações
10. CSRF, dependências e segurança complementar

## Princípio de execução

O desenvolvimento deve seguir ordem de precedência.

Não implementar autenticação, segurança avançada, assinatura digital ou auditoria antes da infraestrutura base estar pronta.

## Documentos importantes

- `docs/ai/CURRENT_TASK.md`: tarefa ativa
- `docs/product/roadmap.md`: visão macro do backlog
- `docs/product/epics.md`: resumo dos épicos
- `docs/product/requirements.md`: requisitos funcionais e não funcionais
- `docs/architecture/backend.md`: arquitetura backend
- `docs/architecture/frontend.md`: arquitetura frontend
- `docs/security/security-overview.md`: visão de segurança
- `docs/standards/coding.md`: padrões de código
- `docs/standards/api.md`: padrões de API

## Decisão importante

O agente deve tratar este projeto como um sistema de segurança sensível.

Qualquer implementação envolvendo autenticação, tokens, senhas, PDFs, criptografia, sessões ou headers de segurança deve ser feita com cautela e sem simplificações inseguras.
