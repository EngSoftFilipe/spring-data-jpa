package br.com.projects.springdatajpa.repository;

import br.com.projects.springdatajpa.model.Autor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Transactional
public class AutorRepositoryTest {

    @Autowired
    AutorRepository repository;

    @Test
    @DisplayName("count: deve mostrar a quantidade de autores")
    public void qtdAutoresTest(){
        System.out.println("Quantidade de autores: " + repository.count());
    }

    @Test
    @DisplayName("save: deve criar o autor")
    public void salvarTest(){
        Autor autor = new Autor();
        autor.setNome("José");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 25));

        var autorSalvo = repository.save(autor);
        System.out.println("Autor Salvo: " + autorSalvo.getId());
    }

    @Test
    @DisplayName("save: deve atualizar o autor")
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

    @Test
    @DisplayName("findAll: deve listar todos os autores")
    public void listarTest(){
        List<Autor> lista = repository.findAll();
        lista.forEach(System.out::println);
    }

    @Test
    @DisplayName("delete: deve excluir autor por ID")
    public void excluirPorIdTest(){
        var id = UUID.fromString("4afbaf42-a4d6-4b2f-9137-2192208af3db");
        repository.deleteById(id);
    }

    @Test
    @DisplayName("delete: deve excluir autor pelo objeto")
    public void excluirPorObjetoTest(){
        var id = UUID.fromString("35796157-a27e-423e-82d6-63106ee3e5bc");
        var objeto = repository.findById(id).orElseThrow();
        repository.delete(objeto);

    }
}