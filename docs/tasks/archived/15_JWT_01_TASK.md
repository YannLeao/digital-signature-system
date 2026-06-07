# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### JWT-01 — Backend: configuração JWT com algoritmo assimétrico e claims de segurança

## Camada

Backend / Segurança / Autenticação

## Prioridade

Alta

## Objetivo

Implementar a infraestrutura inicial de JWT do sistema utilizando algoritmo assimétrico seguro (RS256 ou ES256), 
claims de segurança adicionais e validação centralizada de tokens.

## Contexto

O sistema já possui:

- cadastro de usuários
- login inicial
- Argon2id
- tratamento global de erros
- PostgreSQL + Flyway
- CORS restritivo
- HTTP security headers
- base arquitetural consolidada

Outros épicos como:

- TOTP
- sessões
- refresh token
- auditoria
- CSRF

dependem diretamente desta infraestrutura JWT.

Esta tarefa NÃO deve implementar refresh token ainda.

O foco é:

- emissão de Access Token
- assinatura assimétrica
- claims de segurança
- validação centralizada
- preparação da arquitetura para sessões futuras

## Objetivo funcional

Implementar:

- geração de JWT assinado
- validação de JWT
- claims adicionais de segurança
- infraestrutura de chaves assimétricas
- integração inicial com login

## Algoritmo obrigatório

Usar algoritmo assimétrico:

- [x] `RS256`
ou
- [ ] `ES256`

### Proibição explícita

Não utilizar:

- [ ] `alg: none`
- [ ] HS256 compartilhando segredo simples
- [ ] tokens não assinados
- [ ] JWT sem expiração

## Decisão arquitetural

Registrar decisão no:

```text
docs/decisions/DECISIONS.md
````

Exemplo:

```text
ADR-00X — JWT usa RS256
```

## Checklist de implementação

### Dependências

* [ ] Adicionar dependências JWT adequadas.
* [ ] Evitar bibliotecas abandonadas.
* [ ] Revisar CVEs conhecidas.
* [ ] Evitar múltiplas libs JWT redundantes.

### Chaves assimétricas

* [ ] Gerar infraestrutura de chave privada/pública.
* [ ] Configurar carregamento seguro das chaves.
* [ ] Não hardcodar chave privada no código.
* [ ] Não commitar chaves reais.
* [ ] Adicionar exemplo seguro/documentado no `.env.example` se necessário.
* [ ] Garantir que aplicação falhe claramente se chave obrigatória estiver ausente.

### Estrutura recomendada

```text
config/
├── JwtConfig.java

