package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.model.Item;
import br.com.smartcollector.admin.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> listarTodos() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Item> buscarPorNome(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return itemRepository.findByNomeContainingIgnoreCaseOrderByNome(termo.trim());
    }

    @Transactional(readOnly = true)
    public BigDecimal volumeTotalCadastrado() {
        return itemRepository.somarVolumeTotal();
    }

    @Transactional
    public void salvar(Item item) {
        Long id = item.getId() == null ? -1L : item.getId();
        if (itemRepository.existsByNomeIgnoreCaseAndIdNot(item.getNome(), id)) {
            throw new IllegalArgumentException("Ja existe um item cadastrado com este nome");
        }
        itemRepository.save(item);
    }

    @Transactional
    public void excluir(Item item) {
        itemRepository.delete(item);
    }
}
