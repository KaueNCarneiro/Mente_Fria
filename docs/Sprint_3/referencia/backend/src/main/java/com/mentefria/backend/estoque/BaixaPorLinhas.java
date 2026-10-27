package com.mentefria.backend.estoque;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/** Baixa por linhas, usada pelo pedido, pela saída convencional e pelo descarte. Sempre DENTRO de @Transactional. */
@Component
public class BaixaPorLinhas {
    private final MovimentacaoRepository movimentacoes;
    private final IngredienteRepository ingredientes;

    public BaixaPorLinhas(MovimentacaoRepository movimentacoes, IngredienteRepository ingredientes) {
        this.movimentacoes = movimentacoes; this.ingredientes = ingredientes;
    }

    public static BigDecimal disponivel(List<MovimentacaoRepository.Linha> linhas) {
        return linhas.stream().map(MovimentacaoRepository.Linha::restante).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Consome 'quantidade' das linhas já carregadas e travadas, na ordem recebida (FEFO ou vencidas).
     * Se não cobrir, lança o erro de 'semEstoque' SEM alterar nada. Mantém a invariante: saldo = soma dos restantes.
     * Devolve o id da SAIDA gravada.
     */
    public int consumir(int ingredienteId, List<MovimentacaoRepository.Linha> linhas, BigDecimal quantidade, String motivo,
                         int usuarioId, LocalDateTime agora, String obs, Supplier<ApiException> semEstoque) {
        if (disponivel(linhas).compareTo(quantidade) < 0) throw semEstoque.get();
        BigDecimal falta = quantidade;
        for (var linha : linhas) {
            if (falta.signum() <= 0) break;
            BigDecimal tira = linha.restante().min(falta);
            movimentacoes.consumirLinha(linha.id(), tira);
            falta = falta.subtract(tira);
        }
        ingredientes.baixarSaldo(ingredienteId, quantidade);
        return movimentacoes.inserirSaida(ingredienteId, usuarioId, quantidade, motivo, agora, obs);   // A9: a hora vem do Java
    }
}
