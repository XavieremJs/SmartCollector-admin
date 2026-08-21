package br.com.smartcollector.admin.repository;

import br.com.smartcollector.admin.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    @Query("SELECT COALESCE(SUM(i.volume), 0) FROM Item i")
    java.math.BigDecimal somarVolumeTotal();

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query("SELECT i FROM Item i WHERE i.volume >= :minimo ORDER BY i.volume DESC")
    List<Item> buscarAcimaDoVolume(@Param("minimo") java.math.BigDecimal minimo);
}
