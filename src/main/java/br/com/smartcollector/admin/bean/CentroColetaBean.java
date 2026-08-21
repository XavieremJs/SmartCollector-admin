package br.com.smartcollector.admin.bean;

import br.com.smartcollector.admin.model.CentroColeta;
import br.com.smartcollector.admin.service.CentroColetaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component("centroBean")
@SessionScope
public class CentroColetaBean implements Serializable {

    private final transient CentroColetaService centroService;

    private List<CentroColeta> centros = new ArrayList<>();
    private CentroColeta centroSelecionado;
    private String filtro;

    public CentroColetaBean(CentroColetaService centroService) {
        this.centroService = centroService;
    }

    @PostConstruct
    public void init() {
        carregar();
    }

    public void carregar() {
        centros = centroService.buscarPorEndereco(filtro);
    }

    public void novo() {
        centroSelecionado = new CentroColeta();
    }

    public void editar(CentroColeta centro) {
        centroSelecionado = centro;
    }

    public void salvar() {
        try {
            centroService.salvar(centroSelecionado);
            carregar();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                                 "Centro de coleta gravado."));
            PrimeFaces.current().executeScript("PF('dlgCentro').hide()");
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencao", e.getMessage()));
        }
        PrimeFaces.current().ajax().update("form:mensagens", "form:tabela");
    }

    public void excluir(CentroColeta centro) {
        centroService.excluir(centro);
        carregar();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Removido",
                             "Centro de coleta excluido."));
    }

    public List<CentroColeta> getCentros() {
        return centros;
    }

    public CentroColeta getCentroSelecionado() {
        return centroSelecionado;
    }

    public void setCentroSelecionado(CentroColeta centroSelecionado) {
        this.centroSelecionado = centroSelecionado;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }
}
