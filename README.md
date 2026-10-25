# Sistema de Controle de Pacientes — Consultório Odontológico

Sistema web para gestão do consultório odontológico Dr. Valdomiro Razlosnek Alvares, desenvolvido como Projeto Integrador da UNIVESP (Engenharia de Computação e Tecnologia da Informação).

## Equipe

| Integrante |
|---|
| Alexandre Yamashita |
| Cleiton Rochel Vieira |
| Elder Andrei Domingues |
| Eliézer Paes Polsaque |
| Fabio Kamagian Gomes |
| Leandro Polsaque |

**Tutor:** Leonardo Luis Lemes

---

## Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Frontend | Angular | 17 |
| Backend | Java + Spring Boot | 21 / 3.3.4 |
| Banco de Dados | PostgreSQL | 16 |
| Armazenamento de arquivos | MinIO | latest |
| Infraestrutura | Docker + Docker Compose | — |
| Deploy | Oracle Cloud (ARM) | — |

---

## Estrutura do Repositório

```
sistema-consultorio-odontologico/
├── frontend/    ← Aplicação Angular (SPA)
├── backend/     ← API REST Spring Boot
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

### 2. Variáveis de ambiente

Copie o arquivo de exemplo na raiz do projeto e ajuste os valores:

```bash
cp .env.example .env
```

Principais variáveis:

| Variável | Descrição | Exemplo (dev) |
|---|---|---|
| `POSTGRES_DB` | Nome do banco | `consultorio` |
| `POSTGRES_USER` | Usuário do banco | `consultorio` |
| `POSTGRES_PASSWORD` | Senha do banco | `sua-senha` |
| `JWT_SECRET` | Chave secreta JWT (mín. 32 chars) | `chave-secreta-longa` |
| `MINIO_ENDPOINT` | URL do MinIO | `http://localhost:9004` |
| `MINIO_ACCESS_KEY` | Chave de acesso MinIO | `minioadmin` |
| `MINIO_SECRET_KEY` | Chave secreta MinIO | `minioadmin` |
| `MINIO_BUCKET` | Nome do bucket | `consultorio` |

> Em produção, `MINIO_ENDPOINT` deve apontar para o serviço interno Docker: `http://minio:9000`.

---

### 3. Subir o ambiente completo via Docker Compose (recomendado)

```bash
docker compose up -d --build
```

Serviços disponíveis:

| Serviço | URL local |
|---|---|
| Frontend (Angular) | `http://localhost:4200` |
| API REST | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| MinIO (API) | `http://localhost:9004` |
| MinIO (Console) | `http://localhost:9005` |

> Na primeira execução o build pode levar alguns minutos (download de imagens + compilação).

#### Ver logs

```bash
docker compose logs -f api       # logs da API
docker compose logs -f frontend  # logs do Nginx
docker compose logs -f minio     # logs do MinIO
```

#### Parar os serviços

```bash
docker compose down        # mantém o volume do banco
docker compose down -v     # remove também os dados
```

---

### 4. Executar o backend fora do Docker (desenvolvimento)

Use o script `start-dev.sh` na raiz do projeto, que carrega o `.env` e inicia o Spring Boot:

```bash
./start-dev.sh
```

A API ficará disponível em `http://localhost:8080`. As migrações do banco são aplicadas automaticamente pelo Flyway na inicialização.

#### Usuário padrão (seed)

| Campo | Valor |
|---|---|
| E-mail | `admin@consultorio.com` |
| Senha | `Admin@123` |
| Perfil | Administrador |

> Altere a senha após o primeiro acesso.

#### Rodar os testes

```bash
cd backend
./mvnw test
```

---

### 5. Executar o frontend fora do Docker (desenvolvimento)

```bash
cd frontend
npm install
npm start
```

A aplicação ficará disponível em `http://localhost:4200`. O proxy de desenvolvimento (`proxy.conf.json`) já redireciona `/api` para `http://localhost:8080`.

---

## Fluxo de Trabalho (Git)

| Branch | Finalidade |
|---|---|
| `main` | Produção — código estável, deploy automático na Oracle Cloud |
| `develop` | Integração — base para todas as novas funcionalidades |
| `feature/<nome>` | Desenvolvimento de uma funcionalidade específica |

### Regras

