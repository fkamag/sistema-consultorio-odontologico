# Sistema de Controle de Pacientes — Consultório Odontológico

Sistema web para gestão do consultório odontológico Dr. Valdomiro Razlosnek Alvares, desenvolvido como Projeto Integrador da UNIVESP (Engenharia de Computação e Tecnologia da Informação).

## Equipe

| Integrante | Polo |
|---|---|
| Eliézer Paes Polsaque | — |
| Fabio Kamagian Gomes | — |
| Leandro Polsaque | — |
| Cleiton Rochel Vieira | — |

**Tutor:** Leonardo Luis Lemes — Polo Tatuí / Polo Itu

---

## Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Frontend | Angular | 17 |
| Backend | Java + Spring Boot | 21 / 3.3.4 |
| Banco de Dados | PostgreSQL | 16 |
| Infraestrutura | Docker + Docker Compose | — |
| Deploy | VPS Hostinger | — |

---

## Estrutura do Repositório

```
sistema-consultorio-odontologico/
├── frontend/    ← Aplicação Angular (SPA)
├── backend/     ← API REST Spring Boot + Docker
└── docs/        ← Especificação técnica e documentação
```

---

## Como Executar Localmente

### Pré-requisitos

- [Node.js](https://nodejs.org/) 20+
- [Java](https://aws.amazon.com/corretto/) 21+ (recomendado Amazon Corretto)
- [Docker](https://www.docker.com/) com Docker Compose
- [Git](https://git-scm.com/)

---

### 1. Clonar o repositório

```bash
git clone https://github.com/fkamag/sistema-consultorio-odontologico.git
cd sistema-consultorio-odontologico
```

---

### 2. Backend

#### 2.1 Configurar variáveis de ambiente

Dentro da pasta `backend/`, copie o arquivo de exemplo e ajuste os valores:

```bash
cd backend
cp .env.example .env
```

Edite o `.env` com suas configurações locais (banco, JWT secret, portas). O arquivo já vem com valores padrão funcionais para desenvolvimento.

#### 2.2 Subir o banco de dados

```bash
docker compose up -d db
```

O PostgreSQL ficará disponível na porta `5441` (configurável no `.env`).

#### 2.3 Iniciar a API

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

As migrações do banco são aplicadas automaticamente pelo Flyway na inicialização.

#### Usuário padrão (seed)

| Campo | Valor |
|---|---|
| E-mail | `admin@consultorio.com` |
| Senha | `Admin@123` |
| Perfil | Administrador |

> Altere a senha após o primeiro acesso.

#### Rodar os testes

```bash
./mvnw test
```

---

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

A aplicação ficará disponível em `http://localhost:4200`.

> O frontend aponta para `http://localhost:8080` por padrão. Certifique-se de que a API está rodando antes de acessar.

---

### 4. Ambiente completo via Docker (opcional)

Para rodar backend + banco juntos em container:

```bash
cd backend
docker compose up --build
```

A API ficará disponível na porta definida em `API_PORT` no `.env` (padrão: `8086`).

---

## Fluxo de Trabalho (Git)

| Branch | Finalidade |
|---|---|
| `main` | Produção — código estável, deploy automático na VPS |
| `develop` | Integração — base para todas as novas funcionalidades |
| `feature/<nome>` | Desenvolvimento de uma funcionalidade específica |

### Regras

1. **Nunca commitar direto na `main`.** Ela recebe apenas merges validados da `develop`.
2. Cada nova funcionalidade parte de uma branch criada a partir da `develop`:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/nome-da-funcionalidade
   ```
3. Ao concluir, abrir PR da `feature/*` → `develop` para revisão da equipe.
4. Após validação e testes na `develop`, abrir PR `develop` → `main` para deploy na VPS.

---

## Documentação

A especificação técnica completa está em [`docs/especificacao_sistema_consultorio.md`](docs/especificacao_sistema_consultorio.md).

---

## Planejamento e Progresso

### Fundação

- [x] Estrutura inicial do projeto (monorepo, Docker, CI)
- [x] Banco de dados PostgreSQL com Docker
- [x] Migrações com Flyway

### Autenticação

- [x] Login com JWT (access token 60 min + refresh token 30 dias)
- [x] Tela de login (Angular)
- [x] Interceptor HTTP para envio automático do token
- [x] Proteção de rotas (guards)

### Layout e Navegação

- [x] Shell do dashboard com sidebar responsiva
- [x] Navegação inferior mobile
- [x] Controle de visibilidade de menus por perfil (ADMIN / SECRETARIA / DENTISTA)

### Pacientes

- [x] CRUD completo de pacientes (backend)
- [x] Validação de CPF com algoritmo oficial da Receita Federal
- [x] Máscara de CPF e telefone
- [x] Endereço detalhado (logradouro, número, complemento, bairro, cidade, UF)
- [x] Tela de listagem paginada com busca por nome/CPF
- [x] Modal de cadastro e edição
- [x] Testes unitários do PatientService

### Auditoria

- [x] Tabela `audit_log` no banco
- [x] AuditService com transação independente (`REQUIRES_NEW`)
- [x] Rastreabilidade de quem criou/editou cada paciente
- [x] Endpoint `GET /api/auditoria` restrito a ADMIN
- [x] Testes unitários do AuditService
- [x] Tela de visualização do log de auditoria (frontend)

### Usuários

- [x] CRUD completo de usuários (backend, TDD)
- [x] Perfis: Administrador, Secretária, Dentista
- [x] Proteção: usuário não pode alterar o próprio perfil nem se desativar
- [x] Tela de listagem paginada com busca
- [x] Modal de cadastro e edição com senha opcional na edição
- [x] Ativar / desativar usuário inline na lista

### Notificações

- [x] Sistema de toast global (sucesso e erro)
- [x] Toast no login, cadastro e edição de pacientes e usuários

### Agenda

- [ ] Modelo de dados para agendamentos
- [ ] CRUD de agendamentos (backend)
- [ ] Tela de agenda com visualização semanal/diária
- [ ] Confirmação e cancelamento de consultas

### Prontuário

- [ ] Modelo de dados para prontuário odontológico
- [ ] Registro de procedimentos por consulta
- [ ] Histórico do paciente
- [ ] Upload de imagens/radiografias

### Financeiro

- [ ] Orçamentos vinculados ao plano de tratamento
- [ ] Registro de pagamentos
- [ ] Relatório de faturamento por período

### Comunicação

- [ ] Lista de espera
- [ ] Retorno / recall automático
- [ ] Envio de lembretes (e-mail ou WhatsApp)

### Materiais e Estoque

- [ ] Cadastro de materiais e equipamentos
- [ ] Controle de estoque com alertas de quantidade mínima
- [ ] Registro de esterilização de instrumentos

### Relatórios

- [ ] Relatório de pacientes ativos/inativos
- [ ] Relatório de produção por dentista
- [ ] Exportação para PDF/Excel

### Infraestrutura e Deploy

- [ ] Pipeline CI/CD
- [ ] Deploy automatizado na VPS Hostinger
- [ ] Configuração de domínio e HTTPS
- [ ] Backup automático do banco de dados
