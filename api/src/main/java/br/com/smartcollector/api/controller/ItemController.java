package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.ItemRequest;
import br.com.smartcollector.api.model.Item;
import br.com.smartcollector.api.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> listar() {
        return itemService.listar();
    }

    @GetMapping("/{id}")
    public Item buscar(@PathVariable Long id) {
        return itemService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Item> criar(@Valid @RequestBody ItemRequest requisicao,
                                      UriComponentsBuilder uriBuilder) {
        Item criado = itemService.criar(requisicao);

        return ResponseEntity
                .created(uriBuilder.path("/itens/{id}").buildAndExpand(criado.getId()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    public Item atualizar(@PathVariable Long id, @Valid @RequestBody ItemRequest requisicao) {
        return itemService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        itemService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
