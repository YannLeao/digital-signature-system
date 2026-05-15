# Épicos do Produto

## EPIC-00 — Setup & Infraestrutura Base

Inicialização dos projetos, configuração de ambiente, banco de dados e pipeline base.

Deve ser concluído antes dos épicos funcionais.

## EPIC-01 — Autenticação com Login e Senha

Cadastro, login, política de senha forte, bloqueio por tentativas e mensagens seguras.

## EPIC-02 — Passkeys / WebAuthn

Registro e autenticação via WebAuthn, com validação de counter contra clonagem.

## EPIC-03 — TOTP / 2FA

Ativação de autenticação de dois fatores com TOTP, QR Code, backup codes e validação no login.

## EPIC-04 — JWT e Refresh Tokens

Access token curto, refresh token opaco com rotação, cookie HttpOnly e denylist pós-logout.

## EPIC-05 — CORS e Headers de Segurança

Configuração de CORS restritivo e headers HTTP obrigatórios.

## EPIC-06 — Assinatura Digital de PDFs

Geração de chaves, assinatura criptográfica embutida no PDF, selo visual e registro no banco.

## EPIC-07 — Verificação Pública de Autenticidade

Endpoint e tela pública para verificar assinatura, integridade e autenticidade de PDFs.

## EPIC-08 — Sessões, Auditoria e Notificações

Sessões ativas, revogação, logs append-only e notificações por e-mail para eventos sensíveis.

## EPIC-09 — CSRF e Segurança Complementar

Proteção CSRF, auditoria de dependências e reforços finais de segurança.
