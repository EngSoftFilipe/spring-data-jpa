# Spring Data JPA

> ## Importante
- JPA são as regras (`@Entity`, `@Id`, `@ManyToOne`...), o Hibernate é quem executa (transforma as regras em SQL) e o Spring Data JPA tira o código repetitivo (repositories prontos, consultas pelo nome do método)
- O código Java e o banco podem ter nomes diferentes: quem faz a ponte são o `@Table` e o `@Column`
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
| `@Data` | Getters, setters, `equals`, `hashCode` e `toString` juntos |

- **Recomendado em entidades:** `@Getter` e `@Setter` separados
- **Não recomendado em entidades:** `@Data`, porque o `equals`, o `hashCode` e o `toString` usam todos os atributos. Com relacionamentos (Autor --> Livros --> Autor...), pode gerar consultas desnecessárias ou até um loop infinito (`StackOverflowError`)
- Nos DTOs, os `records` do Java estão substituindo o Lombok
- Precisa da dependência no `pom.xml` e do annotation processing ativado na IDE