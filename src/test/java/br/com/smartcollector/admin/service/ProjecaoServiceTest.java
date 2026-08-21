package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.readmodel.model.EventoAplicado;
import br.com.smartcollector.admin.readmodel.model.HistoricoColeta;
import br.com.smartcollector.admin.readmodel.model.RelatorioMensal;
import br.com.smartcollector.admin.readmodel.repository.EventoAplicadoRepository;
import br.com.smartcollector.admin.readmodel.repository.HistoricoColetaRepository;
import br.com.smartcollector.admin.readmodel.repository.RelatorioMensalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjecaoServiceTest {

    private static final String PAYLOAD = """
        {"idColeta":10,"idCatador":7,"idCentro":3,
         "enderecoCentro":"Rua das Flores, 200","volume":25.50,
         "qtdItens":4,"dataEntrega":"2026-08-15T14:30:00"}
        """;

    @Mock private HistoricoColetaRepository historicoRepository;
    @Mock private RelatorioMensalRepository relatorioRepository;
    @Mock private EventoAplicadoRepository eventoAplicadoRepository;

    private ProjecaoService projecaoService;

    @BeforeEach
    void setUp() {
        projecaoService = new ProjecaoService(historicoRepository, relatorioRepository,
                                             eventoAplicadoRepository, new ObjectMapper());
    }

    @Test
    @DisplayName("Deve projetar o evento no historico e no relatorio mensal")
    void deveProjetarEvento() throws Exception {
        when(eventoAplicadoRepository.existsById(1L)).thenReturn(false);
        when(relatorioRepository.findByAnoMesAndIdCatador("2026-08", 7L))
            .thenReturn(Optional.empty());

        projecaoService.aplicar(1L, "COLETA_ENTREGUE", PAYLOAD);

        ArgumentCaptor<HistoricoColeta> hist = ArgumentCaptor.forClass(HistoricoColeta.class);
        verify(historicoRepository).save(hist.capture());
        assertEquals(10L, hist.getValue().getIdColeta());
        assertEquals("2026-08", hist.getValue().getAnoMes());
        assertEquals(0, new BigDecimal("25.50").compareTo(hist.getValue().getVolume()));

        ArgumentCaptor<RelatorioMensal> rel = ArgumentCaptor.forClass(RelatorioMensal.class);
        verify(relatorioRepository).save(rel.capture());
        assertEquals(1, rel.getValue().getTotalColetas());
        assertEquals(4, rel.getValue().getItensTotal());

        verify(eventoAplicadoRepository).save(any(EventoAplicado.class));
    }

    @Test
    @DisplayName("Evento ja aplicado deve ser ignorado (idempotencia)")
    void deveIgnorarEventoDuplicado() throws Exception {
        when(eventoAplicadoRepository.existsById(1L)).thenReturn(true);

        projecaoService.aplicar(1L, "COLETA_ENTREGUE", PAYLOAD);

        verify(historicoRepository, never()).save(any());
        verify(relatorioRepository, never()).save(any());
        verify(eventoAplicadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve acumular no relatorio mensal ja existente")
    void deveAcumularNoRelatorioExistente() throws Exception {
        RelatorioMensal existente = new RelatorioMensal();
        existente.setAnoMes("2026-08");
        existente.setIdCatador(7L);
        existente.setTotalColetas(2);
        existente.setVolumeTotal(new BigDecimal("100.00"));
        existente.setItensTotal(9);

        when(eventoAplicadoRepository.existsById(2L)).thenReturn(false);
        when(relatorioRepository.findByAnoMesAndIdCatador("2026-08", 7L))
            .thenReturn(Optional.of(existente));

        projecaoService.aplicar(2L, "COLETA_ENTREGUE", PAYLOAD);

        assertEquals(3, existente.getTotalColetas());
        assertEquals(0, new BigDecimal("125.50").compareTo(existente.getVolumeTotal()));
        assertEquals(13, existente.getItensTotal());
    }

    @Test
    @DisplayName("Tipo de evento desconhecido deve ser marcado sem projetar")
    void deveIgnorarTipoDesconhecido() throws Exception {
        when(eventoAplicadoRepository.existsById(3L)).thenReturn(false);

        projecaoService.aplicar(3L, "TIPO_QUE_NAO_EXISTE", "{}");

        verify(historicoRepository, never()).save(any());
        verify(eventoAplicadoRepository).save(any(EventoAplicado.class));
    }
}
