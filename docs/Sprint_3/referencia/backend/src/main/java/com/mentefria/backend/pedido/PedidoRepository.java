package com.mentefria.backend.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository {
    public record ItemGravado(int produtoId, int quantidade, BigDecimal precoUnitarioRegistrado) {}
    public record ItemPedido(int produtoId, String nome, int quantidade, BigDecimal precoUnitarioRegistrado) {}
    public record PedidoResumo(int id, LocalDateTime dataPedido, Integer clienteId, String usuarioNome, BigDecimal valorTotal,
                               String status, List<ItemPedido> itens) {}

    private final NamedParameterJdbcTemplate jdbc;
    public PedidoRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** data_pedido = :agora do Java (A9), nunca o CURRENT_TIMESTAMP do banco (UTC no Render). */
    public int inserir(Integer clienteId, int usuarioId, BigDecimal total, LocalDateTime agora) {
        return jdbc.queryForObject("INSERT INTO pedido (cliente_id, usuario_id, valor_total, data_pedido) "
                + "VALUES (:cliente, :usuario, :total, :agora) RETURNING id",
                new MapSqlParameterSource().addValue("cliente", clienteId).addValue("usuario", usuarioId)
                        .addValue("total", total).addValue("agora", agora), Integer.class);
    }

    public void inserirItens(int pedidoId, List<ItemGravado> itens) {
        MapSqlParameterSource[] lote = itens.stream().map(i -> new MapSqlParameterSource().addValue("pedido", pedidoId)
                .addValue("produto", i.produtoId()).addValue("q", i.quantidade()).addValue("preco", i.precoUnitarioRegistrado()))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate("INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) "
                + "VALUES (:pedido, :produto, :q, :preco)", lote);
    }

    /** GET /api/pedidos: do mais recente para o mais antigo, com os itens (CT40). */
    public List<PedidoResumo> listar() {
        Map<Integer, PedidoResumo> pedidos = new LinkedHashMap<>();
        jdbc.query("""
                SELECT p.id, p.data_pedido, p.cliente_id, u.nome AS usuario_nome, p.valor_total, p.status,
                       pp.produto_id, pr.nome AS produto_nome, pp.quantidade, pp.preco_unitario_registrado
                FROM pedido p
                JOIN usuario u ON u.id = p.usuario_id
                JOIN pedido_produto pp ON pp.pedido_id = p.id
                JOIN produto pr ON pr.id = pp.produto_id
                ORDER BY p.data_pedido DESC, p.id DESC, pp.produto_id""", Map.of(), rs -> {
            int id = rs.getInt("id");
            PedidoResumo p = pedidos.get(id);
            if (p == null) {
                p = new PedidoResumo(id, rs.getObject("data_pedido", LocalDateTime.class), (Integer) rs.getObject("cliente_id"),
                        rs.getString("usuario_nome"), rs.getBigDecimal("valor_total"), rs.getString("status"), new ArrayList<>());
                pedidos.put(id, p);
            }
            p.itens().add(new ItemPedido(rs.getInt("produto_id"), rs.getString("produto_nome"), rs.getInt("quantidade"),
                    rs.getBigDecimal("preco_unitario_registrado")));
        });
        return new ArrayList<>(pedidos.values());
    }
}
