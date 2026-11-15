# Spring Data JPA

Projeto de estudo de **Spring Data JPA**, com banco **PostgreSQL** rodando em **Docker** e versionamento do banco com **Flyway**.

O projeto modela uma livraria simples, com autores e livros, para praticar o mapeamento de entidades, relacionamentos e a configuração de acesso a dados no Spring Boot.

> Em evolução: novas funcionalidades são adicionadas conforme avanço nos estudos.
 
---

## Tecnologias

- **Java 21**
- **Spring Boot 4**
- **Spring Data JPA / Hibernate**: mapeamento das entidades e acesso ao banco
- **PostgreSQL 18**: banco de dados, rodando em container
- **Docker**: containers do banco e do pgAdmin
- **pgAdmin**: ferramenta visual para consultar o banco
- **Flyway**: criação e versionamento das tabelas por scripts SQL
- **HikariCP**: pool de conexões
- **Lombok**: geração de getters e setters
- **Maven**: gerenciamento de dependências
---

## Modelo de dados

```text
autor                                livro
-----------------------              ------------------------------
id               (uuid, PK)          id               (uuid, PK)
nome             (varchar 100)       isbn             (varchar 20)
data_nascimento  (date)              titulo           (varchar 150)
nacionalidade    (varchar 50)        data_publicacao  (date)
                                     genero           (varchar 30)
                                     preco            (numeric 18,2)
                                     id_autor         (uuid, FK --> autor.id)
```

- Um autor pode ter vários livros (`@OneToMany`), e cada livro pertence a um autor (`@ManyToOne`)
- O gênero do livro é um `enum` (`FICCAO`, `FANTASIA`, `MISTERIO`, `ROMANCE`, `BIOGRAFIA`, `HQ`), salvo pelo nome com `EnumType.STRING`
- O preço usa `BigDecimal` no Java e `numeric` no banco, para guardar o valor exato
---

## Estrutura

```text
src/main
├── java/br/com/projects/springdatajpa
│   ├── Application.java                  # Classe principal
│   ├── config
│   │   └── DatabaseConfiguration.java    # Exemplo de configuração manual do DataSource (desativado)
│   └── model
│       ├── Autor.java                    # Entidade autor
│       ├── Livro.java                    # Entidade livro
│       └── GeneroLivro.java              # Enum dos gêneros
└── resources
    ├── application.yaml                  # Configuração da aplicação
    └── db/migration
        └── V1__cria_tabelas_autor_e_livro.sql   # Script do Flyway
```
 
---

## Variáveis de ambiente

As credenciais ficam em um arquivo `.env`, que não é enviado para o repositório. Para rodar o projeto, copie o arquivo de exemplo e preencha os valores:

```bash
# Windows
copy .env.example .env
 
# Linux/Mac
cp .env.example .env
```

| Variável | Descrição |
|----------|-----------|
| `POSTGRES_USER` | Usuário do PostgreSQL |
| `POSTGRES_PASSWORD` | Senha do PostgreSQL |
| `POSTGRES_DB` | Nome do banco |
| `PGADMIN_DEFAULT_EMAIL` | E-mail de login do pgAdmin |
| `PGADMIN_DEFAULT_PASSWORD` | Senha de login do pgAdmin |
 
---

## Como executar

**Pré-requisitos:** Java 21 e Docker instalados, e o arquivo `.env` criado. O Maven não é necessário, porque o projeto usa o Maven Wrapper.

**1. Clonar o repositório**

```bash
git clone https://github.com/EngSoftFilipe/spring-data-jpa.git
cd spring-data-jpa
```

**2. Subir o banco e o pgAdmin** (de dentro da pasta do projeto, onde está o `.env`)

```bash
docker network create springdatajpa-net
docker run --name springdatajpa-db -p 5432:5432 --env-file .env --network springdatajpa-net -d postgres:18.7
docker run --name springdatajpa-pgadmin -p 15432:80 --env-file .env --network springdatajpa-net -d dpage/pgadmin4:9.19
```

**3. Rodar a aplicação**

```bash
# Windows
mvnw.cmd spring-boot:run
 
# Linux/Mac
./mvnw spring-boot:run
```

Ao subir, o Flyway cria as tabelas automaticamente a partir dos scripts em `db/migration`, e o Hibernate confere se as entidades batem com as tabelas (`ddl-auto: validate`).

### pgAdmin

Com os containers rodando, o pgAdmin fica disponível em `http://localhost:15432`, com o login definido no `.env`. Para cadastrar o servidor (Add New Server):

| Campo | Valor |
|-------|-------|
| Host | `springdatajpa-db` |
| Port | `5432` |
| Username | O valor de `POSTGRES_USER` |
| Password | O valor de `POSTGRES_PASSWORD` |

> Os containers desligam quando o computador reinicia. Para ligar de novo: `docker start springdatajpa-db springdatajpa-pgadmin`
 
---

## Anotações de estudo

Durante o desenvolvimento, registro o que aprendo na pasta [`docs`](docs):

| Arquivo | Conteúdo |
|---------|----------|
| [Spring Data JPA](docs/SpringDataJPA.md) | Mapeamento de entidades, relacionamentos, `BigDecimal` e Lombok |
| [Docker](docs/docker.md) | Comandos do Docker, PostgreSQL, pgAdmin e problemas comuns |
 
---

## O que pratiquei neste projeto

- Mapeamento de entidades com JPA (`@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue` com UUID)
- Relacionamentos `@ManyToOne` e `@OneToMany`, com `@JoinColumn` e carregamento `LAZY`
- `enum` mapeado com `@Enumerated(EnumType.STRING)`
- `BigDecimal` para valores monetários
- Versionamento do banco com Flyway e validação do schema pelo Hibernate
- PostgreSQL e pgAdmin em containers Docker, com rede entre os containers
- Credenciais em variáveis de ambiente (`.env`)
- Configuração do pool de conexões HikariCP pelo `application.yaml`
- Lombok (`@Getter` e `@Setter`) nas entidades