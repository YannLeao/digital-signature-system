# Security Overview

Este projeto deve ser tratado como um sistema sensível.

## Áreas críticas

- Autenticação
- Senhas
- Tokens JWT
- Refresh tokens
- Cookies
- CSRF
- Passkeys/WebAuthn
- TOTP/2FA
- Assinatura digital de PDFs
- Validação de PDFs
- Logs de auditoria
- Sessões
- Headers de segurança
- CORS

## Princípios

- Nunca armazenar senha em texto claro.
- Nunca expor chave privada.
- Nunca expor stack trace ao usuário.
- Nunca diferenciar usuário inexistente de senha incorreta.
- Nunca usar wildcard `*` em CORS autenticado.
- Nunca armazenar refresh token em localStorage.
- Nunca aceitar PDF apenas por extensão de arquivo.
- Nunca salvar PDF enviado em disco como solução padrão.

## Autenticação

Senhas devem usar Argon2id.

O login deve retornar erro genérico para credenciais inválidas.

Proteções contra brute force devem incluir bloqueio temporário e rate limiting.

## Tokens

Access tokens devem ter expiração curta.

Refresh tokens devem ser opacos, armazenados como hash e enviados por cookie HttpOnly.

A rotação de refresh token deve detectar reuso.

## PDFs

A assinatura precisa ser criptográfica e embutida no PDF.

Selo visual não é prova suficiente de autenticidade.

A verificação pública deve validar assinatura e integridade.

## Auditoria

Eventos sensíveis devem gerar logs append-only.

Logs não devem expor segredos.
