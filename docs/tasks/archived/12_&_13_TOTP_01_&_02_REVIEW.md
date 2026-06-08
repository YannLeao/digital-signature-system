# CURRENT_TASK.md — Tarefa Atual

## Status

Concluida.

## Tarefa ativa

### REVIEW-TOTP-01-02 — Revisar ativação e validação de 2FA com TOTP

## Camada

Backend / Segurança / Autenticação / Testes

## Prioridade

Alta

## Objetivo

Revisar a PR que implementa, ao mesmo tempo, as tarefas:

- TOTP-01 — Backend: ativação do 2FA e geração de segredo TOTP
- TOTP-02 — Backend: validação TOTP no fluxo de login e alertas

A revisão deve garantir segurança, aderência arquitetural, integração correta com JWT, criptografia adequada, persistência correta dos backup codes, fluxo de meia-sessão e testes automatizados suficientes.

## Contexto

A PR adicionou endpoints de configuração e verificação do 2FA com TOTP, incluindo:

- geração de segredo TOTP
- armazenamento criptografado do segredo
- backup codes
- fluxo de meia-sessão
- emissão de JWT final apenas após validação TOTP
- bloqueio temporário após tentativas inválidas
- evento de segurança para notificação por e-mail
- integração com envio de e-mail
- migration de banco
- alterações em `pom.xml` e `.env.example`

## Arquivos impactados

```text
backend/src/main/java/.../
├── controller/auth/
│   └── AuthController.java
├── domain/
│   ├── User.java
│   └── TotpBackupCode.java
├── dto/auth/
│   ├── TotpSetupResponse.java
│   ├── TotpVerifyRequest.java
│   └── TotpVerifyResponse.java
├── event/
│   └── TotpLockedEvent.java
├── exception/
│   ├── InvalidTotpException.java
│   └── TotpLockedException.java
├── listener/
│   └── TotpLockedEmailListener.java
├── repository/
│   └── TotpBackupCodeRepository.java
├── security/
│   ├── JwtClaimsFactory.java
│   ├── JwtService.java
│   └── TotpEncryptionService.java
└── service/auth/
    ├── TotpSetupService.java
    └── TotpVerifyService.java

backend/src/main/resources/db/migration/
└── V8__add_totp_to_users.sql

backend/src/test/java/.../service/auth/
└── AuthControllerTests.java
````

Também revisar:

```text
pom.xml
.env.example
```

## Observação arquitetural importante

A PR criou novos pacotes:

```text
event/
listener/
```

Esses pacotes não constavam na arquitetura inicial do backend.

A revisão deve decidir uma das opções:

1. aceitar os pacotes e documentar essa decisão em `docs/architecture/backend.md`;
2. mover para uma estrutura já existente;
3. criar uma estrutura mais clara, como:

```text
event/
listener/
```

ou

```text
notification/
```

desde que a decisão seja consistente, documentada e alinhada ao projeto.

Não deixar pacotes novos sem justificativa arquitetural.

## Checklist de revisão arquitetural

* [ ] Confirmar que controllers não contêm regra de negócio.
* [ ] Confirmar que `TotpSetupService` concentra ativação/configuração.
* [ ] Confirmar que `TotpVerifyService` concentra validação.
* [ ] Confirmar que criptografia fica isolada em serviço próprio.
* [ ] Confirmar que DTOs não expõem entidades.
* [ ] Confirmar que `User` não ficou inchado com lógica de negócio.
* [ ] Confirmar que `TotpBackupCode` está em `domain/`, seguindo padrão atual.
* [ ] Confirmar que repository apenas acessa dados.
* [ ] Confirmar que eventos/listeners foram organizados e documentados.
* [ ] Confirmar uso de constructor injection.
* [ ] Evitar field injection com `@Autowired`.

## Checklist de segurança — TOTP setup

### Endpoint esperado

```text
POST /api/v1/auth/2fa/setup
```

Verificar:

* [ ] Endpoint exige usuário autenticado ou contexto seguro equivalente.
* [ ] Não permite ativar 2FA para outro usuário.
* [ ] Gera segredo TOTP com no mínimo 160 bits.
* [ ] Segredo é retornado apenas no momento do setup.
* [ ] Segredo não é logado.
* [ ] Segredo não é salvo em texto claro.
* [ ] URL `otpauth://` está correta.
* [ ] Issuer/nome da aplicação estão configuráveis.
* [ ] Compatível com Google Authenticator/Authy.

