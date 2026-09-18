package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.ColetaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColetaItemRepository extends JpaRepository<ColetaItem, ColetaItem.ColetaItemId> {

    List<ColetaItem> findByIdIdColeta(Long idColeta);
}
