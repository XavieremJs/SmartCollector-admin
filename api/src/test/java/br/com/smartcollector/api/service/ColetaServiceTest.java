package br.com.smartcollector.api.service;

import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.Catador;
import br.com.smartcollector.api.model.Coleta;
import br.com.smartcollector.api.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColetaServiceTest {

    @Mock private ColetaRepository coletaRepository;
    @Mock private ColetaItemRepository coletaItemRepository;
    @Mock private CatadorItemRepository catadorItemRepository;
    @Mock private DescartadorRepository descartadorRepository;
    @Mock private CatadorRepository catadorRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private PkgRelatorio pkgRelatorio;

    @InjectMocks
    private ColetaService coletaService;

    private Coleta coletaAberta() {
        Coleta coleta = new Coleta();
        coleta.setId(1L);
        coleta.setFinalizada(false);
        return coleta;
    }

    @Test
    void naoDeveAceitarColetaAcimaDaCapacidadeDoCatador() {
        Catador catador = new Catador();
        catador.setId(7L);
        catador.setCapacidadeTotal(new BigDecimal("100.00"));

        when(coletaRepository.findById(1L)).thenReturn(Optional.of(coletaAberta()));
        when(catadorRepository.findById(7L)).thenReturn(Optional.of(catador));
        when(pkgRelatorio.volumeDaColeta(1L)).thenReturn(new BigDecimal("80.00"));
        when(pkgRelatorio.capacidadeDisponivel(7L)).thenReturn(new BigDecimal("30.00"));

        assertThatThrownBy(() -> coletaService.aceitar(1L, 7L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Capacidade insuficiente");

        verify(coletaRepository, never()).save(any());
        verify(catadorItemRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarColetaQueJaTemCatador() {
        Catador jaAtribuido = new Catador();
        jaAtribuido.setId(9L);

        Coleta coleta = coletaAberta();
        coleta.setCatador(jaAtribuido);

        when(coletaRepository.findById(1L)).thenReturn(Optional.of(coleta));

        assertThatThrownBy(() -> coletaService.aceitar(1L, 7L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ja tem um catador");

        verify(coletaRepository, never()).save(any());
    }

    @Test
    void naoDeveFinalizarColetaSemCatador() {
        when(coletaRepository.findById(1L)).thenReturn(Optional.of(coletaAberta()));

        assertThatThrownBy(() -> coletaService.finalizar(1L, 3L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("precisa de um catador");

        verify(pkgRelatorio, never()).registrarEntrega(any(), any());
    }
}
