package com.mentefria.backend.pedido;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.estoque.BaixaPorLinhas;
import com.mentefria.backend.estoque.MovimentacaoRepository;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import com.mentefria.backend.pedido.PedidoRepository.ItemGravado;
import com.mentefria.backend.pedido.PedidoRepository.PedidoResumo;
import com.mentefria.backend.produto.Produto;
import com.mentefria.backend.produto.ProdutoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {
    public record ItemNovo(Integer produtoId, Integer quantidade) {}
    public record NovoPedido(Integer clienteId, List<ItemNovo> itens) {}
    public record PedidoCriado(int id, BigDecimal valorTotal, LocalDateTime dataPedido) {}

    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final IngredienteRepository ingredientes;
    private final MovimentacaoRepository movimentacoes;
    private final BaixaPorLinhas baixa;
    private final Clock clock;

    public PedidoService(PedidoRepository pedidos, ProdutoRepository produtos, IngredienteRepository ingredientes,
                         MovimentacaoRepository movimentacoes, BaixaPorLinhas baixa, Clock clock) {
        this.pedidos = pedidos; this.produtos = produtos; this.ingredientes = ingredientes;
        this.movimentacoes = movimentacoes; this.baixa = baixa; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PedidoResumo> listar() { return pedidos.listar(); }

    /** UC12: tudo ou nada (CT25). Só consome linhas VÁLIDAS (CT29/CT33). Preço sempre do banco, nunca do cliente. */
    @Transactional
    public PedidoCriado criar(NovoPedido req, int usuarioId) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);

        // 1. Validar os itens (400, campo "itens")
        List<ItemNovo> itens = req.itens();
        if (itens == null || itens.isEmpty()) throw ApiException.campo(400, "itens", "Adicione ao menos um produto.");
        var vistos = new HashSet<Integer>();
        for (ItemNovo i : itens) {
            if (i.produtoId() == null) throw ApiException.campo(400, "itens", "Produto não encontrado.");
            if (!vistos.add(i.produtoId())) throw ApiException.campo(400, "itens", "Este produto já foi adicionado ao pedido.");
            if (i.quantidade() == null || i.quantidade() <= 0) throw ApiException.campo(400, "itens", "A quantidade deve ser maior que zero.");
        }
        // 2. Produtos existem e estão ativos
        List<Integer> ids = itens.stream().map(ItemNovo::produtoId).toList();
        Map<Integer, Produto.Basico> porId = produtos.buscarPorIds(ids);
        for (Integer id : ids) {
            Produto.Basico p = porId.get(id);
            if (p == null || !p.ativo()) throw ApiException.campo(400, "itens", "Produto não encontrado.");
        }
        // 3. Quanto cada ingrediente precisa (BigDecimal) e qual produto o pede primeiro (para a mensagem do 422)
        Map<Integer, List<Produto.ItemComposicao>> composicoes = produtos.composicoes(ids);
        Map<Integer, BigDecimal> necessario = new TreeMap<>();               // TreeMap: ingredientes em ordem de id
        Map<Integer, String> produtoQuePede = new TreeMap<>();
        for (ItemNovo i : itens) {
            for (Produto.ItemComposicao c : composicoes.getOrDefault(i.produtoId(), List.of())) {
                necessario.merge(c.ingredienteId(), c.quantidadeUtilizada().multiply(BigDecimal.valueOf(i.quantidade())), BigDecimal::add);
                produtoQuePede.putIfAbsent(c.ingredienteId(), porId.get(i.produtoId()).nome());
            }
        }
        // 4. Travar em ORDEM DE ID (sem deadlock) e 5. carregar só as linhas válidas
        ingredientes.travar(new ArrayList<>(necessario.keySet()));
        Map<Integer, List<MovimentacaoRepository.Linha>> linhas = new TreeMap<>();
        for (var e : necessario.entrySet()) {
            var validas = movimentacoes.linhasValidas(e.getKey(), hoje);
            // 6. Algum ingrediente não cobre: 422 e nada é gravado
            if (BaixaPorLinhas.disponivel(validas).compareTo(e.getValue()) < 0)
                throw new ApiException(422, "Estoque insuficiente para o produto " + produtoQuePede.get(e.getKey()), null);
            linhas.put(e.getKey(), validas);
        }
        // 7. Total com o preço DO BANCO e 8. itens com o preço registrado
        BigDecimal total = BigDecimal.ZERO;
        List<ItemGravado> gravar = new ArrayList<>();
        for (ItemNovo i : itens) {
            BigDecimal preco = porId.get(i.produtoId()).precoVenda();
            total = total.add(preco.multiply(BigDecimal.valueOf(i.quantidade())));
            gravar.add(new ItemGravado(i.produtoId(), i.quantidade(), preco));
        }
        int pedidoId = pedidos.inserir(req.clienteId(), usuarioId, total, agora);       // cliente inexistente: FK -> 400
        pedidos.inserirItens(pedidoId, gravar);
        // 9. Baixa por linhas: uma SAIDA (motivo PEDIDO) por ingrediente
        for (var e : necessario.entrySet()) {
            baixa.consumir(e.getKey(), linhas.get(e.getKey()), e.getValue(), "PEDIDO", usuarioId, agora,
                    "Baixa automática do pedido " + pedidoId,
                    () -> new ApiException(422, "Estoque insuficiente para o produto " + produtoQuePede.get(e.getKey()), null));
        }
        return new PedidoCriado(pedidoId, total, agora);
    }
}