## Checklist de criptografia

### TotpEncryptionService

Verificar:

* [ ] Usa AES-256-GCM ou estratégia equivalente segura.
* [ ] Usa IV/nonce único por criptografia.
* [ ] Não reutiliza IV.
* [ ] Não usa AES ECB.
* [ ] Não hardcoda chave criptográfica.
* [ ] Chave vem de variável de ambiente.
* [ ] `.env.example` documenta a variável sem valor real.
* [ ] Falta de chave causa erro claro no startup.
* [ ] Não loga segredo, chave, IV ou payload sensível.

## Checklist de backup codes

Verificar:

* [ ] Gera 10 backup codes.
* [ ] Codes são fortes e imprevisíveis.
* [ ] Codes são retornados em texto claro apenas uma vez.
* [ ] Codes são armazenados como hash.
* [ ] Codes não são armazenados em texto claro.
* [ ] Codes são de uso único.
* [ ] Após uso, code é invalidado.
* [ ] Não logar backup codes.
* [ ] Não retornar backup codes em endpoints futuros.

## Checklist de validação TOTP

### Endpoint esperado

```text
POST /api/v1/auth/2fa/verify
```

Verificar:

* [ ] Código TOTP tem 6 dígitos.
* [ ] Validação usa janela de 30 segundos.
* [ ] Tolerância máxima de ±1 janela, se prevista.
* [ ] Código fora da janela é rejeitado.
* [ ] Código inválido não emite JWT final.
* [ ] Backup code válido pode ser usado, se implementado no escopo.
* [ ] Backup code usado é invalidado.

## Fluxo de meia-sessão

Esta é uma parte crítica.

Verificar:

* [ ] Login com senha NÃO emite JWT final quando 2FA está ativo.
* [ ] Login com senha emite apenas token temporário/meia-sessão.
* [ ] Token temporário não autoriza chamadas normais à API.
* [ ] Token temporário tem expiração curta.
* [ ] Token temporário só serve para validar TOTP.
* [ ] Após TOTP válido, o JWT final é emitido.
* [ ] Após TOTP válido, refresh token segue padrão JWT-02, se já integrado.
* [ ] Token temporário é invalidado ou deixa de ser aceito.
* [ ] Não existe bypass para obter JWT final sem TOTP.

## Integração com JWT

Verificar alterações em:

```text
JwtClaimsFactory.java
JwtService.java
```

Checklist:

* [ ] Não quebrou JWT-01.
* [ ] Não quebrou JWT-02.
* [ ] Claims obrigatórias continuam presentes.
* [ ] Access Token final continua usando algoritmo assimétrico.
* [ ] Refresh Token continua via cookie HttpOnly.
* [ ] Refresh Token não aparece no JSON.
* [ ] Tokens temporários são claramente diferenciados dos tokens finais.
* [ ] Tokens temporários não possuem permissões de API completa.

## Bloqueio por tentativas inválidas

Verificar:

* [ ] Tentativas TOTP inválidas são contadas.
* [ ] Após limite configurado, usuário/fluxo é bloqueado temporariamente.
* [ ] Tempo de bloqueio está claro.
* [ ] Erro público não expõe detalhes sensíveis.
* [ ] Bloqueio não permite brute force.
* [ ] Sucesso reseta contador de tentativas, se aplicável.
* [ ] Estado de bloqueio persiste corretamente.

## Eventos e listener de e-mail

Arquivos:

```text
TotpLockedEvent.java
TotpLockedEmailListener.java
```

Verificar:

* [ ] Evento não carrega dados sensíveis desnecessários.
* [ ] Listener não bloqueia fluxo principal de autenticação, se possível.
* [ ] Falha no envio de e-mail não deve quebrar autenticação de forma indevida.
* [ ] Listener está anotado/configurado corretamente.
* [ ] Não há dependência circular.
* [ ] Pacotes `event/` e `listener/` estão documentados ou reorganizados.
* [ ] Integração com serviço de e-mail não hardcoda credenciais.
* [ ] `.env.example` foi atualizado corretamente.

## Migration V8

Arquivo:

```text
V8__add_totp_to_users.sql
```

Verificar:

* [ ] Adiciona campos necessários em `users`.
* [ ] Cria tabela de backup codes, se aplicável.
* [ ] Usa nomes consistentes.
* [ ] Não armazena segredo TOTP em texto claro.
* [ ] Campos sensíveis permitem armazenamento criptografado.
* [ ] Índices e FKs estão corretos.
* [ ] Migration roda em banco limpo.
* [ ] Migration não depende de alteração manual.
* [ ] Não quebra usuários existentes.

