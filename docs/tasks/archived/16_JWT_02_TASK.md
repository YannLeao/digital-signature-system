# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### JWT-02 — Backend: Refresh token com rotação e cookie HttpOnly

## Camada

Backend / Segurança / Autenticação

## Prioridade

Alta

## Objetivo

Implementar Refresh Token opaco, armazenado de forma segura no banco, enviado ao cliente via cookie HttpOnly e 
rotacionado a cada renovação de sessão.

## Contexto

O sistema já possui:

- cadastro de usuários
- login com senha
- Argon2id
- Access Token JWT com algoritmo assimétrico
- claims de segurança
- CORS restritivo
- HTTP Security Headers
- PostgreSQL + Flyway
- tratamento global de erros

Agora o sistema precisa permitir renovação segura do Access Token sem exigir novo login a cada 15 minutos.

Esta tarefa implementa Refresh Token com:

- token opaco
- armazenamento como hash
- cookie HttpOnly
- rotação a cada uso
- detecção de reuso
- invalidação de família de tokens em caso de suspeita de comprometimento

## Decisão sobre Redis

Não implementar Redis nesta tarefa.

### Motivo

Neste momento, PostgreSQL é suficiente e mais simples para:

- persistir refresh tokens
- rastrear famílias de tokens
- detectar reuso
- invalidar sessões
- auditar comportamento futuro

Redis poderá ser avaliado futuramente no EPIC-08, quando houver implementação de sessões ativas, auditoria e necessidade
real de cache ou armazenamento distribuído.

Registrar esta decisão em:

