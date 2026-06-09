# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-SIGN-02 — Revisar assinatura criptográfica embutida no PDF

## Camada

Backend / Segurança / Documentos / Criptografia / Testes

## Prioridade

Alta

## Objetivo

Revisar a implementação da assinatura digital de PDFs, garantindo que o sistema realize uma assinatura criptográfica real embutida no PDF, e não apenas adicione uma imagem, texto ou selo visual sobre o documento.

## Contexto

A PR implementou múltiplas tarefas de assinatura digital em conjunto.

Já foram revisadas separadamente:

- SIGN-01 — geração e armazenamento de par de chaves;
- SIGN-04 — validação e sanitização de PDFs.

Esta tarefa deve focar exclusivamente em:

```text
SIGN-02 — Backend: assinatura criptográfica embutida no PDF
````

## Arquivos relevantes para esta review

```text
backend/src/main/java/.../
├── controller/document/
│   └── DocumentController.java
├── domain/
│   └── DocumentSignature.java
├── dto/document/
│   └── SignDocumentRequest.java
├── repository/
│   └── DocumentSignatureRepository.java
└── service/document/
    ├── PdfSigningService.java
    └── PdfValidatorService.java

backend/src/main/resources/db/migration/
└── V10__create_document_signatures_table.sql
```

Também revisar, se necessário:

```text
pom.xml
GlobalExceptionHandler.java
UserKeyService.java
UserKeyEncryptionService.java
```

## Regra principal

A assinatura deve ser criptográfica e embutida no PDF.

Não basta:

* desenhar texto;
* inserir imagem;
* adicionar selo visual;
* calcular hash externo;
* registrar metadados no banco.

O PDF final precisa conter assinatura detectável por leitores PDF compatíveis.

## Checklist arquitetural

* [ ] `DocumentController` apenas recebe requisição e delega.
* [ ] `PdfSigningService` concentra a lógica de assinatura.
* [ ] `PdfValidatorService` é chamado antes de assinar.
* [ ] `UserKeyService` fornece a chave privada descriptografada de forma controlada.
* [ ] `UserKeyEncryptionService` não vaza chave privada.
* [ ] `DocumentSignatureRepository` apenas persiste metadados.
* [ ] Entidade `DocumentSignature` não contém PDF binário.
* [ ] DTOs não expõem dados sensíveis.
* [ ] Não salvar PDF original em disco.
* [ ] Não salvar PDF assinado em disco/banco.

## Endpoint esperado

```text
POST /api/v1/documents/sign
```

Deve receber:

* arquivo PDF via multipart;
* página do selo;
* coordenadas X/Y do selo;
* possivelmente dimensões do selo.

## Fluxo esperado

```text
Receber PDF
↓
Validar PDF com SIGN-04
↓
Recuperar chave privada do usuário autenticado
↓
Calcular hash SHA-256 do PDF original
↓
Assinar criptograficamente o PDF
↓
Inserir assinatura embutida compatível com PDF Signature
↓
Adicionar selo visual
↓
Calcular hash SHA-256 do PDF assinado
↓
Persistir metadados da assinatura
↓
Retornar PDF assinado em memória
```

## Assinatura criptográfica

Verificar:

* [ ] Usa biblioteca adequada, como iText 7 ou Apache PDFBox.
* [ ] Não implementa assinatura PDF manualmente de forma frágil.
* [ ] Usa chave privada do usuário.
* [ ] Assinatura fica embutida no PDF.
* [ ] Leitores PDF conseguem detectar assinatura.
* [ ] Alterar o PDF depois da assinatura invalida a assinatura.
* [ ] Algoritmo de assinatura é seguro.
* [ ] Não usa MD5/SHA-1.
* [ ] Não usa chave hardcoded.
* [ ] Não usa chave privada sem criptografia em repouso.

## Selo visual

O selo deve conter, no mínimo:

* [ ] nome do assinante;
* [ ] data/hora UTC;
* [ ] hash SHA-256 do documento;
* [ ] identificador único da assinatura.

Verificar:

* [ ] selo aparece na página informada;
* [ ] coordenadas X/Y são respeitadas;
* [ ] página inválida é rejeitada;
* [ ] coordenadas fora da página são rejeitadas ou normalizadas com regra clara;
* [ ] texto do selo não quebra PDF;
* [ ] dados do selo são sanitizados.

## Hashes

Persistir:

* [ ] hash SHA-256 do documento original;
* [ ] hash SHA-256 do documento assinado.

Verificar:

* [ ] hash é calculado sobre os bytes corretos;
* [ ] hash original é calculado antes da assinatura;
* [ ] hash assinado é calculado depois da assinatura;
* [ ] formato do hash é consistente, preferencialmente hex lowercase;
* [ ] não usa algoritmo fraco.

## Persistência

Migration:

```text
V10__create_document_signatures_table.sql
```

Verificar se a tabela contém:

* [ ] `id`;
* [ ] `user_id`;
* [ ] `original_document_hash`;
* [ ] `signed_document_hash`;
* [ ] `signature_identifier`;
* [ ] `signed_at`;
* [ ] `ip_address` ou hash do IP;
* [ ] `page`;
* [ ] `position_x`;
* [ ] `position_y`.

Boas práticas:

* [ ] `id` como UUID.
* [ ] FK para `users`.
* [ ] campos obrigatórios com `NOT NULL`.
* [ ] índice em `signature_identifier`.
* [ ] índice em hashes, se serão usados para verificação.
* [ ] não armazenar PDF.
* [ ] não armazenar chave privada.

## Retorno da API

A resposta deve retornar o PDF assinado.

Verificar:

* [ ] `Content-Type: application/pdf`;
* [ ] `Content-Disposition` adequado;
* [ ] bytes correspondem ao PDF assinado;
* [ ] não retorna JSON com PDF base64 sem necessidade;
* [ ] não persiste arquivo no servidor;
* [ ] resposta lida corretamente pelo frontend.

## Segurança

Obrigatório:

* [ ] endpoint exige autenticação;
* [ ] usuário só assina com sua própria chave;
* [ ] chave privada é usada apenas em memória;
* [ ] chave privada não é logada;
* [ ] PDF inválido não é assinado;
* [ ] PDF inválido não gera registro em banco;
* [ ] falha de assinatura não gera registro falso;
* [ ] exceções não expõem detalhes internos;
* [ ] não salvar arquivos temporários sem cleanup.

## Validação de posição do selo

Verificar:

* [ ] página deve existir no PDF;
* [ ] X/Y devem ser números válidos;
* [ ] X/Y não podem ser negativos;
* [ ] selo não deve ficar completamente fora da página;
* [ ] campos inválidos retornam erro padronizado.

## Testes obrigatórios

Criar ou revisar testes para:

### PdfSigningService

* [ ] assina PDF válido;
* [ ] retorna bytes de PDF assinado;
* [ ] rejeita PDF inválido;
* [ ] chama `PdfValidatorService`;
* [ ] usa chave privada do usuário;
* [ ] calcula hash original;
* [ ] calcula hash assinado;
* [ ] persiste `DocumentSignature`;
* [ ] não persiste PDF;
* [ ] não persiste registro se assinatura falhar.

### DocumentController

* [ ] upload válido retorna `application/pdf`;
* [ ] upload inválido retorna erro padronizado;
* [ ] usuário não autenticado recebe `401`;
* [ ] coordenada inválida retorna `400`;
* [ ] página inválida retorna `400`.

### Integração mínima

* [ ] PDF assinado continua começando com `%PDF-`;
* [ ] alteração posterior do PDF invalida assinatura, se testável;
* [ ] assinatura é detectável pela biblioteca usada, se possível.

## Critérios de aceitação

* [ ] PDF recebe assinatura criptográfica real.
* [ ] Assinatura é embutida no PDF.
* [ ] Selo visual é adicionado.
* [ ] Selo contém nome, UTC, hash e identificador.
* [ ] Hash original e hash assinado são persistidos.
* [ ] Metadados da assinatura são persistidos.
* [ ] PDF original não é armazenado.
* [ ] PDF assinado não é armazenado.
* [ ] Endpoint retorna PDF assinado.
* [ ] PDF inválido não é assinado.
* [ ] Falha de assinatura não gera registro inconsistente.
* [ ] Testes automatizados foram adicionados.
* [ ] SIGN-01 e SIGN-04 continuam funcionando.

## Fora de escopo

Não implementar nesta review:

* Verificação pública de autenticidade.
* Tela frontend de upload.
* Preview de PDF.
* Posicionamento visual no frontend.
* Auditoria avançada.
* Storage em nuvem.
* Certificado ICP-Brasil.
* Carimbo de tempo externo/TSA.
* Revogação de certificado.
* Múltiplas assinaturas avançadas.

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
