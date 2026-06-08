# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### TOTP-03 — Frontend: UI de ativação e verificação do 2FA com TOTP

## Camada

Frontend / Segurança / Autenticação / UI

## Prioridade

Alta

## Objetivo

Implementar a experiência completa de autenticação em duas etapas utilizando TOTP, seguindo o fluxo adotado por provedores modernos como Google, GitHub e Microsoft.

A ativação do 2FA deve ser explícita e composta por duas etapas:

1. geração do segredo TOTP e QR Code;
2. confirmação da configuração por meio de um código válido.

Somente após essa confirmação o 2FA será considerado ativo.

## Referências obrigatórias

Consultar antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/frontend.md`
* `docs/standards/frontend-style.md`
* `docs/security/security-overview.md`
* endpoints já implementados no backend.

## Objetivos funcionais

Implementar:

### Área autenticada

* ativação do 2FA;
* QR Code;
* configuração manual;
* confirmação da ativação;
* exibição dos backup codes;
* download dos backup codes;
* cópia dos backup codes.

### Fluxo de login

Criar etapa intermediária:

```text
Login (e-mail + senha)
↓
Verificação TOTP
↓
Dashboard
```

## Estrutura recomendada

```text
frontend/src/
├── pages/
│   └── security/
│       ├── TwoFactorPage.tsx
│       └── TotpVerifyPage.tsx
├── components/
│   └── totp/
│       ├── TotpSetupCard.tsx
│       ├── TotpQrCode.tsx
│       ├── TotpSecretField.tsx
│       ├── TotpCodeInput.tsx
│       ├── BackupCodesCard.tsx
│       └── UseBackupCodeDialog.tsx
├── services/
│   └── totpService.ts
├── hooks/
│   └── useTotp.ts
├── schemas/
│   └── totpSchemas.ts
├── types/
│   └── totp.ts
└── utils/
    └── downloadBackupCodes.ts
```

# Área de Segurança

## Página

Rota sugerida:

```text
/settings/security
```

ou

```text
/settings/2fa
```

Deve permitir:

* visualizar estado do 2FA;
* iniciar configuração;
* exibir QR Code;
* confirmar configuração;
* visualizar backup codes;
* regenerar backup codes (se existir endpoint);
* desativar 2FA futuramente.

# Setup do TOTP

Fluxo:

```text
Usuário autenticado
↓
Clica em "Ativar autenticação em duas etapas"
↓
Backend retorna:
- otpauth://
- secret
↓
Renderizar QR Code
↓
Usuário escaneia
↓
Usuário informa código TOTP
↓
Backend valida
↓
2FA é ativado
↓
Backup codes são exibidos
```

## Regras obrigatórias

* [ ] QR Code gerado a partir da URL `otpauth://`.
* [ ] Exibir segredo manual em texto.
* [ ] Não ativar 2FA imediatamente após setup.
* [ ] Exigir confirmação por código TOTP.
* [ ] Não exibir backup codes antes da confirmação.
* [ ] Não exibir backup codes novamente posteriormente.

# QR Code

Utilizar:

* `qrcode.react`

ou equivalente.

Requisitos:

* [ ] QR Code legível.
* [ ] Compatível com Google Authenticator.
* [ ] Compatível com Microsoft Authenticator.
* [ ] Compatível com Authy.

# Segredo manual

Exibir:

```text
ABCDE-FGHIJ-KLMNO
```

ou equivalente.

Permitir:

* [ ] copiar segredo.
* [ ] configuração manual caso a câmera não funcione.

Não:

* [ ] logar segredo.
* [ ] armazenar segredo em localStorage.
* [ ] persistir segredo fora do fluxo.

# Confirmação da ativação

Após escanear:

Usuário deve digitar um código de 6 dígitos.

Somente após validação:

* [ ] marcar 2FA como ativo.
* [ ] exibir backup codes.

# Backup Codes

Exibir:

* aviso de exibição única;
* destaque visual;
* instruções de armazenamento.

