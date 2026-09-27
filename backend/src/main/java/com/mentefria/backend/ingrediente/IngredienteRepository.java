package com.mentefria.backend.ingrediente;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IngredienteRepository {

    private final JdbcTemplate jdbc;

    public IngredienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<IngredienteResumo> listar() {
        return jdbc.query(
                "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id",
                (rs, linha) -> new IngredienteResumo(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("unidade_medida")));
    }
}