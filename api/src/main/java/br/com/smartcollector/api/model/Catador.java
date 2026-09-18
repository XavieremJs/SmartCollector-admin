package br.com.smartcollector.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Especializacao de usuario: a PK e tambem FK para TB_USUARIO.
 */
@Entity
@Table(name = "TB_CATADOR")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Catador {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "ID")
    private Usuario usuario;

    @Column(name = "CAPACIDADE_VOLUME_TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadeTotal = BigDecimal.ZERO;
}
