# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### CSRF-02 — Frontend: integração do token CSRF nas requisições

## Camada

Frontend / Segurança / Integração

## Prioridade

Alta

## Objetivo

Configurar o cliente HTTP global do frontend para trabalhar corretamente com cookies, Refresh Token HttpOnly e proteção CSRF via Double Submit Cookie.

## Contexto

O backend já possui:

- JWT Access Token
- Refresh Token via cookie HttpOnly
- rotação de Refresh Token
- logout com denylist
- proteção CSRF via Double Submit Cookie
- cookie `XSRF-TOKEN`
- header obrigatório `X-CSRF-Token`
- endpoint para emissão/renovação de CSRF, se implementado
- CORS ajustado para aceitar `X-CSRF-Token`

Agora o frontend precisa centralizar a comunicação HTTP para que as próximas telas, como Passkeys e TOTP, não precisem repetir lógica de autenticação e segurança.

## Objetivo funcional

Criar/configurar cliente Axios global que:

- envia cookies com `withCredentials`
- lê o cookie `XSRF-TOKEN`
- envia `X-CSRF-Token` em requisições mutantes
- não envia CSRF em `GET`
- trata `401`, `403` e `429`
- tenta renovar Access Token quando apropriado
- evita loop infinito de refresh
- centraliza tipos TypeScript de erro/resposta

## Estrutura recomendada

