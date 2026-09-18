package br.com.smartcollector.admin.bean;

import br.com.smartcollector.admin.readmodel.model.HistoricoColeta;
import br.com.smartcollector.admin.readmodel.model.RelatorioMensal;
import br.com.smartcollector.admin.service.ConsultaHistoricoService;
import br.com.smartcollector.admin.service.OutboxRelayService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//@Component("historicoBean")
@SessionScope
public class HistoricoBean implements Serializable {

    private final transient ConsultaHistoricoService consultaService;
    private final transient OutboxRelayService relayService;

    private Long idCatador;
    private String anoMes;
    private List<String> meses = new ArrayList<>();
    private List<HistoricoColeta> historico = new ArrayList<>();
    private List<RelatorioMensal> ranking = new ArrayList<>();
    private long eventosPendentes;

    public HistoricoBean(ConsultaHistoricoService consultaService,
                         OutboxRelayService relayService) {
        this.consultaService = consultaService;
        this.relayService = relayService;
    }

    @PostConstruct
    public void init() {
        meses = consultaService.mesesDisponiveis();
        if (!meses.isEmpty()) {
            anoMes = meses.get(0);
            ranking = consultaService.rankingDoMes(anoMes);
        }
        eventosPendentes = relayService.pendentes();
    }

    public void consultar() {
        if (idCatador != null) {
            historico = consultaService.historicoDoCatador(idCatador, anoMes);
        }
        if (anoMes != null) {
            ranking = consultaService.rankingDoMes(anoMes);
        }
        eventosPendentes = relayService.pendentes();
    }

    public BigDecimal getVolumeConsultado() {
        return historico.stream()
                        .map(HistoricoColeta::getVolume)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getIdCatador() { return idCatador; }
    public void setIdCatador(Long idCatador) { this.idCatador = idCatador; }
    public String getAnoMes() { return anoMes; }
    public void setAnoMes(String anoMes) { this.anoMes = anoMes; }
    public List<String> getMeses() { return meses; }
    public List<HistoricoColeta> getHistorico() { return historico; }
    public List<RelatorioMensal> getRanking() { return ranking; }
    public long getEventosPendentes() { return eventosPendentes; }
}
