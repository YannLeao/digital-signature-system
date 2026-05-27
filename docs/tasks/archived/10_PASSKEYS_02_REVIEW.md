# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-PASSKEY-02 — Revisar autenticação com passkey e validação de counter

## Camada

Backend / Segurança / WebAuthn / Testes

## Prioridade

Alta

## Objetivo

Revisar a implementação de `PASSKEY-02 — Backend: fluxo WebAuthn — autenticação e validação de counter`, garantindo que 
o fluxo esteja correto, seguro, testado e alinhado aos padrões documentados em `docs/`.

## Contexto

A tarefa `PASSKEY-01` já foi revisada anteriormente, incluindo registro de credencial WebAuthn.

Agora foi implementada a autenticação com passkey, incluindo validação do counter anti-clonagem.

Arquivos adicionados/modificados:

```text
backend/src/
├── main/java/.../
│   ├── controller/
│   │   └── PasskeyController.java
│   ├── domain/
│   │   └── Passkey.java
│   └── service/
│       └── PasskeyService.java
└── test/java/.../
    └── service/
        └── PasskeyServiceTests.java
````

Esta tarefa deve revisar se a implementação segue corretamente:

* arquitetura do projeto
* boas práticas de Spring
* segurança WebAuthn
* padrão de erros da API
* testes automatizados
* documentação em `docs/`

## Objetivo funcional esperado

O fluxo de autenticação com passkey deve possuir endpoints equivalentes a:

```text
POST /api/v1/auth/passkey/auth/start
POST /api/v1/auth/passkey/auth/finish
```

O fluxo deve:

1. Gerar desafio de autenticação.
2. Enviar opções WebAuthn ao frontend.
3. Receber resposta do `navigator.credentials.get`.
4. Validar assinatura e challenge.
5. Localizar credencial registrada.
6. Validar counter.
7. Rejeitar possível clonagem.
8. Atualizar counter após sucesso.
9. Emitir Access Token/Refresh Token, se JWT-01/JWT-02 já estiverem integrados.

## Checklist de revisão arquitetural

### Organização dos arquivos

* [ ] Confirmar que `PasskeyController` está no pacote correto.
* [ ] Confirmar que `PasskeyService` concentra regra de negócio.
* [ ] Confirmar que `Passkey` está em `domain/`, não em `entity/`.
* [ ] Confirmar que não foram criados pacotes fora do padrão do projeto.
* [ ] Confirmar que DTOs já existentes foram reaproveitados ou criados de forma coerente.
* [ ] Confirmar que não há lógica de WebAuthn diretamente no controller.

### Padrões de Spring

* [ ] Usar constructor injection.
* [ ] Evitar field injection com `@Autowired`.
* [ ] Evitar dependências circulares.
* [ ] Garantir que beans WebAuthn continuam sendo injetados corretamente.
* [ ] Garantir que a aplicação sobe sem erro de DI.
* [ ] Garantir que endpoints públicos/privados estão corretamente configurados no Spring Security.

## Checklist de revisão do controller

### PasskeyController

* [ ] Verificar endpoints implementados.
* [ ] Garantir prefixo `/api/v1`.
* [ ] Garantir aderência ao padrão:

    * [ ] `/auth/passkey/auth/start`
    * [ ] `/auth/passkey/auth/finish`
* [ ] Usar `@Valid` nos requests.
* [ ] Retornar DTOs seguros.
* [ ] Não retornar entidade `Passkey`.
* [ ] Não retornar public key, credential material ou dados sensíveis desnecessários.
* [ ] Não tratar exceções manualmente se o GlobalExceptionHandler já cobre.
* [ ] Não conter regra de negócio.

## Checklist de revisão do service

### PasskeyService

* [ ] Validar se o fluxo de start gera challenge corretamente.
* [ ] Validar se o challenge é associado ao usuário/credencial corretamente.
* [ ] Validar se o fluxo de finish confere a resposta WebAuthn.
* [ ] Validar se a assinatura é verificada pela biblioteca WebAuthn.
* [ ] Validar se a credencial é buscada por `credentialId`.
* [ ] Validar se usuário inexistente ou credencial inexistente gera erro seguro.
* [ ] Validar se erros seguem o padrão global.
* [ ] Garantir que dados sensíveis não são logados.
* [ ] Garantir que não há estado em memória inseguro sem documentação.
* [ ] Garantir que não há bypass manual da validação WebAuthn.

## Validação de counter anti-clonagem

### Regra obrigatória

O counter recebido na autenticação deve ser comparado ao counter armazenado.

* [ ] Se o counter recebido for maior que o armazenado:

    * autenticação pode prosseguir
    * counter armazenado deve ser atualizado

* [ ] Se o counter recebido for menor ou igual ao armazenado:

    * autenticação deve ser rejeitada
    * evento deve ser tratado como possível clonagem de autenticador
    * counter não deve ser atualizado para valor inseguro

### Checklist específico

* [ ] Verificar comparação correta:

    * [ ] `newCounter > storedCounter`
* [ ] Rejeitar:

    * [ ] `newCounter == storedCounter`
    * [ ] `newCounter < storedCounter`
* [ ] Atualizar counter apenas após autenticação WebAuthn bem-sucedida.
* [ ] Atualizar counter dentro de transação segura.
* [ ] Não atualizar counter antes de validar assinatura/challenge.
* [ ] Não ignorar counter `0` sem decisão explícita.
* [ ] Documentar comportamento caso algum autenticador retorne counter não incremental.

## Possível clonagem

Quando counter inválido for detectado:

* [ ] Rejeitar autenticação.
* [ ] Retornar erro seguro.
* [ ] Não expor detalhes técnicos ao usuário.
* [ ] Registrar log interno controlado.
* [ ] Preparar integração futura com auditoria, se ainda não existir.

Resposta pública recomendada:

```json
{
  "code": "AUTH_003",
  "message": "Sessão inválida ou expirada.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

Ou outro código já padronizado pelo projeto para falha de autenticação WebAuthn.

## Integração com JWT

Verificar estado atual do projeto.

Se JWT-01/JWT-02 já foram implementados:

* [ ] Autenticação com passkey válida deve emitir Access Token.
* [ ] Refresh Token deve seguir padrão HttpOnly se aplicável.
* [ ] Não retornar Refresh Token no JSON.
* [ ] Claims obrigatórias devem estar presentes.
* [ ] `session_id` deve ser gerado/associado corretamente.

Se JWT ainda não estiver integrado neste fluxo:

* [ ] Não criar token fake.
* [ ] Não criar resposta enganosa.
* [ ] Documentar pendência claramente.

## Revisão da entidade Passkey

### Campos esperados

* [ ] `id`
* [ ] `user`
* [ ] `credentialId`
* [ ] `publicKey`
* [ ] `counter`
* [ ] `aaguid`
* [ ] `createdAt`
* [ ] `updatedAt`, se houver padrão no projeto
* [ ] `lastUsedAt`, se implementado

### Regras

* [ ] `credentialId` deve ser único.
* [ ] `counter` deve ser persistido corretamente.
* [ ] Relação com `User` deve evitar serialização recursiva.
* [ ] Fetch deve ser avaliado com cuidado.
* [ ] Não expor entidade diretamente na API.
* [ ] Não armazenar dados sensíveis além do necessário.

## Testes automatizados

Arquivo existente:

```text
PasskeyServiceTests.java
```

### Revisar testes existentes

* [ ] Verificar se testes realmente cobrem o fluxo novo.
* [ ] Verificar se mocks representam cenários reais.
* [ ] Verificar se testes não apenas testam implementação interna irrelevante.
* [ ] Verificar se nomes dos testes são claros.
* [ ] Verificar se falhas de segurança são testadas.

### Testes obrigatórios recomendados

* [ ] Start de autenticação gera challenge/opções.
* [ ] Finish com credencial válida autentica com sucesso.
* [ ] Finish com credentialId inexistente falha.
* [ ] Finish com challenge inválido falha.
* [ ] Finish com assinatura inválida falha, se mockável.
* [ ] Counter maior que o armazenado atualiza counter.
* [ ] Counter igual ao armazenado rejeita autenticação.
* [ ] Counter menor que o armazenado rejeita autenticação.
* [ ] Counter inválido não atualiza entidade.
* [ ] Erro de possível clonagem não emite token.
* [ ] Usuário/credencial inexistente retorna erro seguro.

### Testes de controller recomendados

Se ainda não existirem:

* [ ] `auth/start` com request válido retorna opções.
* [ ] `auth/start` com request inválido retorna erro padronizado.
* [ ] `auth/finish` com request inválido retorna erro padronizado.
* [ ] Controller não vaza entidade/dados sensíveis.

## Compatibilidade com tarefas anteriores

Verificar que não quebrou:

* [ ] AUTH-01 — cadastro de usuário.
* [ ] AUTH-02 — login com senha.
* [ ] JWT-01 — emissão/validação de Access Token.
* [ ] JWT-02 — refresh token, se já integrado.
* [ ] PASSKEY-01 — registro de credencial.
* [ ] CORS e security headers.

## Segurança

### Obrigatório

* [ ] Não logar credentialId completo se considerado sensível.
* [ ] Não logar publicKey.
* [ ] Não logar payloads WebAuthn completos.
* [ ] Não retornar stack trace.
* [ ] Não expor detalhes de falha criptográfica.
* [ ] Não aceitar counter inválido.
* [ ] Não aceitar challenge reutilizado.
* [ ] Não aceitar credentialId não registrado.

## Critérios de aceitação

* [ ] Aplicação sobe sem erro.
* [ ] Endpoints de autenticação com passkey existem e seguem padrão do projeto.
* [ ] Validação de counter está correta.
* [ ] Counter inválido é rejeitado.
* [ ] Counter válido é atualizado.
* [ ] Possível clonagem não autentica o usuário.
* [ ] Erros seguem padrão global da API.
* [ ] Não há vazamento de dados sensíveis.
* [ ] Testes automatizados cobrem counter maior, igual e menor.
* [ ] Testes automatizados passam.
* [ ] Código segue os markdowns em `docs/`.
* [ ] Não foram implementadas funcionalidades fora do escopo.

## Fora de escopo

Não implementar nesta review:

* UI de passkeys.
* Listagem de dispositivos.
* Revogação de passkeys.
* Auditoria completa.
* Notificação por e-mail.
* Gerenciamento de sessões.
* TOTP.
* CSRF.
* Redis.
* Refatoração ampla fora dos arquivos relacionados.

## Arquivos relevantes

Consulte antes de revisar:

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

Revise a implementação da tarefa PASSKEY-02 seguindo todos os padrões documentados em `docs/`.

Verifique os arquivos modificados:

* `PasskeyController.java`
* `Passkey.java`
* `PasskeyService.java`
* `PasskeyServiceTests.java`

Confirme se o fluxo de autenticação com passkey está correto, se a validação de counter anti-clonagem foi implementada 
com segurança e se os testes cobrem os cenários críticos.

Corrija problemas encontrados, alinhe o código à arquitetura do projeto e adicione testes quando necessário.

Não implemente UI, revogação de dispositivos, auditoria completa, TOTP, CSRF ou funcionalidades fora do escopo.

Ao final:

* informe os problemas encontrados
* explique correções feitas
* explique como a validação de counter funciona
* liste os testes adicionados/ajustados
* confirme que PASSKEY-01 não foi quebrada
