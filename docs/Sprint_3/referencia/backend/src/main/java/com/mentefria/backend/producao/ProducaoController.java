package com.mentefria.backend.producao;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Só Administrador (SecurityConfig: /api/producao/**). POST porque dispara um processo pesado (D3). */
@RestController
public class ProducaoController {
    private final ProducaoService servico;
    public ProducaoController(ProducaoService servico) { this.servico = servico; }

    @PostMapping("/api/producao/recomendacao")
    public Recomendacao recomendar() { return servico.recomendar(); }
}
