package com.mentefria.backend.ingrediente;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IngredienteController {

    private final IngredienteRepository repositorio;

    public IngredienteController(IngredienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/ingredientes")
    public List<IngredienteResumo> listar() {
        return repositorio.listar();
    }
}