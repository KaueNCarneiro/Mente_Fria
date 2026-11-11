package com.mentefria.backend.python;

import com.mentefria.backend.python.ModulosPython.OtimizadorProducao;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Hello World Java -> Python (pendência da Sprint 1). Só existe no perfil dev (F4): em produção a rota não existe. */
@Profile("dev")
@RestController
public class TestePythonController {
    private final OtimizadorProducao otimizador;
    public TestePythonController(OtimizadorProducao otimizador) { this.otimizador = otimizador; }

    @GetMapping(value = "/api/teste/python", produces = "application/json;charset=UTF-8")
    public String eco() { return otimizador.executar("{\"echo\":\"Açaí (polpa)\"}"); }
}
