package br.com.projects.springdatajpa.repository;

import br.com.projects.springdatajpa.model.Autor;
import br.com.projects.springdatajpa.model.GeneroLivro;
import br.com.projects.springdatajpa.model.Livro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Classe de teste não precisa ser public

@SpringBootTest
//@Transactional
class LivroRepositoryTest {

    @Autowired
    LivroRepository livroRepository;

    @Autowired
    AutorRepository autorRepository;

    @Test
    @DisplayName("save: deve criar o livro")
    void salvarTest() {
        Livro livro = new Livro();
        livro.setIsbn("90887-84874");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("UFO");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));

        Autor autor = autorRepository
                .findById(UUID.fromString("d3f42ce6-4ec2-49cc-8454-6404707d1fb9"))
                .orElseThrow();

        livro.setAutor(autor);

        var livroSalvo = livroRepository.save(livro);
        assertNotNull(livroSalvo.getId());
    }

    @Test
    @DisplayName("save: deve criar o livro criando o autor na hora")
    void salvarCascadeTest() {
        Livro livro = new Livro();
        livro.setIsbn("90887-84874");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("UFO");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));

        Autor autor = new Autor();
        autor.setNome("João");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1964, 10, 25));

        livro.setAutor(autor);

        var livroSalvo = livroRepository.save(livro);
        assertNotNull(livroSalvo.getId());
    }
}