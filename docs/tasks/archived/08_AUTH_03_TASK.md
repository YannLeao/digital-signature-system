# CURRENT_TASK.md — Tarefa Atual

## Status

Em desenvolvimento.

## Tarefa ativa

### AUTH-03 — Tela de cadastro e login no frontend

## Camada

Frontend

## Prioridade

Alta

## Objetivo

Implementar as telas iniciais de cadastro e login no frontend, seguindo a arquitetura já definida do projeto, com validação client-side, integração com os endpoints de autenticação existentes e estilo visual conforme `docs/standard/frontend-style.md`.

## Contexto

O backend já possui:

- cadastro de usuário com Argon2id
- login com validação segura
- mensagem genérica para credenciais inválidas
- bloqueio temporário por tentativas falhas
- rate limiting por IP
- padrão global de erros

O frontend já possui:

- React + Vite + TypeScript
- TailwindCSS
- estrutura inicial de pastas
- roteamento base
- `PrivateRoute`
- configuração inicial de ambiente

Agora será implementado o primeiro fluxo real de interface relacionado à autenticação.

## Referência obrigatória de estilo

Antes de implementar UI, consultar:

```text
docs/standard/frontend-style.md
````

As telas devem seguir esse guia visual.

Caso exista conflito entre preferência visual e funcionalidade/segurança, priorizar funcionalidade e segurança.

## Objetivo funcional

Criar as seguintes telas:

```text
/register
/login
```

Com:

* formulário de cadastro
* formulário de login
* validação client-side
* mensagens de erro seguras
* integração com API
* redirecionamento após sucesso

## Checklist de implementação

### Rotas

* [ ] Criar rota pública `/register`.
* [ ] Criar rota pública `/login`.
* [ ] Garantir que rotas públicas não passem por `PrivateRoute`.
* [ ] Manter rotas privadas protegidas pelo fluxo já existente.

### Estrutura recomendada

Criar ou organizar arquivos seguindo a arquitetura do frontend:

```text
src/
├── pages/
│   └── auth/
│       ├── LoginPage.tsx
│       └── RegisterPage.tsx
├── components/
│   └── auth/
│       ├── AuthCard.tsx
│       ├── PasswordStrengthIndicator.tsx
│       └── AuthFormField.tsx
├── services/
│   └── authService.ts
├── schemas/
│   └── authSchemas.ts
├── types/
│   └── auth.ts
└── routes/
```

A estrutura pode ser adaptada ao padrão já existente, mas deve manter separação clara de responsabilidades.

## Arquitetura e responsabilidades

### Pages

As páginas devem:

* montar a tela
* conectar formulário, estado de carregamento e navegação
* chamar services/hooks
* não conter lógica de validação complexa espalhada

### Components

Componentes devem:

* ser reutilizáveis quando fizer sentido
* focar em apresentação
* receber dados por props
* evitar chamadas diretas à API

### Services

`authService` deve:

* centralizar chamadas HTTP de autenticação
* usar cliente Axios já existente, se houver
* expor funções como:

  * `registerUser`
  * `loginUser`
* não conter lógica visual

### Schemas

Schemas com Zod devem:

* validar cadastro
* validar login
* conter a política de senha do frontend
* manter mensagens claras para o usuário

### Types

Types devem:

* definir payloads de request
* definir responses esperadas
* evitar uso de `any`

## Tela de cadastro

### Campos

* [ ] Nome, se já previsto pelo backend.
* [ ] E-mail.
* [ ] Senha.
* [ ] Confirmação de senha.

> Se o backend ainda não aceitar `name`, não enviar esse campo para a API. Nesse caso, manter o campo fora da tela ou deixar explicitamente fora de escopo.

### Validações

* [ ] E-mail obrigatório e válido.
* [ ] Senha obrigatória.
* [ ] Senha com mínimo de 12 caracteres.
* [ ] Senha com letra maiúscula.
* [ ] Senha com letra minúscula.
* [ ] Senha com número.
* [ ] Senha com símbolo.
* [ ] Confirmação de senha igual à senha.

### Indicador de força da senha

* [ ] Criar indicador visual de força da senha.
* [ ] Mostrar critérios atendidos/não atendidos.
* [ ] Não substituir validação real por indicador visual.
* [ ] O backend continua sendo a fonte final de validação.

## Tela de login

### Campos

* [ ] E-mail.
* [ ] Senha.

### Comportamento

* [ ] Ao submeter, chamar endpoint de login.
* [ ] Exibir estado de carregamento.
* [ ] Em caso de sucesso, redirecionar para próxima etapa prevista.
* [ ] Como JWT definitivo ainda não foi implementado, não inventar persistência final de sessão.
* [ ] Se ainda não houver dashboard real, redirecionar para uma rota segura provisória já existente ou exibir mensagem controlada de sucesso.

## Mensagens de erro

### Regra crítica

A UI nunca deve diferenciar:

* usuário inexistente
* senha incorreta
* conta bloqueada

Mensagem esperada:

```text
Credenciais inválidas.
```

### Tratamento de erros da API

* [ ] Ler formato padronizado de erro do backend.
* [ ] Exibir mensagens amigáveis.
* [ ] Não exibir stack trace.
* [ ] Não exibir detalhes técnicos.
* [ ] Tratar `400`.
* [ ] Tratar `401`.
* [ ] Tratar `429`.
* [ ] Tratar erro de rede.

### Rate limiting

Para `429 Too Many Requests`:

* [ ] Exibir mensagem amigável.
* [ ] Não revelar detalhes internos.
* [ ] Opcionalmente orientar o usuário a tentar novamente em instantes.

## Integração com backend

Endpoints esperados:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
```

