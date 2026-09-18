package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.CentroColetaRequest;
import br.com.smartcollector.api.model.CentroColeta;
import br.com.smartcollector.api.service.CentroColetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/centros")
public class CentroColetaController {

    private final CentroColetaService centroService;

    public CentroColetaController(CentroColetaService centroService) {
        this.centroService = centroService;
    }

    @GetMapping
    public List<CentroColeta> listar() {
        return centroService.listar();
    }

    @GetMapping("/{id}")
    public CentroColeta buscar(@PathVariable Long id) {
        return centroService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CentroColeta> criar(@Valid @RequestBody CentroColetaRequest requisicao,
                                              UriComponentsBuilder uriBuilder) {
        CentroColeta criado = centroService.criar(requisicao);

        return ResponseEntity
                .created(uriBuilder.path("/centros/{id}").buildAndExpand(criado.getId()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    public CentroColeta atualizar(@PathVariable Long id,
                                  @Valid @RequestBody CentroColetaRequest requisicao) {
        return centroService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        centroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
