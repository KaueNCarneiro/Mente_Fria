package com.mentefria.backend.estoque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL das linhas de estoque (consultas 2, 4, 5, 6 e 7 do consultas_sprint3.sql). :hoje e :agora vêm do Java (F3, A9). */
@Repository
public class MovimentacaoRepository {
    /** Uma linha de ENTRADA com saldo, já travada (FOR UPDATE). */
    public record Linha(int id, BigDecimal restante) {}

    /** Uma linha como GET /api/estoque/{id}/linhas devolve. */
    public record LinhaEstoque(int id, LocalDateTime dataMovimentacao, BigDecimal quantidade, BigDecimal quantidadeRestante,
                               LocalDate dataValidade, String situacao) {}

    private final NamedParameterJdbcTemplate jdbc;
    public MovimentacaoRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Consulta 7: a linha nasce com restante = quantidade (chk_movimentacao_restante). */
    public int inserirEntrada(int ingredienteId, int usuarioId, BigDecimal quantidade, LocalDate validade, LocalDateTime agora, String obs) {
        return jdbc.queryForObject("""
                INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante, data_validade,
                                                  data_movimentacao, observacao)
                VALUES (:ing, :usuario, 'ENTRADA', :q, :q, :validade, :agora, :obs) RETURNING id""",
                new MapSqlParameterSource().addValue("ing", ingredienteId).addValue("usuario", usuarioId).addValue("q", quantidade)
                        .addValue("validade", validade).addValue("agora", agora).addValue("obs", obs), Integer.class);
    }

    /** Consulta 4: linhas VÁLIDAS com saldo, da menor validade para a maior; sem validade por último; empate por id. */
    public List<Linha> linhasValidas(int ingredienteId, LocalDate hoje) {
        return jdbc.query("""
                SELECT id, quantidade_restante FROM movimentacao_estoque
                WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0
                  AND (data_validade IS NULL OR data_validade >= :hoje)
                ORDER BY data_validade NULLS LAST, id FOR UPDATE""", Map.of("ing", ingredienteId, "hoje", hoje),
                (rs, n) -> new Linha(rs.getInt("id"), rs.getBigDecimal("quantidade_restante")));
    }

    /** Consulta 5: só linhas VENCIDAS com saldo, da mais antiga para a mais nova (descarte). */
    public List<Linha> linhasVencidas(int ingredienteId, LocalDate hoje) {
        return jdbc.query("""
                SELECT id, quantidade_restante FROM movimentacao_estoque
                WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0 AND data_validade < :hoje
                ORDER BY data_validade, id FOR UPDATE""", Map.of("ing", ingredienteId, "hoje", hoje),
                (rs, n) -> new Linha(rs.getInt("id"), rs.getBigDecimal("quantidade_restante")));
    }

    /** Uma ENTRADA travada para o descarte de uma linha (A12). */
    public record LinhaTravada(int id, BigDecimal restante, LocalDate dataValidade) {}

    /** A12: de qual ingrediente é a ENTRADA (sem travar), para travar o ingrediente ANTES da linha, na mesma ordem do pedido. */
    public java.util.Optional<Integer> ingredienteDaEntrada(int linhaId) {
        return jdbc.query("SELECT ingrediente_id FROM movimentacao_estoque WHERE id = :id AND tipo = 'ENTRADA'",
                Map.of("id", linhaId), (rs, n) -> rs.getInt(1)).stream().findFirst();
    }

    /** A12: a ENTRADA, travada (FOR UPDATE). */
    public java.util.Optional<LinhaTravada> travarLinha(int linhaId) {
        return jdbc.query("""
                SELECT id, quantidade_restante, data_validade FROM movimentacao_estoque
                WHERE id = :id AND tipo = 'ENTRADA' FOR UPDATE""", Map.of("id", linhaId),
                (rs, n) -> new LinhaTravada(rs.getInt("id"), rs.getBigDecimal("quantidade_restante"),
                        rs.getObject("data_validade", LocalDate.class))).stream().findFirst();
    }

    public int consumirLinha(int linhaId, BigDecimal tira) {
        return jdbc.update("UPDATE movimentacao_estoque SET quantidade_restante = quantidade_restante - :tira WHERE id = :id",
                Map.of("tira", tira, "id", linhaId));
    }

    /** Consulta 6 (parte do INSERT): a SAIDA guarda o motivo e a hora do Java. */
    public int inserirSaida(int ingredienteId, int usuarioId, BigDecimal quantidade, String motivo, LocalDateTime agora, String obs) {
        return jdbc.queryForObject("""
                INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, data_movimentacao, observacao)
                VALUES (:ing, :usuario, 'SAIDA', :q, :motivo, :agora, :obs) RETURNING id""",
                new MapSqlParameterSource().addValue("ing", ingredienteId).addValue("usuario", usuarioId).addValue("q", quantidade)
                        .addValue("motivo", motivo).addValue("agora", agora).addValue("obs", obs), Integer.class);
    }

    /** Consulta 2: as ENTRADAs do ingrediente com a situação de cada uma ("hoje" vence hoje, mas ainda vale). */
    public List<LinhaEstoque> linhasDoIngrediente(int ingredienteId, LocalDate hoje) {
        return jdbc.query("""
                SELECT m.id, m.data_movimentacao, m.quantidade, m.quantidade_restante, m.data_validade,
                       CASE WHEN m.data_validade IS NULL THEN 'SEM_VALIDADE'
                            WHEN m.data_validade < :hoje THEN 'VENCIDA'
                            WHEN m.data_validade <= :hoje + 7 THEN 'PROXIMA' ELSE 'OK' END AS situacao
                FROM movimentacao_estoque m WHERE m.ingrediente_id = :ing AND m.tipo = 'ENTRADA'
                ORDER BY m.data_validade NULLS LAST, m.id""", Map.of("ing", ingredienteId, "hoje", hoje),
                (rs, n) -> new LinhaEstoque(rs.getInt("id"), rs.getObject("data_movimentacao", LocalDateTime.class),
                        rs.getBigDecimal("quantidade"), rs.getBigDecimal("quantidade_restante"),
                        rs.getObject("data_validade", LocalDate.class), rs.getString("situacao")));
    }
}
