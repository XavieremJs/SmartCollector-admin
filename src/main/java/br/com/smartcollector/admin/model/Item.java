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

@Entity
@Table(name = "TB_ITENS")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_gen")
    @SequenceGenerator(name = "item_gen", sequenceName = "ITEM_SEQ", allocationSize = 1)
    private Long id;

    @NotBlank(message = "O nome do item e obrigatorio")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @NotNull(message = "O volume e obrigatorio")
    @DecimalMin(value = "0.0", message = "O volume nao pode ser negativo")
    @Column(name = "VOLUME", nullable = false, precision = 10, scale = 2)
    private BigDecimal volume = BigDecimal.ZERO;
}
