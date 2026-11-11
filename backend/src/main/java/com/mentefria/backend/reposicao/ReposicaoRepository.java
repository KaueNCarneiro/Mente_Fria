package com.mentefria.backend.reposicao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReposicaoRepository {
    /** Consumo de um ingrediente no período, com o que o módulo de reposição precisa. */
    public record Consumo(int ingredienteId, String nome, String unidadeMedida, BigDecimal consumo, BigDecimal custoUnitario,
                          BigDecimal quantidadeMinima) {}

    private final NamedParameterJdbcTemplate jdbc;
    public ReposicaoRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /**
     * Consulta 9 (US14, definição de consumo F12): pedidos x composição no período [inicio, fim).
     * Pedidos CANCELADOS contam (decisão de 14/10: o ingrediente foi gasto). Descartes e saídas convencionais não contam.
     * Os 'limite' ingredientes mais consumidos (8 até 30 dias; 15 acima), empate pelo menor id.
     */
    public List<Consumo> maisConsumidos(LocalDateTime inicio, LocalDateTime fim, int limite) {
        return jdbc.query("""
                SELECT i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima,
                       SUM(pp.quantidade * pi.quantidade_utilizada) AS consumo
                FROM pedido p
                JOIN pedido_produto pp ON pp.pedido_id = p.id
                JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
                JOIN ingrediente i ON i.id = pi.ingrediente_id
                WHERE p.data_pedido >= :inicio AND p.data_pedido < :fim
                GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima
                ORDER BY consumo DESC, i.id
                LIMIT :limite""", Map.of("inicio", inicio, "fim", fim, "limite", limite),
                (rs, n) -> new Consumo(rs.getInt("id"), rs.getString("nome"), rs.getString("unidade_medida"),
                        rs.getBigDecimal("consumo"), rs.getBigDecimal("custo_unitario"), rs.getBigDecimal("quantidade_minima")));
    }
}
