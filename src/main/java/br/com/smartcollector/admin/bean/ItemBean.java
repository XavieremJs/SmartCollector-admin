package br.com.smartcollector.admin.bean;

import br.com.smartcollector.admin.model.Item;
import br.com.smartcollector.admin.service.ItemService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Managed bean da tela de itens. Expoe a lista para o DataTable e
 * os metodos chamados pelos botoes da pagina itens.xhtml.
 */
@Component("itemBean")
@SessionScope
public class ItemBean implements Serializable {

    private final transient ItemService itemService;

    private List<Item> itens = new ArrayList<>();
    private Item itemSelecionado;
    private String filtro;

    public ItemBean(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostConstruct
    public void init() {
        carregar();
    }

    public void carregar() {
        itens = itemService.buscarPorNome(filtro);
    }

    public void novo() {
        itemSelecionado = new Item();
    }

    public void editar(Item item) {
        itemSelecionado = item;
    }

    public void salvar() {
        try {
            itemService.salvar(itemSelecionado);
            carregar();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso",
                              "Item gravado com sucesso.");
            PrimeFaces.current().executeScript("PF('dlgItem').hide()");
        } catch (IllegalArgumentException e) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Atencao", e.getMessage());
        }
        PrimeFaces.current().ajax().update("form:mensagens", "form:tabela");
    }

    public void excluir(Item item) {
        itemService.excluir(item);
        carregar();
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Removido",
                          "Item excluido com sucesso.");
    }

    public void limparFiltro() {
        filtro = null;
        carregar();
    }

    private void adicionarMensagem(FacesMessage.Severity severidade,
                                   String titulo, String detalhe) {
        FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(severidade, titulo, detalhe));
    }

    public List<Item> getItens() {
        return itens;
    }

    public Item getItemSelecionado() {
        return itemSelecionado;
    }

    public void setItemSelecionado(Item itemSelecionado) {
        this.itemSelecionado = itemSelecionado;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }
}
