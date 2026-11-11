package com.mentefria.backend.estoque;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.ingrediente.Ingrediente;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import com.mentefria.backend.ingrediente.IngredienteRepository.EstoqueItem;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {
    public static final String ENTRADA = "ENTRADA", SAIDA = "SAIDA";
    public static final String CONVENCIONAL = "CONVENCIONAL", DESCARTE_VENCIDO = "DESCARTE_VENCIDO";

    /** Corpo de POST /api/estoque/movimentacoes (Contrato v2, §2.4). O usuário vem do token, nunca do corpo. */
    public record NovaMovimentacao(Integer ingredienteId, String tipo, String motivo, BigDecimal quantidade,
                                   LocalDate dataValidade, String observacao) {}

    public record MovimentacaoCriada(int id, int ingredienteId, String tipo, String motivo, BigDecimal quantidade,
                                     LocalDate dataValidade, LocalDateTime dataMovimentacao, BigDecimal quantidadeEstoque) {}

    private final IngredienteRepository ingredientes;
    private final MovimentacaoRepository movimentacoes;
    private final BaixaPorLinhas baixa;
    private final Clock clock;

    public EstoqueService(IngredienteRepository ingredientes, MovimentacaoRepository movimentacoes, BaixaPorLinhas baixa, Clock clock) {
        this.ingredientes = ingredientes; this.movimentacoes = movimentacoes; this.baixa = baixa; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<EstoqueItem> listar() { return ingredientes.listarEstoque(LocalDate.now(clock)); }

    @Transactional(readOnly = true)
    public List<MovimentacaoRepository.LinhaEstoque> linhas(int ingredienteId) {
        if (ingredientes.buscarPorId(ingredienteId).isEmpty()) throw new ApiException(404, "Ingrediente não encontrado.", null);
        return movimentacoes.linhasDoIngrediente(ingredienteId, LocalDate.now(clock));
    }

    static Map<String, String> validar(NovaMovimentacao m) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (m.ingredienteId() == null) erros.put("ingrediente_id", "Ingrediente não encontrado.");
        if (!ENTRADA.equals(m.tipo()) && !SAIDA.equals(m.tipo())) erros.put("tipo", "Tipo de movimentação inválido.");
        if (m.quantidade() == null || m.quantidade().signum() <= 0) erros.put("quantidade", "A quantidade deve ser maior que zero.");  // CT26
        if (ENTRADA.equals(m.tipo()) && m.motivo() != null) erros.put("motivo", "O motivo só pode ser informado em saídas.");
        if (SAIDA.equals(m.tipo())) {
            if (m.motivo() == null || m.motivo().isBlank()) erros.put("motivo", "Informe o motivo da saída.");
            else if (!Set.of(CONVENCIONAL, DESCARTE_VENCIDO).contains(m.motivo()))      // PEDIDO só o backend grava
                erros.put("motivo", "Motivo de saída inválido.");
            if (m.dataValidade() != null) erros.put("data_validade", "A validade só pode ser informada em entradas.");
        }
        return erros;
    }

    /** Entrada, saída convencional ou descarte de vencidos, numa ÚNICA transação. */
    @Transactional
    public MovimentacaoCriada movimentar(NovaMovimentacao m, int usuarioId) {
        Map<String, String> erros = validar(m);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        int ingId = m.ingredienteId();
        ingredientes.travar(List.of(ingId));                                                  // FOR UPDATE
        Ingrediente ing = ingredientes.buscarPorId(ingId)
                .orElseThrow(() -> ApiException.campo(400, "ingrediente_id", "Ingrediente não encontrado."));
        int id;
        if (ENTRADA.equals(m.tipo())) {
            // Entrada com validade já vencida é aceita: a linha nasce vencida e aparece para descarte (proposta; decisão do PO).
            id = movimentacoes.inserirEntrada(ingId, usuarioId, m.quantidade(), m.dataValidade(), agora, m.observacao());
            ingredientes.somarSaldo(ingId, m.quantidade());
        } else {
            var linhas = CONVENCIONAL.equals(m.motivo()) ? movimentacoes.linhasValidas(ingId, hoje)     // nunca consome vencido
                                                         : movimentacoes.linhasVencidas(ingId, hoje);   // descarte: só vencido
            id = baixa.consumir(ingId, linhas, m.quantidade(), m.motivo(), usuarioId, agora, m.observacao(),
                    () -> new ApiException(422, "Estoque insuficiente para o ingrediente " + ing.nome(), null));    // CT21
        }
        BigDecimal saldo = ingredientes.buscarPorId(ingId).orElseThrow().quantidadeEstoque();
        return new MovimentacaoCriada(id, ingId, m.tipo(), m.motivo(), m.quantidade(), m.dataValidade(), agora, saldo);
    }

    /**
     * A12 (opcional; pode sair do escopo sem afetar o resto): descarta TUDO o que resta de UMA linha vencida.
     * É um atalho: grava a mesma SAIDA com motivo DESCARTE_VENCIDO que a movimentação gravaria.
     */
    @Transactional
    public MovimentacaoCriada descartarLinha(int linhaId, int usuarioId) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        int ingId = movimentacoes.ingredienteDaEntrada(linhaId)
                .orElseThrow(() -> new ApiException(404, "Linha de estoque não encontrada.", null));
        ingredientes.travar(List.of(ingId));                     // mesma ordem de travas do pedido: ingrediente, depois linhas
        var linha = movimentacoes.travarLinha(linhaId)
                .orElseThrow(() -> new ApiException(404, "Linha de estoque não encontrada.", null));
        if (linha.dataValidade() == null || !linha.dataValidade().isBefore(hoje))
            throw new ApiException(422, "Só é possível descartar linhas vencidas.", null);
        if (linha.restante().signum() == 0)
            throw new ApiException(422, "Esta linha já não tem saldo.", null);
        int id = baixa.consumir(ingId, List.of(new MovimentacaoRepository.Linha(linha.id(), linha.restante())), linha.restante(),
                DESCARTE_VENCIDO, usuarioId, agora, "Descarte da linha " + linhaId,
                () -> new ApiException(422, "Esta linha já não tem saldo.", null));
        BigDecimal saldo = ingredientes.buscarPorId(ingId).orElseThrow().quantidadeEstoque();
        return new MovimentacaoCriada(id, ingId, SAIDA, DESCARTE_VENCIDO, linha.restante(), null, agora, saldo);
    }
}
