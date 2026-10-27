package com.mentefria.backend.unidade;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Só Administrador (SecurityConfig: /api/unidades-medida/**). */
@RestController
public class UnidadeMedidaController {
    private final UnidadeMedidaRepository unidades;
    public UnidadeMedidaController(UnidadeMedidaRepository unidades) { this.unidades = unidades; }

    @GetMapping("/api/unidades-medida")
    public List<UnidadeMedida> listar() { return unidades.listar(); }
}