```text
docs/decisions/DECISIONS.md
````

Sugestão:

```text
ADR-00X — Refresh tokens persistidos inicialmente em PostgreSQL
```

## Objetivo funcional

Implementar:

```text
POST /api/v1/auth/refresh
```

Responsável por:

* ler refresh token do cookie HttpOnly
* validar token no banco via hash
* emitir novo Access Token
* emitir novo Refresh Token
* invalidar refresh token anterior
* detectar reuso de refresh token já usado/revogado
* invalidar família de tokens em caso de reuso

## Modelo de segurança

### Access Token

* JWT
* Curta duração
* Expiração: 15 minutos
* Enviado no corpo da resposta

### Refresh Token

* Opaco
* Aleatório e imprevisível
* Validade: 7 dias
* Enviado em cookie HttpOnly
* Armazenado no banco apenas como hash
* Rotacionado a cada uso

## Checklist de implementação

### Migration

Criar migration para tabela de refresh tokens.

Exemplo:

```text
V{N}__create_refresh_tokens_table.sql
```

Campos mínimos recomendados:

* [ ] `id`
* [ ] `user_id`
* [ ] `token_hash`
* [ ] `family_id`
* [ ] `session_id`
* [ ] `issued_at`
* [ ] `expires_at`
* [ ] `revoked_at`
* [ ] `replaced_by_token_id`
* [ ] `created_by_ip_hash`
* [ ] `created_by_user_agent_hash`

Boas práticas:

* [ ] `id` como UUID.
* [ ] `user_id` com FK para `users`.
* [ ] `token_hash` único e obrigatório.
* [ ] `family_id` obrigatório.
* [ ] `session_id` alinhado ao claim `session_id` do JWT.
* [ ] `expires_at` obrigatório.
* [ ] `revoked_at` nullable.
* [ ] `replaced_by_token_id` nullable.
* [ ] índice em `token_hash`.
* [ ] índice em `user_id`.
* [ ] índice em `family_id`.
* [ ] índice em `session_id`.

## Geração do Refresh Token

* [ ] Gerar token opaco criptograficamente seguro.
* [ ] Usar `SecureRandom`.
* [ ] Usar tamanho forte, por exemplo 256 bits ou mais.
* [ ] Preferir formato URL-safe.
* [ ] Não usar JWT como Refresh Token.
* [ ] Não usar UUID simples se houver alternativa mais forte.
* [ ] Nunca salvar token puro no banco.
* [ ] Nunca logar refresh token.

## Hash do Refresh Token

* [ ] Calcular hash SHA-256 do token antes de persistir.
* [ ] Buscar refresh token pelo hash.
* [ ] Comparar apenas valores hash.
* [ ] Não armazenar token puro.
* [ ] Não retornar token no JSON da resposta.

## Cookie HttpOnly

Enviar Refresh Token via cookie.

Configuração mínima:

* [ ] `HttpOnly`
* [ ] `Secure`
* [ ] `SameSite=Strict`
* [ ] `Path=/api/v1/auth/refresh`
* [ ] `Max-Age=7 dias`

### Ambiente local

Como `Secure` exige HTTPS em navegadores reais:

* [ ] Permitir configuração por ambiente.
* [ ] Em produção: `Secure=true` obrigatório.
* [ ] Em desenvolvimento local: documentar alternativa controlada se necessário.
* [ ] Não comprometer a configuração de produção por conveniência local.

Variáveis sugeridas:

```env
REFRESH_TOKEN_COOKIE_NAME=refresh_token
REFRESH_TOKEN_COOKIE_SECURE=false
REFRESH_TOKEN_COOKIE_SAME_SITE=Strict
REFRESH_TOKEN_COOKIE_PATH=/api/v1/auth/refresh
REFRESH_TOKEN_EXPIRATION_DAYS=7
```

> Em produção, `REFRESH_TOKEN_COOKIE_SECURE=true`.

## Login

Atualizar login para emitir Refresh Token.

Após login válido:

* [ ] emitir Access Token JWT
* [ ] gerar Refresh Token opaco
* [ ] salvar hash do Refresh Token no banco
* [ ] criar `family_id`
* [ ] vincular ao `session_id`
* [ ] enviar Refresh Token via cookie HttpOnly
* [ ] retornar Access Token no corpo da resposta

Resposta esperada:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Não retornar refresh token no JSON.

## Endpoint de refresh

Criar endpoint:

```text
POST /api/v1/auth/refresh
```

Fluxo esperado:

1. Ler cookie de refresh token.
2. Calcular hash.
3. Buscar token no banco.
4. Verificar se existe.
5. Verificar se não expirou.
6. Verificar se não foi revogado.
7. Emitir novo Access Token.
8. Gerar novo Refresh Token.
9. Salvar novo hash.
10. Revogar token anterior.
11. Registrar `replaced_by_token_id`.
12. Enviar novo Refresh Token em novo cookie HttpOnly.
13. Retornar novo Access Token no corpo.

## Rotação obrigatória

A cada `/refresh` bem-sucedido:

* [ ] o refresh token antigo deve ser invalidado.
* [ ] um novo refresh token deve ser emitido.
* [ ] o token antigo não pode ser usado novamente.

## Detecção de reuso

Se um refresh token já revogado for usado novamente:

* [ ] tratar como possível comprometimento.
* [ ] invalidar todos os refresh tokens da mesma `family_id`.
* [ ] retornar erro seguro.
* [ ] não emitir novo Access Token.
* [ ] limpar cookie de refresh token, se apropriado.

Resposta recomendada:

```json
{
  "code": "AUTH_003",
  "message": "Sessão inválida ou expirada.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

## Expiração

* [ ] Refresh Token válido por 7 dias.
* [ ] Token expirado não pode renovar sessão.
* [ ] Token expirado deve retornar erro seguro.
* [ ] Considerar marcar expirados como revogados em rotina futura, mas não é obrigatório nesta tarefa.

## Integração com Access Token

Ao renovar:

* [ ] emitir novo Access Token com claims obrigatórias do JWT-01.
* [ ] preservar ou atualizar `session_id` conforme decisão arquitetural.
* [ ] preferencialmente manter o mesmo `session_id` durante a mesma família/sessão.
* [ ] gerar novo `jti` para cada novo Access Token.

## CORS e cookies

Como cookie será usado:

* [ ] garantir compatibilidade com CORS configurado.
* [ ] frontend deve usar `withCredentials: true` futuramente.
* [ ] backend deve permitir credenciais apenas para origens confiáveis.
* [ ] não usar wildcard em CORS.

## CSRF

Esta tarefa introduz cookie HttpOnly.

Portanto:

* [ ] documentar que CSRF será tratado no EPIC-09.
* [ ] não implementar CSRF ainda, se não estiver no escopo.
* [ ] garantir que o endpoint `/refresh` tenha superfície mínima.
* [ ] manter `SameSite=Strict` como mitigação inicial.

## Logout

Não implementar logout completo ainda.

Mas preparar estrutura para futura invalidação em JWT-03.

## Testes automatizados

Adicionar testes mínimos obrigatórios.

### RefreshTokenService

* [ ] Gera refresh token opaco.
* [ ] Salva apenas hash no banco.
* [ ] Não salva token puro.
* [ ] Valida token existente.
* [ ] Rejeita token inexistente.
* [ ] Rejeita token expirado.
* [ ] Rejeita token revogado.
* [ ] Rotaciona token válido.
* [ ] Revoga token anterior após rotação.
* [ ] Detecta reuso de token revogado.
* [ ] Invalida família ao detectar reuso.

### AuthController / endpoint

* [ ] Login válido cria cookie HttpOnly.
* [ ] Login válido não retorna refresh token no JSON.
* [ ] `/refresh` com cookie válido retorna novo Access Token.
* [ ] `/refresh` sem cookie retorna erro seguro.
* [ ] `/refresh` com token inválido retorna erro seguro.
* [ ] `/refresh` com token reutilizado não emite Access Token.

### Cookie

* [ ] Cookie contém `HttpOnly`.
* [ ] Cookie contém `SameSite=Strict`.
* [ ] Cookie contém `Path` correto.
* [ ] Cookie contém `Max-Age` correto.
* [ ] `Secure` respeita configuração de ambiente.

## Testes manuais recomendados

### Login

* [ ] Fazer login válido.
* [ ] Verificar response JSON com Access Token.
* [ ] Verificar cookie `refresh_token` no navegador/Postman.
* [ ] Confirmar que refresh token não aparece no corpo.

### Refresh

* [ ] Chamar `POST /api/v1/auth/refresh` com cookie.
* [ ] Receber novo Access Token.
* [ ] Verificar que novo cookie foi emitido.
* [ ] Tentar usar cookie antigo.
* [ ] Confirmar rejeição e invalidação da família.

### Banco

* [ ] Confirmar que `token_hash` foi salvo.
* [ ] Confirmar que token puro não aparece.
* [ ] Confirmar que token antigo recebeu `revoked_at`.
* [ ] Confirmar que `replaced_by_token_id` foi preenchido.

## Critérios de aceitação

* [ ] Login válido emite Access Token no JSON.
* [ ] Login válido envia Refresh Token via cookie HttpOnly.
* [ ] Refresh Token não aparece no JSON.
* [ ] Refresh Token é salvo apenas como hash.
* [ ] `/refresh` emite novo Access Token.
* [ ] `/refresh` rotaciona Refresh Token.
* [ ] Token antigo é invalidado após rotação.
* [ ] Reuso de token antigo invalida a família.
* [ ] Refresh Token expira em 7 dias.
* [ ] Cookie usa `HttpOnly`.
* [ ] Cookie usa `SameSite=Strict`.
* [ ] Cookie usa `Secure=true` em produção.
* [ ] Testes automatizados foram adicionados.
* [ ] Redis não foi implementado nesta tarefa.

## Fora de escopo

Não implementar ainda:

* Redis.
* Logout completo.
* Denylist de Access Token.
* CSRF completo.
* Sessões ativas completas.
* Tela de gerenciamento de sessões.
* Auditoria completa.
* Notificações por e-mail.
* Revogação manual por dispositivo.
* Refresh token em múltiplos dispositivos com UI.
* OAuth.
* TOTP.
* Passkeys login.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/architecture/database.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
* `docs/decisions/DECISIONS.md`

## Prompt recomendado para o agente

Implemente completamente a tarefa JWT-02 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, 
`docs/architecture/backend.md`, `docs/architecture/database.md`, `docs/security/security-overview.md`, 
`docs/standards/api.md`, `docs/standards/coding.md` e este arquivo.

Implemente Refresh Token opaco com rotação, armazenamento apenas como hash em PostgreSQL, cookie HttpOnly, expiração de 
7 dias e detecção de reuso com invalidação da família de tokens.

Não implemente Redis, logout completo, denylist de Access Token, CSRF completo, sessões ativas, auditoria ou frontend.

Ao final:

* informe os arquivos criados/alterados
* explique a modelagem da tabela de refresh tokens
* explique como o cookie HttpOnly foi configurado
* explique como testar login com refresh token
* explique como testar rotação
* explique como testar reuso de token antigo
* confirme que o refresh token nunca é retornado no JSON nem salvo em texto puro
