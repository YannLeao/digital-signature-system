# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-PASSKEY-01 — Revisar implementação de PASSKEY-01 e adicionar testes

## Camada

Backend / Segurança / Testes

## Prioridade

Alta

## Objetivo

Revisar o código adicionado para o fluxo inicial de registro de passkeys/WebAuthn, corrigir erro de injeção de dependência do Spring, alinhar a implementação ao padrão atual do projeto e adicionar testes automatizados mínimos.

## Contexto

Um PR foi aberto para a tarefa:

```text
PASSKEY-01 — Backend: fluxo WebAuthn — registro de credencial
````

Foram adicionados/modificados arquivos relacionados a:

* configuração WebAuthn
* controller de passkeys
* DTOs de start/finish
* service
* repository
* entidade Passkey
* migration `V3__create_passkeys_table.sql`
* `pom.xml`

Entretanto, o PR apresentou erro de DI do Spring em uma das classes e ainda não possui testes automatizados.

Esta tarefa não deve expandir o escopo de WebAuthn além do que já foi implementado no PR. O objetivo é revisar, corrigir, organizar e testar.

## Arquivos impactados pelo PR

```text
backend/src/main/java/.../
├── config/
│   └── WebAuthnConfig.java
├── controller/
│   └── PasskeyController.java
├── domain/
│   └── User.java
├── dto/
│   ├── PasskeyFinishRequest.java
│   ├── PasskeyResponse.java
│   └── PasskeyStartRequest.java
├── entity/
│   └── Passkey.java
├── repository/
│   ├── PasskeyRepository.java
│   └── UserRepository.java
├── service/
│   └── PasskeyService.java
└── resources/db/migration/
    └── V3__create_passkeys_table.sql
