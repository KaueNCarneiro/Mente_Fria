package com.mentefria.backend.dashboard;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.dashboard.DashboardRepository.Faturamento;
import com.mentefria.backend.dashboard.DashboardRepository.IngredienteUtilizado;
import com.mentefria.backend.dashboard.DashboardRepository.LinhaRanking;
import com.mentefria.backend.dashboard.DashboardRepository.ProdutoVendido;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import com.mentefria.backend.ingrediente.IngredienteRepository.EstoqueItem;
import com.mentefria.backend.marketing.MarketingService;
import com.mentefria.backend.producao.UltimaRecomendacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * US17 = Tela 03 (D14): faturamento do mês, mais vendidos, ingredientes mais usados e ranking por usuário (filtros de 14/10).
 * Mais os itens que o Termo de Aceite cita e que não exigem Python: a receita máxima estimada e os produtos prioritários
 * do ÚLTIMO cálculo de produção (memória do backend, D15) e a situação do estoque (decisão A16, proposta).
 */
@Service
public class DashboardService {
    public record RankingItem(int rank, int usuarioId, String usuario, int quantidadePedidos, BigDecimal valorTotal,
                              String tiposDeProduto) {}
    public record UltimoPlano(LocalDateTime calculadoEm, BigDecimal receitaEstimada, BigDecimal margemEstimada,
                              List<String> produtosPriorizados) {}
    public record ResumoEstoque(int ingredientesAbaixoDoMinimo, int ingredientesComVencido) {}
    public record Painel(String mes, Faturamento faturamento, List<ProdutoVendido> maisVendidos,
                         List<IngredienteUtilizado> ingredientesMaisUtilizados, List<RankingItem> ranking,
                         UltimoPlano ultimoPlano, ResumoEstoque estoque) {}

    private final DashboardRepository dashboard;
    private final IngredienteRepository ingredientes;
    private final UltimaRecomendacao ultima;
    private final Clock clock;

    public DashboardService(DashboardRepository dashboard, IngredienteRepository ingredientes, UltimaRecomendacao ultima, Clock clock) {
        this.dashboard = dashboard; this.ingredientes = ingredientes; this.ultima = ultima; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Painel painel(String mes, Integer usuarioId, List<Integer> produtoIds, BigDecimal valorMin, BigDecimal valorMax) {
        YearMonth m;
        try {
            m = (mes == null || mes.isBlank()) ? YearMonth.now(clock) : YearMonth.parse(mes);    // padrão: o mês atual
        } catch (DateTimeParseException e) {
            throw ApiException.campo(400, "mes", "Informe o mês no formato AAAA-MM.");
        }
        if (valorMin != null && valorMax != null && valorMin.compareTo(valorMax) > 0)
            throw ApiException.campo(400, "valor_min", "O valor mínimo não pode ser maior que o máximo.");
        LocalDateTime inicio = m.atDay(1).atStartOfDay(), fim = m.plusMonths(1).atDay(1).atStartOfDay();

        List<RankingItem> ranking = new ArrayList<>();
        int pos = 1;
        for (LinhaRanking l : dashboard.ranking(inicio, fim, usuarioId, produtoIds, valorMin, valorMax))
            ranking.add(new RankingItem(pos++, l.usuarioId(), l.usuario(), l.quantidadePedidos(), l.valorTotal(), l.tiposDeProduto()));

        UltimoPlano plano = ultima.ler().map(r -> new UltimoPlano(r.calculadoEm(), r.receitaEstimada(), r.margemEstimada(),
                MarketingService.priorizados(r).stream().map(i -> i.nome()).toList())).orElse(null);

        int abaixo = 0, vencido = 0;
        for (EstoqueItem e : ingredientes.listarEstoque(LocalDate.now(clock))) {
            if (e.quantidadeValida().compareTo(e.quantidadeMinima()) <= 0) abaixo++;       // D13: saldo válido <= mínimo
            if (e.quantidadeVencida().signum() > 0) vencido++;
        }
        return new Painel(m.toString(), dashboard.faturamento(inicio, fim), dashboard.maisVendidos(inicio, fim),
                dashboard.ingredientesMaisUtilizados(inicio, fim), ranking, plano, new ResumoEstoque(abaixo, vencido));
    }
}
