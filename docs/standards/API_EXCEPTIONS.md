# API error standard

All API errors must return JSON with a stable machine-readable `code`, a safe
human-readable `message`, and a UTC ISO-8601 `timestamp`.

Generic format:

```json
{
  "code": "VAL_001",
  "message": "Dados invalidos.",
  "timestamp": "2026-05-16T12:00:00Z"
}
```

Validation format:

```json
{
  "code": "VAL_001",
  "message": "Dados invalidos.",
  "timestamp": "2026-05-16T12:00:00Z",
  "fields": [
    {
      "field": "email",
      "message": "E-mail invalido."
    }
  ]
}
```

## Initial codes

| Code       | HTTP status                 | Meaning                                           |
|------------|-----------------------------|---------------------------------------------------|
| `VAL_001`  | `400 Bad Request`           | Invalid request body or field validation failure. |
| `VAL_002`  | `400 Bad Request`           | Invalid request parameter.                        |
| `VAL_003`  | `409 Conflict`              | Business rule violation or state conflict.        |
| `AUTH_001` | `401 Unauthorized`          | Invalid credentials.                              |
| `AUTH_002` | `401 Unauthorized`          | Missing or failed authentication.                 |
| `AUTH_003` | `401 Unauthorized`          | Invalid or revoked token.                         |
| `SEC_001`  | `403 Forbidden`             | Request blocked by security policy.               |
| `DOC_001`  | `400 Bad Request`           | Invalid document.                                 |
| `SYS_001`  | `500 Internal Server Error` | Unexpected internal error.                        |
| `SYS_002`  | `404 Not Found`             | Resource not found.                               |

## Security rules

- Do not expose stack traces.
- Do not expose SQL queries.
- Do not expose class names, package names, credentials, tokens, keys, or internal
  implementation details.
- Unexpected exceptions must return `SYS_001` with a generic message.
- Field validation errors may include field names and validation messages when
  they are safe for API consumers.
