# CURRENT_TASK.md — Tarefa Atual

## Status

Em desenvolvimento.

## Tarefa ativa

### SEC-01 — Backend: configuração de CORS restritivo

## Camada

Backend / Segurança

## Prioridade

Alta

## Objetivo

Configurar CORS de forma restritiva no backend, permitindo apenas origens explicitamente autorizadas e preparando a aplicação para comunicação segura entre frontend e API.

## Contexto

O projeto já possui:

- backend Spring Boot estruturado
- frontend React + Vite
- endpoints de cadastro e login
- padrão global de erros
- versionamento `/api/v1`
- configuração via variáveis de ambiente

Outros integrantes estão trabalhando em:

- EPIC-02: Passkeys/WebAuthn
- EPIC-04: JWT e tokens

O EPIC-03 depende de tokens, então esta tarefa avança na camada de segurança HTTP sem bloquear as demais frentes.

## Objetivo funcional

Configurar CORS no Spring Security para:

- permitir somente origens confiáveis
- rejeitar origens não listadas
- permitir credenciais apenas para origens confiáveis
- responder corretamente a requisições preflight `OPTIONS`
- restringir métodos HTTP permitidos
- restringir headers permitidos
- configurar cache de preflight

## Variáveis de ambiente

Adicionar configuração para origens permitidas.

Exemplo:

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Request-ID
CORS_ALLOW_CREDENTIALS=true
CORS_MAX_AGE=3600
````

## Checklist de implementação

### Configuração

* [ ] Criar ou atualizar configuração de CORS no backend.
* [ ] Integrar CORS com Spring Security.
* [ ] Usar `CorsConfigurationSource`.
* [ ] Ler origens permitidas de variável de ambiente.
* [ ] Não hardcodar origem diretamente no código.
* [ ] Documentar variáveis no `.env.example`.

### Origens permitidas

* [ ] Permitir explicitamente o frontend local:

  * [ ] `http://localhost:5173`
  * [ ] `http://127.0.0.1:5173`
* [ ] Não usar wildcard `*` em endpoints autenticados.
* [ ] Garantir que origem não listada não receba headers CORS permissivos.

### Métodos HTTP

Permitir somente:

* [ ] `GET`
* [ ] `POST`
* [ ] `PUT`
* [ ] `DELETE`
* [ ] `OPTIONS`

Evitar liberar métodos desnecessários como:

* `PATCH`, se não estiver sendo usado
* `TRACE`
* `HEAD`, se não for necessário explicitamente

### Headers permitidos

Permitir apenas headers necessários:

* [ ] `Authorization`
* [ ] `Content-Type`
* [ ] `X-Request-ID`

Preparar para headers futuros, mas não liberar tudo com `*`.

### Credenciais

* [ ] Configurar `allowCredentials=true` apenas quando as origens forem explícitas.
* [ ] Não combinar `allowCredentials=true` com `allowedOrigins=*`.
* [ ] Preparar a base para cookies HttpOnly de refresh token no futuro.

### Preflight

* [ ] Requisições `OPTIONS` devem responder corretamente.
* [ ] Preflight deve retornar status adequado.
* [ ] Configurar `Access-Control-Max-Age`.
* [ ] Garantir que o navegador consiga chamar o backend a partir do frontend local.

### Spring Security

* [ ] Garantir que `.cors()` esteja habilitado na configuração de segurança.
* [ ] Não desabilitar segurança globalmente.
* [ ] Não liberar endpoints desnecessários além do escopo atual.
* [ ] Manter endpoints públicos já existentes funcionando.

## Testes manuais recomendados

### Teste com curl — origem permitida

```bash
curl -i -X OPTIONS http://localhost:8080/api/v1/health \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET"
```

Esperado:

* status adequado
* `Access-Control-Allow-Origin: http://localhost:5173`
* `Access-Control-Allow-Methods` presente
* `Access-Control-Max-Age` presente

### Teste com curl — origem não permitida

```bash
curl -i -X OPTIONS http://localhost:8080/api/v1/health \
  -H "Origin: http://malicious.example.com" \
  -H "Access-Control-Request-Method: GET"
```

Esperado:

* não retornar `Access-Control-Allow-Origin` permissivo
* navegador bloquearia a requisição

### Teste pelo frontend

* [ ] Rodar backend.
* [ ] Rodar frontend.
* [ ] Chamar endpoint do backend pelo frontend.
* [ ] Confirmar que origem permitida funciona.
* [ ] Alterar origem/porta e confirmar bloqueio.

## Critérios de aceitação

* [ ] CORS configurado via `CorsConfigurationSource`.
* [ ] Origens permitidas vêm de variável de ambiente.
* [ ] Origem não listada é bloqueada pelo navegador.
* [ ] Wildcard `*` não aparece em endpoints autenticados.
* [ ] `OPTIONS` retorna headers CORS corretos para origem permitida.
* [ ] `Access-Control-Max-Age` está presente.
* [ ] `allowCredentials=true` não é usado com wildcard.
* [ ] `.env.example` documenta as variáveis de CORS.
* [ ] Frontend local consegue consumir backend.
* [ ] Configuração está preparada para cookies HttpOnly futuros.

## Cuidados importantes

### CORS não é autenticação

CORS é uma política de navegador. Ele não substitui autenticação, autorização, CSRF, JWT ou validação no backend.

### CORS não bloqueia curl/Postman

Ferramentas como curl e Postman não obedecem CORS como o navegador. Por isso, o teste real de CORS deve considerar comportamento do browser.

### Wildcard com credenciais é proibido

Nunca usar:

```text
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
```

Essa combinação é insegura e inválida para fluxos com cookies/autenticação.

## Fora de escopo

Não implementar ainda:

* CSRF.
* JWT.
* Refresh token.
* Cookie HttpOnly.
* Sessões.
* Passkeys.
* TOTP.
* Security Headers completos.
* CSP.
* Rate limiting global.
* Autorização por roles.
* Deploy em produção.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
