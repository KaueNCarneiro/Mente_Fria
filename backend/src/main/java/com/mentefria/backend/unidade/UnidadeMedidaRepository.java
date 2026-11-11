package com.mentefria.backend.unidade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UnidadeMedidaRepository {
    private static final RowMapper<UnidadeMedida> UNIDADE =
            (rs, n) -> new UnidadeMedida(rs.getString("unidade_medida"), rs.getBigDecimal("porcao_padrao"));

    private final NamedParameterJdbcTemplate jdbc;
    public UnidadeMedidaRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<UnidadeMedida> listar() {
        return jdbc.query("SELECT unidade_medida, porcao_padrao FROM unidade_medida_porcao_padrao ORDER BY unidade_medida",
                Map.of(), UNIDADE);
    }

    /** US26: unidade repetida estoura unidade_medida_porcao_padrao_pkey (409); porção 0 estoura chk_unidade_porcao_padrao (400). */
    public UnidadeMedida inserir(String unidade, BigDecimal porcao) {
        return jdbc.queryForObject("""
                INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES (:unidade, :porcao)
                RETURNING unidade_medida, porcao_padrao""", Map.of("unidade", unidade, "porcao", porcao), UNIDADE);
    }

    /** US26: altera só a porção padrão (os ingredientes guardam a deles). Vazio = unidade inexistente (404 no serviço). */
    public Optional<UnidadeMedida> atualizarPorcao(String unidade, BigDecimal porcao) {
        return jdbc.query("""
                UPDATE unidade_medida_porcao_padrao SET porcao_padrao = :porcao WHERE unidade_medida = :unidade
                RETURNING unidade_medida, porcao_padrao""", Map.of("unidade", unidade, "porcao", porcao), UNIDADE)
                .stream().findFirst();
    }
}
