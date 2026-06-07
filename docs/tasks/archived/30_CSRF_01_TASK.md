# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### CSRF-01 — Backend: proteção CSRF via Double Submit Cookie

## Camada

Backend / Segurança

## Prioridade

Alta

## Objetivo

Implementar proteção CSRF no backend para requisições mutantes, considerando que o sistema utiliza Refresh Token em cookie HttpOnly.

## Contexto

O projeto já possui:

- autenticação com login e senha
- JWT com algoritmo assimétrico
- Refresh Token via cookie HttpOnly
- rotação de Refresh Token
- denylist pós-logout
- CORS restritivo
- HTTP Security Headers
- fluxo de TOTP/2FA
- fluxo de Passkeys/WebAuthn

Como o Refresh Token é enviado automaticamente pelo navegador via cookie, o sistema precisa de proteção CSRF para impedir requisições forjadas a partir de sites maliciosos.

Esta tarefa implementa a proteção no backend. A integração global do Axios e envio automático do header CSRF será feita na próxima tarefa:

```text
CSRF-02 — Frontend: integração do token CSRF nas requisições
````

## Estratégia escolhida

Implementar Double Submit Cookie.

O backend deve:

1. gerar um token CSRF aleatório;
2. enviar esse token em cookie legível pelo JavaScript;
3. exigir que requisições mutantes enviem o mesmo valor em header;
4. comparar cookie e header no backend;
5. rejeitar requisições inválidas com `403 Forbidden`.

## Conceito

O navegador envia cookies automaticamente.

Um site malicioso poderia tentar fazer:

```text
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/documents/sign
```

usando cookies da vítima.

Com Double Submit Cookie:

* o cookie CSRF é enviado pelo navegador;
* mas o atacante não consegue ler esse cookie em outro domínio;
* então não consegue preencher corretamente o header `X-CSRF-Token`.

## Cookies envolvidos

### Refresh Token

Já existente:

```text
refresh_token
```

Características:

* HttpOnly
* Secure em produção
* SameSite=Strict
* não legível por JavaScript

### CSRF Token

Novo cookie:

```text
XSRF-TOKEN
```

Características:

* não HttpOnly
* Secure em produção
* SameSite=Strict
* legível pelo frontend
* usado apenas para montar o header

## Header obrigatório

```text
X-CSRF-Token
```

Em requisições mutantes, o backend deve exigir:

```text
Cookie: XSRF-TOKEN=<valor>
X-CSRF-Token: <mesmo valor>
```

## Escopo de proteção

Aplicar proteção CSRF para métodos mutantes:

* [ ] `POST`
* [ ] `PUT`
* [ ] `DELETE`
* [ ] `PATCH`, se existir no projeto

Não exigir CSRF para:

* [ ] `GET`
* [ ] `HEAD`
* [ ] `OPTIONS`

## Endpoints que devem exigir CSRF

Exigir CSRF em endpoints mutantes autenticados e/ou sensíveis, incluindo:

* [ ] `/api/v1/auth/refresh`
* [ ] `/api/v1/auth/logout`
* [ ] endpoints de assinatura de documentos
* [ ] endpoints futuros de alteração de dados
* [ ] endpoints futuros de configuração de 2FA
* [ ] endpoints futuros de passkeys protegidos

## Endpoints que podem ser isentos

Avaliar isenção para endpoints públicos que ainda não dependem de cookie autenticado, como:

* [ ] `POST /api/v1/auth/login`
* [ ] `POST /api/v1/auth/register`

Mas documentar a decisão.

### Regra recomendada

* `login` e `register` podem ficar sem CSRF inicialmente, pois ainda não dependem de sessão via cookie.
* `refresh` e `logout` devem exigir CSRF, pois dependem de cookie.

## Checklist de implementação

### Configuração

* [ ] Criar configuração centralizada de CSRF.
* [ ] Integrar com Spring Security.
* [ ] Não espalhar validação manual em controllers.
* [ ] Não quebrar CORS.
* [ ] Não quebrar JWT filter.
* [ ] Não quebrar Refresh Token.
* [ ] Não quebrar logout.

### Token generation

* [ ] Gerar token aleatório criptograficamente seguro.
* [ ] Usar `SecureRandom`.
* [ ] Usar tamanho adequado, por exemplo 256 bits.
* [ ] Usar formato URL-safe.
* [ ] Não usar token previsível.
* [ ] Não usar valor fixo.
* [ ] Não reutilizar segredo sensível como token CSRF.

### Cookie CSRF

* [ ] Criar cookie `XSRF-TOKEN`.
* [ ] Cookie deve ser legível pelo JavaScript.
* [ ] Cookie deve ter `HttpOnly=false`.
* [ ] Cookie deve usar `SameSite=Strict`.
* [ ] Cookie deve usar `Secure=true` em produção.
* [ ] Cookie deve ter `Path=/`.
* [ ] Cookie deve respeitar configuração por ambiente.

### Header CSRF

* [ ] Exigir header `X-CSRF-Token`.
* [ ] Comparar header com cookie.
* [ ] Rejeitar se cookie ausente.
* [ ] Rejeitar se header ausente.
* [ ] Rejeitar se valores forem diferentes.
* [ ] Usar comparação segura quando aplicável.

### Emissão do token CSRF

Emitir/renovar token CSRF em momentos adequados:

* [ ] após login bem-sucedido;
* [ ] após refresh bem-sucedido;
* [ ] opcionalmente em endpoint dedicado;
* [ ] após logout, limpar cookie CSRF.

Endpoint dedicado recomendado:

```text
GET /api/v1/auth/csrf
```

Esse endpoint pode ser útil para o frontend inicializar o token antes de chamadas mutantes.

Se implementado:

* [ ] não deve exigir CSRF;
* [ ] deve emitir cookie `XSRF-TOKEN`;
* [ ] deve retornar resposta simples;
* [ ] não deve expor dados sensíveis.

Exemplo:

```json
{
  "message": "CSRF token issued."
}
```

## Variáveis de ambiente

Adicionar ao `.env.example`, se necessário:

```env
CSRF_COOKIE_NAME=XSRF-TOKEN
CSRF_HEADER_NAME=X-CSRF-Token
CSRF_COOKIE_SECURE=false
CSRF_COOKIE_SAME_SITE=Strict
CSRF_COOKIE_PATH=/
```

Em produção:

```env
CSRF_COOKIE_SECURE=true
```

## CORS

Como o frontend enviará header customizado:

```text
X-CSRF-Token
```

Atualizar CORS para permitir esse header.

Checklist:

* [ ] `X-CSRF-Token` está em allowed headers.
* [ ] `withCredentials` continuará funcionando no frontend.
* [ ] `allowCredentials=true` continua restrito a origens explícitas.
* [ ] wildcard `*` não foi introduzido.
* [ ] preflight `OPTIONS` funciona com `X-CSRF-Token`.

## Tratamento de erro

Requisição CSRF inválida deve retornar:

```http
403 Forbidden
```

Com erro padronizado:

```json
{
  "code": "SEC_001",
  "message": "Requisição bloqueada por política de segurança.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

Garantir:

* [ ] sem stack trace;
* [ ] sem detalhes internos;
* [ ] sem vazamento de tokens;
* [ ] sem indicar exatamente qual parte falhou, se isso enfraquecer segurança.

## Spring Security

Verificar abordagem mais adequada:

* [ ] usar CSRF nativo do Spring Security com CookieCsrfTokenRepository, se compatível;
* [ ] ou implementar filtro próprio simples e testável;
* [ ] evitar soluções duplicadas;
* [ ] documentar decisão.

### Atenção

Se usar `CookieCsrfTokenRepository`, verificar:

* nome do cookie;
* nome do header;
* `HttpOnly=false`;
* comportamento com API REST;
* integração com JSON error handler;
* rotas ignoradas.

## Testes automatizados

Adicionar testes para os fluxos críticos.

### CSRF válido

* [ ] POST protegido com cookie e header iguais retorna sucesso ou passa pelo filtro.
* [ ] PUT protegido com cookie e header iguais passa.
* [ ] DELETE protegido com cookie e header iguais passa.

### CSRF inválido

* [ ] POST sem cookie retorna `403`.
* [ ] POST sem header retorna `403`.
* [ ] POST com valores diferentes retorna `403`.
* [ ] POST com token vazio retorna `403`.
* [ ] GET não exige CSRF.
* [ ] OPTIONS não exige CSRF.

### Endpoints específicos

* [ ] `/api/v1/auth/refresh` exige CSRF.
* [ ] `/api/v1/auth/logout` exige CSRF.
* [ ] `/api/v1/auth/login` não exige CSRF, se essa for a decisão.
* [ ] `/api/v1/auth/register` não exige CSRF, se essa for a decisão.
* [ ] `/api/v1/auth/csrf` emite cookie, se implementado.

### CORS

* [ ] Preflight com `X-CSRF-Token` é aceito para origem permitida.
* [ ] Origem não permitida continua bloqueada.

## Testes manuais recomendados

### Obter token CSRF

```bash
curl -i http://localhost:8080/api/v1/auth/csrf
```

Verificar:

* [ ] `Set-Cookie: XSRF-TOKEN=...`

### Requisição sem CSRF

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout
```

Esperado:

```http
403 Forbidden
```

### Requisição com CSRF válido

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H "X-CSRF-Token: <valor>" \
  -b "XSRF-TOKEN=<valor>; refresh_token=<valor>"
```

Esperado:

* passar pela validação CSRF;
* falhar apenas se autenticação/token estiver inválido.

### Preflight com header CSRF

```bash
curl -i -X OPTIONS http://localhost:8080/api/v1/auth/logout \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: X-CSRF-Token, Content-Type"
```

Esperado:

* `Access-Control-Allow-Headers` inclui `X-CSRF-Token`.

## Critérios de aceitação

* [ ] Requisições mutantes protegidas exigem CSRF.
* [ ] GET/OPTIONS não exigem CSRF.
* [ ] `/auth/refresh` exige CSRF.
* [ ] `/auth/logout` exige CSRF.
* [ ] Header `X-CSRF-Token` é aceito no CORS.
* [ ] Cookie `XSRF-TOKEN` é emitido corretamente.
* [ ] Token CSRF é aleatório e seguro.
* [ ] Erros CSRF retornam `403` com formato padronizado.
* [ ] Login/register continuam funcionando.
* [ ] Refresh/logout continuam funcionando quando CSRF válido é enviado.
* [ ] Testes automatizados foram adicionados.
* [ ] Não houve implementação de frontend nesta tarefa.

## Fora de escopo

Não implementar nesta tarefa:

* Axios global.
* Interceptor frontend.
* Renovação automática frontend.
* UI de login/cadastro.
* UI de passkeys.
* UI de TOTP.
* Assinatura digital.
* Auditoria completa.
* Redis.
* Mudança ampla de autenticação.
* CSRF-02.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
* `docs/decisions/DECISIONS.md`
