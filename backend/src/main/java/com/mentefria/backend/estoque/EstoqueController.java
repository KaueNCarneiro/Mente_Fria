package com.mentefria.backend.estoque;

import com.mentefria.backend.estoque.EstoqueService.MovimentacaoCriada;
import com.mentefria.backend.estoque.EstoqueService.NovaMovimentacao;
import com.mentefria.backend.ingrediente.IngredienteRepository.EstoqueItem;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US9. Administrador e Funcionário (CT27). */
@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {
    private final EstoqueService servico;
    public EstoqueController(EstoqueService servico) { this.servico = servico; }

    @GetMapping
    public List<EstoqueItem> listar() { return servico.listar(); }

    @GetMapping("/{ingredienteId}/linhas")
    public List<MovimentacaoRepository.LinhaEstoque> linhas(@PathVariable int ingredienteId) { return servico.linhas(ingredienteId); }

    /** O subject do token é o id do usuário (JwtService): quem fez a movimentação vem daqui, nunca do corpo. */
    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentacaoCriada> movimentar(@RequestBody NovaMovimentacao req, Authentication auth) {
        return ResponseEntity.status(201).body(servico.movimentar(req, Integer.parseInt(auth.getName())));
    }

    /** A12 (opcional): botão "Descartar" de uma linha vencida, sem corpo. */
    @PostMapping("/linhas/{linhaId}/descarte")
    public ResponseEntity<MovimentacaoCriada> descartar(@PathVariable int linhaId, Authentication auth) {
        return ResponseEntity.status(201).body(servico.descartarLinha(linhaId, Integer.parseInt(auth.getName())));
    }
}