A base da API deve vir de:

```text
VITE_API_BASE_URL
```

Não hardcodar URL da API nos componentes.

## Segurança frontend

* [ ] Não armazenar senha em localStorage.
* [ ] Não armazenar senha em sessionStorage.
* [ ] Não logar senha no console.
* [ ] Não logar payloads sensíveis.
* [ ] Não simular autenticação como solução final.
* [ ] Não criar JWT falso.
* [ ] Não criar token fake.
* [ ] Não persistir estado de autenticação definitivo ainda.

## Boas práticas de formulário

* [ ] Usar `react-hook-form`.
* [ ] Usar `zod`.
* [ ] Usar `@hookform/resolvers`.
* [ ] Desabilitar botão durante submit.
* [ ] Evitar duplo submit.
* [ ] Exibir erro próximo ao campo quando for validação client-side.
* [ ] Exibir erro geral quando for erro da API.
* [ ] Manter acessibilidade básica:

  * [ ] labels associados aos inputs
  * [ ] mensagens compreensíveis
  * [ ] foco e navegação por teclado

## Estilo visual

* [ ] Seguir `docs/standard/frontend-style.md`.
* [ ] Manter consistência entre login e cadastro.
* [ ] Criar layout limpo, responsivo e profissional.
* [ ] Evitar excesso de efeitos visuais.
* [ ] Priorizar legibilidade, hierarquia visual e clareza.

## Critérios de aceitação

* [ ] `/register` renderiza corretamente.
* [ ] `/login` renderiza corretamente.
* [ ] Cadastro válido chama backend corretamente.
* [ ] Cadastro com senha fraca é bloqueado no frontend antes do submit.
* [ ] Cadastro com senhas diferentes exibe erro.
* [ ] Login válido chama backend corretamente.
* [ ] Login inválido exibe mensagem genérica.
* [ ] Erro `429` é tratado de forma amigável.
* [ ] API base usa `VITE_API_BASE_URL`.
* [ ] Nenhuma senha é salva em storage.
* [ ] Nenhum token fake é criado.
* [ ] Código segue separação entre page, component, service, schema e type.
* [ ] UI segue `docs/standard/frontend-style.md`.

## Testes manuais recomendados

* [ ] Abrir `/register`.
* [ ] Tentar cadastrar com e-mail inválido.
* [ ] Tentar cadastrar com senha fraca.
* [ ] Tentar cadastrar com confirmação diferente.
* [ ] Cadastrar usuário válido.
* [ ] Abrir `/login`.
* [ ] Logar com usuário válido.
* [ ] Logar com senha incorreta.
* [ ] Logar com usuário inexistente.
* [ ] Confirmar que ambos exibem mensagem genérica.
* [ ] Simular backend fora do ar.
* [ ] Simular resposta `429`.
* [ ] Conferir responsividade básica.

## Fora de escopo

Não implementar ainda:

* JWT definitivo.
* Refresh token.
* Persistência real de sessão.
* TOTP.
* Passkeys.
* Recuperação de senha.
* Dashboard completo.
* Logout real.
* CSRF.
* Perfil de usuário.
* Gerenciamento de sessões.
* Auditoria no frontend.

## Arquivos relevantes

Consulte antes de implementar:

* `docs/ai/AGENTS.md`
* `docs/ai/CONTEXT.md`
* `docs/architecture/frontend.md`
* `docs/standard/frontend-style.md`
* `docs/security/security-overview.md`
* `docs/standards/api.md`
* `docs/standards/coding.md`
* `docs/product/roadmap.md`
