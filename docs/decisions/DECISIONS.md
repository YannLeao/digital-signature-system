# Architectural Decisions

Este arquivo registra decisões técnicas importantes para evitar mudanças acidentais.

## ADR-001 — Monorepositório simples

O projeto usa um monorepositório com `backend/` e `frontend/`.

Motivo: simplificar desenvolvimento, documentação e integração nas fases iniciais.

## ADR-002 — Backend em Spring Boot

O backend será desenvolvido com Spring Boot 3 e Java 21.

Motivo: ecossistema maduro, integração com segurança, JPA, validação e APIs REST.

## ADR-003 — Frontend em React + Vite + TypeScript

O frontend será desenvolvido com React, Vite e TypeScript.

Motivo: produtividade, tipagem, ecossistema amplo e build rápido.

## ADR-004 — PostgreSQL + Flyway

O banco principal será PostgreSQL e o versionamento de schema será feito com Flyway.

Motivo: robustez, suporte a recursos avançados e rastreabilidade das mudanças.

## ADR-005 — Segurança implementada de forma incremental

Funcionalidades avançadas de segurança serão implementadas apenas quando suas bases estiverem prontas.

Motivo: evitar overengineering prematuro e reduzir risco de implementações parciais inseguras.

## ADR-006 - JWT usa RS256

Access tokens JWT serao assinados com RS256 usando par de chaves RSA assimetrico.

Motivo: separar emissao e validacao por chave privada/publica, evitar segredo compartilhado HS256 e preparar a 
arquitetura para validacao centralizada, denylist futura e gerenciamento de sessoes.

As chaves devem ser fornecidas por ambiente como DER codificado em Base64:

- `JWT_PRIVATE_KEY_BASE64`: chave privada PKCS#8.
- `JWT_PUBLIC_KEY_BASE64`: chave publica X.509/SPKI.

Chaves reais de ambiente nao devem ser commitadas no repositorio.

## ADR-007 - Refresh tokens persistidos inicialmente em PostgreSQL

Refresh tokens opacos serao persistidos inicialmente em PostgreSQL, armazenando apenas SHA-256 do token.

Motivo: PostgreSQL ja e a base transacional do projeto e atende a necessidade atual de rotacao, rastreio de familia de 
tokens, deteccao de reuso e invalidacao de sessao sem introduzir Redis antes de haver demanda real de cache ou distribuicao.

Redis podera ser reavaliado em epicos futuros de sessoes ativas, auditoria e escala.

## ADR-008 - Denylist JWT persistida inicialmente em PostgreSQL

Access tokens revogados apos logout serao registrados inicialmente em PostgreSQL usando a claim `jti` como identificador 
unico.

Motivo: PostgreSQL ja e a base transacional do projeto, simplifica auditoria e evita introduzir Redis antes de haver 
necessidade concreta de cache distribuido ou multiplas instancias.

A denylist armazena `jti`, usuario, `session_id`, expiracao do access token, instante de revogacao e motivo controlado. 
Entradas expiradas devem ser removidas futuramente por rotina periodica simples.

Redis podera ser reavaliado em epicos futuros de sessoes ativas, auditoria, multiplas instancias e escalabilidade horizontal.
