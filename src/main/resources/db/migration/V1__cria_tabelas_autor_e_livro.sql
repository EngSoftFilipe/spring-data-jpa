-- Script de criação das tabelas (por enquanto não é executado pela aplicação, porque o Flyway ainda não está instalado)
-- Rodado manualmente no pgAdmin

-- Flyway
-- Ferramenta de migration: versiona o banco de dados, como um "Git do banco"
-- Roda cada script uma vez só, em ordem (V1, V2, V3...), e guarda no banco quais já foram executados
-- Nome do arquivo: V + número + dois underlines + descrição (ex: V1__cria_tabelas_autor_e_livro.sql)
-- Para mudar o banco, cria um script novo (ex: V2__adiciona_coluna_x.sql), sem alterar os que já rodaram
-- Pasta padrão: src/main/resources/db/migration
-- Só roda se a dependência do Flyway estiver no pom.xml; sem ela, este arquivo é ignorado
-- As tabelas deste script já foram criadas manualmente no pgAdmin: ao instalar o Flyway, apagar as tabelas antes de rodar

-- Dependências do Flyway no pom.xml (Spring Boot 4 + PostgreSQL):
-- <dependency>
--     <groupId>org.springframework.boot</groupId>
--     <artifactId>spring-boot-starter-flyway</artifactId>
-- </dependency>
-- <dependency>
--     <groupId>org.flywaydb</groupId>
--     <artifactId>flyway-database-postgresql</artifactId>
-- </dependency>
-- Não precisa de versão: o Spring Boot já gerencia

create table autor(
  id uuid not null primary key,
  nome varchar(100) not null,
  data_nascimento date not null,
  nacionalidade varchar(50) not null
);

create table livro(
  id uuid primary key,
  isbn varchar(20) not null,
  titulo varchar(150) not null,
  data_publicacao date not null,
  genero varchar(30) not null,
  preco numeric(18,2),
  id_autor uuid not null references autor(id),
  constraint chk_genero check (genero in ('FICCAO', 'FANTASIA', 'MISTERIO', 'ROMANCE', 'BIOGRAFIA', 'HQ'))
);