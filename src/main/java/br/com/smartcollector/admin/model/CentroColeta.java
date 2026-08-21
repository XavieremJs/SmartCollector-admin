package br.com.smartcollector.admin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "TB_CENTROS_COLETA")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CentroColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "centro_gen")
    @SequenceGenerator(name = "centro_gen", sequenceName = "CENTRO_COLETA_SEQ", allocationSize = 1)
    private Long id;

    @NotBlank(message = "O endereco e obrigatorio")
    @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres")
    @Column(name = "ENDERECO", nullable = false, length = 200)
    private String endereco;

    @NotNull(message = "O volume total e obrigatorio")
    @DecimalMin(value = "0.0", message = "O volume total nao pode ser negativo")
    @Column(name = "VOLUME_ITENS_TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeTotal = BigDecimal.ZERO;

    @NotNull(message = "O volume atual e obrigatorio")
    @DecimalMin(value = "0.0", message = "O volume atual nao pode ser negativo")
    @Column(name = "VOLUME_ITENS_ATUAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeAtual = BigDecimal.ZERO;

    @Transient
    public BigDecimal getPercentualOcupacao() {
        if (volumeTotal == null || volumeTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return volumeAtual.multiply(BigDecimal.valueOf(100))
                          .divide(volumeTotal, 1, RoundingMode.HALF_UP);
    }
}
