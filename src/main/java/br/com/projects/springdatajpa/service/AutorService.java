package br.com.projects.springdatajpa.service;

import br.com.projects.springdatajpa.model.Autor;
import br.com.projects.springdatajpa.repository.AutorRepository;
import org.springframework.stereotype.Service;

@Service
public class AutorService {

    private final AutorRepository repository;

    public AutorService(AutorRepository repository){
        this.repository = repository;
    }

    public Autor salvar(Autor autor){
        return repository.save(autor);
    }
}