# API Standards

## Prefixo

Todos os endpoints devem usar:

```text
/api/v1
```

## Respostas de erro

Erros devem seguir formato padronizado:

```json
{
  "code": "AUTH_001",
  "message": "Credenciais inválidas",
  "timestamp": "2026-05-15T12:00:00Z"
}
```

## Códigos por domínio

- `AUTH_XXX`: autenticação
- `VAL_XXX`: validação
- `DOC_XXX`: documentos
- `SEC_XXX`: segurança
- `SYS_XXX`: erros internos

## Regras

- Não expor stack trace.
- Não expor query SQL.
- Não expor detalhes internos.
- Usar status HTTP correto.
- Validações devem informar campos inválidos quando seguro.

## Versionamento

Breaking changes devem gerar nova versão da API.

Exemplo:

```text
/api/v2
```
