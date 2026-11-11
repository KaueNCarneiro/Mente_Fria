package com.mentefria.backend.ingrediente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL de ingrediente e do estoque agregado (consultas 1 e 3 do consultas_sprint3.sql). Sem regra de negócio. */
@Repository
public class IngredienteRepository {
    private static final String COLUNAS = "id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima";
    private static final RowMapper<Ingrediente> INGREDIENTE = (rs, n) -> new Ingrediente(rs.getInt("id"), rs.getString("nome"),
            rs.getString("unidade_medida"), rs.getBigDecimal("custo_unitario"), rs.getBigDecimal("porcao_padrao"),
            rs.getBigDecimal("quantidade_estoque"), rs.getBigDecimal("quantidade_minima"));

    /** Uma linha de GET /api/estoque (consulta 1). */
    public record EstoqueItem(int ingredienteId, String nome, String unidadeMedida, BigDecimal quantidadeEstoque,
                              BigDecimal quantidadeMinima, BigDecimal quantidadeValida, BigDecimal quantidadeVencida,
                              LocalDate validadeMaisProxima) {}

    /** Ingrediente para o módulo Python (consulta 3): quantidade_estoque aqui é o saldo VÁLIDO. */
    public record IngredienteOtimizador(int id, BigDecimal quantidadeEstoque, BigDecimal custoUnitario) {}

    private final NamedParameterJdbcTemplate jdbc;
    public IngredienteRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Ingrediente> listar() {
        return jdbc.query("SELECT " + COLUNAS + " FROM ingrediente ORDER BY nome, id", Map.of(), INGREDIENTE);
    }

    public Optional<Ingrediente> buscarPorId(int id) {
        return jdbc.query("SELECT " + COLUNAS + " FROM ingrediente WHERE id = :id", Map.of("id", id), INGREDIENTE).stream().findFirst();
    }

    private static MapSqlParameterSource parametros(Ingrediente.Dados d) {
        return new MapSqlParameterSource().addValue("nome", d.nome()).addValue("unidade", d.unidadeMedida())
                .addValue("custo", d.custoUnitario()).addValue("porcao", d.porcaoPadrao()).addValue("minima", d.quantidadeMinima());
    }

    public Ingrediente inserir(Ingrediente.Dados d) {     // o estoque nasce 0 (DEFAULT): só muda por movimentação
        return jdbc.queryForObject("INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_minima) "
                + "VALUES (:nome, :unidade, :custo, :porcao, :minima) RETURNING " + COLUNAS, parametros(d), INGREDIENTE);
    }

    public Optional<Ingrediente> atualizar(int id, Ingrediente.Dados d) {
        return jdbc.query("UPDATE ingrediente SET nome = :nome, unidade_medida = :unidade, custo_unitario = :custo, "
                + "porcao_padrao = :porcao, quantidade_minima = :minima WHERE id = :id RETURNING " + COLUNAS,
                parametros(d).addValue("id", id), INGREDIENTE).stream().findFirst();
    }

    public Map<Integer, String> buscarNomesPorIds(List<Integer> ids) {
        Map<Integer, String> nomes = new LinkedHashMap<>();
        if (ids.isEmpty()) return nomes;
        jdbc.query("SELECT id, nome FROM ingrediente WHERE id IN (:ids) ORDER BY id", Map.of("ids", ids),
                rs -> { nomes.put(rs.getInt("id"), rs.getString("nome")); });
        return nomes;
    }

    /** SELECT ... FOR UPDATE em ORDEM DE ID: dois pedidos que travam os mesmos ingredientes nunca se bloqueiam em ciclo. */
    public void travar(List<Integer> idsOrdenados) {
        if (idsOrdenados.isEmpty()) return;
        jdbc.query("SELECT id FROM ingrediente WHERE id IN (:ids) ORDER BY id FOR UPDATE", Map.of("ids", idsOrdenados), rs -> {});
    }

    public int somarSaldo(int id, BigDecimal quantidade) {
        return jdbc.update("UPDATE ingrediente SET quantidade_estoque = quantidade_estoque + :q WHERE id = :id",
                Map.of("q", quantidade, "id", id));
    }

    public int baixarSaldo(int id, BigDecimal quantidade) {   // chk_ingrediente_quantidade_estoque é a rede de segurança
        return jdbc.update("UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - :q WHERE id = :id",
                Map.of("q", quantidade, "id", id));
    }

    /** Consulta 1: total, válido, vencido e a validade mais próxima, por ingrediente. :hoje vem do Java (F3). */
    public List<EstoqueItem> listarEstoque(LocalDate hoje) {
        return jdbc.query("""
                SELECT i.id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima,
                       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS valida,
                       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade < :hoje), 0) AS vencida,
                       MIN(m.data_validade) FILTER (WHERE m.quantidade_restante > 0 AND m.data_validade >= :hoje) AS proxima
                FROM ingrediente i
                LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
                GROUP BY i.id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima
                ORDER BY i.nome, i.id""", Map.of("hoje", hoje),
                (rs, n) -> new EstoqueItem(rs.getInt("id"), rs.getString("nome"), rs.getString("unidade_medida"),
                        rs.getBigDecimal("quantidade_estoque"), rs.getBigDecimal("quantidade_minima"),
                        rs.getBigDecimal("valida").setScale(3), rs.getBigDecimal("vencida").setScale(3),
                        rs.getObject("proxima", LocalDate.class)));
    }

    /** Consulta 3: ingredientes dos produtos ATIVOS (inclusive com 0), com o saldo VÁLIDO e o custo. */
    public List<IngredienteOtimizador> entradaOtimizador(LocalDate hoje) {
        return jdbc.query("""
                SELECT i.id, i.custo_unitario,
                       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS valida
                FROM ingrediente i
                LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
                WHERE i.id IN (SELECT pi.ingrediente_id FROM produto_ingrediente pi JOIN produto p ON p.id = pi.produto_id WHERE p.ativo)
                GROUP BY i.id, i.custo_unitario ORDER BY i.id""", Map.of("hoje", hoje),
                (rs, n) -> new IngredienteOtimizador(rs.getInt("id"), rs.getBigDecimal("valida").setScale(3), rs.getBigDecimal("custo_unitario")));
    }

    /** Saldo válido de todos os ingredientes (reposição e resumo do dashboard). */
    public Map<Integer, BigDecimal> saldosValidos(LocalDate hoje) {
        Map<Integer, BigDecimal> saldos = new HashMap<>();
        for (EstoqueItem e : listarEstoque(hoje)) saldos.put(e.ingredienteId(), e.quantidadeValida());
        return saldos;
    }
}
