package com.mentefria.backend.marketing;

import com.mentefria.backend.marketing.CatalogoIdeias.Ideia;
import com.mentefria.backend.producao.Recomendacao;
import com.mentefria.backend.producao.UltimaRecomendacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * US18: sugestões de campanha para os produtos PRIORIZADOS pela última recomendação de produção (D15).
 * Priorizado = quantidade recomendada > 0, da maior para a menor, empate pelo menor id; no máximo 3 (decisão de 14/10).
 */
@Service
public class MarketingService {
    public static final int MAXIMO = 3;
    public static final String SELO = "Produto priorizado";
    public static final String SEM_CALCULO = "Gere a recomendação na tela Produção para ver as sugestões.";
    public static final String SEM_PRIORIZADO = "A última recomendação não indicou nenhum produto para produzir. "
            + "Confira o estoque e gere a recomendação de novo.";

    public record Sugestao(int produtoId, String produto, int quantidadeRecomendada, BigDecimal lucroPorCopo, String selo,
                           String ideia, String comoFazer) {}
    public record Sugestoes(LocalDateTime calculadoEm, String mensagem, List<Sugestao> sugestoes) {}

    private final UltimaRecomendacao ultima;
    public MarketingService(UltimaRecomendacao ultima) { this.ultima = ultima; }

    /** Até 3 produtos priorizados, na ordem; os outros consumidores (dashboard) usam o mesmo critério. */
    public static List<Recomendacao.ItemRecomendado> priorizados(Recomendacao r) {
        return r.recomendacao().stream().filter(i -> i.quantidade() > 0)
                .sorted(Comparator.comparingInt(Recomendacao.ItemRecomendado::quantidade).reversed()
                        .thenComparingInt(Recomendacao.ItemRecomendado::produtoId))
                .limit(MAXIMO).toList();
    }

    public Sugestoes sugestoes() {
        Optional<Recomendacao> guardada = ultima.ler();
        if (guardada.isEmpty()) return new Sugestoes(null, SEM_CALCULO, List.of());
        Recomendacao r = guardada.get();
        List<Recomendacao.ItemRecomendado> lista = priorizados(r);
        if (lista.isEmpty()) return new Sugestoes(r.calculadoEm(), SEM_PRIORIZADO, List.of());
        // Ideias diferentes no mesmo resultado; o ponto de partida muda com o dia do cálculo, para variar ao longo do mês.
        int inicio = (r.calculadoEm().getDayOfYear() * MAXIMO) % CatalogoIdeias.IDEIAS.size();
        List<Sugestao> sugestoes = new ArrayList<>();
        for (int pos = 0; pos < lista.size(); pos++) {
            var item = lista.get(pos);
            Ideia ideia = CatalogoIdeias.IDEIAS.get((inicio + pos) % CatalogoIdeias.IDEIAS.size());
            sugestoes.add(new Sugestao(item.produtoId(), item.nome(), item.quantidade(), item.lucroPorCopo(), SELO,
                    ideia.titulo(), ideia.para(item.nome())));
        }
        return new Sugestoes(r.calculadoEm(), null, sugestoes);
    }
}
