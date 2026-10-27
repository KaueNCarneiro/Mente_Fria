package com.mentefria.backend.produto;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET: os dois perfis. POST e PUT: só Administrador (SecurityConfig). */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService servico;
    public ProdutoController(ProdutoService servico) { this.servico = servico; }

    @GetMapping
    public List<Produto> listar() { return servico.listar(); }

    @PostMapping
    public ResponseEntity<Produto.Criado> criar(@RequestBody Produto.Novo req) {
        return ResponseEntity.status(201).body(servico.criar(req));
    }

    @PutMapping("/{id}/preco")
    public Produto.PrecoAtualizado atualizarPreco(@PathVariable int id, @RequestBody Produto.NovoPreco req) {
        return servico.atualizarPreco(id, req.precoVenda());
    }
}
