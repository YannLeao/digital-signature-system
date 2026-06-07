# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### SEC-02 — Backend: HTTP Security Headers obrigatórios

## Camada

Backend / Segurança

## Prioridade

Alta

## Objetivo

Implementar os principais HTTP Security Headers da aplicação para reduzir riscos relacionados a XSS, clickjacking, MIME sniffing, cache inseguro, carregamento de recursos não autorizados e downgrade de HTTPS.

## Contexto

O projeto já possui:

- backend Spring Boot estruturado
- CORS restritivo configurado
- autenticação inicial
- tratamento global de erros
- configuração centralizada
- frontend React + Vite

Esta tarefa fortalece a camada HTTP da aplicação antes da evolução completa de:

- JWT
- cookies HttpOnly
- CSRF
- TOTP
- sessões
- dashboard autenticado
- assinatura digital de PDFs

Os headers implementados aqui devem se tornar padrão global da API.

## Objetivo funcional

Adicionar headers HTTP de segurança globalmente em todas as respostas da API.

Os headers devem ser aplicados preferencialmente via:

- Spring Security
- filtro global
- configuração centralizada

Evitar repetição manual em controllers.

## Checklist de implementação

### Configuração global

- [ ] Implementar configuração centralizada de security headers.
- [ ] Garantir aplicação global para endpoints da API.
- [ ] Integrar com Spring Security já existente.
- [ ] Não adicionar headers manualmente em controllers individuais.
- [ ] Manter configuração organizada e legível.

## Headers obrigatórios

### Content-Security-Policy (CSP)

Adicionar política inicial segura.

Header esperado:

```text
Content-Security-Policy:
default-src 'self';
script-src 'self';
object-src 'none';
frame-ancestors 'none';
````

### Objetivos

* [ ] Bloquear scripts externos por padrão.
* [ ] Bloquear plugins/objects inseguros.
* [ ] Impedir embedding da aplicação via iframe.
* [ ] Preparar política para evolução futura do frontend.

### Cuidados

* [ ] Não usar CSP excessivamente permissiva.
* [ ] Evitar `unsafe-inline` sem necessidade.
* [ ] Evitar `*`.

## Strict-Transport-Security (HSTS)

Header esperado:

```text
Strict-Transport-Security:
max-age=63072000; includeSubDomains; preload
```

### Objetivos

* [ ] Forçar HTTPS em ambientes apropriados.
* [ ] Preparar compatibilidade futura com preload list.

### Cuidados

* [ ] Avaliar comportamento em desenvolvimento local HTTP.
* [ ] Não quebrar ambiente local.
* [ ] Documentar limitações para localhost.

## X-Frame-Options

Header esperado:

```text
X-Frame-Options: DENY
```

### Objetivo

* [ ] Impedir clickjacking via iframe.

## X-Content-Type-Options

Header esperado:

```text
X-Content-Type-Options: nosniff
```

### Objetivo

* [ ] Impedir MIME sniffing inseguro.

## Referrer-Policy

Header esperado:

```text
Referrer-Policy: strict-origin-when-cross-origin
```

### Objetivo

* [ ] Reduzir exposição desnecessária de URLs/referrer.

## Permissions-Policy

Header esperado:

```text
Permissions-Policy:
camera=(), microphone=(), geolocation=()
```

### Objetivo

* [ ] Bloquear APIs sensíveis não utilizadas.

## Cache-Control

Para rotas autenticadas ou sensíveis:

```text
Cache-Control: no-store
```

### Objetivos

* [ ] Evitar cache inseguro de dados sensíveis.
* [ ] Preparar segurança para JWT e sessões futuras.

### Cuidados

* [ ] Avaliar se deve ser aplicado globalmente ou apenas em endpoints específicos.
* [ ] Não prejudicar assets estáticos futuramente sem necessidade.

## X-XSS-Protection

Header esperado:

```text
X-XSS-Protection: 0
```

### Objetivo

* [ ] Desabilitar mecanismo legado/inconsistente de navegadores antigos.
* [ ] Confiar em CSP moderna em vez de heurísticas antigas.

## Spring Security

* [ ] Implementar headers preferencialmente via configuração do Spring Security.
* [ ] Manter configuração modular.
* [ ] Não desabilitar proteções existentes.
* [ ] Garantir compatibilidade com configuração de CORS já implementada.

## Arquitetura e organização

### Estrutura recomendada

```text
config/
├── SecurityConfig.java
├── CorsConfig.java
└── SecurityHeadersConfig.java (opcional)
```

A implementação pode variar, desde que mantenha clareza arquitetural.

### Regras

* [ ] Evitar duplicação de configuração.
* [ ] Não espalhar lógica de segurança HTTP em múltiplos arquivos sem necessidade.
* [ ] Preferir centralização.
* [ ] Manter legibilidade da configuração.

## Variáveis de ambiente

Caso necessário, permitir configuração externa de:

* CSP
* HSTS
* ambientes

Mas:

* [ ] não criar complexidade prematura
* [ ] não externalizar tudo sem necessidade real

## Compatibilidade frontend

* [ ] Garantir que frontend atual continue funcionando.
* [ ] Validar que CSP não bloqueia frontend legítimo.
* [ ] Validar chamadas API do frontend.
* [ ] Validar ambiente local React/Vite.

## Testes manuais recomendados

### curl

Executar:

```bash
curl -I http://localhost:8080/api/v1/health
```

Verificar presença de:

* [ ] `Content-Security-Policy`
* [ ] `Strict-Transport-Security`
* [ ] `X-Frame-Options`
* [ ] `X-Content-Type-Options`
* [ ] `Referrer-Policy`
* [ ] `Permissions-Policy`
* [ ] `X-XSS-Protection`

### Browser DevTools

* [ ] Abrir Network tab.
* [ ] Inspecionar response headers.
* [ ] Validar ausência de erros inesperados de CSP.
* [ ] Validar funcionamento do frontend.

### SecurityHeaders.com

* [ ] Validar futuramente em:

  * [ ] `https://securityheaders.com`

## Critérios de aceitação

* [ ] Todos os headers obrigatórios estão presentes.
* [ ] Configuração é centralizada.
* [ ] Frontend continua funcional.
* [ ] CSP não usa wildcard inseguro.
* [ ] `X-Frame-Options` está como `DENY`.
* [ ] `X-Content-Type-Options` está como `nosniff`.
* [ ] `Referrer-Policy` está configurado.
* [ ] `Permissions-Policy` restringe APIs sensíveis.
* [ ] `X-XSS-Protection` está desabilitado corretamente.
* [ ] Configuração não quebra desenvolvimento local.
* [ ] Integração com Spring Security permanece funcional.

## Cuidados importantes

### CSP pode quebrar frontend

Uma CSP excessivamente restritiva pode bloquear:

* scripts do frontend
* HMR do Vite
* chamadas API
* estilos inline necessários

Validar cuidadosamente no ambiente local.

### HSTS em localhost

HSTS é relevante principalmente para HTTPS real.

Não assumir HTTPS total em desenvolvimento local.

### Headers não substituem autenticação

Security headers complementam:

* autenticação
* CSRF
* JWT
* validação backend
* controle de acesso

Eles não substituem essas camadas.

## Fora de escopo

Não implementar ainda:

* CSRF.
* JWT.
* Cookies HttpOnly.
* Sessões.
* TOTP.
* Passkeys login.
* OAuth.
* Rate limiting global.
* WAF.
* Helmet no frontend.
* CDN policies.
* Deploy HTTPS real.
* Reverse proxy nginx.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
