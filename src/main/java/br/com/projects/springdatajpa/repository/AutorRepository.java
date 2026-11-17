package br.com.projects.springdatajpa.repository;

import br.com.projects.springdatajpa.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AutorRepository extends JpaRepository<Autor, UUID> {
}
