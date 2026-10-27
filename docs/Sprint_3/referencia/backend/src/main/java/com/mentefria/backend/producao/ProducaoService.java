package com.mentefria.backend.producao;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import com.mentefria.backend.ingrediente.IngredienteRepository.IngredienteOtimizador;
import com.mentefria.backend.produto.Produto;
import com.mentefria.backend.produto.ProdutoRepository;
import com.mentefria.backend.python.ChaveOtimizador;
import com.mentefria.backend.python.ModulosPython.OtimizadorProducao;
import com.mentefria.backend.python.ProcessoPython.FalhaPython;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * US10/US11: monta a entrada do Python (Contrato v2 §1.1), chama o módulo e recalcula tudo com BigDecimal.
 * Sem @Transactional de propósito: a chamada ao Python pode levar segundos e não deve segurar uma conexão do banco.
 */
@Service
public class ProducaoService {
    private static final Logger log = LoggerFactory.getLogger(ProducaoService.class);

    // Formatos do Contrato §1 (snake_case pela configuração global do Jackson).
    record IngredientePy(int id, BigDecimal quantidadeEstoque, BigDecimal custoUnitario) {}
    record ItemPy(int ingredienteId, BigDecimal quantidadeUtilizada) {}
    record ProdutoPy(int id, BigDecimal precoVenda, List<ItemPy> composicao) {}
    record EntradaPy(List<IngredientePy> ingredientes, List<ProdutoPy> produtos) {}
    record QuantidadePy(int produtoId, int quantidade) {}
    record RespostaPy(String status, String mensagem, BigDecimal receitaEstimada, BigDecimal margemEstimada,
                      List<QuantidadePy> recomendacao) {}

    private final IngredienteRepository ingredientes;
    private final ProdutoRepository produtos;
    private final OtimizadorProducao otimizador;
    private final UltimaRecomendacao ultima;
    private final JsonMapper json;
    private final Clock clock;
    private final ChaveOtimizador chave;

    public ProducaoService(IngredienteRepository ingredientes, ProdutoRepository produtos, OtimizadorProducao otimizador,
                           UltimaRecomendacao ultima, JsonMapper json, Clock clock, ChaveOtimizador chave) {
        this.ingredientes = ingredientes; this.produtos = produtos; this.otimizador = otimizador;
        this.ultima = ultima; this.json = json; this.clock = clock; this.chave = chave;
    }

    public Recomendacao recomendar() {
        chave.exigir();                                                              // plano B da licença (A31): 503 sem chamar o Python
        LocalDate hoje = LocalDate.now(clock);
        List<IngredienteOtimizador> ings = ingredientes.entradaOtimizador(hoje);     // saldo VÁLIDO (CT29), inclusive 0
        List<Produto.Basico> ativos = produtos.listarAtivos();
        Map<Integer, List<Produto.ItemComposicao>> comp = produtos.composicoes(ativos.stream().map(Produto.Basico::id).toList());

        List<ProdutoPy> produtosPy = new ArrayList<>();
        for (Produto.Basico p : ativos) {
            List<ItemPy> itens = comp.getOrDefault(p.id(), List.of()).stream()
                    .map(c -> new ItemPy(c.ingredienteId(), c.quantidadeUtilizada())).toList();
            produtosPy.add(new ProdutoPy(p.id(), p.precoVenda(), itens));
        }
        EntradaPy entrada = new EntradaPy(ings.stream()
                .map(i -> new IngredientePy(i.id(), i.quantidadeEstoque(), i.custoUnitario())).toList(), produtosPy);

        RespostaPy r = json.readValue(otimizador.executar(json.writeValueAsString(entrada)), RespostaPy.class);
        if ("erro".equals(r.status())) throw new ApiException(422, r.mensagem(), null);          // problema de dados: o usuário corrige
        if (!"sucesso".equals(r.status()) || r.recomendacao() == null) throw new FalhaPython("Resposta fora do contrato", null);

        // Recalcular com BigDecimal (o Python é a fonte do PLANO; os valores em reais são do Java)
        Map<Integer, BigDecimal> custo = new HashMap<>(), estoque = new HashMap<>();
        for (IngredienteOtimizador i : ings) { custo.put(i.id(), i.custoUnitario()); estoque.put(i.id(), i.quantidadeEstoque()); }
        Map<Integer, Integer> plano = new HashMap<>();
        for (QuantidadePy q : r.recomendacao()) plano.put(q.produtoId(), q.quantidade());
        BigDecimal receita = BigDecimal.ZERO, margem = BigDecimal.ZERO;
        Map<Integer, BigDecimal> consumo = new HashMap<>();
        List<Recomendacao.ItemRecomendado> itens = new ArrayList<>();
        for (Produto.Basico p : ativos) {
            Integer x = plano.get(p.id());
            if (x == null || x < 0) throw new FalhaPython("Produto " + p.id() + " sem quantidade na resposta", null);
            BigDecimal custoCopo = BigDecimal.ZERO;
            for (Produto.ItemComposicao c : comp.getOrDefault(p.id(), List.of())) {
                custoCopo = custoCopo.add(custo.get(c.ingredienteId()).multiply(c.quantidadeUtilizada()));
                consumo.merge(c.ingredienteId(), c.quantidadeUtilizada().multiply(BigDecimal.valueOf(x)), BigDecimal::add);
            }
            BigDecimal lucroCopo = p.precoVenda().subtract(custoCopo);
            receita = receita.add(p.precoVenda().multiply(BigDecimal.valueOf(x)));
            margem = margem.add(lucroCopo.multiply(BigDecimal.valueOf(x)));
            itens.add(new Recomendacao.ItemRecomendado(p.id(), p.nome(), x, lucroCopo.setScale(2, RoundingMode.HALF_UP)));
        }
        for (var e : consumo.entrySet())                                    // nunca mostrar um plano que não cabe no estoque
            if (e.getValue().compareTo(estoque.getOrDefault(e.getKey(), BigDecimal.ZERO)) > 0)
                throw new FalhaPython("Plano do Python excede o estoque do ingrediente " + e.getKey(), null);
        receita = receita.setScale(2, RoundingMode.HALF_UP);
        margem = margem.setScale(2, RoundingMode.HALF_UP);
        if (r.margemEstimada() != null && r.margemEstimada().subtract(margem).abs().compareTo(new BigDecimal("0.01")) > 0)
            log.warn("Margem do Python ({}) diverge da recalculada ({}); usando a do Java", r.margemEstimada(), margem);

        Recomendacao resultado = new Recomendacao(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS), receita, margem, itens);
        ultima.guardar(resultado);                                          // Tela 14 (marketing) e dashboard leem daqui
        return resultado;
    }
}