## Revisão do User.java

Verificar:

* [ ] Campos TOTP não expõem segredo em serialização.
* [ ] Não adicionou lógica de negócio à entidade.
* [ ] Não quebrou autenticação existente.
* [ ] Não criou relacionamento com fetch inadequado.
* [ ] Não expõe backup codes acidentalmente.

## Tratamento de exceções

Arquivos:

```text
InvalidTotpException.java
TotpLockedException.java
```

Verificar:

* [ ] Exceções são tratadas pelo GlobalExceptionHandler.
* [ ] Erros seguem padrão global.
* [ ] Não expõem se código estava perto/correto.
* [ ] Não expõem detalhes criptográficos.
* [ ] Status HTTP adequado.
* [ ] Mensagem pública é segura.

## Testes automatizados

Atualmente foi modificado:

```text
AuthControllerTests.java
```

Verificar se isso é suficiente.

### Testes recomendados obrigatórios

* [ ] Setup de TOTP retorna `otpauth://`.
* [ ] Setup gera backup codes uma única vez.
* [ ] Segredo é armazenado criptografado.
* [ ] Segredo não aparece em texto claro no banco.
* [ ] Verificação com TOTP válido emite JWT final.
* [ ] Verificação com TOTP inválido não emite JWT.
* [ ] Token temporário não acessa endpoint protegido comum.
* [ ] Login com 2FA ativo não emite JWT final antes do TOTP.
* [ ] Backup code válido funciona, se implementado.
* [ ] Backup code usado não funciona novamente.
* [ ] Tentativas inválidas repetidas bloqueiam temporariamente.
* [ ] Evento `TotpLockedEvent` é publicado no bloqueio.
* [ ] Listener de e-mail não quebra fluxo em caso de falha.
* [ ] JWT-01/JWT-02 continuam funcionando.
* [ ] AUTH-01/AUTH-02 continuam funcionando.

## Possíveis novos testes a criar

Considerar criar testes específicos:

```text
TotpSetupServiceTests.java
TotpVerifyServiceTests.java
TotpEncryptionServiceTests.java
TotpBackupCodeRepositoryTests.java
```

Não concentrar tudo em `AuthControllerTests` se isso deixar os testes frágeis ou superficiais.

## Revisão de pom.xml

Verificar:

* [ ] Dependências TOTP são adequadas.
* [ ] Dependências de criptografia são adequadas.
* [ ] Não há dependências abandonadas.
* [ ] Não há bibliotecas duplicadas.
* [ ] Não há versões hardcoded desnecessárias.
* [ ] Não houve downgrade acidental.
* [ ] Dependências novas são realmente usadas.

## Revisão do .env.example

Verificar se documenta:

* [ ] chave de criptografia do TOTP
* [ ] issuer/nome da aplicação
* [ ] configurações de e-mail, se usadas
* [ ] nenhuma credencial real
* [ ] comentários claros para desenvolvedores

## Critérios de aceitação

* [ ] Setup TOTP funciona corretamente.
* [ ] Segredo TOTP é criptografado em repouso.
* [ ] Backup codes são gerados e armazenados como hash.
* [ ] Login com 2FA ativo usa fluxo de meia-sessão.
* [ ] JWT final só é emitido após TOTP válido.
* [ ] TOTP inválido não emite JWT.
* [ ] Tentativas inválidas repetidas bloqueiam temporariamente.
* [ ] Evento de bloqueio é publicado corretamente.
* [ ] Pacotes `event/` e `listener/` foram aceitos/documentados ou reorganizados.
* [ ] Erros seguem padrão global.
* [ ] Não há vazamento de segredo, backup codes, tokens ou chaves.
* [ ] Testes automatizados foram revisados e ampliados.
* [ ] AUTH-01, AUTH-02, JWT-01, JWT-02 e JWT-03 continuam funcionando.

## Fora de escopo

Não implementar nesta review:

* UI de ativação 2FA.
* QR Code no frontend.
* Tela de verificação TOTP.
* Gerenciamento de dispositivos.
* Desativação de 2FA.
* Auditoria completa.
* Painel de sessões.
* CSRF.
* Redis.
* Mudança ampla de arquitetura fora do necessário.

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
