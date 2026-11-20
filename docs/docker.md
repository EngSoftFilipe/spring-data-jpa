# Docker
 
> ## Importante
- Imagem é o molde (como uma classe) e container é o que roda a partir dela (como um objeto)
- O Docker Desktop precisa estar aberto antes de usar qualquer comando
- Comandos com `--env-file .env` precisam ser rodados de dentro da pasta do projeto, porque o `.env` é procurado na pasta em que o terminal está
- Usar sempre imagens oficiais (ou do próprio projeto, como o `dpage/pgadmin4`) e com versão fixa (ex: `postgres:18.7`), nunca `latest`, que muda sozinha
- Versões `rc` (release candidate) e `SNAPSHOT` ainda estão em teste e podem ter bugs
- O Docker Desktop é gratuito para uso pessoal, estudo e empresas pequenas
> ## Comandos
 
### docker run
- Cria e liga um container a partir de uma imagem
- Exemplo prático: quero um PostgreSQL rodando sem instalar nada no computador. O `docker run` baixa a imagem (se ainda não tiver) e liga o container
```bash
docker run --name meu-projeto-db -p 5432:5432 --env-file .env -d postgres:18.7
```
 
| Parte | O que faz |
|---|---|
| `--name meu-projeto-db` | Dá um nome ao container |
| `-p 5432:5432` | Porta do meu computador : porta dentro do container |
| `--env-file .env` | Lê as variáveis (usuário, senha, banco) do arquivo `.env` |
| `-d` | Roda em segundo plano, sem prender o terminal |
| `postgres:18.7` | A imagem e a versão |
 
#### Portas (-p)
- A esquerda é a porta do **meu computador**, que eu escolho (precisa estar livre)
- A direita é a porta **dentro do container**, onde o programa roda de verdade, e normalmente não muda
- O PostgreSQL roda na 5432 e o pgAdmin na 80
- Se a porta do meu computador já estiver ocupada, troco só a da esquerda (ex: `-p 5433:5432`), e a URL do banco passa a usar a 5433
#### Variáveis de ambiente
- Cada `-e VARIAVEL=valor` no comando pode virar uma linha no `.env`, e o comando usa só o `--env-file .env`
- Assim a senha nunca aparece no comando
- O Docker lê o `.env` só na hora de criar o container: se eu mudar o `.env` depois, preciso apagar e recriar o container
```properties
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha
POSTGRES_DB=nome_do_banco
PGADMIN_DEFAULT_EMAIL=seu_email@exemplo.com
PGADMIN_DEFAULT_PASSWORD=sua_senha
```
 
- `POSTGRES_PASSWORD` é obrigatória: sem ela, o container desliga na hora
- O usuário do `POSTGRES_USER` é criado como superusuário (o "root" do PostgreSQL)
- O `POSTGRES_DB` cria um banco, e não um schema. Todo banco já vem com o schema `public`
- O login do pgAdmin pode ser qualquer e-mail (só precisa ter formato de e-mail) e senha, de preferência diferente da senha do banco
### docker ps
- Lista os containers ligados
- `docker ps -a` mostra também os desligados
```bash
docker ps
```
 
- Na coluna STATUS:
  - `Up` --> ligado
  - `Exited (0)` --> desligou normalmente
  - `Exited (255)` --> desligou de repente (ex: o computador ou o Docker fechou). O PostgreSQL se recupera sozinho na próxima vez que ligar
### docker start e docker stop
- `docker start` liga um container que já existe (não cria outro, diferente do `docker run`)
- `docker stop` desliga o container de forma segura, deixando o banco fechar tudo direito
- Os containers desligam quando o computador reinicia, então ao voltar preciso ligar de novo
```bash
docker start meu-projeto-db meu-projeto-pgadmin
docker stop meu-projeto-pgadmin meu-projeto-db
```
 
### docker rm
- Apaga um container (`docker container rm` faz a mesma coisa)
- O `-f` força, mesmo se ele estiver ligado
- Usado quando dá erro de nome repetido, ou quando preciso recriar o container
- Os dados que estão dentro do container somem junto com ele: um container novo começa com o banco vazio
- Para os dados sobreviverem, o container do banco precisa ser criado com um volume (`-v nome-do-volume:/var/lib/postgresql`)
- Apagar o container do pgAdmin não afeta o banco: os dados ficam no container do PostgreSQL
```bash
docker rm -f meu-projeto-db
```
 
