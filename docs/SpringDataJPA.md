# Spring Data JPA

> ## Importante
- JPA são as regras (`@Entity`, `@Id`, `@ManyToOne`...), o Hibernate é quem executa (transforma as regras em SQL) e o Spring Data JPA tira o código repetitivo (repositories prontos, consultas pelo nome do método)
- O código Java e o banco podem ter nomes diferentes: quem faz a ponte são o `@Table` e o `@Column`
- No log do `show-sql`, os valores aparecem como `?`: o Hibernate manda os dados separados do comando (`PreparedStatement`), e é isso que protege contra SQL injection
> ## Mapeamento da entidade

### @Column
- Diz qual **coluna do banco** o atributo representa, e as regras dela (tamanho, se aceita nulo)
- Usado em cima do atributo da entidade
- Exemplo prático: no Java o atributo é `dataNascimento`, e no banco a coluna é `data_nascimento`. O `@Column` liga os dois
```java
@Column(name = "data_nascimento", nullable = false)
private LocalDate dataNascimento;
```

| Parte | O que faz |
|---|---|
| `name` | Nome da coluna no banco |
| `length` | Tamanho máximo do texto (igual ao `varchar(100)` do banco) |
| `nullable = false` | A coluna não aceita nulo (igual ao `not null` do banco) |
| `precision` e `scale` | Total de dígitos e casas decimais (igual ao `numeric(18,2)` do banco) |

#### Mesmo idioma no Java e no banco
- Se o Java e o banco usam o mesmo idioma, o `name` do `@Column` vira só uma **confirmação**, porque o Hibernate já encontra a coluna sozinho
- O Hibernate converte o camelCase do Java para o snake_case do banco: `dataNascimento` --> `data_nascimento`
- Mesmo assim, o `@Column` continua útil para as regras (`length`, `nullable`)
```java
@Column(name = "nome", length = 100, nullable = false)   // o name aqui só confirma
private String nome;
```

- Se o Java e o banco usam idiomas diferentes, o `name` é **obrigatório**: sem ele, o Hibernate procuraria uma coluna que não existe
```java
@Column(name = "data_nascimento")   // sem o name, procuraria a coluna birth_date
private LocalDate birthDate;
```

- O mesmo vale para o `@Table(name = "...")`: com nomes diferentes, sem ele o Hibernate procuraria uma tabela que não existe
### @Id e @GeneratedValue
- `@Id` marca a chave primária
- `@GeneratedValue` diz que quem gera o valor não sou eu
- Com `GenerationType.UUID`, o Hibernate gera o UUID no Java antes de salvar
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

### @Enumerated
- Diz como um `enum` é salvo no banco
- **Recomendado:** `EnumType.STRING`, que salva o nome (`FICCAO`)
- **Não recomendado:** `EnumType.ORDINAL` (o padrão), que salva a posição (0, 1, 2...). Se alguém mudar a ordem do enum ou colocar um valor no meio, os dados antigos passam a apontar para o valor errado
```java
@Enumerated(EnumType.STRING)
@Column(name = "genero", length = 30)
private GeneroLivro genero;
```

### BigDecimal no lugar de Double
- Para dinheiro, usar `BigDecimal` no Java e `numeric` no banco
- O `Double` guarda valores aproximados e pode gerar erros de arredondamento
- O `BigDecimal` guarda o valor exato
```java
// Não recomendado: valor aproximado
double total = 0.1 + 0.2;   // --> 0.30000000000000004
 
// Recomendado: valor exato
@Column(name = "preco", precision = 18, scale = 2)
private BigDecimal preco;
```

- Com `scale = 2`, o `BigDecimal.valueOf(100)` vai para o banco como `100.00`
> ## Relacionamentos

### @ManyToOne
- `@ManyToOne` --> Many (muitos) Livros To (para) One (um) Autor
- Usado no lado que tem a chave estrangeira no banco (a tabela `livro` tem a coluna `id_autor`)
- No Java, o livro não guarda só o id do autor: guarda o objeto `Autor` inteiro
- Exemplo prático: o Machado de Assis escreveu vários livros. Cada livro aponta para o mesmo autor
```text
tabela autor                         tabela livro
id  | nome                           id | titulo               | id_autor
----+-----------------               ---+----------------------+---------
a1  | Machado de Assis               l1 | Dom Casmurro         | a1
a2  | Clarice Lispector              l2 | Memórias Póstumas    | a1
                                     l3 | A Hora da Estrela    | a2
```

