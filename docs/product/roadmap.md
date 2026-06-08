# Roadmap Macro

Este arquivo contem apenas a visao macro do backlog.

Detalhes completos de cada card devem ficar nos arquivos especificos de epicos e requisitos.

## EPIC-00 - Setup & Infraestrutura Base

- [x] SETUP-01 - Inicializar projeto Spring Boot
- [x] SETUP-02 - Inicializar projeto React + Vite + Tailwind
- [x] SETUP-03 - Configurar PostgreSQL e Flyway
- [x] SETUP-04 - Configurar padrao global de erros da API
- [x] SETUP-05 - Configurar versionamento de API `/api/v1`

## EPIC-01 - Autenticacao com Login e Senha

- [x] AUTH-01 - Cadastro de usuario com Argon2id
- [x] AUTH-02 - Login com mensagem generica e protecao contra brute force
- [x] AUTH-03 - Tela de cadastro e login

## EPIC-02 - Passkeys / WebAuthn

- [x] PASSKEY-01 - Registro de credencial
- [x] PASSKEY-02 - Autenticacao e validacao de counter
- [x] PASSKEY-03 - UI de registro e autenticacao com passkey

## EPIC-03 - TOTP / 2FA

- [x] TOTP-01 - Ativacao do 2FA
- [x] TOTP-02 - Validacao TOTP no login
- [x] TOTP-03 - UI de ativacao e verificacao

## EPIC-04 - JWT e Refresh Tokens

- [x] JWT-01 - JWT com algoritmo assimetrico e claims
- [x] JWT-02 - Refresh token com rotacao e cookie HttpOnly
- [x] JWT-03 - Denylist pos-logout

## EPIC-05 - CORS e Headers de Seguranca

- [x] SEC-01 - CORS restritivo
- [x] SEC-02 - HTTP security headers

## EPIC-06 - Assinatura Digital de PDFs

- [ ] SIGN-01 - Geracao e armazenamento de par de chaves
- [ ] SIGN-02 - Assinatura criptografica embutida no PDF
- [ ] SIGN-03 - Tela de upload e posicionamento do selo
- [ ] SIGN-04 - Validacao e sanitizacao de PDFs

## EPIC-07 - Verificacao Publica

- [ ] VERIFY-01 - Endpoint publico de verificacao
- [ ] VERIFY-02 - Tela publica de verificacao

## EPIC-08 - Sessoes, Auditoria e Notificacoes

- [ ] AUDIT-01 - Log imutavel de auditoria
- [ ] AUDIT-02 - Sessoes ativas e revogacao
- [ ] AUDIT-03 - Notificacoes por e-mail
- [ ] AUDIT-04 - Tela de sessoes e historico

## EPIC-09 - CSRF e Seguranca Complementar

- [x] CSRF-01 - Protecao CSRF
- [x] CSRF-02 - Integracao CSRF no frontend
- [ ] DEP-01 - Auditoria de dependencias
