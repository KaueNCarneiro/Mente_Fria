package com.mentefria.backend.ingrediente;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET: Administrador e Funcionário. POST e PUT: só Administrador (SecurityConfig). */
@RestController
@RequestMapping("/api/ingredientes")
public class IngredienteController {
    private final IngredienteService servico;
    public IngredienteController(IngredienteService servico) { this.servico = servico; }

    @GetMapping
    public List<Ingrediente> listar() { return servico.listar(); }

    @PostMapping
    public ResponseEntity<Ingrediente> criar(@RequestBody Ingrediente.Dados dados) {
        return ResponseEntity.status(201).body(servico.criar(dados));
    }

    @PutMapping("/{id}")
    public Ingrediente atualizar(@PathVariable int id, @RequestBody Ingrediente.Dados dados) { return servico.atualizar(id, dados); }
}
