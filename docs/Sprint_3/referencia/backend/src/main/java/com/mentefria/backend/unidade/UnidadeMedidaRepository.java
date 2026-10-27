package com.mentefria.backend.unidade;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UnidadeMedidaRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public UnidadeMedidaRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<UnidadeMedida> listar() {
        return jdbc.query("SELECT unidade_medida, porcao_padrao FROM unidade_medida_porcao_padrao ORDER BY unidade_medida",
                Map.of(), (rs, n) -> new UnidadeMedida(rs.getString("unidade_medida"), rs.getBigDecimal("porcao_padrao")));
    }
}