```java
public class Livro {
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false)
    private Autor autor;
}
 
// livro.getAutor().getNome() --> "Machado de Assis"
```

#### LAZY e EAGER
- O `@ManyToOne` é **EAGER** por padrão: busca o autor toda vez que busca um livro, mesmo sem precisar
- Com uma lista de livros, isso pode virar uma consulta extra para cada livro (problema N+1)
- **Recomendado:** `fetch = FetchType.LAZY`, que só busca o autor quando `livro.getAutor()` for chamado
- **Funciona, mas não é o ideal:** deixar o padrão (EAGER), porque pesa com listas grandes
### @JoinColumn
- Diz **qual coluna** do banco guarda a ligação do relacionamento
- Usado junto com o `@ManyToOne`, em cima do atributo
- Exemplo prático: na tabela `livro`, quem aponta para o autor é a coluna `id_autor`
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_autor", nullable = false)
private Autor autor;
```

- Para salvar, passo o objeto `Autor`, e o Hibernate grava o id dele na coluna:
```java
livro.setAutor(machado);
livroRepository.save(livro);   // --> grava "a1" na coluna id_autor
```

### @OneToMany
- O outro lado do relacionamento: One (um) Autor To (para) Many (muitos) Livros
- Fica na entidade que **não** tem a chave estrangeira (o `Autor`), como uma lista
- O `mappedBy` recebe o **nome do atributo** do outro lado (`autor`, em minúsculo, igual está no `Livro`), e não o nome da classe
- Já é LAZY por padrão: a lista só é buscada quando alguém usa
```java
public class Autor {
 
    @OneToMany(mappedBy = "autor")
    private List<Livro> livros;
}
```

- Erro comum: `mappedBy = "Autor"` (com maiúscula) ou a lista na classe errada. O Hibernate não acha o atributo e a aplicação não sobe
### Cascade
- O que eu fizer com um objeto, o Hibernate faz também com o objeto ligado a ele
- Exemplo prático: salvar um livro com um autor novo (que ainda não está no banco). Sem cascade, dá erro (`object references an unsaved transient instance`); com `PERSIST`, o autor é salvo junto
```java
@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
@JoinColumn(name = "id_autor", nullable = false)
private Autor autor;
```

| Tipo | Quando eu... | ...faz o mesmo com o ligado |
|---|---|---|
| `PERSIST` | salvo um objeto novo | salva o ligado também |
| `MERGE` | atualizo | atualiza o ligado |
| `REMOVE` | excluo | exclui o ligado também |
| `ALL` | qualquer operação | todas as acima |

- **Recomendado:** cascade do dono para as partes, no `@OneToMany` (ex: pedido --> itens do pedido, que não existem sem o pedido)
- **Funciona, mas não é o ideal:** `PERSIST` no `@ManyToOne` (livro --> autor), porque o autor existe sozinho
- **Não recomendado:** `ALL` ou `REMOVE` no `@ManyToOne`: excluir um livro tentaria excluir o autor (e afetaria os outros livros dele)
> ## Flyway

### O que é
- Ferramenta que cria e versiona as tabelas a partir de scripts SQL que eu escrevo
- Cada script roda **uma vez só**, na ordem da versão, quando a aplicação sobe
- O Flyway registra o que já rodou na tabela `flyway_schema_history`
- Só roda se a dependência estiver no `pom.xml` (autoconfiguração do Spring Boot)
```text
src/main/resources/db/migration
└── V1__cria_tabelas_autor_e_livro.sql
```

- Nome do arquivo: `V` + versão + dois underlines + descrição (`V2__adiciona_coluna_email.sql`)
- **Nunca editar um script que já rodou**: o Flyway compara o checksum e a aplicação não sobe. Para mudar algo, criar um novo script (`V2`, `V3`...)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### ddl-auto
- Diz o que o Hibernate faz com as tabelas quando a aplicação sobe
| Valor | O que faz |
|---|---|
| `none` | Não faz nada |
| `validate` | Confere se as entidades batem com as tabelas, e não sobe se não baterem |
| `update` | Altera as tabelas para ficarem iguais às entidades |
| `create` | Apaga e cria as tabelas toda vez que sobe |
| `create-drop` | Cria ao subir e apaga ao desligar |

- **Recomendado:** `validate` junto com o Flyway (o Flyway cria, o Hibernate só confere)
- **Funciona, mas não é o ideal:** `update` em desenvolvimento, porque não deixa histórico das mudanças
- **Não recomendado:** `create` ou `create-drop` com dados reais, porque apagam tudo
> ## Repository

### JpaRepository
- Interface que já vem com os métodos de acesso ao banco prontos, sem eu escrever implementação
- Recebe a entidade e o tipo do id
```java
public interface AutorRepository extends JpaRepository<Autor, UUID> {
}
```

| Método | O que faz |
|---|---|
| `save(objeto)` | Insere se o id for novo, atualiza se o id já existir |
| `saveAndFlush(objeto)` | Igual ao `save`, mas manda o SQL para o banco na hora |
| `findById(id)` | Busca pelo id e devolve um `Optional` |
| `findAll()` | Lista todos |
| `count()` | Quantidade de registros |
| `deleteById(id)` | Exclui pelo id |
| `delete(objeto)` | Exclui pelo objeto |

- O mesmo `save` serve para inserir e para atualizar
### Optional (orElse, orElseThrow, orElseGet)
- O `findById` devolve um `Optional`, porque o registro pode não existir
- `orElse(valor)`: se não achar, usa esse valor no lugar
- `orElseThrow()`: se não achar, lança exceção e para ali
- `orElseGet(() -> ...)`: igual ao `orElse`, mas só calcula o valor se o `Optional` estiver vazio
```java
// Recomendado quando o dado precisa existir (atualizar, excluir, ligar a outro objeto)
Autor autor = repository.findById(id).orElseThrow();
 
