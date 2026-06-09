# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-SIGN-04 — Revisar validação e sanitização de PDFs enviados

## Camada

Backend / Segurança / Documentos / Testes

## Prioridade

Alta

## Objetivo

Revisar se a PR implementou corretamente a validação e sanitização de PDFs enviados, conforme a tarefa:

```text
SIGN-04 — Backend: validação e sanitização de PDFs enviados
````

Caso a implementação esteja ausente, incompleta ou misturada indevidamente com a assinatura do PDF, corrigir a arquitetura criando uma camada explícita, testável e segura para validação de PDFs.

## Contexto

A PR implementou múltiplas tarefas relacionadas à assinatura digital em conjunto.

A tarefa SIGN-04 é dependência direta de SIGN-02, pois o PDF deve ser validado antes de qualquer tentativa de assinatura criptográfica.

Até o momento, os arquivos impactados não parecem indicar uma camada clara e isolada de validação/sanitização de PDF, exceto possível presença de:

```text
PdfValidatorService.java
PdfValidationException.java
GlobalExceptionHandler.java
DocumentController.java
PdfSigningService.java
```

Esta review deve verificar se SIGN-04 foi realmente implementada.

## Arquivos relevantes para esta review

```text
backend/src/main/java/.../
├── controller/document/
│   └── DocumentController.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── PdfValidationException.java
└── service/document/
    ├── PdfValidatorService.java
    └── PdfSigningService.java
