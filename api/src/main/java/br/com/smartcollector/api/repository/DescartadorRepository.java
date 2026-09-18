package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.Descartador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescartadorRepository extends JpaRepository<Descartador, Long> {
}
