package com.mentefria.backend.dashboard;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas agregadas do dashboard (US17, requisito R4: GROUP BY e JOIN). Período = um mês [inicio, fim).
 * Só pedidos ATIVO contam (decisão de 14/10: cancelado não entra em faturamento nem em ranking).
 */
@Repository
public class DashboardRepository {
    public record Faturamento(BigDecimal valorTotal, int quantidadePedidos) {}
    public record ProdutoVendido(int produtoId, String nome, int quantidade, BigDecimal valorTotal) {}
    public record IngredienteUtilizado(int ingredienteId, String nome, String unidadeMedida, BigDecimal quantidade,
                                       BigDecimal custoEstimado) {}
    public record LinhaRanking(int usuarioId, String usuario, int quantidadePedidos, BigDecimal valorTotal, String tiposDeProduto) {}

    private final NamedParameterJdbcTemplate jdbc;
    public DashboardRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static MapSqlParameterSource periodo(LocalDateTime inicio, LocalDateTime fim) {
        return new MapSqlParameterSource().addValue("inicio", inicio).addValue("fim", fim);
    }

    public Faturamento faturamento(LocalDateTime inicio, LocalDateTime fim) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(valor_total), 0) AS valor, COUNT(*) AS pedidos FROM pedido
                WHERE status = 'ATIVO' AND data_pedido >= :inicio AND data_pedido < :fim""", periodo(inicio, fim),
                (rs, n) -> new Faturamento(rs.getBigDecimal("valor").setScale(2), rs.getInt("pedidos")));
    }

    public List<ProdutoVendido> maisVendidos(LocalDateTime inicio, LocalDateTime fim) {
        return jdbc.query("""
                SELECT pr.id, pr.nome, SUM(pp.quantidade) AS quantidade, SUM(pp.quantidade * pp.preco_unitario_registrado) AS valor
                FROM pedido p
                JOIN pedido_produto pp ON pp.pedido_id = p.id
                JOIN produto pr ON pr.id = pp.produto_id
                WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
                GROUP BY pr.id, pr.nome
                ORDER BY quantidade DESC, valor DESC, pr.id""", periodo(inicio, fim),
                (rs, n) -> new ProdutoVendido(rs.getInt("id"), rs.getString("nome"), rs.getInt("quantidade"), rs.getBigDecimal("valor")));
    }

    /** Consumo pela composição (a mesma definição da reposição, F12), ordenado pelo CUSTO do que foi usado (unidades diferentes não se comparam). */
    public List<IngredienteUtilizado> ingredientesMaisUtilizados(LocalDateTime inicio, LocalDateTime fim) {
        return jdbc.query("""
                SELECT i.id, i.nome, i.unidade_medida, SUM(pp.quantidade * pi.quantidade_utilizada) AS quantidade,
                       ROUND(SUM(pp.quantidade * pi.quantidade_utilizada) * i.custo_unitario, 2) AS custo
                FROM pedido p
                JOIN pedido_produto pp ON pp.pedido_id = p.id
                JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
                JOIN ingrediente i ON i.id = pi.ingrediente_id
                WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
                GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario
                ORDER BY custo DESC, i.id""", periodo(inicio, fim),
                (rs, n) -> new IngredienteUtilizado(rs.getInt("id"), rs.getString("nome"), rs.getString("unidade_medida"),
                        rs.getBigDecimal("quantidade"), rs.getBigDecimal("custo")));
    }

    /**
     * Ranking de vendas por usuário (decidido em 14/10). Filtro de produto restringe também a quantidade e o valor somados;
     * filtro de valor é aplicado DEPOIS de somar (HAVING). Ordem: valor total, depois quantidade de pedidos.
     */
    public List<LinhaRanking> ranking(LocalDateTime inicio, LocalDateTime fim, Integer usuarioId, List<Integer> produtoIds,
                                      BigDecimal valorMin, BigDecimal valorMax) {
        boolean filtrarProduto = produtoIds != null && !produtoIds.isEmpty();
        var p = periodo(inicio, fim)
                .addValue("usuario", usuarioId, Types.INTEGER)
                .addValue("filtrar_produto", filtrarProduto)
                .addValue("produtos", filtrarProduto ? produtoIds : List.of(-1))
                .addValue("valor_min", valorMin, Types.NUMERIC)
                .addValue("valor_max", valorMax, Types.NUMERIC);
        return jdbc.query("""
                SELECT u.id, u.nome, COUNT(DISTINCT p.id) AS pedidos,
                       SUM(pp.quantidade * pp.preco_unitario_registrado) AS valor,
                       STRING_AGG(DISTINCT pr.nome, ', ' ORDER BY pr.nome) AS tipos
                FROM pedido p
                JOIN usuario u ON u.id = p.usuario_id
                JOIN pedido_produto pp ON pp.pedido_id = p.id
                JOIN produto pr ON pr.id = pp.produto_id
                WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
                  AND (CAST(:usuario AS INTEGER) IS NULL OR p.usuario_id = :usuario)
                  AND (NOT :filtrar_produto OR pp.produto_id IN (:produtos))
                GROUP BY u.id, u.nome
                HAVING (CAST(:valor_min AS NUMERIC) IS NULL OR SUM(pp.quantidade * pp.preco_unitario_registrado) >= :valor_min)
                   AND (CAST(:valor_max AS NUMERIC) IS NULL OR SUM(pp.quantidade * pp.preco_unitario_registrado) <= :valor_max)
                ORDER BY valor DESC, pedidos DESC, u.id""", p,
                (rs, n) -> new LinhaRanking(rs.getInt("id"), rs.getString("nome"), rs.getInt("pedidos"),
                        rs.getBigDecimal("valor"), rs.getString("tipos")));
    }
}
