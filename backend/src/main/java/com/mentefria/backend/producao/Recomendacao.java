package com.mentefria.backend.producao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Resposta de POST /api/producao/recomendacao (Contrato v2, §2.6). Também é o que fica guardado para marketing e dashboard. */
public record Recomendacao(LocalDateTime calculadoEm, BigDecimal receitaEstimada, BigDecimal margemEstimada,
                           List<ItemRecomendado> recomendacao) {

    /** lucro_por_copo <= 0: a tela mostra "Sem lucro"; > 0 com quantidade 0: "Falta ingrediente" (D9). */
    public record ItemRecomendado(int produtoId, String nome, int quantidade, BigDecimal lucroPorCopo) {}
}