// Com a minha própria exceção
Autor autor = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Autor não encontrado"));
```

- **Recomendado:** `orElseThrow` quando o dado é obrigatório, porque o erro aparece onde o problema nasceu
- **Funciona, mas não é o ideal:** `orElse(null)`, porque o `null` estoura mais à frente, longe da causa
- **Funciona, mas não é o ideal:** `.get()`, que faz o mesmo que o `orElseThrow()`, mas deixa a intenção menos clara
- O valor dentro do `orElse` é **sempre** calculado, mesmo quando acha o registro. Se for caro de gerar, usar `orElseGet`
- Na mensagem da exceção, não colocar detalhes internos (SQL, nome de tabela), porque podem ir parar na resposta da API
> ## Transações

### @Transactional
- Transação é um pacote de operações no banco que vale tudo ou nada
- No final, ou faz **commit** (confirma tudo) ou **rollback** (desfaz tudo)
- Exemplo prático: um Pix. Sair o dinheiro de uma conta e entrar na outra precisa acontecer junto
- Import do Spring: `org.springframework.transaction.annotation.Transactional` (o do `jakarta` funciona, mas tem menos recursos)
- Na aplicação, faz commit no final do método
- Rollback automático só com `RuntimeException` (e filhas). Com exceção checada (`IOException`...), por padrão faz commit mesmo assim
```java
@Transactional(rollbackFor = Exception.class)   // faz rollback com qualquer exceção
public void transferir(...) { ... }
```

- Em regra crítica (pagamento, saldo), usar o `rollbackFor`, para o banco não ficar pela metade
### Dirty checking
- Dentro de uma transação, o Hibernate vigia os objetos que buscou
- Se eu alterar um atributo, ele gera o `update` sozinho no final, mesmo sem chamar `save`
```java
@Transactional
public void atualizarData(UUID id) {
    Autor autor = repository.findById(id).orElseThrow();
    autor.setDataNascimento(LocalDate.of(1975, 8, 15));   // vira update no commit
}
```

> ## Testes

### @SpringBootTest
- Sobe a aplicação inteira no teste, com o mesmo `application.yaml` e o mesmo banco
- Sem ele, o Spring não participa do teste e o `@Autowired` fica `null`
- `@Autowired` no atributo é aceitável em teste, porque quem cria a classe é o JUnit
- No JUnit 5, a classe e os métodos de teste não precisam ser `public`
```java
@SpringBootTest
class AutorRepositoryTest {
 
