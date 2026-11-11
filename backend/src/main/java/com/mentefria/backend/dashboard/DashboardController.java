package com.mentefria.backend.dashboard;

import com.mentefria.backend.dashboard.DashboardService.Painel;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Só Administrador (SecurityConfig: /api/dashboard/**).
 * Ex.: GET /api/dashboard?mes=2026-10&usuario_id=2&produto_id=1&produto_id=3&valor_min=50&valor_max=500 (todos opcionais).
 */
@RestController
public class DashboardController {
    private final DashboardService servico;
    public DashboardController(DashboardService servico) { this.servico = servico; }

    @GetMapping("/api/dashboard")
    public Painel painel(@RequestParam(required = false) String mes,
                         @RequestParam(name = "usuario_id", required = false) Integer usuarioId,
                         @RequestParam(name = "produto_id", required = false) List<Integer> produtoIds,
                         @RequestParam(name = "valor_min", required = false) BigDecimal valorMin,
                         @RequestParam(name = "valor_max", required = false) BigDecimal valorMax) {
        return servico.painel(mes, usuarioId, produtoIds, valorMin, valorMax);
    }
}
