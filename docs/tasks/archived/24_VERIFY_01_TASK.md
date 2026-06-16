# CURRENT_TASK.md — Tarefa Atual

## Status

Em desenvolvimento.

## Tarefa ativa

### VERIFY-01 — Backend: endpoint público de verificação criptográfica de PDF

## Camada

Backend / Documentos / Segurança / Verificação Pública

## Prioridade

Alta

## Objetivo

Implementar um endpoint público para verificar a autenticidade e integridade de PDFs assinados pelo sistema.

O endpoint deve receber um PDF, validar a assinatura criptográfica embutida, comparar os hashes registrados no banco e retornar um resultado claro para o usuário:

- `VALID`
- `TAMPERED`
- `NOT_FOUND`

## Contexto

O sistema já possui:

- geração e armazenamento seguro de par de chaves por usuário;
- validação e sanitização de PDFs;
- assinatura criptográfica embutida no PDF;
- selo visual;
- registro de metadados da assinatura;
- hash do documento original;
- hash do documento assinado;
- identificador único da assinatura;
- endpoint autenticado de assinatura.

Agora será implementado o primeiro fluxo público de verificação.

Este endpoint deve ser acessível sem login.

## Endpoint esperado

```text
POST /api/v1/verify
````

## Autenticação

Este endpoint deve ser público.

Checklist:

* [ ] Não exigir JWT.
* [ ] Não exigir sessão.
* [ ] Não exigir refresh token.
* [ ] Não exigir usuário autenticado.
* [ ] Não retornar dados sensíveis.
* [ ] Manter rate limiting.

## Entrada

Receber PDF via upload multipart.

Exemplo conceitual:

```text
file=<documento.pdf>
```

Checklist:

* [ ] aceitar apenas multipart.
* [ ] campo do arquivo deve ter nome claro, como `file`.
* [ ] rejeitar arquivo ausente.
* [ ] rejeitar arquivo vazio.
* [ ] rejeitar arquivo acima do limite.
* [ ] validar PDF antes de processar.

## Fluxo esperado

```text
Receber PDF
↓
Validar PDF
↓
Extrair assinatura criptográfica embutida
↓
Verificar assinatura usando chave pública correspondente
↓
Extrair identificador da assinatura, se disponível
↓
Calcular hash SHA-256 do PDF recebido
↓
Buscar registro de assinatura no banco
↓
Comparar hash recebido com hash registrado
↓
Retornar VALID, TAMPERED ou NOT_FOUND
↓
Descartar PDF imediatamente
```

## Resultados possíveis

### VALID

O documento é autêntico e íntegro.

Condições:

* assinatura criptográfica é válida;
* assinatura foi criada pelo sistema;
* registro existe no banco;
* hash do documento recebido corresponde ao hash registrado;
* documento não foi alterado após assinatura.

Resposta sugerida:

```json
{
  "status": "VALID",
  "message": "Documento íntegro e autenticado.",
  "signature": {
    "signatureId": "uuid",
    "signedAt": "2026-05-16T12:00:00Z",
    "signerName": "Nome do Assinante"
  }
}
```

### TAMPERED

O documento possui assinatura conhecida, mas foi alterado ou a assinatura/hash não corresponde.

Condições possíveis:

* assinatura criptográfica inválida;
* hash atual diferente do hash assinado registrado;
* PDF foi alterado após assinatura;
* registro existe, mas integridade falhou.

Resposta sugerida:

```json
{
  "status": "TAMPERED",
  "message": "Documento adulterado — a integridade foi comprometida."
}
```

### NOT_FOUND

Não foi encontrada assinatura reconhecida pelo sistema.

Condições possíveis:

* PDF não possui assinatura;
* assinatura não foi criada pelo sistema;
* identificador da assinatura não existe no banco;
* metadados necessários não foram encontrados.

Resposta sugerida:

```json
{
  "status": "NOT_FOUND",
  "message": "Assinatura não encontrada neste documento."
}
```

## Modelagem de resposta

Criar DTOs claros, por exemplo:

```text
VerifyDocumentResponse
VerifySignatureData
VerifyStatus
```

`VerifyStatus` deve ser enum:

```text
VALID
TAMPERED
NOT_FOUND
```

## Validação do PDF

Reutilizar a camada de validação já implementada em SIGN-04.

Checklist:

* [ ] usar `PdfValidatorService`;
* [ ] validar magic number `%PDF-`;
* [ ] validar tamanho máximo;
* [ ] rejeitar PDFs com JavaScript/ações suspeitas;
* [ ] rejeitar PDF malformado;
* [ ] não duplicar lógica de validação;
* [ ] não assinar, alterar ou persistir o PDF.

## Verificação criptográfica

Criar serviço próprio, se ainda não existir:

```text
PdfVerificationService
```

Responsabilidades:

* [ ] extrair assinatura do PDF;
* [ ] identificar assinatura criada pelo sistema;
* [ ] localizar registro no banco;
* [ ] verificar assinatura criptográfica;
* [ ] comparar hashes;
* [ ] retornar status de verificação.

Não colocar essa lógica no controller.

## Banco de dados

Usar os registros existentes de:

```text
DocumentSignature
DocumentSignatureRepository
```

Verificar se o registro contém informações suficientes:

* [ ] `signatureIdentifier`;
* [ ] `signedDocumentHash`;
* [ ] `originalDocumentHash`;
* [ ] `user`;
* [ ] `signedAt`;
* [ ] metadados necessários para resposta.

Se faltar algum campo essencial, registrar claramente como limitação ou ajustar migration apenas se necessário e seguro.

## Hash

Calcular SHA-256 do PDF recebido.

Checklist:

* [ ] usar algoritmo SHA-256;
* [ ] formato consistente com o usado no momento da assinatura;
* [ ] comparar contra `signedDocumentHash`;
* [ ] não comparar com hash visual/selo;
* [ ] não usar MD5/SHA-1.

## Chave pública

A verificação deve usar chave pública do assinante.

Checklist:

* [ ] recuperar chave pública associada ao usuário da assinatura;
* [ ] não usar chave privada;
* [ ] não descriptografar chave privada;
* [ ] não acessar segredo desnecessário;
* [ ] assinatura deve ser verificável apenas com a chave pública.

## Rate limiting

Como o endpoint é público, aplicar rate limiting.

Requisito:

```text
10 verificações por minuto por IP
```

Checklist:

* [ ] reutilizar componente de rate limiting existente, se houver;
* [ ] não criar duplicação desnecessária;
* [ ] chavear por IP;
* [ ] retornar `429 Too Many Requests`;
* [ ] erro deve seguir padrão global;
* [ ] não bloquear usuários autenticados de outros fluxos por acidente.

## Segurança

Obrigatório:

* [ ] endpoint público não deve vazar dados sensíveis;
* [ ] não retornar chave pública completa, chave privada, hashes internos desnecessários ou IP;
* [ ] não salvar PDF recebido;
* [ ] não salvar PDF temporário sem cleanup;
* [ ] não executar scripts ou ações embutidas;
* [ ] não confiar no selo visual;
* [ ] não considerar válido só porque existe texto/selo no PDF;
* [ ] não considerar válido sem assinatura criptográfica;
* [ ] não expor stack trace ou detalhes de biblioteca.

## Processamento em memória

* [ ] PDF deve ser processado em memória.
* [ ] PDF deve ser descartado após processamento.
* [ ] Nenhum arquivo deve permanecer em disco.
* [ ] Nenhum PDF deve ser salvo no banco.

## Controller

Criar ou atualizar:

```text
VerifyController
```

ou equivalente.

Checklist:

* [ ] controller público;
* [ ] endpoint `POST /api/v1/verify`;
* [ ] recebe multipart file;
* [ ] delega para service;
* [ ] não contém lógica criptográfica;
* [ ] retorna DTO limpo;
* [ ] trata erros pelo GlobalExceptionHandler.

## CORS e CSRF

### CORS

Endpoint público pode ser chamado pelo frontend público de verificação.

Checklist:

* [ ] garantir compatibilidade com CORS existente;
* [ ] não usar wildcard inseguro se política do projeto não permitir.

### CSRF

Como o endpoint é público e não depende de cookie de autenticação:

* [ ] avaliar isenção de CSRF;
* [ ] documentar decisão;
* [ ] não exigir refresh token/cookie;
* [ ] não criar dependência de sessão.

## Testes automatizados

Criar testes para:

### Service

* [ ] PDF assinado válido retorna `VALID`.
* [ ] PDF sem assinatura retorna `NOT_FOUND`.
* [ ] PDF com assinatura desconhecida retorna `NOT_FOUND`.
* [ ] PDF adulterado retorna `TAMPERED`.
* [ ] PDF inválido é rejeitado.
* [ ] PDF malformado é rejeitado.
* [ ] Hash divergente retorna `TAMPERED`.
* [ ] Registro inexistente retorna `NOT_FOUND`.
* [ ] Verificação não usa chave privada.

### Controller

* [ ] Upload válido retorna `200`.
* [ ] Upload inválido retorna erro padronizado.
* [ ] Endpoint não exige autenticação.
* [ ] Endpoint respeita rate limiting.
* [ ] 11ª verificação no mesmo IP em 1 minuto retorna `429`.

### Persistência

* [ ] Busca por identificador da assinatura funciona.
* [ ] Busca por hash funciona, se aplicável.
* [ ] Não cria novos registros ao verificar.

## Testes manuais recomendados

### PDF válido

* [ ] Assinar um PDF pelo fluxo SIGN-03.
* [ ] Enviar para `/api/v1/verify`.
* [ ] Confirmar status `VALID`.

### PDF adulterado

* [ ] Alterar o PDF após assinatura.
* [ ] Enviar para `/api/v1/verify`.
* [ ] Confirmar status `TAMPERED`.

### PDF não assinado

* [ ] Enviar PDF comum.
* [ ] Confirmar status `NOT_FOUND`.

### Rate limiting

* [ ] Enviar mais de 10 verificações em 1 minuto do mesmo IP.
* [ ] Confirmar `429`.

## Critérios de aceitação

* [ ] Endpoint público `POST /api/v1/verify` implementado.
* [ ] Endpoint não exige login.
* [ ] PDF válido assinado retorna `VALID`.
* [ ] PDF adulterado retorna `TAMPERED`.
* [ ] PDF sem assinatura reconhecida retorna `NOT_FOUND`.
* [ ] Verificação usa assinatura criptográfica, não selo visual.
* [ ] Hash do PDF recebido é comparado com hash registrado.
* [ ] Rate limiting público aplicado.
* [ ] PDF não é salvo em disco nem no banco.
* [ ] Erros seguem padrão global.
* [ ] Testes automatizados foram adicionados.

## Fora de escopo

Não implementar nesta tarefa:

* Tela pública de verificação.
* UI de upload público.
* Histórico de verificações.
* Auditoria avançada.
* Notificações.
* QR Code público.
* Página de resultado visual.
* Integração com blockchain.
* Certificados externos.
* TSA/carimbo do tempo externo.
* ICP-Brasil.

## Arquivos relevantes

Consultar antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/backend.md`
* `docs/architecture/database.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
* `docs/decisions/DECISIONS.md`