```

Também houve alteração em:

```text
pom.xml
```

## Problema arquitetural conhecido

A entidade `Passkey` foi adicionada em:

```text
entity/Passkey.java
```

Mas o padrão atual do projeto usa entidades/modelos persistentes em:

```text
domain/
```

Portanto, avaliar e corrigir essa inconsistência.

## Checklist de revisão arquitetural

### Pacotes e organização

* [ ] Verificar se `Passkey` deve ser movida de `entity/` para `domain/`.
* [ ] Ajustar imports após mover a classe.
* [ ] Garantir consistência com a organização atual do projeto.
* [ ] Evitar criação de novo pacote arquitetural sem justificativa.
* [ ] Garantir que DTOs permaneçam em `dto/`.
* [ ] Garantir que repositories permaneçam em `repository/`.
* [ ] Garantir que services permaneçam em `service/`.
* [ ] Garantir que configs permaneçam em `config/`.

### Spring DI

* [ ] Identificar a classe com erro de injeção de dependência.
* [ ] Corrigir o erro sem usar workaround frágil.
* [ ] Preferir constructor injection.
* [ ] Evitar field injection com `@Autowired` em atributo.
* [ ] Garantir que todos os beans necessários estejam anotados/configurados corretamente.
* [ ] Garantir que classes de configuração exponham beans corretamente.
* [ ] Garantir que services estejam anotados com `@Service`.
* [ ] Garantir que repositories estejam como interfaces Spring Data válidas.
* [ ] Garantir que não existam dependências circulares.

### WebAuthnConfig

* [ ] Revisar se os beans da biblioteca WebAuthn foram criados corretamente.
* [ ] Verificar se valores como `rpId`, `rpName` e origem vêm de configuração/ambiente.
* [ ] Não hardcodar domínio de produção.
* [ ] Permitir configuração local para desenvolvimento.
* [ ] Documentar variáveis novas no `.env.example`, se existirem.
* [ ] Validar que a aplicação falha claramente se configuração obrigatória estiver ausente.

### PasskeyService

* [ ] Garantir que regra de negócio fique no service, não no controller.
* [ ] Garantir que o service não retorne entidades diretamente.
* [ ] Garantir que dados sensíveis da credencial não sejam logados.
* [ ] Garantir que desafios WebAuthn tenham ciclo de vida seguro.
* [ ] Verificar se o challenge é armazenado/validado de forma coerente.
* [ ] Evitar estado em memória inadequado para produção, ou documentar limitação temporária se inevitável.

### PasskeyController

* [ ] Garantir endpoints sob `/api/v1`.
* [ ] Garantir nomes coerentes com o roadmap:

  * [ ] `POST /api/v1/auth/passkey/register/start`
  * [ ] `POST /api/v1/auth/passkey/register/finish`
* [ ] Usar `@Valid` nos DTOs de entrada.
* [ ] Não colocar lógica WebAuthn diretamente no controller.
* [ ] Retornar DTOs seguros.
* [ ] Integrar com padrão global de erros.

### DTOs

* [ ] Validar campos obrigatórios com Bean Validation.
* [ ] Evitar `any`/tipos genéricos sem necessidade.
* [ ] Evitar expor entidades internas.
* [ ] Garantir nomes claros para request/response.
* [ ] Verificar se campos WebAuthn estão alinhados com o formato esperado pelo frontend.

### User.java

* [ ] Revisar alterações feitas em `User`.
* [ ] Garantir que não quebrem AUTH-01/AUTH-02.
* [ ] Garantir que relacionamento com `Passkey` não gere serialização recursiva.
* [ ] Evitar expor lista de passkeys em respostas de usuário.
* [ ] Avaliar `fetch = LAZY` para relação com passkeys.
* [ ] Avaliar cascade/orphanRemoval com cuidado.

### Repository

* [ ] Verificar queries de `PasskeyRepository`.
* [ ] Verificar alterações em `UserRepository`.
* [ ] Garantir métodos necessários sem excesso de consultas prematuras.
* [ ] Garantir busca por usuário/e-mail/id de forma consistente com o projeto.

### Migration V3

* [ ] Revisar `V3__create_passkeys_table.sql`.
* [ ] Garantir que tabela tenha campos necessários:

  * [ ] `id`
  * [ ] `user_id`
  * [ ] `credential_id`
  * [ ] `public_key`
  * [ ] `counter`
  * [ ] `aaguid`
  * [ ] `created_at`
* [ ] Garantir FK para `users`.
* [ ] Garantir unicidade de `credential_id`.
* [ ] Garantir índices necessários.
* [ ] Garantir tipos adequados para dados binários/textuais.
* [ ] Garantir que migration rode em banco limpo.
* [ ] Garantir que migration não dependa de alteração manual do banco.

### pom.xml

* [ ] Revisar dependências adicionadas.
* [ ] Garantir que a biblioteca WebAuthn seja adequada.
* [ ] Remover dependências não utilizadas.
* [ ] Evitar versões duplicadas ou conflitantes.
* [ ] Verificar se não houve downgrade acidental de dependências existentes.

## Checklist de testes

Adicionar testes automatizados mínimos para evitar regressão.

### Testes de contexto Spring

* [ ] Criar teste que valida que o contexto Spring sobe com a configuração WebAuthn.
* [ ] Garantir que o erro de DI não volte.
* [ ] Testar criação dos beans principais relacionados a WebAuthn.

### Testes de controller

* [ ] Testar `POST /api/v1/auth/passkey/register/start`.
* [ ] Testar request inválido retornando erro padronizado.
* [ ] Testar response segura.
* [ ] Se possível, testar `finish` com mock do service.

### Testes de service

* [ ] Testar fluxo de início de registro.
* [ ] Testar comportamento quando usuário não existe.
* [ ] Testar persistência/associação de credencial em cenário controlado.
* [ ] Mockar dependências externas complexas da biblioteca WebAuthn quando necessário.

### Testes de repository/migration

* [ ] Garantir que migration V3 cria tabela corretamente.
* [ ] Garantir que `credential_id` é único.
* [ ] Garantir que FK com `users` funciona.
* [ ] Usar teste com banco de teste se já houver infraestrutura.
* [ ] Se não houver Testcontainers ainda, não adicionar sem avaliar impacto; preferir teste compatível com a estrutura atual.

## Critérios de aceitação

* [ ] Aplicação sobe sem erro de DI.
* [ ] `Passkey` está no pacote correto conforme arquitetura do projeto.
* [ ] Endpoints de start/finish permanecem disponíveis.
* [ ] Endpoints seguem `/api/v1`.
* [ ] Nenhuma credencial sensível é logada.
* [ ] Configurações WebAuthn vêm de ambiente/configuração.
* [ ] Migration V3 roda corretamente.
* [ ] AUTH-01 e AUTH-02 continuam funcionando.
* [ ] Testes automatizados foram adicionados.
* [ ] Testes passam.
* [ ] Dependências no `pom.xml` foram revisadas.
* [ ] Não houve implementação de PASSKEY-02.

## Fora de escopo

Não implementar ainda:

* Autenticação com passkey.
* Validação de counter em login.
* Emissão de JWT por passkey.
* Revogação de dispositivo.
* UI de passkeys.
* Tela de gerenciamento de dispositivos.
* Fluxo completo de sessão.
* Alterações no EPIC-04.
* Alterações no EPIC-03.

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

## Prompt recomendado para o agente

Revise o PR referente à PASSKEY-01 seguindo `docs/ai/AGENTS.md`, `docs/ai/CONTEXT.md`, `docs/architecture/backend.md`, `docs/architecture/database.md`, `docs/security/security-overview.md`, `docs/standards/api.md` e este arquivo.

Corrija o erro de DI do Spring, alinhe os arquivos ao padrão arquitetural atual do projeto, revise a migration `V3__create_passkeys_table.sql`, revise as dependências adicionadas ao `pom.xml` e adicione testes automatizados mínimos.

Não implemente PASSKEY-02, login com passkey, emissão de JWT, validação de counter ou frontend.

Ao final:

* informe o erro de DI encontrado e como foi corrigido
* liste arquivos alterados
* explique os testes adicionados
* explique como validar manualmente os endpoints de registro de passkey
* confirme que AUTH-01 e AUTH-02 continuam funcionando
