package com.mentefria.backend.reposicao;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import com.mentefria.backend.python.ChaveOtimizador;
import com.mentefria.backend.python.ModulosPython.OtimizadorReposicao;
import com.mentefria.backend.python.ProcessoPython.FalhaPython;
import com.mentefria.backend.reposicao.ReposicaoRepository.Consumo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** US14 (redesenhada em 14/10): período + orçamento -> quanto comprar de cada ingrediente (módulo reposicao.py). */
@Service
public class ReposicaoService {
    public record PedidoReposicao(Integer periodoDias, BigDecimal orcamento) {}

    public record ItemReposicao(int ingredienteId, String nome, String unidadeMedida, BigDecimal consumoPeriodo,
                                BigDecimal quantidadeEstoque, BigDecimal quantidadeMinima, BigDecimal custoUnitario,
                                BigDecimal quantidadeSugerida, BigDecimal custoEstimado) {}

    public record RecomendacaoReposicao(LocalDateTime calculadoEm, int periodoDias, BigDecimal orcamento,
                                        boolean orcamentoSuficiente, BigDecimal custoTotal, BigDecimal valorFaltante,
                                        BigDecimal coberturaMinima, String mensagem, List<ItemReposicao> itens) {}

    // Contrato v2 §1.6 (snake_case pela configuração global do Jackson)
    record IngredientePy(int id, BigDecimal consumoPeriodo, BigDecimal quantidadeEstoque, BigDecimal quantidadeMinima,
                         BigDecimal custoUnitario) {}
    record EntradaPy(BigDecimal orcamento, List<IngredientePy> ingredientes) {}
    record ItemPy(int ingredienteId, BigDecimal quantidadeSugerida, BigDecimal custoEstimado) {}
    record RespostaPy(String status, String mensagem, Boolean orcamentoSuficiente, BigDecimal custoTotal,
                      BigDecimal valorFaltante, BigDecimal coberturaMinima, List<ItemPy> itens) {}

    static final int LIMITE_ATE_30_DIAS = 8, LIMITE_ACIMA_30_DIAS = 15;      // decisão de 14/10

    private final ReposicaoRepository reposicao;
    private final IngredienteRepository ingredientes;
    private final OtimizadorReposicao otimizador;
    private final JsonMapper json;
    private final Clock clock;
    private final ChaveOtimizador chave;

    public ReposicaoService(ReposicaoRepository reposicao, IngredienteRepository ingredientes, OtimizadorReposicao otimizador,
                            JsonMapper json, Clock clock, ChaveOtimizador chave) {
        this.reposicao = reposicao; this.ingredientes = ingredientes; this.otimizador = otimizador; this.json = json; this.clock = clock;
        this.chave = chave;
    }

    static Map<String, String> validar(PedidoReposicao req) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (req.periodoDias() == null || req.periodoDias() < 1 || req.periodoDias() > 365)
            erros.put("periodo_dias", "Informe um período entre 1 e 365 dias.");
        if (req.orcamento() == null) erros.put("orcamento", "Informe o orçamento.");
        else if (req.orcamento().signum() < 0) erros.put("orcamento", "O orçamento não pode ser negativo.");
        else if (req.orcamento().stripTrailingZeros().scale() > 2) erros.put("orcamento", "Use no máximo 2 casas decimais no orçamento.");
        return erros;
    }

    public RecomendacaoReposicao recomendar(PedidoReposicao req) {
        Map<String, String> erros = validar(req);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        int dias = req.periodoDias();
        // Período = os últimos N dias, contando hoje: [hoje - (N-1) 00:00, amanhã 00:00)
        List<Consumo> consumos = reposicao.maisConsumidos(hoje.minusDays(dias - 1L).atStartOfDay(), hoje.plusDays(1).atStartOfDay(),
                dias <= 30 ? LIMITE_ATE_30_DIAS : LIMITE_ACIMA_30_DIAS);
        if (consumos.isEmpty())
            return new RecomendacaoReposicao(agora, dias, req.orcamento(), true, BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                    null, "Não houve pedidos no período escolhido, então não há consumo para repor.", List.of());

        chave.exigir();                                                              // só aqui o solver é necessário (A31)
        Map<Integer, BigDecimal> saldo = ingredientes.saldosValidos(hoje);          // saldo VÁLIDO (decisão de 14/10)
        EntradaPy entrada = new EntradaPy(req.orcamento(), consumos.stream().map(c -> new IngredientePy(c.ingredienteId(),
                c.consumo().setScale(3), saldo.getOrDefault(c.ingredienteId(), BigDecimal.ZERO), c.quantidadeMinima(),
                c.custoUnitario())).toList());
        RespostaPy r = json.readValue(otimizador.executar(json.writeValueAsString(entrada)), RespostaPy.class);
        if ("erro".equals(r.status())) throw new ApiException(422, r.mensagem(), null);
        if (!"sucesso".equals(r.status()) || r.itens() == null || r.orcamentoSuficiente() == null)
            throw new FalhaPython("Resposta da reposição fora do contrato", null);

        Map<Integer, Consumo> porId = new LinkedHashMap<>();
        consumos.forEach(c -> porId.put(c.ingredienteId(), c));
        List<ItemReposicao> itens = new ArrayList<>();
        for (ItemPy i : r.itens()) {                                                  // mantém a ordem do Python (maior custo primeiro)
            Consumo c = porId.get(i.ingredienteId());
            if (c == null) throw new FalhaPython("Ingrediente " + i.ingredienteId() + " não foi enviado", null);
            itens.add(new ItemReposicao(c.ingredienteId(), c.nome(), c.unidadeMedida(), c.consumo().setScale(3),
                    saldo.getOrDefault(c.ingredienteId(), BigDecimal.ZERO), c.quantidadeMinima(), c.custoUnitario(),
                    i.quantidadeSugerida().setScale(2), i.custoEstimado().setScale(2)));
        }
        String mensagem = r.orcamentoSuficiente() ? null
                : "O orçamento não cobre nem os mínimos. A tabela mostra os mínimos e quanto falta.";
        return new RecomendacaoReposicao(agora, dias, req.orcamento(), r.orcamentoSuficiente(), r.custoTotal().setScale(2),
                r.valorFaltante().setScale(2), r.coberturaMinima(), mensagem, itens);
    }
}
