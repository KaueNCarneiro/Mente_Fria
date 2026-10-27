package com.mentefria.backend.auth;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.usuario.Usuario;
import com.mentefria.backend.usuario.UsuarioDtos.NovoUsuario;
import com.mentefria.backend.usuario.UsuarioDtos.UsuarioCriado;
import com.mentefria.backend.usuario.UsuarioRepository;
import com.mentefria.backend.usuario.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(String email, String senha) {}
    public record LoginResposta(String token, String perfil, String nome) {}

    private final UsuarioService servico;
    private final UsuarioRepository usuarios;
    private final JwtService jwt;
    private final PasswordEncoder encoder;

    public AuthController(UsuarioService servico, UsuarioRepository usuarios, JwtService jwt, PasswordEncoder encoder) {
        this.servico = servico; this.usuarios = usuarios; this.jwt = jwt; this.encoder = encoder;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioCriado> registrar(@RequestBody NovoUsuario req) {
        return ResponseEntity.status(201).body(servico.registrarAdministrador(req));
    }

    /** E-mail inexistente, senha errada ou usuário inativo: a MESMA resposta (CT07), para não revelar quem existe. */
    @PostMapping("/login")
    public LoginResposta login(@RequestBody LoginRequest req) {
        var invalido = new ApiException(401, "E-mail ou senha inválidos.", null);
        if (req.email() == null || req.senha() == null) throw invalido;
        Usuario u = usuarios.buscarPorEmail(UsuarioService.normalizarEmail(req.email())).orElseThrow(() -> invalido);
        if (!u.ativo() || !encoder.matches(req.senha(), u.senhaHash())) throw invalido;
        return new LoginResposta(jwt.gerarToken(u.id(), u.perfil()), u.perfil(), u.nome());
    }
}
