package com.mentefria.backend.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * Rotas SÓ DE TESTE: simulam endpoints reais para exercitar segurança e erros sem banco.
 * O "@Profile" faz estas rotas existirem só no SegurancaEErrosTest. Sem isso, os testes de integração (@SpringBootTest) também as carregariam
 * e o Spring recusaria subir por "mapeamento ambíguo" com os controllers reais.
 */
@Profile("teste-seguranca")
@RestController
@RequestMapping("/api")
public class RotasDeTesteController {
    public record IngredienteReq(String nome, BigDecimal custo_unitario) {}
    public record PrecoReq(BigDecimal preco_venda) {}

    @GetMapping("/ingredientes")
    public List<Map<String, Object>> listarIngredientes() { return List.of(); }

    @PostMapping("/ingredientes")
    public Map<String, Object> criarIngrediente(@RequestBody IngredienteReq req) { return Map.of("nome", req.nome()); }

    @PutMapping("/produtos/{id}/preco")
    public Map<String, Object> preco(@PathVariable int id, @RequestBody PrecoReq req) { return Map.of("id", id, "preco_venda", req.preco_venda()); }

    @GetMapping("/usuarios")
    public Map<String, Object> usuarios() { return Map.of("ok", true); }

    @GetMapping("/estoque")
    public Map<String, Object> estoque() { return Map.of("ok", true); }

    @GetMapping("/pedidos")
    public Map<String, Object> pedidos(@RequestParam int pagina) { return Map.of("pagina", pagina); }

    @GetMapping("/estoque/falha")
    public Map<String, Object> falha() { throw new IllegalStateException("detalhe interno secreto: senha=xyz"); }
}
