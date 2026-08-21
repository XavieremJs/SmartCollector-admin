package br.com.smartcollector.admin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_OUTBOX_EVENTO")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvento {

    public enum Status { PENDENTE, PROCESSADO, FALHA }

    /** Numero de tentativas apos o qual o evento vai para FALHA. */
    public static final int MAX_TENTATIVAS = 5;

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "TIPO_EVENTO", nullable = false, length = 60)
    private String tipoEvento;

    @Column(name = "AGREGADO_ID", nullable = false)
    private Long agregadoId;

    @Lob
    @Column(name = "PAYLOAD", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status = Status.PENDENTE;

    @Column(name = "TENTATIVAS", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "ULTIMO_ERRO", length = 500)
    private String ultimoErro;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "PROCESSADO_EM")
    private LocalDateTime processadoEm;

    public void marcarProcessado() {
        this.status = Status.PROCESSADO;
        this.processadoEm = LocalDateTime.now();
        this.ultimoErro = null;
    }

    public void registrarFalha(String erro) {
        this.tentativas++;
        this.ultimoErro = erro != null && erro.length() > 500 ? erro.substring(0, 500) : erro;
        if (this.tentativas >= MAX_TENTATIVAS) {
            this.status = Status.FALHA;
        }
    }
}
