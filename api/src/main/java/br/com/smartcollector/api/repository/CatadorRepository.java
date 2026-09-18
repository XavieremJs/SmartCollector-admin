package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.Catador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatadorRepository extends JpaRepository<Catador, Long> {
}
