package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.DescartadorRequest;
import br.com.smartcollector.api.model.Descartador;
import br.com.smartcollector.api.service.DescartadorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/descartadores")
public class DescartadorController {

    private final DescartadorService descartadorService;

    public DescartadorController(DescartadorService descartadorService) {
        this.descartadorService = descartadorService;
    }

    @GetMapping
    public List<Descartador> listar() {
        return descartadorService.listar();
    }

    @GetMapping("/{id}")
    public Descartador buscar(@PathVariable Long id) {
        return descartadorService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Descartador> criar(@Valid @RequestBody DescartadorRequest requisicao,
                                             UriComponentsBuilder uriBuilder) {
        Descartador criado = descartadorService.criar(requisicao);

        return ResponseEntity
                .created(uriBuilder.path("/descartadores/{id}").buildAndExpand(criado.getId()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    public Descartador atualizar(@PathVariable Long id,
                                 @Valid @RequestBody DescartadorRequest requisicao) {
        return descartadorService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        descartadorService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
