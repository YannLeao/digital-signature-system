# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### PASSKEY-03 — Frontend: UI de registro, autenticação e gerenciamento de passkeys

## Camada

Frontend / Segurança / WebAuthn / UI

## Prioridade

Alta

## Objetivo

Implementar a interface frontend para passkeys usando WebAuthn, priorizando Conditional UI/autofill no login e botões explícitos apenas para cadastro e gerenciamento de passkeys em área autenticada.

## Decisão de UX

A autenticação com passkey NÃO deve ser apresentada como um botão principal separado competindo com o login por e-mail e senha.

A tela de login deve priorizar:

- campo de e-mail com suporte a WebAuthn Conditional UI/autofill;
- fluxo natural de autenticação;
- passkey integrada ao login existente;
- fallback claro para e-mail e senha.

Botões explícitos devem ser usados principalmente em área autenticada, para:

- adicionar passkey;
- listar dispositivos;
- revogar passkey.

## Contexto

O backend já possui endpoints relacionados a passkeys:

- registro de credencial;
- autenticação com passkey;
- validação de counter;
- integração com JWT/Refresh Token;
- erros padronizados.

O frontend já possui:

- Axios global;
- CSRF integrado;
- Refresh Token via cookie HttpOnly;
- Access Token em memória;
- AuthContext;
- telas iniciais de login/cadastro;
- padrões definidos em `docs/architecture/frontend.md`;
- estilo definido em `docs/standards/frontend-style.md`.

## Referências obrigatórias

Consultar antes de implementar:

- `docs/ai/AGENTS.md`
- `docs/ai/CONTEXT.md`
- `docs/architecture/frontend.md`
- `docs/standards/frontend-style.md`
- `docs/security/security-overview.md`
- `docs/standards/api.md`

## Objetivo funcional

Implementar:

1. suporte a WebAuthn Conditional UI/autofill na tela de login;
2. registro de passkey em área autenticada;
3. listagem de passkeys registradas;
4. revogação de passkey com confirmação;
5. tratamento claro de erros e incompatibilidade do navegador.

## Estrutura recomendada

