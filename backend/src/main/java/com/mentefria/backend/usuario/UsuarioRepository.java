package com.mentefria.backend.usuario;

import com.mentefria.backend.usuario.UsuarioDtos.UsuarioResumo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {
    private static final RowMapper<Usuario> USUARIO = (rs, n) -> new Usuario(rs.getInt("id"), rs.getString("nome"),
            rs.getString("email"), rs.getString("senha_hash"), rs.getString("perfil"), rs.getBoolean("ativo"));
    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Impede que dois cadastros de Administrador simultâneos passem juntos pela checagem. Só dentro de @Transactional. */
    public void travarParaCadastroDeAdministrador() {
        jdbc.getJdbcOperations().execute("LOCK TABLE usuario IN SHARE ROW EXCLUSIVE MODE");
    }

    public boolean existeAdministrador() {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuario WHERE perfil = 'ADMINISTRADOR')", Map.of(), Boolean.class));
    }

    public int inserir(String nome, String email, String hash, String perfil) {   // RETURNING id sempre devolve 1 linha
        return jdbc.queryForObject(
                "INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES (:nome, :email, :hash, :perfil) RETURNING id",
                Map.of("nome", nome, "email", email, "hash", hash, "perfil", perfil), Integer.class);
    }

    public Optional<Usuario> buscarPorEmail(String email) {          // nunca queryForObject: lança exceção se não achar
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil, ativo FROM usuario WHERE email = :email",
                Map.of("email", email), USUARIO).stream().findFirst();
    }

    public List<UsuarioResumo> listarFuncionarios() {
        return jdbc.query("SELECT id, nome, email, ativo FROM usuario WHERE perfil = 'FUNCIONARIO' ORDER BY nome, id", Map.of(),
                (rs, n) -> new UsuarioResumo(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getBoolean("ativo")));
    }
}
