# Auditoria de Dependencias

Este projeto deve manter auditoria continua para dependencias de frontend, backend e automacoes do repositorio.

## Frontend

Execute a auditoria npm a partir da pasta `frontend`:

```bash
npm audit
npm audit fix
npm run build
npm run lint
```

Use `npm audit fix --force` apenas quando o impacto de upgrades major for entendido e validado por build, lint e testes manuais dos fluxos criticos.

## Backend

Execute a auditoria Maven a partir da pasta `backend`:

```bash
./mvnw dependency-check:check
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd dependency-check:check
.\mvnw.cmd test
```

O OWASP Dependency-Check gera relatorios em:

- `backend/target/dependency-check-report.html`
- `backend/target/dependency-check-report.json`

O build da auditoria falha para vulnerabilidades com CVSS maior ou igual a 8.

Para execucoes locais ou em CI, recomenda-se configurar uma chave da NVD na variavel `NVD_API_KEY`. Sem essa chave, a primeira atualizacao da base publica pode demorar bastante ou falhar por timeout/rate limit do servico da NVD.

## Politica de Tratamento

- `critical`: corrigir imediatamente. Uma PR nao deve ser aprovada com vulnerabilidade critica sem justificativa formal.
- `high`: corrigir antes do merge, salvo falso positivo ou mitigacao documentada.
- `moderate`: avaliar impacto, priorizando dependencias sensiveis e bibliotecas expostas a entrada do usuario.
- `low`: monitorar e corrigir em ciclos regulares de manutencao.

Falsos positivos so devem ser suprimidos com arquivo de suppressions especifico, justificativa por CVE e escopo minimo. Nao usar suppressions amplas.

## Dependencias Sensiveis

Revisar com prioridade bibliotecas relacionadas a:

- PDF preview, validacao e assinatura;
- criptografia e Bouncy Castle;
- JWT e Spring Security;
- WebAuthn e TOTP;
- Axios e chamadas HTTP;
- upload de arquivos;
- PostgreSQL e Flyway.

## Dependabot

O arquivo `.github/dependabot.yml` verifica semanalmente:

- Maven em `/backend`;
- npm em `/frontend`;
- GitHub Actions em `/`.

O limite de PRs abertas por ecossistema e 5 para reduzir ruido e manter revisoes pequenas.

## Ultima Execucao Local

Data: 2026-06-18.

### Frontend

Comando executado:

```bash
npm audit
```

Resultado inicial:

- `@babel/core <= 7.29.0`: low, leitura arbitraria via `sourceMappingURL`.
- `form-data 4.0.0 - 4.0.5`: high, CRLF injection em nomes de campos/arquivos multipart.
- `vite 8.0.0 - 8.0.15`: high, disclosure de NTLMv2 hash via UNC path no Windows e bypass de `server.fs.deny`.

Tratamento aplicado:

```bash
npm audit fix
npm audit
npm run build
npm run lint
```

Resultado final:

- `npm audit`: 0 vulnerabilidades.
- `npm run build`: passou.
- `npm run lint`: passou.

### Backend

Comando executado:

```powershell
.\mvnw.cmd dependency-check:check
```

Resultado:

- O plugin foi baixado e iniciado corretamente.
- A atualizacao da base NVD falhou com `NVD Returned Status Code: 524`.
- Nenhum relatorio de vulnerabilidades foi gerado porque a analise nao prosseguiu sem dados NVD.

Tratamento aplicado:

- Plugin configurado no `backend/pom.xml`.
- Suporte a `NVD_API_KEY` configurado para execucoes locais/CI.
- Politica documentada para repetir a auditoria quando a NVD estiver disponivel ou com chave NVD configurada.

Validacao backend:

```powershell
.\mvnw.cmd test
```

Resultado final:

- 173 testes executados.
- 0 falhas.
- 0 erros.