    @Autowired
    AutorRepository repository;
 
    @Test
    @DisplayName("save: deve criar o autor")
    void salvarTest() {
        Autor autor = new Autor();
        autor.setNome("José");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 25));
 
        var autorSalvo = repository.save(autor);
        assertNotNull(autorSalvo.getId());
    }
}
```

- Como usa o banco de verdade, o `save` do teste salva de verdade
### @Transactional no teste
- Em teste, o `@Transactional` faz **rollback** automático no final de cada método, e o banco fica como estava
- Na classe vale para todos os métodos, mas também pode ir em um método só
- O Hibernate pode segurar o SQL até o commit. Como o teste não faz commit, o SQL pode nem aparecer no log. O `saveAndFlush` força o envio na hora
- `@Commit` (`org.springframework.test.annotation.Commit`) faz um teste específico salvar de verdade
- O rollback só desfaz o que está dentro daquela transação (código em outra thread ou em outra transação não é desfeito)
- Nunca apontar os testes para o banco de produção: um `@Commit` esquecido altera dados reais
### Teste de integração x teste unitário

| | Teste unitário | Teste de integração |
|---|---|---|
| Usa banco? | Não (usa mock) | Sim |
| Sobe o Spring? | Não | Sim (`@SpringBootTest`) |
| Velocidade | Milissegundos | Segundos |
| Testa | A lógica de uma classe | As peças funcionando juntas |

- `@DataJpaTest`: sobe só a parte de JPA (repositories e entidades), sem o resto da aplicação. Já vem com rollback automático (slice test)
- Pirâmide de testes: muitos unitários e menos de integração, porque os de integração são mais lentos
- Padrão de mercado: banco separado só para os testes (ex: Testcontainers, que sobe um PostgreSQL descartável no Docker)
### @DisplayName
- O que vem antes dos dois-pontos é o alvo do teste: o método no repository (`save:`, `findAll:`) e a rota no controller (`POST /products:`)
- Não usar verbo HTTP em teste de repository, porque o repository não trabalha com HTTP
> ## Lombok

### Lombok
- Biblioteca que gera getters, setters e construtores na compilação, a partir de annotations
- Os métodos existem, mas não aparecem no código
- Muito usado em projetos Spring, principalmente nas entidades
```java
@Entity
@Table(name = "autor")
@Getter
@Setter
@NoArgsConstructor
public class Autor { ... }
```

| Annotation | O que gera |
|---|---|
| `@Getter` | Os getters de todos os atributos |
| `@Setter` | Os setters de todos os atributos |
| `@NoArgsConstructor` | O construtor vazio (obrigatório para o JPA) |
| `@ToString` | O `toString` com todos os atributos |
| `@Data` | Getters, setters, `equals`, `hashCode` e `toString` juntos |

- **Recomendado em entidades:** `@Getter` e `@Setter` separados
- **Não recomendado em entidades:** `@Data`, porque o `equals`, o `hashCode` e o `toString` usam todos os atributos. Com relacionamentos (Autor --> Livros --> Autor...), pode gerar consultas desnecessárias ou até um loop infinito (`StackOverflowError`)
- Nos DTOs, os `records` do Java estão substituindo o Lombok
- Precisa da dependência no `pom.xml` e do annotation processing ativado na IDE
### @ToString e @ToString.Exclude
- Sem `toString`, o Java imprime o padrão da classe `Object`: o nome da classe e um código de memória (`Autor@3f2a1b`)
- Em entidade com relacionamento, a lista precisa ficar de fora do `toString`, senão ele busca a lista no banco (ou dá `LazyInitializationException`) e pode entrar em loop com o outro lado
```java
@ToString
public class Autor {
 
    @OneToMany(mappedBy = "autor")
    @ToString.Exclude
    private List<Livro> livros;
}
```

- **Recomendado:** `@ToString.Exclude` em cima do atributo
- **Funciona, mas não é o ideal:** `@ToString(exclude = "livros")` na classe, porque o nome fica numa String e para de funcionar sem erro se o atributo for renomeado