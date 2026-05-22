# CURRENT_TASK.md - Tarefa Atual

## Status

Concluido.

## Tarefa ativa

### SETUP-05 - Configurar versionamento da API e documentacao inicial de execucao do projeto

## Camada

Backend + Documentacao

## Prioridade

Baixa

## Objetivo

Padronizar o versionamento da API utilizando o prefixo global `/api/v1`, criar endpoint de health check e documentar claramente como executar o projeto localmente para novos integrantes do time.

## Checklist de implementacao

### API Versioning

- [x] Configurar prefixo global `/api/v1` para todos os endpoints REST.
- [x] Garantir que futuros controllers utilizem automaticamente o prefixo.
- [x] Evitar repeticao manual de `/api/v1` em cada controller.
- [x] Escolher abordagem consistente: `spring.mvc.servlet.path=/api/v1`.

### Health Check

- [x] Criar endpoint `GET /api/v1/health`.
- [x] Retornar status simples da aplicacao.
- [x] Retornar versao da API.
- [x] Retornar timestamp UTC.
- [x] Endpoint responde sem autenticacao.

### README.md da raiz do projeto

- [x] Atualizar `README.md` principal do repositorio.
- [x] Adicionar visao geral do projeto.
- [x] Explicar estrutura do monorepo.
- [x] Explicar como executar backend.
- [x] Explicar como executar frontend.
- [x] Explicar dependencias necessarias: Java 21, Node.js e PostgreSQL.
- [x] Explicar criacao do banco PostgreSQL `digital_signature_db`.
- [x] Explicar configuracao dos `.env`.
- [x] Explicar que cada modulo possui `.env.example`.
- [x] Explicar como criar `backend/.env` e `frontend/.env`.
- [x] Explicar execucao do backend com Maven Wrapper.
- [x] Explicar execucao do frontend com `npm install` e `npm run dev`.
- [x] Explicar URL do frontend.
- [x] Explicar URL da API.
- [x] Explicar porta padrao do backend.
- [x] Explicar porta padrao do frontend.

### Banco de Dados

- [x] Documentar criacao manual inicial do banco `digital_signature_db`.
- [x] Explicar que as tabelas nao devem ser criadas manualmente.
- [x] Explicar que o Flyway executa migrations automaticamente.

### Qualidade e onboarding

- [x] README permite que outro integrante rode o projeto sem ajuda externa.
- [x] Instrucoes funcionam em ambiente limpo.
- [x] Passos implicitos foram evitados.
- [x] Nao assume conhecimento previo do projeto.

## Criterios de aceitacao

- [x] Todos os endpoints respondem sob `/api/v1`.
- [x] `GET /api/v1/health` retorna 200 corretamente.
- [x] Health endpoint retorna status, versao e timestamp.
- [x] README da raiz foi atualizado.
- [x] Outro integrante consegue executar frontend e backend apenas lendo o README.
- [x] `.env.example` esta documentado corretamente.
- [x] Banco `digital_signature_db` esta documentado.
- [x] Nenhuma credencial real foi adicionada ao README.

## Fora de escopo

Nao implementar ainda:

- Swagger/OpenAPI.
- Docker Compose.
- Kubernetes.
- Deploy.
- Monitoramento.
- Observabilidade.
- Autenticacao.
- JWT.
- Login.
- Cadastro.
- Rate limiting.
- Endpoints protegidos.

## Validacao realizada

- `backend`: `.\mvnw.cmd test`
- `frontend`: `npm.cmd run build` com `VITE_API_BASE_URL=http://localhost:8080/api/v1`
- `GET http://localhost:8080/api/v1/health` validado contra backend real com PostgreSQL temporario e Flyway executando migrations.
