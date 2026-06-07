# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### AUTH-02 — Endpoint de login com mensagem de erro genérica

## Camada

Backend

## Resultado

AUTH-02 implementada com endpoint `POST /api/v1/auth/login`, validacao Argon2id via `PasswordEncoder.matches`, resposta generica `AUTH_001` para falhas de credenciais e conta bloqueada, bloqueio temporario apos 5 falhas consecutivas por 15 minutos e rate limiting local por IP com limite de 10 tentativas por minuto.

## Prioridade

Alta

## Objetivo

Implementar o endpoint de login com validação segura de credenciais, mensagem genérica para falhas, bloqueio temporário 
de conta após tentativas incorretas e base inicial para proteção contra brute force.

## Contexto

O endpoint de cadastro já foi implementado em `AUTH-01`, incluindo:

- persistência de usuário
- validação de e-mail
- política forte de senha
- hash Argon2id
- erro padronizado da API

Agora o sistema precisa permitir login com senha, mas sem expor informações que permitam enumeração de usuários.

Este card ainda não deve emitir JWT definitivo, pois o módulo de JWT será implementado posteriormente.

## Objetivo funcional

Criar endpoint:

```text
POST /api/v1/auth/login
````

Responsável por:

* receber e-mail e senha
* buscar usuário por e-mail
* validar senha com Argon2id
* retornar erro genérico em qualquer falha
* incrementar tentativas falhas
* bloquear conta após múltiplas falhas
* permitir login bem-sucedido quando credenciais forem válidas

## Checklist de implementação

### Endpoint

* [ ] Criar endpoint:

  * [ ] `POST /api/v1/auth/login`
* [ ] Definir request DTO.
* [ ] Definir response DTO provisório.
* [ ] Usar Bean Validation (`@Valid`).

### Request esperado

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

### Resposta provisória esperada

Como JWT ainda não foi implementado, retornar uma resposta simples e segura:

```json
{
  "message": "Login realizado com sucesso."
}
```

> Observação: o retorno com access token e refresh token será implementado posteriormente nos cards de JWT.

## Validação de credenciais

* [ ] Buscar usuário por e-mail.
* [ ] Validar senha usando o matcher do Argon2id.
* [ ] Nunca comparar hash manualmente.
* [ ] Nunca descriptografar senha.
* [ ] Nunca logar senha.
* [ ] Nunca retornar senha ou hash.

## Mensagem genérica obrigatória

Falhas abaixo devem retornar exatamente o mesmo tipo de erro:

* usuário inexistente
* senha incorreta
* conta bloqueada, se a decisão for ocultar o motivo

Resposta recomendada:

```json
{
  "code": "AUTH_001",
  "message": "Credenciais inválidas.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

## Proteção contra enumeração de usuários

* [ ] Não retornar "usuário não encontrado".
* [ ] Não retornar "senha incorreta".
* [ ] Não retornar "e-mail não cadastrado".
* [ ] Não deixar diferença óbvia de resposta entre usuário inexistente e senha incorreta.
* [ ] Evitar logs com informações sensíveis.

## Bloqueio temporário de conta

* [ ] Usar campo `failed_attempts`.
* [ ] Usar campo `locked_until`.
* [ ] Após 5 tentativas consecutivas falhas, bloquear a conta por 15 minutos.
* [ ] Se a conta estiver bloqueada, negar login.
* [ ] Após login bem-sucedido, zerar `failed_attempts`.
* [ ] Após login bem-sucedido, limpar `locked_until`.

## Atenção importante sobre resposta de conta bloqueada

Por segurança, a resposta pública pode continuar sendo:

```text
Credenciais inválidas.
```

Internamente, a aplicação pode registrar que a conta está bloqueada.

Não expor detalhes como:

* "conta bloqueada"
* "tente novamente em 15 minutos"
* "muitas tentativas"

a menos que o projeto decida explicitamente priorizar UX sobre resistência à enumeração.

## Rate limiting

* [ ] Implementar rate limiting por IP: máximo 10 tentativas por minuto.
* [ ] Retornar `429 Too Many Requests` na 11ª tentativa no mesmo IP em 1 minuto.
* [ ] Manter implementação simples e local neste momento, se não houver Redis ainda.
* [ ] Isolar a lógica em um serviço/componente próprio para permitir troca futura por Redis.

## E-mail de notificação

O roadmap prevê notificação por e-mail após bloqueio de conta.

Como o serviço de e-mail ainda não existe, nesta tarefa:

* [ ] Não implementar envio real de e-mail ainda, se a infraestrutura não existir.
* [ ] Criar ponto de extensão claro para notificação futura, se fizer sentido.
* [ ] Não criar mock enganoso como solução final.
* [ ] Registrar TODO técnico ou comentário controlado, se necessário.

## Tratamento de erros

* [ ] Usar padrão global de erros.
* [ ] Retornar `AUTH_001` para credenciais inválidas.
* [ ] Retornar `429` para rate limiting.
* [ ] Não expor stack trace.
* [ ] Não expor SQL.
* [ ] Não expor se o e-mail existe.

## Segurança

* [ ] Usar Argon2id password encoder/matcher já configurado.
* [ ] Não usar bcrypt.
* [ ] Não usar MD5/SHA simples.
* [ ] Não retornar token ainda.
* [ ] Não criar sessão ainda.
* [ ] Não armazenar senha em texto claro.
* [ ] Não armazenar senha em memória além do necessário.

## Critérios de aceitação

* [ ] Login com credenciais válidas retorna sucesso.
* [ ] Usuário inexistente retorna `AUTH_001`.
* [ ] Senha incorreta retorna `AUTH_001`.
* [ ] Usuário inexistente e senha incorreta têm resposta pública equivalente.
* [ ] Após 5 falhas consecutivas, usuário é bloqueado por 15 minutos.
* [ ] Login bem-sucedido reseta `failed_attempts`.
* [ ] Login bem-sucedido limpa `locked_until`.
* [ ] 11ª tentativa no mesmo IP em 1 minuto retorna `429`.
* [ ] Nenhuma senha aparece em logs.
* [ ] Nenhum hash aparece em resposta.
* [ ] JWT ainda não foi implementado.

## Testes recomendados

* [ ] Login válido.
* [ ] Login com usuário inexistente.
* [ ] Login com senha incorreta.
* [ ] Comparar resposta pública entre usuário inexistente e senha incorreta.
* [ ] Cinco falhas consecutivas bloqueiam conta.
* [ ] Conta bloqueada não consegue logar.
* [ ] Após ajustar/expirar `locked_until`, login válido funciona.
* [ ] Login válido reseta contador de falhas.
* [ ] Mais de 10 tentativas no mesmo IP em 1 minuto retorna `429`.

## Fora de escopo

Não implementar ainda:

* JWT.
* Refresh token.
* Cookie HttpOnly.
* Sessões.
* TOTP.
* Passkeys.
* CSRF.
* Auditoria completa.
* Envio real de e-mail.
* Frontend de login.
* Tela de erro.
* Recuperação de senha.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
