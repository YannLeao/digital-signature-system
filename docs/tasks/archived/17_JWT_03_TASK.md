# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### JWT-03 — Backend: denylist pós-logout para Access Tokens

## Camada

Backend / Segurança / Autenticação

## Prioridade

Alta

## Objetivo

Implementar mecanismo de invalidação de Access Tokens após logout utilizando denylist baseada em `jti`, impedindo 
reutilização de tokens ainda válidos após encerramento explícito de sessão.

## Contexto

O sistema já possui:

- Access Token JWT assimétrico
- claims de segurança
- Refresh Token com rotação
- cookie HttpOnly
- detecção de reuso
- família de refresh tokens
- PostgreSQL + Flyway
- Spring Security
- CORS restritivo
- HTTP security headers

Atualmente, um Access Token continua válido até sua expiração natural mesmo após logout.

Esta tarefa implementa invalidação explícita via denylist.

## Objetivo funcional

Implementar:

```text
POST /api/v1/auth/logout
````

Responsável por:

* invalidar Refresh Token atual
* invalidar família/sessão quando apropriado
* adicionar `jti` do Access Token à denylist
* impedir reutilização do Access Token após logout
* limpar cookie HttpOnly do Refresh Token

## Modelo de segurança

### Problema

JWT é stateless.

Mesmo após logout:

* o token continua criptograficamente válido
* a assinatura continua correta
* a expiração ainda não ocorreu

Portanto, é necessário invalidar explicitamente tokens revogados.

## Estratégia obrigatória

Usar denylist baseada em:

```text
jti
```

claim já existente no JWT.

## Decisão arquitetural

### Persistência

Implementar denylist inicialmente em PostgreSQL.

Não implementar Redis nesta tarefa.

### Motivo

Neste estágio:

* volume ainda é controlável
* persistência relacional simplifica auditoria
* reduz complexidade operacional
* mantém consistência com arquitetura atual

Redis poderá ser avaliado futuramente em:

* EPIC-08
* múltiplas instâncias
* escalabilidade horizontal
* invalidação distribuída

Registrar decisão em:

```text
docs/decisions/DECISIONS.md
```

Sugestão:

```text
ADR-00X — Denylist JWT persistida inicialmente em PostgreSQL
```

## Checklist de implementação

### Migration

Criar migration:

```text
V{N}__create_jwt_denylist_table.sql
```

Campos mínimos recomendados:

* [ ] `id`
* [ ] `jti`
* [ ] `user_id`
* [ ] `session_id`
* [ ] `token_expires_at`
* [ ] `revoked_at`
* [ ] `reason`

### Boas práticas

* [ ] `id` como UUID.
* [ ] `jti` único.
* [ ] índice em `jti`.
* [ ] índice em `token_expires_at`.
* [ ] FK opcional para `users`.
* [ ] `revoked_at` obrigatório.
* [ ] `reason` limitado e controlado.

## Estrutura recomendada

```text
security/
├── JwtDenylistService.java
├── JwtLogoutService.java
├── JwtAuthenticationFilter.java
└── JwtValidator.java
```

A estrutura pode variar desde que mantenha clareza arquitetural.

## Logout

Criar endpoint:

```text
POST /api/v1/auth/logout
```

### Fluxo esperado

1. Receber Access Token autenticado.
2. Extrair `jti`.
3. Extrair `session_id`.
4. Adicionar `jti` à denylist.
5. Invalidar Refresh Token atual.
6. Opcionalmente invalidar família/sessão completa.
7. Limpar cookie HttpOnly.
8. Retornar resposta segura.

## Resposta esperada

```json
{
  "message": "Logout realizado com sucesso."
}
```

Não retornar detalhes internos.

## JwtAuthenticationFilter

Atualizar validação JWT para:

* [ ] validar assinatura
* [ ] validar expiração
* [ ] validar denylist
* [ ] rejeitar token revogado

Fluxo:

1. Token chega.
2. Assinatura validada.
3. Claims validadas.
4. Buscar `jti` na denylist.
5. Se existir:

    * rejeitar autenticação
    * retornar erro seguro

## Erro esperado

```json
{
  "code": "AUTH_003",
  "message": "Sessão inválida ou expirada.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

## Refresh Token no logout

Ao realizar logout:

* [ ] invalidar Refresh Token atual.
* [ ] limpar cookie HttpOnly.
* [ ] impedir futuras renovações.
* [ ] impedir reutilização da sessão.

### Cookie cleanup

Enviar cookie com:

* [ ] mesmo nome
* [ ] mesmo path
* [ ] `Max-Age=0`
  ou
* [ ] expiração passada

## Sessão

### session_id

Aproveitar `session_id` já existente.

Preparar arquitetura para:

* logout de sessão específica
* logout global
* sessões ativas
* painel de dispositivos

Mas:

* [ ] não implementar painel ainda
* [ ] não implementar múltiplas estratégias complexas ainda

## Política de invalidação

### Nesta tarefa

Logout deve invalidar:

* [ ] Access Token atual
* [ ] Refresh Token atual

### Opcionalmente

A implementação pode invalidar:

* [ ] toda a família da sessão

Desde que:

* comportamento esteja documentado
* seja consistente

## Limpeza futura

Tokens expirados na denylist não devem crescer indefinidamente.

### Nesta tarefa

* [ ] Preparar estratégia de limpeza.
* [ ] Não implementar scheduler complexo ainda, se não houver infraestrutura.
* [ ] Documentar necessidade futura de cleanup.

Sugestão:

```text
TODO: remover denylist expirados periodicamente
```

## Segurança

### Obrigatório

* [ ] Nunca logar JWT completo.
* [ ] Nunca confiar apenas no frontend para logout.
* [ ] Nunca aceitar token denylisted.
* [ ] Nunca deixar Refresh Token válido após logout.
* [ ] Nunca expor detalhes internos da denylist.

### Access Token roubado

Esta tarefa reduz janela de reutilização pós-logout, mas:

* não elimina completamente risco antes do logout
* não substitui expiração curta
* não substitui rotação de Refresh Token

## Spring Security

* [ ] Integrar denylist ao fluxo atual.
* [ ] Não quebrar endpoints públicos.
* [ ] Não quebrar autenticação existente.
* [ ] Garantir compatibilidade com JWT-01/JWT-02.

## Testes automatizados

Adicionar testes mínimos obrigatórios.

### JwtDenylistService

* [ ] Adiciona `jti` à denylist.
* [ ] Verifica token denylisted.
* [ ] Rejeita token denylisted.
* [ ] Permite token válido não denylisted.

### Logout

* [ ] Logout adiciona `jti` à denylist.
* [ ] Logout invalida Refresh Token.
* [ ] Logout limpa cookie HttpOnly.
* [ ] Logout retorna sucesso seguro.

### JwtAuthenticationFilter

* [ ] Token válido funciona.
* [ ] Token denylisted falha.
* [ ] Token expirado falha.
* [ ] Token inválido falha.

### Refresh Token

* [ ] Refresh após logout falha.
* [ ] Cookie inválido não renova sessão.

## Testes manuais recomendados

### Login

* [ ] Fazer login.
* [ ] Obter Access Token.
* [ ] Obter cookie HttpOnly.

### Logout

* [ ] Chamar `/api/v1/auth/logout`.
* [ ] Confirmar resposta de sucesso.
* [ ] Confirmar limpeza do cookie.

### Token denylisted

* [ ] Tentar reutilizar Access Token após logout.
* [ ] Confirmar rejeição.

### Refresh

* [ ] Tentar `/refresh` após logout.
* [ ] Confirmar falha.

### Banco

* [ ] Confirmar presença do `jti` na denylist.
* [ ] Confirmar Refresh Token revogado.
* [ ] Confirmar timestamps corretos.

## Critérios de aceitação

* [ ] Logout adiciona `jti` à denylist.
* [ ] JwtAuthenticationFilter rejeita tokens denylisted.
* [ ] Logout invalida Refresh Token.
* [ ] Logout limpa cookie HttpOnly.
* [ ] Refresh após logout falha.
* [ ] Tokens válidos não denylisted continuam funcionando.
* [ ] Endpoints públicos continuam acessíveis.
* [ ] Testes automatizados foram adicionados.
* [ ] PostgreSQL foi usado para persistência.
* [ ] Redis não foi implementado nesta tarefa.

## Fora de escopo

Não implementar ainda:

* Redis.
* Sessões ativas completas.
* Painel de dispositivos.
* Logout global multi-device.
* Auditoria avançada.
* Notificações de logout suspeito.
* Geolocalização de sessão.
* CSRF completo.
* OAuth.
* Revogação distribuída.
* Cleanup automático sofisticado.
* Monitoramento em tempo real.

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

Implemente completamente a tarefa JWT-03 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, 
`docs/architecture/backend.md`, `docs/architecture/database.md`, `docs/security/security-overview.md`, 
`docs/standards/api.md`, `docs/standards/coding.md` e este arquivo.

Implemente denylist pós-logout baseada em `jti`, integração com JwtAuthenticationFilter, invalidação de Refresh Token e 
limpeza de cookie HttpOnly.

Utilize PostgreSQL como persistência inicial da denylist.

Não implemente Redis, sessões ativas completas, painel de dispositivos, logout global multi-device ou auditoria avançada.

Ao final:

* informe os arquivos criados/alterados
* explique a modelagem da denylist
* explique como a validação foi integrada ao filtro JWT
* explique como testar reutilização de token após logout
* explique como validar limpeza do cookie HttpOnly
* confirme que Refresh Tokens também são invalidados no logout
