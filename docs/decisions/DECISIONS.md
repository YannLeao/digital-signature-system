# Architectural Decisions

Este arquivo registra decisões técnicas importantes para evitar mudanças acidentais.

## ADR-001 — Monorepositório simples

O projeto usa um monorepositório com `backend/` e `frontend/`.

Motivo: simplificar desenvolvimento, documentação e integração nas fases iniciais.

## ADR-002 — Backend em Spring Boot

O backend será desenvolvido com Spring Boot 3 e Java 21.

Motivo: ecossistema maduro, integração com segurança, JPA, validação e APIs REST.

## ADR-003 — Frontend em React + Vite + JavaScript

O frontend será desenvolvido com React, Vite e TypeScript.

Motivo: produtividade, tipagem, ecossistema amplo e build rápido.

## ADR-004 — PostgreSQL + Flyway

O banco principal será PostgreSQL e o versionamento de schema será feito com Flyway.

Motivo: robustez, suporte a recursos avançados e rastreabilidade das mudanças.

## ADR-005 — Segurança implementada de forma incremental

Funcionalidades avançadas de segurança serão implementadas apenas quando suas bases estiverem prontas.

Motivo: evitar overengineering prematuro e reduzir risco de implementações parciais inseguras.
