package com.mentefria.backend.usuario;

import com.mentefria.backend.usuario.UsuarioDtos.NovoUsuario;
import com.mentefria.backend.usuario.UsuarioDtos.UsuarioCriado;
import com.mentefria.backend.usuario.UsuarioDtos.UsuarioResumo;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Funcionários (US2). Só Administrador: a regra está no SecurityConfig (/api/usuarios/**). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servico;
    public UsuarioController(UsuarioService servico) { this.servico = servico; }

    @GetMapping
    public List<UsuarioResumo> listar() { return servico.listarFuncionarios(); }

    @PostMapping
    public ResponseEntity<UsuarioCriado> criar(@RequestBody NovoUsuario req) {
        return ResponseEntity.status(201).body(servico.criarFuncionario(req));
    }
}
