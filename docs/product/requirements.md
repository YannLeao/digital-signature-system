# Requirements

Este arquivo centraliza requisitos funcionais e não funcionais.

No momento, os requisitos detalhados ainda estão no roadmap completo original.

Quando um épico entrar em desenvolvimento, mover os requisitos relacionados para este arquivo ou para um arquivo específico do domínio.

## Requisitos de autenticação

- Senhas fortes.
- Hash seguro com Argon2id.
- Mensagem genérica para credenciais inválidas.
- Bloqueio temporário após tentativas falhas.
- Rate limiting em login.

## Requisitos de segurança

- JWT assinado com algoritmo seguro.
- Refresh token opaco, rotacionável e armazenado como hash.
- Cookie HttpOnly para refresh token.
- Proteção CSRF quando houver cookies.
- CORS restritivo.
- Headers HTTP de segurança.

## Requisitos de PDF

- Assinatura criptográfica real embutida no PDF.
- Verificação por assinatura e hash.
- PDF processado em memória.
- Validação de tipo, tamanho e conteúdo perigoso.

## Requisitos de auditoria

- Log append-only.
- Registro de eventos sensíveis.
- Sessões ativas revogáveis.
- Notificações por e-mail em eventos críticos.
