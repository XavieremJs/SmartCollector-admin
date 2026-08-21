package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.model.CentroColeta;
import br.com.smartcollector.admin.repository.CentroColetaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CentroColetaServiceTest {

    @Mock
    private CentroColetaRepository centroRepository;

    @InjectMocks
    private CentroColetaService centroService;

    private CentroColeta centro(String total, String atual) {
        CentroColeta c = new CentroColeta();
        c.setEndereco("Rua Teste, 100");
        c.setVolumeTotal(new BigDecimal(total));
        c.setVolumeAtual(new BigDecimal(atual));
        return c;
    }

    @Test
    @DisplayName("Deve gravar quando o volume atual cabe na capacidade total")
    void deveSalvarVolumeValido() {
        CentroColeta c = centro("100.00", "40.00");

        centroService.salvar(c);

        verify(centroRepository).save(c);
    }

    @Test
    @DisplayName("Nao deve gravar quando o volume atual excede a capacidade")
    void naoDeveSalvarVolumeAcimaDaCapacidade() {
        CentroColeta c = centro("100.00", "150.00");

        IllegalArgumentException erro =
            assertThrows(IllegalArgumentException.class, () -> centroService.salvar(c));

        assertEquals("O volume atual nao pode ser maior que a capacidade total do centro",
                     erro.getMessage());
        verify(centroRepository, never()).save(any(CentroColeta.class));
    }

    @Test
    @DisplayName("Percentual de ocupacao deve ser calculado corretamente")
    void deveCalcularPercentualDeOcupacao() {
        assertEquals(new BigDecimal("45.0"), centro("200.00", "90.00").getPercentualOcupacao());
    }

    @Test
    @DisplayName("Percentual deve ser zero quando a capacidade total e zero")
    void deveEvitarDivisaoPorZero() {
        assertEquals(BigDecimal.ZERO, centro("0.00", "0.00").getPercentualOcupacao());
    }
}
