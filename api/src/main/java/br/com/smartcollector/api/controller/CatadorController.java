package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.CatadorRequest;
import br.com.smartcollector.api.model.Catador;
import br.com.smartcollector.api.service.CatadorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/catadores")
public class CatadorController {

    private final CatadorService catadorService;

    public CatadorController(CatadorService catadorService) {
        this.catadorService = catadorService;
    }

    @GetMapping
    public List<Catador> listar() {
        return catadorService.listar();
    }

    @GetMapping("/{id}")
    public Catador buscar(@PathVariable Long id) {
        return catadorService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Catador> criar(@Valid @RequestBody CatadorRequest requisicao,
                                         UriComponentsBuilder uriBuilder) {
        Catador criado = catadorService.criar(requisicao);

        return ResponseEntity
                .created(uriBuilder.path("/catadores/{id}").buildAndExpand(criado.getId()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    public Catador atualizar(@PathVariable Long id, @Valid @RequestBody CatadorRequest requisicao) {
        return catadorService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        catadorService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
