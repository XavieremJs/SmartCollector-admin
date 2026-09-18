package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.ItemRequest;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.model.Item;
import br.com.smartcollector.api.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> listar() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Item buscar(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item", id));
    }

    @Transactional
    public Item criar(ItemRequest requisicao) {
        Item item = new Item();
        item.setNome(requisicao.nome());
        item.setVolume(requisicao.volume());

        return itemRepository.save(item);
    }

    @Transactional
    public Item atualizar(Long id, ItemRequest requisicao) {
        Item item = buscar(id);
        item.setNome(requisicao.nome());
        item.setVolume(requisicao.volume());

        return itemRepository.save(item);
    }

    @Transactional
    public void remover(Long id) {
        itemRepository.delete(buscar(id));
    }
}
