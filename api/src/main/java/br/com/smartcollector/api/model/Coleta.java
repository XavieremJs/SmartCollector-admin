package br.com.smartcollector.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_COLETAS")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Coleta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coleta_gen")
    @SequenceGenerator(name = "coleta_gen", sequenceName = "COLETA_SEQ", allocationSize = 1)
    @Column(name = "ID_COLETA")
    private Long id;

    @Column(name = "DATA_COLETA", nullable = false)
    private LocalDateTime dataColeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CATADOR")
    private Catador catador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DESCARTADOR")
    private Descartador descartador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CENTRO")
    private CentroColeta centro;

    @Column(name = "FOI_FINALIZADA", nullable = false)
    private Boolean finalizada = false;

    @Transient
    public boolean estaAceita() {
        return catador != null;
    }
}
