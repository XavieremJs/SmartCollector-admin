package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.Coleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColetaRepository extends JpaRepository<Coleta, Long> {

    List<Coleta> findByFinalizadaFalseAndCatadorIsNull();

    List<Coleta> findByCatadorId(Long idCatador);
}
