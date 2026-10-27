package com.mentefria.backend.usuario;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.usuario.UsuarioDtos.NovoUsuario;
import com.mentefria.backend.usuario.UsuarioDtos.UsuarioCriado;
import com.mentefria.backend.usuario.UsuarioDtos.UsuarioResumo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String FUNCIONARIO = "FUNCIONARIO";
    private static final String EMAIL_VALIDO = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) { this.usuarios = usuarios; this.encoder = encoder; }

    /** trim + minúsculas: " DONO@Acai.com " e "dono@acai.com" são o mesmo e-mail (CT03). */
    public static String normalizarEmail(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }

    /** Devolve TODOS os campos inválidos de uma vez (US7), com as mensagens do mapa_constraints.md. */
    static Map<String, String> validar(NovoUsuario req) {
        Map<String, String> erros = new LinkedHashMap<>();
        String email = normalizarEmail(req.email());
        if (req.nome() == null || req.nome().isBlank()) erros.put("nome", "Informe o nome.");
        if (email.isEmpty()) erros.put("email", "Informe o e-mail.");
        else if (!email.matches(EMAIL_VALIDO)) erros.put("email", "Informe um e-mail válido.");
        if (req.senha() == null || req.senha().isEmpty()) erros.put("senha", "Informe a senha.");
        else if (req.senha().length() < 8) erros.put("senha", "A senha deve ter no mínimo 8 caracteres.");   // CT02
        return erros;
    }

    @Transactional
    public UsuarioCriado registrarAdministrador(NovoUsuario req) {
        usuarios.travarParaCadastroDeAdministrador();
        if (usuarios.existeAdministrador())                                          // CT01B: checar ANTES de validar
            throw new ApiException(409, "O cadastro do Administrador já foi realizado.", null);
        return criar(req, ADMINISTRADOR);                                            // o perfil nunca vem do corpo
    }

    @Transactional
    public UsuarioCriado criarFuncionario(NovoUsuario req) { return criar(req, FUNCIONARIO); }

    private UsuarioCriado criar(NovoUsuario req, String perfil) {
        Map<String, String> erros = validar(req);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        String nome = req.nome().trim();
        String email = normalizarEmail(req.email());
        int id = usuarios.inserir(nome, email, encoder.encode(req.senha()), perfil);   // e-mail repetido: uq_usuario_email -> 409
        return new UsuarioCriado(id, nome, email, perfil);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumo> listarFuncionarios() { return usuarios.listarFuncionarios(); }
}
