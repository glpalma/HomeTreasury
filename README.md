# HomeTreasury

Aplicação para quem divide uma casa e uma conta bancária e precisa saber, todo mês, se as contas foram pagas e se o dinheiro de todo mundo entrou.

## Por que existe

O caso de origem é a república de estudantes: várias pessoas moram juntas, o aluguel, a luz, a internet e a água saem de uma conta só, e cada morador manda a sua parte por PIX. Alguém da casa fica responsável por pagar em dia e por manter uma reserva parada, para o mês em que a conta de luz vem mais alta ou um repasse atrasa.

Quem nunca morou assim pode pensar no mesmo arranjo em qualquer casa compartilhada. O dinheiro de todos cai numa conta. As despesas fixas saem dela. A pessoa que administra precisa responder três perguntas, e o aplicativo do banco não responde nenhuma:

- A luz deste mês já foi paga?
- O dinheiro de cada morador já entrou?
- Depois de pagar o que vence agora, ainda sobra a reserva combinada?

O extrato ajuda pouco. O lançamento aparece como `PIX ENEL`, um nome cortado ou um valor que não bate com o combinado. O grupo de mensagens guarda a conversa, não o estado das contas. A planilha só fica certa se alguém atualizar na mão, e na prática ela se descola do que aconteceu no banco.

O HomeTreasury fecha esse espaço. A casa declara uma vez o que espera todo mês: o nome da despesa, o dia em que ela precisa estar paga, uma palavra que o banco usa no extrato (por exemplo `ENEL`) e um valor de referência. A aplicação lê a conta de verdade e marca sozinha o que já saiu e o que já entrou. Todo mundo da casa vê o mesmo painel. Só quem administra altera o que foi declarado e mexe na reserva.