```text
frontend/src/
├── services/
│   ├── api.ts
│   ├── csrfService.ts
│   └── authService.ts
├── types/
│   ├── api.ts
│   └── auth.ts
├── utils/
│   └── cookies.ts
└── contexts/
    └── AuthContext.tsx
````

Adaptar ao padrão já existente do projeto.

## Checklist de implementação

### Axios global

* [ ] Criar ou revisar `api.ts`.
* [ ] Usar `VITE_API_BASE_URL`.
* [ ] Não hardcodar URL da API.
* [ ] Configurar `withCredentials: true`.
* [ ] Configurar timeout razoável.
* [ ] Centralizar headers comuns.
* [ ] Exportar instância única reutilizável.

Exemplo conceitual:

```ts
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});
```

## CSRF

### Cookie

O backend emite:

```text
XSRF-TOKEN
```

### Header

O frontend deve enviar:

```text
X-CSRF-Token
```

### Regras

* [ ] Ler cookie `XSRF-TOKEN`.
* [ ] Enviar header `X-CSRF-Token` somente em métodos mutantes.
* [ ] Métodos mutantes:

    * [ ] `POST`
    * [ ] `PUT`
    * [ ] `DELETE`
    * [ ] `PATCH`
* [ ] Não enviar header em:

    * [ ] `GET`
    * [ ] `HEAD`
    * [ ] `OPTIONS`
* [ ] Não enviar header vazio.
* [ ] Não logar token CSRF.

## Cookie utility

Criar função utilitária segura e simples:

```text
utils/cookies.ts
```

Responsável por:

* [ ] ler cookie por nome.
* [ ] decodificar valor.
* [ ] lidar com cookie ausente.
* [ ] não escrever cookies sensíveis manualmente.

## CSRF bootstrap

Se o backend possuir endpoint dedicado, usar:

```text
GET /api/v1/auth/csrf
```

Criar serviço:

```text
csrfService.ts
```

Com função:

```ts
ensureCsrfToken()
```

Responsável por:

* [ ] verificar se `XSRF-TOKEN` existe.
* [ ] buscar novo token se ausente.
* [ ] não chamar repetidamente sem necessidade.
* [ ] não causar loop em interceptor.

## Request interceptor

Implementar interceptor de request para:

* [ ] detectar método HTTP.
* [ ] se método for mutante, buscar cookie CSRF.
* [ ] adicionar header `X-CSRF-Token`.
* [ ] manter headers existentes.
* [ ] preservar tipagem TypeScript.
* [ ] não quebrar `FormData`.

## Response interceptor

Implementar interceptor de response para tratar:

### 401 Unauthorized

* [ ] Se Access Token expirou, tentar `/auth/refresh`.
* [ ] Evitar múltiplos refresh concorrentes desnecessários.
* [ ] Repetir requisição original após refresh bem-sucedido.
* [ ] Se refresh falhar, limpar estado de autenticação.
* [ ] Redirecionar para `/login`, se apropriado.

### 403 Forbidden

* [ ] Se erro indicar CSRF inválido/expirado:

    * [ ] buscar novo CSRF token.
    * [ ] repetir uma única vez a requisição original.
* [ ] Evitar loop infinito.
* [ ] Se não for CSRF, propagar erro.

### 429 Too Many Requests

* [ ] Propagar erro para UI.
* [ ] Exibir mensagem amigável no nível da tela, não no interceptor.
* [ ] Não repetir automaticamente.

## Atenção ao refresh

Endpoint esperado:

```text
POST /api/v1/auth/refresh
```

Como é mutante e usa cookie:

* [ ] deve enviar `X-CSRF-Token`.
* [ ] deve usar `withCredentials`.
* [ ] deve receber novo Access Token.
* [ ] deve atualizar estado/token em memória.
* [ ] não deve acessar Refresh Token via JS.

## Access Token

Como o Refresh Token é HttpOnly, o frontend não acessa o refresh diretamente.

Para Access Token:

* [ ] manter estratégia atual do projeto.
* [ ] preferir armazenamento em memória/contexto se possível.
* [ ] evitar localStorage para token sensível, se ainda houver margem de decisão.
* [ ] se o projeto já usa localStorage temporariamente, documentar risco e preparar refatoração futura.

## AuthService

Atualizar `authService.ts` para usar a instância global `api`.

Funções esperadas:

* [ ] `login`
* [ ] `register`
* [ ] `refresh`
* [ ] `logout`
* [ ] futuras funções de TOTP/Passkeys

Garantir:

* [ ] login recebe Access Token no JSON.
* [ ] refresh recebe novo Access Token no JSON.
* [ ] logout limpa estado local.
* [ ] refresh token nunca aparece no frontend.

## AuthContext

Se existir, revisar.

* [ ] Centralizar estado de autenticação.
* [ ] Armazenar Access Token conforme estratégia definida.
* [ ] Expor login/logout.
* [ ] Não expor Refresh Token.
* [ ] Evitar duplicar lógica de axios dentro de componentes.
* [ ] Preparar base para TOTP e Passkeys.

## TypeScript

Criar tipos para:

```text
ApiError
ApiErrorResponse
AuthResponse
LoginRequest
RegisterRequest
RefreshResponse
```

Regras:

* [ ] evitar `any`.
* [ ] usar `unknown` quando necessário.
* [ ] tratar AxiosError com type guard.
* [ ] centralizar parsing de erro.
* [ ] manter responses compatíveis com backend.

## Tratamento de erros

Criar utilitário ou função para normalizar erros da API.

Exemplo de backend:

```json
{
  "code": "SEC_001",
  "message": "Requisição bloqueada por política de segurança.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

Frontend deve:

* [ ] ler `code`.
* [ ] ler `message`.
* [ ] preservar status HTTP.
* [ ] não exibir detalhes técnicos.
* [ ] permitir que tela decida como mostrar erro.

## Segurança frontend

* [ ] Não ler Refresh Token.
* [ ] Não salvar Refresh Token.
* [ ] Não logar Access Token.
* [ ] Não logar CSRF token.
* [ ] Não logar payloads sensíveis.
* [ ] Não criar token fake.
* [ ] Não simular autenticação como solução final.
* [ ] Não duplicar lógica de segurança em páginas.

## Integração com telas existentes

* [ ] Login usa `authService.login`.
* [ ] Cadastro usa `authService.register`.
* [ ] Logout usa `authService.logout`.
* [ ] Requisições futuras usarão `api`.
* [ ] Nenhuma página deve criar `axios.create` próprio.

## Testes manuais recomendados

### CSRF bootstrap

* [ ] Abrir app.
* [ ] Confirmar cookie `XSRF-TOKEN`.
* [ ] Confirmar que não há erro CORS.

### Login

* [ ] Fazer login.
* [ ] Confirmar Access Token recebido.
* [ ] Confirmar cookie HttpOnly de refresh pelo DevTools.
* [ ] Confirmar cookie `XSRF-TOKEN`.

### Request mutante

* [ ] Fazer POST usando `api`.
* [ ] Confirmar header `X-CSRF-Token`.
* [ ] Confirmar cookies enviados.

### GET

* [ ] Fazer GET usando `api`.
* [ ] Confirmar ausência de `X-CSRF-Token`.

### Refresh

* [ ] Expirar/remover Access Token.
* [ ] Confirmar tentativa de refresh.
* [ ] Confirmar nova requisição após refresh.
* [ ] Confirmar ausência de loop infinito.

### CSRF inválido

* [ ] Remover ou alterar cookie `XSRF-TOKEN`.
* [ ] Fazer POST.
* [ ] Confirmar tratamento de `403`.
* [ ] Confirmar tentativa única de renovar CSRF, se implementada.

## Critérios de aceitação

* [ ] Axios global configurado.
* [ ] `withCredentials: true` ativo.
* [ ] `VITE_API_BASE_URL` utilizado.
* [ ] `X-CSRF-Token` enviado apenas em métodos mutantes.
* [ ] GET não envia CSRF.
* [ ] Refresh Token nunca é acessado pelo JS.
* [ ] 401 tenta refresh sem loop infinito.
* [ ] 403 por CSRF tenta renovar token no máximo uma vez.
* [ ] 429 é propagado para UI.
* [ ] Login/cadastro usam `authService`.
* [ ] Não há múltiplas instâncias Axios espalhadas.
* [ ] Tipos TypeScript foram criados/ajustados.
* [ ] Nenhum token sensível é logado.
* [ ] Base pronta para PASSKEY-03 e TOTP-03.

## Fora de escopo

Não implementar nesta tarefa:

* UI de Passkeys.
* UI de TOTP.
* Dashboard completo.
* Design final das telas.
* CSRF backend.
* Alterações em JWT backend.
* Auditoria.
* Assinatura digital.
* Refresh token em localStorage.
* Persistência insegura de sessão.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/frontend.md`
* `docs/standard/frontend-style.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
