package com.mentefria.backend.reposicao;

import com.mentefria.backend.reposicao.ReposicaoService.PedidoReposicao;
import com.mentefria.backend.reposicao.ReposicaoService.RecomendacaoReposicao;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Só Administrador (SecurityConfig: /api/reposicao/**). POST, como a produção: dispara o solver (decisão de 14/10). */
@RestController
public class ReposicaoController {
    private final ReposicaoService servico;
    public ReposicaoController(ReposicaoService servico) { this.servico = servico; }

    @PostMapping("/api/reposicao/recomendacao")
    public RecomendacaoReposicao recomendar(@RequestBody PedidoReposicao req) { return servico.recomendar(req); }
}