1. **Nunca commitar direto na `main` ou `develop`.** Toda mudança entra via Pull Request.
2. Cada nova funcionalidade parte de uma branch criada a partir da `develop`:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/nome-da-funcionalidade
   ```
3. Ao concluir, abrir PR da `feature/*` → `develop`. O CI executa testes e build automaticamente — o merge só é liberado se tudo passar.
4. Após validação na `develop`, abrir PR `develop` → `main` para deploy em produção.

### CI/CD

- **Pull Requests** para `develop` e `main`: executa testes do backend (com PostgreSQL de teste) e build do frontend
- **Merge na `main`**: faz build das imagens Docker, push no Docker Hub e deploy automático no servidor via SSH

---

## Documentação

A especificação técnica completa está em [`docs/especificacao_sistema_consultorio.md`](docs/especificacao_sistema_consultorio.md).

---

## Planejamento e Progresso

### Fundação

- [x] Estrutura inicial do projeto (monorepo, Docker, CI)
- [x] Banco de dados PostgreSQL com Docker
- [x] Migrações com Flyway
- [x] Armazenamento de arquivos com MinIO

### Autenticação

- [x] Login com JWT (access token 60 min + refresh token 30 dias)
- [x] Tela de login (Angular)
- [x] Interceptor HTTP para envio automático do token
- [x] Proteção de rotas (guards)

### Layout e Navegação

- [x] Shell do dashboard com sidebar responsiva
- [x] Navegação inferior mobile
- [x] Controle de visibilidade de menus por perfil (ADMIN / SECRETARIA / DENTISTA)

### Home / Dashboard

- [x] Resumo diário: consultas do dia, confirmadas, canceladas
- [x] Contador de pacientes ativos e orçamentos em aberto
- [x] Lista de próximas consultas do dia com link para o paciente

### Pacientes

- [x] CRUD completo de pacientes (backend)
- [x] Validação de CPF com algoritmo oficial da Receita Federal
- [x] Máscara de CPF e telefone
- [x] Endereço detalhado (logradouro, número, complemento, bairro, cidade, UF)
- [x] Tela de listagem paginada com busca por nome/CPF
- [x] Modal de cadastro e edição
- [x] Testes unitários do PatientService

### Prontuário

- [x] Página de detalhe do paciente com abas: Dados, Prontuário, Evolução & Imagens
- [x] Odontograma interativo
- [x] Linha do tempo de atendimentos
- [x] Orçamentos vinculados ao paciente
- [x] Consentimento informado
- [x] Upload e visualização de fotos clínicas (armazenadas no MinIO)

### Auditoria

- [x] Tabela `audit_log` no banco
- [x] AuditService com transação independente (`REQUIRES_NEW`)
- [x] Rastreabilidade de quem criou/editou cada registro
- [x] Endpoint `GET /api/auditoria` restrito a ADMIN
- [x] Tela de visualização do log de auditoria (frontend)

### Usuários

- [x] CRUD completo de usuários (backend, TDD)
- [x] Perfis: Administrador, Secretária, Dentista
- [x] Proteção: usuário não pode alterar o próprio perfil nem se desativar
- [x] Tela de listagem paginada com busca
- [x] Modal de cadastro e edição com senha opcional na edição
- [x] Ativar / desativar usuário inline na lista

### Dentistas

- [x] Tela dedicada de cadastro e listagem de dentistas
- [x] Filtro por role=DENTISTA no endpoint de usuários

### Notificações

- [x] Sistema de toast global (sucesso e erro)

### Agenda

- [x] Modelo de dados para agendamentos (`appointments`)
- [x] CRUD de agendamentos com validação de conflito de horário (backend, TDD)
- [x] Suporte a duração variável por procedimento
- [x] Filtro por dentista opcional (todos ou individual)
- [x] Tela de agenda: mini-calendário mensal + lista de consultas do dia
- [x] Grade de horários (slots de 30 min, 08h–18h)
- [x] Formulário com autocomplete de paciente, tipo de procedimento e duração
- [x] Alteração de status (Agendado → Confirmado → Realizado / Cancelado)

### Infraestrutura e Deploy

- [x] Pipeline CI/CD com GitHub Actions
- [x] Build e push automático de imagens Docker para o Docker Hub
- [x] Deploy automático na Oracle Cloud via SSH após merge na `main`
- [x] Branch protection: merge bloqueado se testes falharem
- [ ] Configuração de domínio e HTTPS
- [ ] Backup automático do banco de dados

### Financeiro

- [ ] Registro de pagamentos
- [ ] Relatório de faturamento por período

### Comunicação

- [ ] Lista de espera
- [ ] Retorno / recall automático
- [ ] Envio de lembretes (e-mail ou WhatsApp)

### Relatórios

- [ ] Relatório de pacientes ativos/inativos
- [ ] Relatório de produção por dentista
- [ ] Exportação para PDF/Excel
