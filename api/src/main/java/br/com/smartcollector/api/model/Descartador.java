package br.com.smartcollector.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Especializacao de usuario: a PK e tambem FK para TB_USUARIO.
 */
@Entity
@Table(name = "TB_DESCARTADOR")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Descartador {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "ID")
    private Usuario usuario;

    @Column(name = "ENDERECO", nullable = false, length = 200)
    private String endereco;
}