### docker logs
- Mostra o que o container escreveu
- Primeiro lugar para olhar quando o container não sobe
```bash
docker logs meu-projeto-db
```
 
### docker exec
- Roda um comando dentro de um container que já está ligado
- O `-it` abre de forma interativa, para eu poder digitar
- Exemplo prático: abrir o `psql`, o cliente de terminal do PostgreSQL
```bash
docker exec -it meu-projeto-db psql -U seu_usuario -d nome_do_banco
 
# dentro do psql:
# \l --> lista os bancos
# \q --> sai
```
 
- O `-U` é o usuário do `POSTGRES_USER`. O usuário `postgres` só existe se o `POSTGRES_USER` não for definido
- Também serve para ver com quais variáveis o container foi criado (útil quando o login dá erro):
```bash
docker exec meu-projeto-db printenv POSTGRES_USER
docker exec meu-projeto-pgadmin printenv PGADMIN_DEFAULT_EMAIL
```
 
### docker network
- Cria uma rede para os containers se enxergarem
- Na mesma rede, um container acha o outro **pelo nome**, e não pelo `localhost`
- O `localhost` de um container é ele mesmo, por isso o pgAdmin não acha o PostgreSQL por `localhost`
```bash
docker network create meu-projeto-net                   # cria a rede
docker network connect meu-projeto-net meu-projeto-db   # coloca um container que já existe na rede
docker run ... --network meu-projeto-net ...            # cria um container já dentro da rede
```
 
> ## pgAdmin
- Ferramenta visual do PostgreSQL (como o Workbench do MySQL e o Compass do MongoDB)
- Gratuita e de código aberto
- O PostgreSQL em si não tem tela: é só o servidor. O visual vem do pgAdmin, e o terminal vem do `psql`
```bash
docker network create meu-projeto-net
docker run --name meu-projeto-db -p 5432:5432 --env-file .env --network meu-projeto-net -d postgres:18.7
docker run --name meu-projeto-pgadmin -p 15432:80 --env-file .env --network meu-projeto-net -d dpage/pgadmin4:9.19
```
 
- Acesso pelo navegador: `http://localhost:15432`
- Para conectar no banco (Add New Server):
  - General --> Name: só um apelido. Padrão de mercado é projeto + ambiente (ex: `meu-projeto (local)`), para não confundir o banco local com o de produção
  - Connection --> Host:
    - Com os dois containers na mesma rede: nome do container do banco (ex: `meu-projeto-db`)
    - Sem rede: `host.docker.internal`
  - Connection --> Port = `5432`
  - Connection --> Username e Password = os do `POSTGRES_USER` e `POSTGRES_PASSWORD` (e não o login do pgAdmin)
- O cadastro do servidor fica dentro do container do pgAdmin: se o container for apagado e criado de novo, preciso cadastrar outra vez
> ## Problemas comuns
 
| Erro | Causa | Solução |
|---|---|---|
| `open .env: O sistema não pode encontrar o arquivo especificado` | Terminal fora da pasta do projeto | Entrar na pasta com `cd` antes do `docker run` |
| `server closed the connection unexpectedly` | O container do banco está desligado | `docker ps -a` para conferir e `docker start` para ligar |
| `password authentication failed for user "..."` | Usuário ou senha diferentes dos que o container usou na criação | Conferir com `docker exec ... printenv`, ou recriar o container com o `.env` atual |
| Container desliga logo depois do `docker run` | Faltou o `POSTGRES_PASSWORD` | Colocar a variável no `.env` e recriar |
| Nome repetido ao rodar `docker run` | Já existe um container com esse nome | `docker rm -f nome` ou usar `docker start` em vez de `docker run` |
| Login do pgAdmin volta sempre para a tela de login | E-mail ou senha errados, ou conta bloqueada depois de várias tentativas erradas | Conferir com `docker exec ... printenv PGADMIN_DEFAULT_EMAIL`. Se estiver bloqueada, recriar o container do pgAdmin (o banco não é afetado) |
| `relation "..." does not exist` no Query Tool | Query Tool aberto no banco errado (ex: `postgres` em vez do banco do projeto) | Abrir o Query Tool clicando no banco do projeto |
 
- No `.env`, escrever os valores sem aspas: o Docker considera as aspas como parte do valor
- Abrir o terminal direto na pasta do projeto: no Explorador de Arquivos, digitar `cmd` na barra de endereço