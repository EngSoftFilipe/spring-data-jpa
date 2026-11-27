package br.com.projects.springdatajpa.service;

import br.com.projects.springdatajpa.model.Autor;
import br.com.projects.springdatajpa.repository.AutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AutorService {

    private final AutorRepository repository;

    public AutorService(AutorRepository repository){
        this.repository = repository;
    }

    public Autor salvar(Autor autor){
        return repository.save(autor);
    }

    public Optional<Autor> obterPorId(UUID id){
        return repository.findById(id);
    }

    public void deletar(Autor autor){
        repository.delete(autor);
    }

    public List<Autor> pesquisaDetalhada(String nome, String nacionalidade){
        if(nome != null && nacionalidade != null){
            return repository.findByNomeContainingIgnoreCaseAndNacionalidadeContainingIgnoreCase(nome, nacionalidade);
        }
        if(nome != null){
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        if(nacionalidade != null){
            return repository.findByNacionalidadeContainingIgnoreCase(nacionalidade);
        }
        return repository.findAll();
    }

    public Optional<Autor> atualizarAutor(UUID id, Autor dadosAtualizados){
        return repository.findById(id).map(autor -> {
          autor.setNome(dadosAtualizados.getNome());
          autor.setDataNascimento(dadosAtualizados.getDataNascimento());
          autor.setNacionalidade(dadosAtualizados.getNacionalidade());
          return repository.save(autor);
        });
    }
}