security/
├── JwtService.java
├── JwtClaimsFactory.java
├── JwtAuthenticationFilter.java (se necessário)
└── JwtValidator.java
```

A estrutura pode variar desde que mantenha clareza arquitetural.

## Claims obrigatórias

O token deve conter:

* [ ] `sub`
* [ ] `jti`
* [ ] `iat`
* [ ] `exp`
* [ ] `session_id`
* [ ] `ip`
* [ ] `ua_hash`

## Claims detalhadas

### sub

Identificador do usuário.

Preferencialmente:

* UUID
* ou identificador interno seguro

Evitar usar e-mail diretamente como identificador principal do token.

### jti

* [ ] UUID único por token.
* [ ] Preparar denylist futura.

### iat

* [ ] Timestamp de emissão.

### exp

* [ ] Expiração obrigatória.
* [ ] Access Token deve expirar em 15 minutos.

### session_id

* [ ] UUID da sessão.
* [ ] Preparar gerenciamento de sessões futuras.

### ip

* [ ] Hash SHA-256 do IP.
* [ ] Não salvar IP puro diretamente no token.

### ua_hash

* [ ] Hash SHA-256 do User-Agent.
* [ ] Não salvar User-Agent puro diretamente no token.

## Hashes de IP/User-Agent

### Objetivo

Reduzir reutilização indevida de token em contexto diferente.

### Cuidados

* [ ] Não criar mecanismo extremamente rígido que quebre usuários legítimos facilmente.
* [ ] Preparar arquitetura para políticas futuras.
* [ ] Manter validação configurável/evolutiva.

## Login

Integrar emissão de JWT ao endpoint de login existente.

### Fluxo esperado

```text
POST /api/v1/auth/login
```

Após credenciais válidas:

* [ ] emitir Access Token
* [ ] retornar response segura
* [ ] incluir expiração
* [ ] incluir metadados mínimos necessários

### Response esperada

Exemplo:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## Validação de JWT

* [ ] Implementar validação de assinatura.
* [ ] Implementar validação de expiração.
* [ ] Implementar validação de claims mínimas.
* [ ] Implementar parsing centralizado.
* [ ] Preparar integração futura com filtros Spring Security.

## Integração Spring Security

* [ ] Preparar filtro/interceptor JWT.
* [ ] Não quebrar endpoints públicos.
* [ ] Garantir compatibilidade futura com roles/permissões.
* [ ] Não criar autorização complexa ainda.

## Password change invalidation

Preparar estrutura para:

```text
password_changed_at
```

### Objetivo

Tokens emitidos antes da troca de senha deverão ser invalidados futuramente.

### Nesta tarefa

* [ ] Preparar arquitetura.
* [ ] Não implementar fluxo completo ainda se dependências não estiverem prontas.
* [ ] Evitar acoplamento excessivo.

## Segurança

### Obrigatório

* [ ] Nunca logar JWT completo.
* [ ] Nunca logar chave privada.
* [ ] Nunca expor segredo criptográfico.
* [ ] Nunca aceitar token sem assinatura válida.
* [ ] Nunca aceitar token expirado.
* [ ] Nunca aceitar algoritmo inseguro.

### Expiração

* [ ] Access Token: 15 minutos.
* [ ] Não criar tokens permanentes.

### Transporte

* [ ] Token deve ser retornado via response JSON neste momento.
* [ ] Não implementar cookie HttpOnly ainda.
* [ ] Refresh token ainda NÃO faz parte desta tarefa.

## Testes automatizados

Adicionar testes mínimos obrigatórios.

### JwtService

* [ ] Gerar token válido.
* [ ] Validar token válido.
* [ ] Rejeitar token expirado.
* [ ] Rejeitar assinatura inválida.
* [ ] Confirmar presença das claims obrigatórias.
* [ ] Confirmar algoritmo correto.

### Login

* [ ] Login válido retorna JWT.
* [ ] Login inválido NÃO retorna JWT.
* [ ] Token possui expiração correta.

### Segurança

* [ ] Garantir que token sem assinatura falha.
* [ ] Garantir que token alterado falha.

## Testes manuais recomendados

### Login

* [ ] Fazer login válido.
* [ ] Receber token.
* [ ] Confirmar formato Bearer.

### jwt.io

Validar:

* [ ] algoritmo
* [ ] claims
* [ ] expiração
* [ ] assinatura

### Expiração

* [ ] Esperar expirar.
* [ ] Confirmar rejeição.

## Critérios de aceitação

* [ ] JWT usa RS256 ou ES256.
* [ ] Access Token expira em 15 minutos.
* [ ] Claims obrigatórias estão presentes.
* [ ] Login válido retorna token.
* [ ] Login inválido não retorna token.
* [ ] Chaves não estão hardcoded.
* [ ] Chave privada não está commitada.
* [ ] Tokens inválidos são rejeitados.
* [ ] Tokens expirados são rejeitados.
* [ ] Testes automatizados foram adicionados.
* [ ] Spring Security continua funcional.
* [ ] Endpoints públicos continuam acessíveis.

## Fora de escopo

Não implementar ainda:

* Refresh token.
* Cookie HttpOnly.
* Rotação de refresh token.
* Denylist.
* Logout completo.
* Sessões persistidas.
* Roles/permissões complexas.
* CSRF.
* OAuth.
* Passkeys login.
* TOTP final.
* Revogação distribuída.
* Redis.
* Blacklist distribuída.

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
