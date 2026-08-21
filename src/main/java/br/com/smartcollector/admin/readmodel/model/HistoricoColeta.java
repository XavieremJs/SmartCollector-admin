package br.com.smartcollector.admin.readmodel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_coleta")
@Getter
@Setter
@NoArgsConstructor
public class HistoricoColeta {

    @Id
    @Column(name = "id_coleta")
    private Long idColeta;

    @Column(name = "id_catador", nullable = false)
    private Long idCatador;

    @Column(name = "id_centro", nullable = false)
    private Long idCentro;

    @Column(name = "endereco_centro", nullable = false, length = 200)
    private String enderecoCentro;

    @Column(name = "volume", nullable = false, precision = 10, scale = 2)
    private BigDecimal volume;

    @Column(name = "qtd_itens", nullable = false)
    private Integer qtdItens;

    @Column(name = "data_entrega", nullable = false)
    private LocalDateTime dataEntrega;

    @Column(name = "ano_mes", nullable = false, length = 7)
    private String anoMes;
}