A leitura da conta usa o Open Finance, o mecanismo oficial em que o titular autoriza um aplicativo a consultar saldo e extrato. Quem faz essa ponte é o [Pluggy](https://pluggy.ai). A senha do banco fica nesse fluxo e não chega ao HomeTreasury. Saldo e extrato também não são copiados para o banco da aplicação: cada consulta busca os dados na hora. O que fica gravado são os moradores, a casa, o vínculo com o banco, as contas e as rendas declaradas, e as vezes em que alguém marcou um pagamento à mão porque ele não apareceu no extrato.

## O que dá para fazer

- Criar uma Casa ou entrar em uma existente com um código de convite.
- Entrar com e-mail e senha (JWT) e redefinir a senha por e-mail.
- Conectar o banco da Casa pelo widget do Pluggy. As credenciais bancárias não passam pelo backend.
- Ver o painel: saldo atual, saldo ideal, diferença e variação recente.
- Declarar contas mensais e rendas esperadas, e conferir o mês contra as transações reais.
- Marcar uma conta como paga quando o pagamento não aparece no extrato.
- Convidar outro membro. Quem entra pelo convite fica como **Visualizador**.

## Papéis

| Papel | O que pode |
| --- | --- |
| **Dono** (`OWNER`) | Alterar o saldo ideal, conectar o banco, criar convites, declarar e editar contas e rendas, marcar pagamento. |
| **Visualizador** (`VIEWER`) | Ver painel, extrato, contas e rendas. |

Cada usuário pertence a uma única Casa.

## Stack

- **Backend:** Java 21, Spring Boot 4, Spring Security, JWT, JPA, PostgreSQL
- **Frontend:** React 18, TypeScript, Vite, Tailwind CSS, React Query
- **Banco externo:** Pluggy (Open Finance)
- **Testes:** JUnit via Spring Boot, H2 em memória

## Como rodar

Pré-requisitos: JDK 21, Node.js 22, PostgreSQL e uma aplicação no Pluggy (`client id` e `client secret`).

### Backend

Na raiz do repositório, crie um arquivo `.env` (ele é ignorado pelo Git):

```env
PLUGGY_CLIENT_ID=
PLUGGY_CLIENT_SECRET=

DB_URL=jdbc:postgresql://localhost:5432/hometreasury
DB_USER=postgres
DB_PASSWORD=postgres

JWT_SECRET=uma-chave-com-pelo-menos-32-caracteres
JWT_EXPIRATION_MS=86400000

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_FRONTEND_URL=http://localhost:5173

MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
```

`JWT_SECRET` precisa ter pelo menos 32 caracteres. O schema é criado e atualizado automaticamente (`spring.jpa.hibernate.ddl-auto=update`).

O envio de e-mail só é necessário para “esqueci minha senha”. Em desenvolvimento, um servidor SMTP local (por exemplo [MailHog](https://github.com/mailhog/MailHog) ou [Mailpit](https://github.com/axllent/mailpit) na porta 1025) basta para ver o link de redefinição.

Suba a API:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080`.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

No Windows, copie `frontend/.env.example` para `frontend/.env`. O arquivo já aponta para `http://localhost:8080`.

A interface fica em `http://localhost:5173`.

O primeiro acesso é em `/register`: informe o nome da Casa para criá-la (você vira Dono) ou um código de convite para entrar como Visualizador.

## Telas

| Rota | Conteúdo |
| --- | --- |
| `/login`, `/register` | Entrada e cadastro |
| `/forgot-password`, `/reset-password` | Recuperação de senha |
| `/dashboard` | Saldo, saldo ideal e variação |
| `/bills` | Contas e rendas do mês, com prévia de palavra-chave |
| `/transactions` | Extrato do mês da conta conectada |
| `/settings` | Conexão Pluggy, saldo ideal, convites e troca de senha |

## API

Rotas autenticadas usam `Authorization: Bearer <token>`. Abertas: login, cadastro, esqueci a senha e redefinição.

| Método | Caminho | Uso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/register` | Criar Casa (`homeName`) ou entrar (`inviteCode`) |
| `POST` | `/api/auth/forgot-password` | Pedir e-mail de redefinição |
| `POST` | `/api/auth/reset-password` | Definir a nova senha |
| `GET` | `/api/me` | Usuário, papel e Casa atuais |
| `PUT` | `/api/me/password` | Trocar a senha |
| `PUT` | `/api/home/idealBalance` | Definir o saldo ideal (Dono) |
| `POST` | `/api/home/invites` | Gerar convite (Dono) |
| `POST` | `/api/pluggy/connect-token` | Token do widget Pluggy (Dono) |
| `POST` | `/api/pluggy/item` | Vincular o item devolvido pelo widget (Dono) |
| `GET` | `/api/pluggy/item` | Estado da conexão |
| `GET` | `/api/treasury/dashboard` | Painel (`periodDays`, padrão 30) |
| `GET` | `/api/treasury/transactions` | Extrato de um mês (`month=YYYY-MM`) |
| `GET`/`POST` | `/api/bills` | Listar e declarar contas |
| `GET` | `/api/bills/status` | Status do mês |
| `GET` | `/api/bills/preview` | Transações que batem com uma palavra-chave |
| `PUT` | `/api/bills/{id}` | Editar conta (Dono) |
| `POST` | `/api/bills/{id}/archive` | Encerrar a conta a partir do mês atual (Dono) |
| `PUT` | `/api/bills/{id}/months/{month}/paid` | Marcar como paga (Dono) |
| `GET`/`POST` | `/api/incomes` | Listar e declarar rendas esperadas |
| `GET` | `/api/incomes/status` | Status das rendas no mês |

Uma conta ou renda casa com a transação mais antiga do mês cuja descrição (ou nome da contraparte) contém a palavra-chave, sem diferenciar maiúsculas nem acentos. Contas olham débitos; rendas olham créditos. O valor esperado não impede o match: se a diferença passar de 15%, a interface só avisa.

## Testes

```bash
./mvnw test
```

Os testes usam H2 e não precisam de PostgreSQL nem de credenciais reais do Pluggy. As variáveis de teste estão em `src/test/resources/application.properties`.

No frontend:

```bash
cd frontend
npm run lint
npm run build
```

## Estrutura

```
src/main/java/com/glpalma/HomeTreasury/
  auth/        login, cadastro, JWT, redefinição de senha
  user/        usuário, membership e papel
  home/        Casa, convite, saldo ideal
  pluggy/      cliente HTTP e vínculo do item
  treasury/    painel e extrato
  bills/       contas mensais e marcação de pagamento
  incomes/     rendas esperadas
  shared/      matching de transações e cálculo de status
frontend/      SPA (React + Vite)
```
