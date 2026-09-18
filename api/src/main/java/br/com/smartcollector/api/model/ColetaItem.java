package br.com.smartcollector.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Itens que compoem uma coleta (N:N entre coleta e item).
 */
@Entity
@Table(name = "TB_COLETA_ITENS")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ColetaItem {

    @EmbeddedId
    private ColetaItemId id;

    public ColetaItem(Long idColeta, Long idItem) {
        this.id = new ColetaItemId(idColeta, idItem);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ColetaItemId implements Serializable {

        @Column(name = "ID_COLETA")
        private Long idColeta;

        @Column(name = "ID_ITEM")
        private Long idItem;

        public ColetaItemId(Long idColeta, Long idItem) {
            this.idColeta = idColeta;
            this.idItem = idItem;
        }
    }
}
