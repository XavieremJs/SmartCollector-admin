package br.com.smartcollector.admin.readmodel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de idempotencia. Se o relay reentregar um evento (por exemplo,
 * apos uma falha entre a projecao e a marcacao no Oracle), a chave primaria
 * impede que ele seja aplicado duas vezes.
 */
@Entity
@Table(name = "evento_aplicado")
@Getter
@Setter
@NoArgsConstructor
public class EventoAplicado {

    @Id
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "tipo_evento", nullable = false, length = 60)
    private String tipoEvento;

    @Column(name = "aplicado_em", nullable = false)
    private LocalDateTime aplicadoEm;

    public EventoAplicado(Long idEvento, String tipoEvento) {
        this.idEvento = idEvento;
        this.tipoEvento = tipoEvento;
        this.aplicadoEm = LocalDateTime.now();
    }
}
