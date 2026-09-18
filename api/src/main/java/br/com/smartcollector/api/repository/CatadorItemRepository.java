package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.CatadorItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatadorItemRepository extends JpaRepository<CatadorItem, CatadorItem.CatadorItemId> {

    List<CatadorItem> findByIdIdCatadorAndEntregueFalse(Long idCatador);
}
