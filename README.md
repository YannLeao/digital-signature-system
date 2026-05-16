# Plataforma de Assinatura Digital

Projeto fullstack de segurança da informação focado em autenticação segura, assinatura digital de PDFs, verificação pública de autenticidade e auditoria.

> [!NOTE]
> [Quadro Kanban do Projeto](https://github.com/users/YannLeao/projects/6)

## Tecnologias

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apache-maven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)

![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwind-css&logoColor=white)

## Estrutura do Projeto

O repositório é um monorepo simples estruturado da seguinte forma:

```text
.
├── backend/          # API REST Spring Boot
├── frontend/         # Aplicação React + Vite
├── docs/             # Contexto, arquitetura e segurança
└── API_EXCEPTIONS.md # Padrão de respostas de erro da API

```

## Como Executar Localmente

### Pré-requisitos

Certifique-se de ter instalado: **Java 21**, **Node.js (com npm)**, **PostgreSQL** e **Git**.

### 1. Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` na raiz de cada módulo e ajuste os valores locais 
(as credenciais do banco, chaves secretas, etc).

**Windows (PowerShell):**

```powershell
Copy-Item backend\.env.example backend\.env
Copy-Item frontend\.env.example frontend\.env
```

**Linux / macOS:**

```bash
cp backend/.env.example backend/.env && cp frontend/.env.example frontend/.env
```

### 2. Banco de Dados

Apenas crie o banco de dados inicial no PostgreSQL. **Não crie tabelas manualmente**, o Flyway fará isso no startup do backend.

```sql
CREATE DATABASE digital_signature_db;
```

## Executando o Backend

Abra o terminal na pasta do backend e inicie a API.

**Windows:** `.\mvnw.cmd spring-boot:run` | **Linux/macOS:** `./mvnw spring-boot:run`

```bash
cd backend
./mvnw spring-boot:run
```

* **Porta padrão:** `8080`
* **URL Base:** `http://localhost:8080/api/v1`
* **Health Check:** `GET http://localhost:8080/api/v1/health`

## Executando o Frontend

Em outro terminal, instale as dependências e inicie o servidor de desenvolvimento:

```bash
cd frontend
npm install
npm run dev
```

* **Porta padrão:** `5173`
* **URL:** `http://localhost:5173`