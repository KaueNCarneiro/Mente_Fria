package com.mentefria.backend.produto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProdutoRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public ProdutoRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Produtos com a composição: as linhas produto x ingrediente são agrupadas num LinkedHashMap (mantém a ordem). */
    public List<Produto> listarComComposicao() {
        Map<Integer, Produto> produtos = new LinkedHashMap<>();
        jdbc.query("""
                SELECT p.id, p.nome, p.preco_venda, p.ativo, pi.ingrediente_id, i.nome AS ingrediente_nome,
                       pi.quantidade_utilizada, i.unidade_medida
                FROM produto p
                LEFT JOIN produto_ingrediente pi ON pi.produto_id = p.id
                LEFT JOIN ingrediente i ON i.id = pi.ingrediente_id
                ORDER BY p.nome, p.id, i.nome""", Map.of(), rs -> {
            int id = rs.getInt("id");
            Produto p = produtos.get(id);
            if (p == null) {
                p = new Produto(id, rs.getString("nome"), rs.getBigDecimal("preco_venda"), rs.getBoolean("ativo"), new ArrayList<>());
                produtos.put(id, p);
            }
            if (rs.getObject("ingrediente_id") != null)
                p.composicao().add(new Produto.Composicao(rs.getInt("ingrediente_id"), rs.getString("ingrediente_nome"),
                        rs.getBigDecimal("quantidade_utilizada"), rs.getString("unidade_medida")));
        });
        return new ArrayList<>(produtos.values());
    }

    public int inserir(String nome, BigDecimal preco) {
        return jdbc.queryForObject("INSERT INTO produto (nome, preco_venda) VALUES (:nome, :preco) RETURNING id",
                Map.of("nome", nome, "preco", preco), Integer.class);
    }

    public void inserirComposicao(int produtoId, List<Produto.ItemComposicao> itens) {
        MapSqlParameterSource[] lote = itens.stream().map(i -> new MapSqlParameterSource()
                .addValue("p", produtoId).addValue("i", i.ingredienteId()).addValue("q", i.quantidadeUtilizada()))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate("INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (:p, :i, :q)", lote);
    }

    public int atualizarPreco(int id, BigDecimal preco) {
        return jdbc.update("UPDATE produto SET preco_venda = :preco WHERE id = :id", Map.of("preco", preco, "id", id));
    }

    public Map<Integer, Produto.Basico> buscarPorIds(List<Integer> ids) {
        Map<Integer, Produto.Basico> mapa = new LinkedHashMap<>();
        if (ids.isEmpty()) return mapa;
        jdbc.query("SELECT id, nome, preco_venda, ativo FROM produto WHERE id IN (:ids) ORDER BY id", Map.of("ids", ids),
                rs -> { mapa.put(rs.getInt("id"), new Produto.Basico(rs.getInt("id"), rs.getString("nome"),
                        rs.getBigDecimal("preco_venda"), rs.getBoolean("ativo"))); });
        return mapa;
    }

    /** Composição de vários produtos: produto_id -> itens, em ordem de ingrediente. */
    public Map<Integer, List<Produto.ItemComposicao>> composicoes(List<Integer> produtoIds) {
        Map<Integer, List<Produto.ItemComposicao>> mapa = new LinkedHashMap<>();
        if (produtoIds.isEmpty()) return mapa;
        jdbc.query("SELECT produto_id, ingrediente_id, quantidade_utilizada FROM produto_ingrediente WHERE produto_id IN (:ids) "
                + "ORDER BY produto_id, ingrediente_id", Map.of("ids", produtoIds), rs -> {
            mapa.computeIfAbsent(rs.getInt("produto_id"), k -> new ArrayList<>())
                .add(new Produto.ItemComposicao(rs.getInt("ingrediente_id"), rs.getBigDecimal("quantidade_utilizada")));
        });
        return mapa;
    }

    public List<Produto.Basico> listarAtivos() {
        return jdbc.query("SELECT id, nome, preco_venda, ativo FROM produto WHERE ativo ORDER BY id", Map.of(),
                (rs, n) -> new Produto.Basico(rs.getInt("id"), rs.getString("nome"), rs.getBigDecimal("preco_venda"), true));
    }
}
