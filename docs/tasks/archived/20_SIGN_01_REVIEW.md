# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-SIGN-01 — Revisar geração e armazenamento de par de chaves por usuário

## Camada

Backend / Segurança / Criptografia / Testes

## Prioridade

Alta

## Objetivo

Revisar a implementação relacionada à geração e armazenamento do par de chaves do usuário, garantindo segurança criptográfica, aderência arquitetural, persistência correta e testes automatizados.

## Contexto

A PR implementou múltiplas issues de assinatura digital ao mesmo tempo. Esta review deve focar exclusivamente na parte correspondente a:

```text
SIGN-01 — Backend: geração e armazenamento de par de chaves por usuário
````

Não revisar ainda validação de PDF nem assinatura criptográfica embutida. Essas serão revisadas em tarefas separadas.

## Arquivos relevantes para esta review

```text
backend/src/main/java/.../
├── domain/
│   └── UserKey.java
├── security/
│   └── UserKeyEncryptionService.java
└── service/auth/
    ├── UserKeyService.java
    └── UserRegistrationService.java

backend/src/main/resources/db/migration/
└── V9__create_user_keys_table.sql

backend/src/test/java/.../service/auth/
└── UserRegistrationServiceTests.java
```

Também revisar:

```text
pom.xml
.env.example
```

## Checklist arquitetural

* [ ] `UserKey` está em `domain/`, seguindo o padrão do projeto.
* [ ] `UserKeyService` está em pacote coerente.
* [ ] Avaliar se `service/auth/UserKeyService.java` é o melhor local ou se deveria ficar em `service/security/` ou `service/signature/`.
* [ ] `UserRegistrationService` apenas orquestra a criação das chaves, sem conter lógica criptográfica diretamente.
* [ ] `UserKeyEncryptionService` concentra criptografia/descriptografia da chave privada.
* [ ] Controller não participa da geração de chaves.
* [ ] Repository, se existir, apenas persiste dados.
* [ ] Usar constructor injection.
* [ ] Evitar field injection.
* [ ] Não criar dependências circulares.

## Checklist de geração de chaves

* [ ] Par de chaves é gerado automaticamente no cadastro do usuário.
* [ ] Algoritmo usado é seguro:

    * [ ] RSA-2048 ou superior;
    * [ ] ou ECDSA P-256.
* [ ] Não usar algoritmos fracos.
* [ ] Não usar tamanho de chave inseguro.
* [ ] Não reutilizar par de chaves entre usuários.
* [ ] Não gerar chave determinística.
* [ ] Usar fonte segura de aleatoriedade.
* [ ] Garantir que falha na geração de chave não cria usuário em estado inconsistente.

## Checklist de armazenamento

* [ ] Chave pública é armazenada em formato adequado.
* [ ] Chave privada nunca é salva em texto claro.
* [ ] Chave privada é criptografada antes de persistir.
* [ ] Chave privada nunca aparece em resposta de API.
* [ ] Chave privada nunca aparece em logs.
* [ ] Chave privada nunca aparece em mensagens de erro.
* [ ] `UserKey` está associado corretamente ao usuário.
* [ ] Não há múltiplas chaves ativas por usuário sem regra clara.

## Checklist de criptografia da chave privada

Arquivo:

```text
UserKeyEncryptionService.java
```

Verificar:

* [ ] Usa AES-256-GCM ou abordagem equivalente segura.
* [ ] Não usa AES/ECB.
* [ ] Usa IV/nonce único por criptografia.
* [ ] Armazena IV junto do ciphertext ou em coluna própria.
* [ ] Não reutiliza IV.
* [ ] Usa tag de autenticação do GCM corretamente.
* [ ] Chave mestra vem de variável de ambiente.
* [ ] Chave mestra não está hardcoded.
* [ ] `.env.example` documenta a variável sem valor real.
* [ ] Falta de chave causa erro claro no startup.
* [ ] Não loga chave mestra, IV, plaintext ou ciphertext completo.

## Migration V9

Arquivo:

```text
V9__create_user_keys_table.sql
```

Verificar se a tabela possui campos como:

* [ ] `id`
* [ ] `user_id`
* [ ] `public_key`
* [ ] `encrypted_private_key`
* [ ] `key_algorithm`
* [ ] `created_at`

Avaliar também:

* [ ] FK correta para `users`.
* [ ] unicidade por `user_id`, se o modelo for uma chave ativa por usuário.
* [ ] tipos adequados para chaves.
* [ ] campos obrigatórios como `NOT NULL`.
* [ ] índice em `user_id`.
* [ ] migration roda em banco limpo.
* [ ] migration não depende de alteração manual.

## UserRegistrationService

Verificar:

* [ ] Cadastro de usuário continua funcionando.
* [ ] Par de chaves é gerado dentro do fluxo transacional adequado.
* [ ] Se a geração/persistência da chave falhar, cadastro não fica parcialmente inconsistente.
* [ ] Não duplicou lógica de cadastro.
* [ ] Não quebrou AUTH-01.
* [ ] Não quebrou AUTH-02.
* [ ] Não expõe dados de chave no retorno.

## pom.xml

Verificar:

* [ ] Dependências criptográficas adicionadas são necessárias.
* [ ] Não há dependências abandonadas.
* [ ] Não há bibliotecas duplicadas.
* [ ] Não houve downgrade de dependências existentes.
* [ ] Dependências novas não introduzem APIs inseguras sem necessidade.

## Testes obrigatórios

Adicionar ou revisar testes para:

* [ ] Cadastro de usuário gera par de chaves.
* [ ] Chave pública é persistida.
* [ ] Chave privada é persistida criptografada.
* [ ] Chave privada em texto claro não aparece no banco.
* [ ] Algoritmo da chave é salvo corretamente.
* [ ] Cada usuário recebe par de chaves diferente.
* [ ] Falha na geração/persistência da chave não deixa usuário inconsistente.
* [ ] `UserKeyEncryptionService` criptografa e descriptografa corretamente.
* [ ] Criptografias repetidas do mesmo conteúdo geram ciphertext diferente por causa do IV.
* [ ] Descriptografia com chave errada falha.
* [ ] AUTH-01 continua passando.

## Critérios de aceitação

* [ ] Usuário recebe par de chaves automaticamente no cadastro.
* [ ] Chave privada nunca é armazenada em texto claro.
* [ ] Chave privada é protegida com criptografia autenticada.
* [ ] Chave mestra vem de variável de ambiente.
* [ ] Migration V9 está correta.
* [ ] Arquitetura está alinhada aos markdowns em `docs/`.
* [ ] Testes automatizados foram adicionados/ampliados.
* [ ] Cadastro existente não foi quebrado.
* [ ] Nenhum segredo é logado ou retornado em API.

## Fora de escopo

Não revisar/implementar nesta tarefa:

* Validação de PDFs.
* Sanitização de PDFs.
* Assinatura criptográfica do PDF.
* Selo visual.
* Endpoint de upload.
* Verificação pública.
* UI de assinatura.
* Auditoria completa.
* Storage de arquivos.

## Arquivos de referência

Consultar antes de revisar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/architecture/database.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
* `docs/decisions/DECISIONS.md`
