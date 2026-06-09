# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### SIGN-03 — Frontend: upload de PDF e posicionamento do selo

## Camada

Frontend / Documentos / UI

## Prioridade

Muito Alta

## Objetivo

Implementar a principal funcionalidade do produto:

* upload do PDF;
* preview do documento;
* posicionamento visual do selo;
* captura das coordenadas;
* envio para o backend;
* recebimento do PDF assinado;
* disponibilização para download;
* feedback visual durante todo o fluxo.

## Filosofia

O frontend NÃO assina PDFs.

O frontend apenas:

* exibe o PDF;
* mostra uma prévia do selo;
* captura posição;
* envia informações ao backend.

O PDF assinado final é sempre produzido pelo backend.

## Referências obrigatórias

Consultar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/frontend.md`
* `docs/standards/frontend-style.md`
* `docs/security/security-overview.md`
* endpoints já implementados do backend.

# Fluxo esperado

```text
Selecionar PDF
↓
Preview do PDF
↓
Escolher página
↓
Posicionar selo visual
↓
Confirmar posição
↓
Enviar para backend
↓
Loading
↓
Receber PDF assinado
↓
Sucesso
↓
Download
```

# Estrutura recomendada

```text
frontend/src/
├── pages/
│   └── documents/
│       └── SignDocumentPage.tsx
├── components/
│   └── document/
│       ├── PdfUploader.tsx
│       ├── PdfViewer.tsx
│       ├── SignatureStampPreview.tsx
│       ├── PageSelector.tsx
│       ├── StampPositionOverlay.tsx
│       ├── SignButton.tsx
│       ├── SignedPdfDialog.tsx
│       └── SignProgress.tsx
├── hooks/
│   └── usePdfSigning.ts
├── services/
│   └── documentService.ts
├── schemas/
│   └── signDocumentSchemas.ts
├── types/
│   └── document.ts
└── utils/
    └── downloadPdf.ts
```

Adaptar ao padrão existente do projeto.

# Upload

## Aceitar apenas

```text
application/pdf
```

Checklist:

* [ ] validar extensão.
* [ ] validar MIME type.
* [ ] tamanho máximo compatível com backend.
* [ ] rejeitar arquivo vazio.
* [ ] exibir erro amigável.
* [ ] permitir trocar arquivo.

# Preview do PDF

Utilizar:

* react-pdf
* pdf.js

ou equivalente.

Checklist:

* [ ] renderizar páginas.
* [ ] loading.
* [ ] estado vazio.
* [ ] zoom.
* [ ] scroll.
* [ ] navegação entre páginas.

Não:

* [ ] modificar PDF.
* [ ] salvar PDF em localStorage.

# Escolha da página

Permitir:

* [ ] página atual.
* [ ] seletor numérico.
* [ ] navegação anterior/próxima.

Mostrar:

```text
Página 2 de 10
```

# Prévia do selo

Mostrar uma representação semelhante ao selo real.

Deve conter:

* nome do usuário;
* data/hora aproximada;
* hash abreviado;
* identificador.

Atenção:

Essa é apenas uma prévia visual.

O selo verdadeiro será criado no backend.

# Posicionamento

Permitir:

* clique;
* arrastar;
* reposicionar.

Capturar:

* X;
* Y;
* página.

Mostrar:

* bounding box;
* indicação visual do local.

# Coordenadas

Converter corretamente:

coordenadas da tela

↓

coordenadas do PDF

considerando:

* zoom;
* escala;
* resolução;
* scroll.

Checklist:

* [ ] coordenadas corretas.
* [ ] independentes do zoom.
* [ ] independentes do tamanho da tela.

# SignDocumentRequest

Enviar:

```typescript
file
page
positionX
positionY
```

e demais campos esperados pelo backend.

Usar:

* Axios global;
* CSRF;
* cookies;
* AuthContext.

# documentService

Criar:

```text
documentService.ts
```

Funções:

* signDocument()

Sempre usando:

* api.ts

Nunca criar nova instância Axios.

# Loading

Durante assinatura:

Mostrar:

* spinner;
* progresso;
* botão desabilitado.

Mensagem:

```text
Assinando documento...
```

Não permitir múltiplos envios.

# Sucesso

Após resposta:

Receber:

```text
application/pdf
```

Criar Blob.

Exibir:

```text
Documento assinado com sucesso.
```

Disponibilizar:

* download;
* abrir em nova aba.

# Nome do arquivo

Sugestão:

```text
documento-assinado.pdf
```

ou

```text
nome_original_signed.pdf
```

# Download

Implementar utilitário:

```text
downloadPdf.ts
```

Responsável por:

* criar Blob;
* criar URL temporária;
* iniciar download;
* revogar URL.

# Erros

Tratar:

### PDF inválido

```text
Documento PDF inválido.
```

### Página inválida

```text
Página selecionada é inválida.
```

### Coordenadas inválidas

```text
Posição do selo inválida.
```

### Erro de rede

```text
Não foi possível assinar o documento.
```

### 401

Sessão expirada.

### 403

CSRF.

### 429

Rate limit.

### 500

Erro interno.

# TypeScript

Criar:

```text
types/document.ts
```

Modelos:

* SignDocumentRequest
* SignDocumentResponse
* SignatureMetadata

Evitar:

* any
* casts desnecessários

# Validação

Usar Zod.

Arquivo:

```text
signDocumentSchemas.ts
```

Validar:

* PDF obrigatório.
* Página válida.
* Coordenadas válidas.
* Valores negativos.
* Campos vazios.

# Hook

Criar:

```text
usePdfSigning
```

Responsável por:

* loading;
* sucesso;
* erro;
* envio;
* download.

Evitar lógica pesada na página.

# Segurança

Não:

* armazenar PDFs em localStorage.
* logar PDF.
* logar coordenadas sensíveis.
* persistir blobs desnecessariamente.
* assinar no navegador.
* gerar hashes no frontend para substituir backend.

# Estilo

Seguir:

```text
docs/standards/frontend-style.md
```

Inspirar-se em:

* DocuSign;
* Adobe Acrobat;
* Smallpdf.

Priorizar:

* clareza;
* feedback visual;
* loading;
* confirmação;
* UX profissional.

# Acessibilidade

* [ ] teclado.
* [ ] foco.
* [ ] labels.
* [ ] zoom.
* [ ] leitores de tela.

# Testes manuais

### PDF válido

* [ ] upload.
* [ ] preview.
* [ ] mover selo.
* [ ] assinar.
* [ ] download.

### PDF inválido

* [ ] erro amigável.

### Zoom

* [ ] coordenadas continuam corretas.

### Várias páginas

* [ ] selo vai para página correta.

### Repetição

* [ ] botão desabilitado durante loading.

### Arquivo grande

* [ ] feedback adequado.

# Critérios de aceitação

* [ ] Upload funciona.
* [ ] Preview funciona.
* [ ] Navegação entre páginas funciona.
* [ ] Selo pode ser posicionado.
* [ ] Coordenadas são capturadas corretamente.
* [ ] Request é enviado corretamente.
* [ ] PDF assinado é recebido.
* [ ] Download funciona.
* [ ] Loading funciona.
* [ ] Erros são tratados.
* [ ] Nenhum PDF é armazenado no navegador.
* [ ] Não há lógica de assinatura no frontend.

# Fora de escopo

Não implementar:

* verificação pública;
* múltiplas assinaturas;
* drag and resize do selo;
* histórico;
* auditoria;
* carimbo de tempo externo;
* certificado ICP-Brasil;
* armazenamento em nuvem;
* edição do PDF;
* geração de assinatura no frontend.
