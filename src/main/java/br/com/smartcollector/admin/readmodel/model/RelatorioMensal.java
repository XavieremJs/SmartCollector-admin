package br.com.smartcollector.admin.readmodel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "relatorio_mensal")
@IdClass(RelatorioMensal.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class RelatorioMensal {

    @Id
    @Column(name = "ano_mes", length = 7)
    private String anoMes;

    @Id
    @Column(name = "id_catador")
    private Long idCatador;

    @Column(name = "total_coletas", nullable = false)
    private Integer totalColetas = 0;

    @Column(name = "volume_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal volumeTotal = BigDecimal.ZERO;

    @Column(name = "itens_total", nullable = false)
    private Integer itensTotal = 0;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public void acumular(BigDecimal volume, int qtdItens) {
        this.totalColetas += 1;
        this.volumeTotal = this.volumeTotal.add(volume);
        this.itensTotal += qtdItens;
        this.atualizadoEm = LocalDateTime.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Chave implements Serializable {
        private String anoMes;
        private Long idCatador;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Chave chave)) return false;
            return Objects.equals(anoMes, chave.anoMes)
                && Objects.equals(idCatador, chave.idCatador);
        }

        @Override
        public int hashCode() {
            return Objects.hash(anoMes, idCatador);
        }
    }
}