```

Também revisar, se necessário:

```text
pom.xml
.env.example
```

## Escopo da SIGN-04

A validação de PDFs deve acontecer antes da assinatura.

A tarefa deve garantir:

* arquivo realmente é PDF;
* tamanho máximo é respeitado;
* conteúdo potencialmente perigoso é rejeitado;
* entradas textuais usadas no selo são validadas/sanitizadas;
* PDF não é salvo em disco;
* erro retorna formato padronizado.

## Checklist arquitetural

* [ ] Verificar se existe `PdfValidatorService`.
* [ ] Se não existir, criar serviço próprio para validação.
* [ ] Garantir que validação não esteja escondida dentro de `PdfSigningService`.
* [ ] Garantir que `DocumentController` não contenha regra de validação complexa.
* [ ] `PdfSigningService` deve receber PDF já validado ou chamar explicitamente o validador antes de assinar.
* [ ] `PdfValidationException` deve ficar em `exception/`.
* [ ] Erros devem ser tratados no `GlobalExceptionHandler`.
* [ ] Não criar lógica de segurança espalhada por controller/service sem clareza.

## Checklist de validação de arquivo

### Magic number

* [ ] Verificar os primeiros bytes do arquivo.
* [ ] Arquivo deve iniciar com `%PDF-`.
* [ ] Não confiar apenas em:

  * extensão `.pdf`;
  * `Content-Type`;
  * nome do arquivo.

### Tamanho máximo

* [ ] Rejeitar arquivos acima de 20MB.
* [ ] Limite deve estar centralizado/configurável.
* [ ] Retornar erro padronizado em caso de excesso.

### Arquivo vazio

* [ ] Rejeitar arquivo vazio.
* [ ] Rejeitar arquivo nulo.
* [ ] Rejeitar upload ausente.

### Content-Type

* [ ] Validar `application/pdf` como checagem complementar.
* [ ] Não depender exclusivamente do Content-Type.

## Checklist de conteúdo perigoso

Verificar rejeição de PDFs com:

* [ ] `/JavaScript`
* [ ] `/JS`
* [ ] `/OpenAction`
* [ ] `/AA`
* [ ] ações automáticas suspeitas, se a biblioteca permitir detecção.

Observação:

A checagem textual simples pode ser uma camada inicial, mas deve ser feita com cuidado para não dar falsa sensação de segurança.

Se usar biblioteca de parsing:

* [ ] tratar PDF malformado;
* [ ] limitar processamento;
* [ ] evitar salvar em disco;
* [ ] não executar ações embutidas;
* [ ] não carregar recursos remotos.

## Sanitização de inputs textuais

Validar/sanitizar campos usados no selo visual, como:

* [ ] nome do assinante;
* [ ] e-mail;
* [ ] identificadores;
* [ ] qualquer texto vindo do usuário.

Regras:

* [ ] rejeitar strings vazias quando obrigatórias;
* [ ] limitar tamanho;
* [ ] remover/controlar caracteres de controle;
* [ ] escapar quando renderizar texto no PDF;
* [ ] evitar XSS em qualquer retorno;
* [ ] não montar SQL manualmente.

## Processamento em memória

* [ ] PDF deve ser processado em memória.
* [ ] Não salvar PDF em disco.
* [ ] Não criar arquivo temporário sem necessidade.
* [ ] Se biblioteca exigir temporário, documentar e garantir cleanup.
* [ ] Não persistir PDF original.
* [ ] Não persistir PDF assinado.

## Tratamento de erros

`PdfValidationException` deve retornar erro padronizado.

Exemplo:

```json
{
  "code": "DOC_001",
  "message": "Documento PDF inválido.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

Verificar:

* [ ] status HTTP adequado, normalmente `400 Bad Request`;
* [ ] sem stack trace;
* [ ] sem detalhes internos da biblioteca;
* [ ] sem path local;
* [ ] sem conteúdo do PDF.

## Integração com DocumentController

Verificar:

* [ ] upload usa `MultipartFile`;
* [ ] validação ocorre antes da assinatura;
* [ ] controller delega para service;
* [ ] limites de upload são respeitados;
* [ ] erros seguem padrão global;
* [ ] endpoint exige autenticação, se já previsto.

## Integração com PdfSigningService

* [ ] Assinatura só ocorre após validação bem-sucedida.
* [ ] `PdfSigningService` não tenta assinar arquivo inválido.
* [ ] Falha de validação interrompe o fluxo.
* [ ] Nenhum registro de assinatura é salvo se PDF for inválido.
* [ ] Nenhuma chave privada é acessada antes de validação básica do PDF, se possível.

## Revisão de dependências

No `pom.xml`, verificar:

* [ ] biblioteca de PDF é adequada;
* [ ] não há dependência desnecessária;
* [ ] não há duplicidade;
* [ ] não há downgrade acidental;
* [ ] biblioteca não está abandonada;
* [ ] tratamento de CVEs relevantes foi considerado.

## Testes obrigatórios

Criar testes específicos para `PdfValidatorService`.

### Casos mínimos

* [ ] PDF válido passa.
* [ ] Arquivo vazio é rejeitado.
* [ ] Arquivo nulo é rejeitado.
* [ ] Arquivo acima de 20MB é rejeitado.
* [ ] Arquivo com extensão `.pdf`, mas magic number inválido, é rejeitado.
* [ ] Arquivo com Content-Type errado é rejeitado ou tratado conforme política.
* [ ] PDF com `/JavaScript` é rejeitado.
* [ ] PDF com `/JS` é rejeitado.
* [ ] PDF com `/OpenAction` é rejeitado, se implementado.
* [ ] PDF malformado é rejeitado de forma segura.
* [ ] Erro retorna `PdfValidationException`.

### Testes de controller

* [ ] Upload inválido retorna `400`.
* [ ] Upload inválido retorna código `DOC_001` ou equivalente.
* [ ] PDF inválido não chama serviço de assinatura.
* [ ] PDF inválido não gera registro em `DocumentSignature`.

## Critérios de aceitação

* [ ] Existe camada clara de validação de PDF.
* [ ] Validação ocorre antes da assinatura.
* [ ] Magic number `%PDF-` é verificado.
* [ ] Arquivos acima de 20MB são rejeitados.
* [ ] PDFs com JavaScript/ações suspeitas são rejeitados.
* [ ] PDF inválido não é assinado.
* [ ] PDF inválido não gera registro de assinatura.
* [ ] PDF não é salvo em disco.
* [ ] Erros seguem padrão global.
* [ ] Testes automatizados foram adicionados.
* [ ] SIGN-02 permanece fora do escopo desta review.

## Fora de escopo

Não revisar/implementar nesta tarefa:

* Assinatura criptográfica embutida.
* Selo visual.
* Registro completo de assinatura.
* Verificação pública.
* UI de upload.
* Download do PDF assinado.
* Geração de par de chaves.
* Rotação de chaves.
* Auditoria avançada.

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