```text
frontend/src/
├── pages/
│   └── security/
│       └── PasskeysPage.tsx
├── components/
│   └── passkeys/
│       ├── PasskeyList.tsx
│       ├── PasskeyItem.tsx
│       ├── AddPasskeyButton.tsx
│       └── RevokePasskeyDialog.tsx
├── services/
│   └── passkeyService.ts
├── hooks/
│   └── usePasskeys.ts
├── types/
│   └── passkey.ts
└── utils/
    └── webauthn.ts
````

Adaptar ao padrão real existente do projeto.

## Tela de login

### Conditional UI/autofill

* [ ] Integrar passkey ao fluxo de login existente.
* [ ] Usar WebAuthn Conditional UI quando disponível.
* [ ] Campo de e-mail deve permitir autocomplete adequado.
* [ ] Evitar botão principal “Entrar com Passkey” competindo com e-mail/senha.
* [ ] Manter login por e-mail/senha como fallback.
* [ ] Não criar UX confusa com múltiplos CTAs principais.

### Comportamento esperado

* [ ] Ao abrir a tela de login, verificar suporte a WebAuthn.
* [ ] Se Conditional UI estiver disponível, preparar autenticação passkey de forma discreta.
* [ ] Se o usuário selecionar uma passkey pelo autofill, finalizar autenticação.
* [ ] Após sucesso, atualizar AuthContext com Access Token.
* [ ] Refresh Token continua via cookie HttpOnly.
* [ ] Redirecionar para área autenticada.

## Área autenticada de segurança

Criar ou atualizar página de segurança do usuário.

Rota sugerida:

```text
/security/passkeys
```

Funcionalidades:

* [ ] listar passkeys registradas;
* [ ] adicionar nova passkey;
* [ ] revogar passkey existente;
* [ ] exibir estado vazio;
* [ ] exibir loading;
* [ ] exibir erros amigáveis.

## Registro de passkey

Fluxo esperado:

1. usuário autenticado acessa área de segurança;
2. clica em “Adicionar passkey”;
3. frontend chama endpoint de start;
4. frontend chama `navigator.credentials.create`;
5. frontend envia resposta para endpoint de finish;
6. lista é atualizada.

Checklist:

* [ ] Implementar `register/start`.
* [ ] Implementar `register/finish`.
* [ ] Converter dados WebAuthn entre Base64URL e ArrayBuffer corretamente.
* [ ] Tratar cancelamento pelo usuário.
* [ ] Tratar navegador incompatível.
* [ ] Tratar autenticador indisponível.
* [ ] Tratar erro do backend.
* [ ] Não logar payloads sensíveis.

## Autenticação com passkey

Fluxo esperado:

1. tela de login inicializa suporte a Conditional UI;
2. frontend chama endpoint de `auth/start`;
3. navegador oferece credencial via autofill;
4. frontend chama `navigator.credentials.get`;
5. frontend envia resposta para `auth/finish`;
6. backend retorna Access Token e cookie de Refresh Token;
7. frontend atualiza AuthContext.

Checklist:

* [ ] Implementar `auth/start`.
* [ ] Implementar `auth/finish`.
* [ ] Usar `navigator.credentials.get`.
* [ ] Preferir Conditional UI/autofill.
* [ ] Não depender de botão principal separado.
* [ ] Atualizar Access Token em memória.
* [ ] Não acessar Refresh Token via JS.
* [ ] Redirecionar após sucesso.

## Listagem de passkeys

A listagem deve exibir, se disponível:

* [ ] nome/apelido do dispositivo;
* [ ] data de criação;
* [ ] último uso;
* [ ] botão de revogar.

Não exibir:

* public key;
* credential raw completo;
* dados internos sensíveis.

## Revogação

* [ ] Exigir confirmação antes de revogar.
* [ ] Chamar endpoint de revogação.
* [ ] Atualizar lista após sucesso.
* [ ] Tratar erro.
* [ ] Evitar revogar silenciosamente.

## WebAuthn utilities

Criar utilitários para:

* [ ] detectar suporte a WebAuthn;
* [ ] detectar suporte a Conditional UI, se possível;
* [ ] converter Base64URL para ArrayBuffer;
* [ ] converter ArrayBuffer para Base64URL;
* [ ] montar options para `navigator.credentials.create`;
* [ ] montar options para `navigator.credentials.get`;
* [ ] normalizar response para backend.

## TypeScript

Criar tipos para:

* [ ] `PasskeyDevice`;
* [ ] `PasskeyRegisterStartResponse`;
* [ ] `PasskeyRegisterFinishRequest`;
* [ ] `PasskeyAuthStartResponse`;
* [ ] `PasskeyAuthFinishRequest`;
* [ ] `PasskeyAuthResponse`.

Evitar:

* [ ] `any`;
* [ ] casts excessivos;
* [ ] tipagem solta em objetos WebAuthn.

## Services

Criar `passkeyService.ts` usando Axios global.

Funções esperadas:

* [ ] `startPasskeyRegistration`;
* [ ] `finishPasskeyRegistration`;
* [ ] `startPasskeyAuthentication`;
* [ ] `finishPasskeyAuthentication`;
* [ ] `listPasskeys`;
* [ ] `revokePasskey`.

Garantir:

* [ ] usa `api.ts`;
* [ ] usa CSRF automaticamente;
* [ ] usa cookies com `withCredentials`;
* [ ] não cria nova instância Axios.

## Hooks

Criar `usePasskeys`, se fizer sentido.

Responsabilidades:

* [ ] carregar lista;
* [ ] expor loading/error;
* [ ] adicionar passkey;
* [ ] revogar passkey;
* [ ] evitar duplicar lógica em componentes.

## Tratamento de erros

Tratar:

* [ ] navegador sem suporte;
* [ ] Conditional UI indisponível;
* [ ] usuário cancelou;
* [ ] credencial não encontrada;
* [ ] autenticador inválido;
* [ ] erro de counter/clonagem;
* [ ] erro 401;
* [ ] erro 403 CSRF;
* [ ] erro 429;
* [ ] erro de rede.

Mensagens devem ser amigáveis e seguras.

Não exibir:

* stack trace;
* detalhes criptográficos;
* payload WebAuthn bruto;
* credential ID completo.

## Segurança frontend

* [ ] Não armazenar dados de passkey em localStorage.
* [ ] Não armazenar payloads WebAuthn.
* [ ] Não logar credential ID, public key ou challenges.
* [ ] Não criar token fake.
* [ ] Não acessar Refresh Token.
* [ ] Não duplicar lógica de autenticação fora do AuthContext.
* [ ] Não usar passkey como bypass de TOTP se backend exigir segundo fator.

## Estilo visual

Seguir obrigatoriamente:

```text
docs/standards/frontend-style.md
```

Diretrizes:

* [ ] UI limpa e profissional;
* [ ] consistência com login/cadastro existentes;
* [ ] passkeys apresentadas como recurso de segurança;
* [ ] mensagens claras;
* [ ] estados de loading;
* [ ] estados vazios;
* [ ] confirmação de ação destrutiva.

## Acessibilidade

* [ ] Labels corretos.
* [ ] Botões com texto claro.
* [ ] Modais acessíveis.
* [ ] Navegação por teclado.
* [ ] Feedback compreensível para erro/sucesso.

## Critérios de aceitação

* [ ] Login suporta WebAuthn Conditional UI/autofill.
* [ ] Não há botão principal separado “Entrar com Passkey” competindo com login normal.
* [ ] Registro de passkey funciona em área autenticada.
* [ ] Lista de passkeys é exibida.
* [ ] Revogação funciona com confirmação.
* [ ] Browser sem suporte recebe mensagem adequada.
* [ ] Cancelamento pelo usuário é tratado.
* [ ] Access Token é salvo apenas em memória/AuthContext.
* [ ] Refresh Token não é acessado pelo frontend.
* [ ] CSRF funciona automaticamente via Axios global.
* [ ] Código segue `docs/architecture/frontend.md`.
* [ ] Estilo segue `docs/standards/frontend-style.md`.
* [ ] Não há dados sensíveis em logs/storage.

## Testes manuais recomendados

* [ ] Abrir tela de login em navegador compatível.
* [ ] Verificar se autofill/Conditional UI aparece quando houver passkey.
* [ ] Fazer login com passkey.
* [ ] Fazer login normal por e-mail/senha.
* [ ] Acessar área de segurança autenticada.
* [ ] Adicionar passkey.
* [ ] Cancelar criação de passkey e verificar mensagem.
* [ ] Listar passkeys.
* [ ] Revogar passkey.
* [ ] Tentar usar passkey revogada.
* [ ] Testar em navegador sem suporte.
* [ ] Conferir DevTools para garantir que nada sensível foi salvo em storage.

## Fora de escopo

Não implementar nesta tarefa:

* Backend de passkeys.
* Alterações em JWT.
* Alterações em CSRF.
* TOTP frontend.
* Auditoria completa.
* Notificações por e-mail.
* Design final de dashboard.
* Sessões ativas completas.
* Redis.
* Recuperação de conta.
