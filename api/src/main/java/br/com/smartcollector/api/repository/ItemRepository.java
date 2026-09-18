package br.com.smartcollector.api.repository;

import br.com.smartcollector.api.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
