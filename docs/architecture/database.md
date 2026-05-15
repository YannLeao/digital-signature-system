# Arquitetura de Banco de Dados

## Banco principal

PostgreSQL.

## Migrações

O schema deve ser controlado por Flyway.

Alterações manuais no banco são proibidas como solução final.

## Padrão de migrations

```text
V{numero}__{descricao}.sql
```

Exemplos:

```text
V1__create_users_table.sql
V2__create_audit_log_table.sql
```

## Princípios

- Toda tabela deve ter chave primária clara.
- Preferir UUID para identificadores públicos.
- Dados sensíveis devem ser armazenados com hash ou criptografia, conforme o caso.
- Senhas nunca devem ser armazenadas em texto claro.
- Refresh tokens devem ser armazenados como hash.
- Logs de auditoria devem ser append-only.
