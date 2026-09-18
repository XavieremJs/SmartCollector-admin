package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.ColetaRequest;
import br.com.smartcollector.api.dto.ColetaResponse;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.*;
import br.com.smartcollector.api.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ColetaService {

    private final ColetaRepository coletaRepository;
    private final ColetaItemRepository coletaItemRepository;
    private final CatadorItemRepository catadorItemRepository;
    private final DescartadorRepository descartadorRepository;
    private final CatadorRepository catadorRepository;
    private final ItemRepository itemRepository;
    private final PkgRelatorio pkgRelatorio;

    @PersistenceContext
    private EntityManager entityManager;

    public ColetaService(ColetaRepository coletaRepository,
                         ColetaItemRepository coletaItemRepository,
                         CatadorItemRepository catadorItemRepository,
                         DescartadorRepository descartadorRepository,
                         CatadorRepository catadorRepository,
                         ItemRepository itemRepository,
                         PkgRelatorio pkgRelatorio) {
        this.coletaRepository = coletaRepository;
        this.coletaItemRepository = coletaItemRepository;
        this.catadorItemRepository = catadorItemRepository;
        this.descartadorRepository = descartadorRepository;
        this.catadorRepository = catadorRepository;
        this.itemRepository = itemRepository;
        this.pkgRelatorio = pkgRelatorio;
    }

    @Transactional(readOnly = true)
    public List<ColetaResponse> listar() {
        return coletaRepository.findAll().stream().map(this::montarResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<ColetaResponse> listarDisponiveis() {
        return coletaRepository.findByFinalizadaFalseAndCatadorIsNull()
                               .stream().map(this::montarResposta).toList();
    }

    @Transactional(readOnly = true)
    public ColetaResponse buscar(Long id) {
        return montarResposta(carregar(id));
    }

    @Transactional
    public ColetaResponse criar(ColetaRequest requisicao) {
        Descartador descartador = descartadorRepository.findById(requisicao.idDescartador())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Descartador", requisicao.idDescartador()));

        List<Long> idsItens = requisicao.idsItens().stream().distinct().toList();
        for (Long idItem : idsItens) {
            if (!itemRepository.existsById(idItem)) {
                throw new RecursoNaoEncontradoException("Item", idItem);
            }
        }

        Coleta coleta = new Coleta();
        coleta.setDescartador(descartador);
        coleta.setDataColeta(LocalDateTime.now());
        coleta.setFinalizada(false);

        Coleta salva = coletaRepository.save(coleta);

        idsItens.forEach(idItem ->
                coletaItemRepository.save(new ColetaItem(salva.getId(), idItem)));

        return montarResposta(salva);
    }

    /**
     * O catador assume a coleta. So aceita se a capacidade livre dele — que o
     * PL/SQL calcula a partir do que ele ja carrega e ainda nao entregou —
     * comportar o volume da coleta.
     */
    @Transactional
    public ColetaResponse aceitar(Long idColeta, Long idCatador) {
        Coleta coleta = carregar(idColeta);

        if (Boolean.TRUE.equals(coleta.getFinalizada())) {
            throw new RegraNegocioException("A coleta " + idColeta + " ja foi finalizada.");
        }
        if (coleta.estaAceita()) {
            throw new RegraNegocioException("A coleta " + idColeta + " ja tem um catador.");
        }

        Catador catador = catadorRepository.findById(idCatador)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Catador", idCatador));

        BigDecimal volume = pkgRelatorio.volumeDaColeta(idColeta);
        BigDecimal disponivel = pkgRelatorio.capacidadeDisponivel(idCatador);

        if (volume.compareTo(disponivel) > 0) {
            throw new RegraNegocioException(
                    "Capacidade insuficiente. Necessario: " + volume + ", disponivel: " + disponivel + ".");
        }

        coleta.setCatador(catador);
        coletaRepository.save(coleta);

        // Os itens passam a estar em posse do catador ate a entrega. E dessas
        // linhas que FN_CAPACIDADE_DISPONIVEL desconta a capacidade dele.
        idsDosItens(idColeta).forEach(idItem ->
                catadorItemRepository.save(new CatadorItem(idCatador, idItem)));

        return montarResposta(coleta);
    }

    /**
     * Entrega num centro. Delegada ao PL/SQL: e la que a trava de linha, a
     * validacao de espaco e a publicacao do evento acontecem numa transacao so.
     */
    @Transactional
    public ColetaResponse finalizar(Long idColeta, Long idCentro) {
        Coleta coleta = carregar(idColeta);

        if (!coleta.estaAceita()) {
            throw new RegraNegocioException(
                    "A coleta " + idColeta + " precisa de um catador antes da entrega.");
        }

        pkgRelatorio.registrarEntrega(idColeta, idCentro);

        // A procedure alterou as linhas por SQL direto; o contexto de
        // persistencia ainda tem a versao anterior.
        entityManager.clear();

        return montarResposta(carregar(idColeta));
    }

    @Transactional
    public void remover(Long id) {
        Coleta coleta = carregar(id);

        if (Boolean.TRUE.equals(coleta.getFinalizada())) {
            throw new RegraNegocioException("Uma coleta finalizada nao pode ser removida.");
        }

        coletaItemRepository.deleteAll(coletaItemRepository.findByIdIdColeta(id));
        coletaRepository.delete(coleta);
    }

    private Coleta carregar(Long id) {
        return coletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Coleta", id));
    }

    private List<Long> idsDosItens(Long idColeta) {
        return coletaItemRepository.findByIdIdColeta(idColeta)
                                   .stream()
                                   .map(item -> item.getId().getIdItem())
                                   .toList();
    }

    private ColetaResponse montarResposta(Coleta coleta) {
        return ColetaResponse.de(coleta,
                                 pkgRelatorio.volumeDaColeta(coleta.getId()),
                                 idsDosItens(coleta.getId()));
    }
}
