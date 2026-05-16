# Roadmap Macro

Este arquivo contém apenas a visão macro do backlog.

Detalhes completos de cada card devem ficar nos arquivos específicos de épicos e requisitos.

## EPIC-00 — Setup & Infraestrutura Base

- [x] SETUP-01 — Inicializar projeto Spring Boot
- [x] SETUP-02 — Inicializar projeto React + Vite + Tailwind
- [ ] SETUP-03 — Configurar PostgreSQL e Flyway
- [ ] SETUP-04 — Configurar padrão global de erros da API
- [ ] SETUP-05 — Configurar versionamento de API `/api/v1`

## EPIC-01 — Autenticação com Login e Senha

- [ ] AUTH-01 — Cadastro de usuário com Argon2id
- [ ] AUTH-02 — Login com mensagem genérica e proteção contra brute force
- [ ] AUTH-03 — Tela de cadastro e login

## EPIC-02 — Passkeys / WebAuthn

- [ ] PASSKEY-01 — Registro de credencial
- [ ] PASSKEY-02 — Autenticação e validação de counter
- [ ] PASSKEY-03 — UI de registro e autenticação com passkey

## EPIC-03 — TOTP / 2FA

- [ ] TOTP-01 — Ativação do 2FA
- [ ] TOTP-02 — Validação TOTP no login
- [ ] TOTP-03 — UI de ativação e verificação

## EPIC-04 — JWT e Refresh Tokens

- [ ] JWT-01 — JWT com algoritmo assimétrico e claims
- [ ] JWT-02 — Refresh token com rotação e cookie HttpOnly
- [ ] JWT-03 — Denylist pós-logout

## EPIC-05 — CORS e Headers de Segurança

- [ ] SEC-01 — CORS restritivo
- [ ] SEC-02 — HTTP security headers

## EPIC-06 — Assinatura Digital de PDFs

- [ ] SIGN-01 — Geração e armazenamento de par de chaves
- [ ] SIGN-02 — Assinatura criptográfica embutida no PDF
- [ ] SIGN-03 — Tela de upload e posicionamento do selo
- [ ] SIGN-04 — Validação e sanitização de PDFs

## EPIC-07 — Verificação Pública

- [ ] VERIFY-01 — Endpoint público de verificação
- [ ] VERIFY-02 — Tela pública de verificação

## EPIC-08 — Sessões, Auditoria e Notificações

- [ ] AUDIT-01 — Log imutável de auditoria
- [ ] AUDIT-02 — Sessões ativas e revogação
- [ ] AUDIT-03 — Notificações por e-mail
- [ ] AUDIT-04 — Tela de sessões e histórico

## EPIC-09 — CSRF e Segurança Complementar

- [ ] CSRF-01 — Proteção CSRF
- [ ] CSRF-02 — Integração CSRF no frontend
- [ ] DEP-01 — Auditoria de dependências
