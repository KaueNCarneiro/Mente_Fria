package com.mentefria.backend.pedido;

import com.mentefria.backend.pedido.PedidoRepository.PedidoResumo;
import com.mentefria.backend.pedido.PedidoService.NovoPedido;
import com.mentefria.backend.pedido.PedidoService.PedidoCriado;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US12. Administrador e Funcionário (CT28). */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService servico;
    public PedidoController(PedidoService servico) { this.servico = servico; }

    @GetMapping
    public List<PedidoResumo> listar() { return servico.listar(); }

    @PostMapping
    public ResponseEntity<PedidoCriado> criar(@RequestBody NovoPedido req, Authentication auth) {
        return ResponseEntity.status(201).body(servico.criar(req, Integer.parseInt(auth.getName())));
    }
}
