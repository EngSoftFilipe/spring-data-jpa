package br.com.projects.springdatajpa.controller;

import br.com.projects.springdatajpa.controller.dto.AutorDTO;
import br.com.projects.springdatajpa.model.Autor;
import br.com.projects.springdatajpa.service.AutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/autores")
public class AutorController {
    private final AutorService service;

    public AutorController(AutorService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> cadastrarAutor(@RequestBody AutorDTO dto){
        Autor autor = dto.mapearParaAutor();
        service.salvar(autor);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(autor.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}