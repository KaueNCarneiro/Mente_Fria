package com.mentefria.backend.marketing;

import com.mentefria.backend.marketing.MarketingService.Sugestoes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Só Administrador (SecurityConfig: /api/marketing/**). Sem parâmetros, como no Contrato §2.2. */
@RestController
public class MarketingController {
    private final MarketingService servico;
    public MarketingController(MarketingService servico) { this.servico = servico; }

    @GetMapping("/api/marketing/sugestoes")
    public Sugestoes sugestoes() { return servico.sugestoes(); }
}
