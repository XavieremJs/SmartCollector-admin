package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.ColetaRequest;
import br.com.smartcollector.api.dto.ColetaResponse;
import br.com.smartcollector.api.service.ColetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/coletas")
public class ColetaController {

    private final ColetaService coletaService;

    public ColetaController(ColetaService coletaService) {
        this.coletaService = coletaService;
    }

    @GetMapping
    public List<ColetaResponse> listar() {
        return coletaService.listar();
    }

    /** Coletas ainda sem catador — e o que um catador consulta para escolher. */
    @GetMapping("/disponiveis")
    public List<ColetaResponse> listarDisponiveis() {
        return coletaService.listarDisponiveis();
    }

    @GetMapping("/{id}")
    public ColetaResponse buscar(@PathVariable Long id) {
        return coletaService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ColetaResponse> criar(@Valid @RequestBody ColetaRequest requisicao,
                                                UriComponentsBuilder uriBuilder) {
        ColetaResponse criada = coletaService.criar(requisicao);

        return ResponseEntity
                .created(uriBuilder.path("/coletas/{id}").buildAndExpand(criada.id()).toUri())
                .body(criada);
    }

    @PutMapping("/{id}/aceitar/{idCatador}")
    public ColetaResponse aceitar(@PathVariable Long id, @PathVariable Long idCatador) {
        return coletaService.aceitar(id, idCatador);
    }

    @PutMapping("/{id}/finalizar/{idCentro}")
    public ColetaResponse finalizar(@PathVariable Long id, @PathVariable Long idCentro) {
        return coletaService.finalizar(id, idCentro);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        coletaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
