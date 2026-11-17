package br.com.projects.springdatajpa.repository;

import br.com.projects.springdatajpa.model.Autor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
public class AutorRepositoryTest {

    @Autowired
    AutorRepository repository;

    @Test
    public void salvarTest(){
        Autor autor = new Autor();
        autor.setNome("José");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 25));

        var autorSalvo = repository.save(autor);
        System.out.println("Autor Salvo: " + autorSalvo.getId());
    }

    @Test
    public void atualizarTest(){
        var id = UUID.fromString("4afbaf42-a4d6-4b2f-9137-2192208af3db");
        Optional<Autor> possivelAutor = repository.findById(id);

        if(possivelAutor.isPresent()){
            Autor autorEncontrado = possivelAutor.get();
            System.out.println("Dados do autor: ");
            System.out.println(autorEncontrado);

            autorEncontrado.setDataNascimento(LocalDate.of(1975, 8, 15));

            repository.save(autorEncontrado);
        }
    }
}