Permitir:

### Copiar

```text
Copiar todos os códigos
```

### Download

Gerar:

```text
backup-codes.txt
```

Conteúdo:

```text
Backup Codes

XXXXX-XXXXX
XXXXX-XXXXX
XXXXX-XXXXX
...
```

## Segurança

* [ ] não armazenar backup codes em localStorage.
* [ ] não salvar em contexto global.
* [ ] não logar.
* [ ] remover da memória após sair da tela.

# Tela intermediária do login

Criar página:

```text
/two-factor
```

Fluxo:

```text
email + senha válidos
↓
half-session
↓
Tela TOTP
↓
JWT final
↓
Dashboard
```

## Não fazer

Não:

* abrir modal;
* misturar com tela de login;
* usar popup.

Deve ser uma página própria.

# Campo TOTP

6 dígitos.

Suportar:

* [ ] digitação individual;
* [ ] colagem de 6 dígitos;
* [ ] preenchimento automático do iOS e Android;
* [ ] teclado numérico;
* [ ] envio automático ao completar.

## Componente

```text
TotpCodeInput
```

Responsável por:

* foco automático;
* mover cursor;
* aceitar paste;
* controlar loading.

# Código de recuperação

Adicionar:

```text
Usar código de recuperação
```

Ao clicar:

abrir formulário alternativo.

Permitir:

* [ ] inserir backup code;
* [ ] autenticar;
* [ ] continuar fluxo normalmente.

# Services

Criar:

```text
totpService.ts
```

Funções:

* startTotpSetup
* verifyTotpSetup
* verifyTotpLogin
* verifyBackupCode
* getTotpStatus

Utilizar sempre:

* Axios global;
* CSRF;
* cookies;
* AuthContext.

# TypeScript

Criar:

```text
types/totp.ts
```

Modelos:

* TotpSetupResponse
* TotpVerifyRequest
* TotpVerifyResponse
* BackupCodesResponse
* BackupCodeVerifyRequest

Evitar:

* any
* tipagem solta

# Validação

Usar:

```text
schemas/totpSchemas.ts
```

com Zod.

Validar:

* código com exatamente 6 dígitos;
* backup code;
* inputs vazios;
* caracteres inválidos.

# Tratamento de erros

Tratar:

* código inválido;
* código expirado;
* backup code inválido;
* bloqueio temporário;
* erro de rede;
* erro 429;
* erro 403;
* half-session expirada.

Não expor:

* detalhes criptográficos;
* stack trace;
* informações internas.

# Estilo

Seguir obrigatoriamente:

```text
docs/standards/frontend-style.md
```

Priorizar:

* aparência semelhante ao GitHub;
* clareza;
* passos explícitos;
* UX segura;
* estados de loading;
* estados vazios;
* feedback visual de sucesso.

# Segurança

Não:

* salvar segredo TOTP em storage;
* salvar backup codes em storage;
* logar códigos;
* manter códigos em memória após sair da página;
* permitir bypass do fluxo intermediário.

# Critérios de aceitação

* [ ] QR Code funcional.
* [ ] Segredo manual disponível.
* [ ] 2FA só é ativado após código válido.
* [ ] Backup codes aparecem apenas após ativação.
* [ ] Backup codes possuem aviso de exibição única.
* [ ] Backup codes podem ser copiados.
* [ ] Backup codes podem ser baixados em `.txt`.
* [ ] Tela intermediária de TOTP existe.
* [ ] Campo suporta colagem.
* [ ] Campo suporta preenchimento automático.
* [ ] Envio automático funciona.
* [ ] Opção "Usar código de recuperação" funciona.
* [ ] Fluxo completo:

```text
Login
↓
TOTP
↓
Dashboard
```

funciona corretamente.

# Fora de escopo

Não implementar:

* WebAuthn;
* Passkeys;
* Sessões ativas;
* Auditoria;
* Notificações;
* Desativação do 2FA (se não existir endpoint);
* Regeneração de backup codes (se não existir endpoint).
