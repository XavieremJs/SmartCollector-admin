package br.com.smartcollector.admin.bean;

import br.com.smartcollector.admin.model.CentroColeta;
import br.com.smartcollector.admin.service.CentroColetaService;
import br.com.smartcollector.admin.service.ItemService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Component("dashboardBean")
@RequestScope
public class DashboardBean implements Serializable {

    private final transient ItemService itemService;
    private final transient CentroColetaService centroService;

    private long totalItens;
    private long totalCentros;
    private BigDecimal volumeCadastrado = BigDecimal.ZERO;
    private int centrosCriticos;
    private List<CentroColeta> centros;

    public DashboardBean(ItemService itemService,
                         CentroColetaService centroService) {
        this.itemService = itemService;
        this.centroService = centroService;
    }

    @PostConstruct
    public void init() {
        totalItens = itemService.listarTodos().size();
        totalCentros = centroService.listarTodos().size();
        volumeCadastrado = itemService.volumeTotalCadastrado();
        centrosCriticos = centroService.proximosDaCapacidade().size();
        centros = centroService.listarTodos();
    }

    public long getTotalItens() { return totalItens; }
    public long getTotalCentros() { return totalCentros; }
    public BigDecimal getVolumeCadastrado() { return volumeCadastrado; }
    public int getCentrosCriticos() { return centrosCriticos; }
    public List<CentroColeta> getCentros() { return centros; }
}
