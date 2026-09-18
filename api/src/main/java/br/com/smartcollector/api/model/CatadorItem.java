package br.com.smartcollector.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Itens em posse do catador. FOI_ENTREGUE vira 1 quando a coleta e
 * finalizada num centro — a rotina PL/SQL PRC_REGISTRAR_ENTREGA cuida disso,
 * e FN_CAPACIDADE_DISPONIVEL le estas linhas para calcular quanto o catador
 * ainda consegue carregar.
 */
@Entity
@Table(name = "TB_CATADOR_ITEM")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CatadorItem {

    @EmbeddedId
    private CatadorItemId id;

    @Column(name = "FOI_ENTREGUE", nullable = false)
    private Boolean entregue = false;

    public CatadorItem(Long idCatador, Long idItem) {
        this.id = new CatadorItemId(idCatador, idItem);
        this.entregue = false;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class CatadorItemId implements Serializable {

        @Column(name = "ID_CATADOR")
        private Long idCatador;

        @Column(name = "ID_ITEM")
        private Long idItem;

        public CatadorItemId(Long idCatador, Long idItem) {
            this.idCatador = idCatador;
            this.idItem = idItem;
        }
    }
}
