package br.com.smartcollector.admin.bean;

import br.com.smartcollector.admin.service.CentroColetaService;
import br.com.smartcollector.admin.service.ItemService;
import br.com.smartcollector.admin.service.RelatorioService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("dashboardBean")
@SessionScope
public class DashboardBean implements Serializable {

    private final transient ItemService itemService;
    private final transient CentroColetaService centroService;
    private final transient RelatorioService relatorioService;

    private long totalItens;
    private long totalCentros;
    private BigDecimal volumeCadastrado = BigDecimal.ZERO;
    private int centrosCriticos;
    private BarChartModel graficoOcupacao;

    public DashboardBean(ItemService itemService,
                         CentroColetaService centroService,
                         RelatorioService relatorioService) {
        this.itemService = itemService;
        this.centroService = centroService;
        this.relatorioService = relatorioService;
    }

    @PostConstruct
    public void init() {
        totalItens = itemService.listarTodos().size();
        totalCentros = centroService.listarTodos().size();
        volumeCadastrado = itemService.volumeTotalCadastrado();
        centrosCriticos = centroService.proximosDaCapacidade().size();
        montarGrafico();
    }

    private void montarGrafico() {
        List<Map<String, Object>> dados = relatorioService.ocupacaoPorCentro();

        List<String> rotulos = new ArrayList<>();
        List<Number> valores = new ArrayList<>();

        for (Map<String, Object> linha : dados) {
            String endereco = String.valueOf(linha.get("ENDERECO"));
            rotulos.add(endereco.length() > 24 ? endereco.substring(0, 24) + "..." : endereco);
            valores.add((Number) linha.get("PERCENTUAL"));
        }

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Ocupacao (%)");
        dataSet.setData(valores);
        dataSet.setBackgroundColor("rgba(29, 158, 117, 0.6)");
        dataSet.setBorderColor("rgba(15, 110, 86, 1)");
        dataSet.setBorderWidth(1);

        ChartData data = new ChartData();
        data.addChartDataSet(dataSet);
        data.setLabels(rotulos);

        BarChartOptions options = new BarChartOptions();
        options.setScales(new CartesianLinearAxes());
        Legend legend = new Legend();
        legend.setDisplay(true);
        options.setLegend(legend);

        graficoOcupacao = new BarChartModel();
        graficoOcupacao.setData(data);
        graficoOcupacao.setOptions(options);
    }

    public long getTotalItens() {
        return totalItens;
    }

    public long getTotalCentros() {
        return totalCentros;
    }

    public BigDecimal getVolumeCadastrado() {
        return volumeCadastrado;
    }

    public int getCentrosCriticos() {
        return centrosCriticos;
    }

    public BarChartModel getGraficoOcupacao() {
        return graficoOcupacao;
    }
}
