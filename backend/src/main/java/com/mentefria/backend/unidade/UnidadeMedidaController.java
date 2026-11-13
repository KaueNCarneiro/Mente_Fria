package com.mentefria.backend.unidade;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Só Administrador (SecurityConfig: /api/unidades-medida/**). POST e PATCH: US26, Contrato §2.11. */
@RestController
public class UnidadeMedidaController {
    private final UnidadeMedidaService servico;
    public UnidadeMedidaController(UnidadeMedidaService servico) { this.servico = servico; }

    @GetMapping("/api/unidades-medida")
    public List<UnidadeMedida> listar() { return servico.listar(); }

    @PostMapping("/api/unidades-medida")
    public ResponseEntity<UnidadeMedida> criar(@RequestBody UnidadeMedida.Nova nova) {
        return ResponseEntity.status(201).body(servico.criar(nova));
    }

    @PatchMapping("/api/unidades-medida/{unidadeMedida}")
    public UnidadeMedida atualizarPorcao(@PathVariable String unidadeMedida, @RequestBody UnidadeMedida.NovaPorcao corpo) {
        return servico.atualizarPorcao(unidadeMedida, corpo);
    }
}
