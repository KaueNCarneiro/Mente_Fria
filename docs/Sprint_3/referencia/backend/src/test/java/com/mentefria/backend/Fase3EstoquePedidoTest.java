package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.pedido.PedidoService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Fase 3 (US9, US12) com banco real: cenários S1–S5, concorrência, CT21, CT25, CT26, CT27, CT28, CT39 e o formato das respostas. */
class Fase3EstoquePedidoTest extends IntegracaoBase {
    @Autowired PedidoService pedidos;

    private Map<String, Object> saida(int ing, String motivo, String qtd) {
        var m = new LinkedHashMap<String, Object>();
        m.put("ingrediente_id", ing); m.put("tipo", "SAIDA"); m.put("motivo", motivo); m.put("quantidade", dec(qtd));
        return m;
    }

    private List<String> restantes(int ing) {
        return jdbc.queryForList("SELECT quantidade_restante::text FROM movimentacao_estoque WHERE ingrediente_id = ? AND tipo = 'ENTRADA' "
                + "ORDER BY data_validade NULLS LAST, id", String.class, ing);
    }

    @Test void entradaSomaAoSaldoEApareceComoValida() throws Exception {                          // CT22, CT30
        int acai = idIngrediente("Açaí (polpa)");
        var r = chamar("POST", "/api/estoque/movimentacoes", funcionario,
                Map.of("ingrediente_id", acai, "tipo", "ENTRADA", "quantidade", 10, "data_validade", "2026-11-30", "observacao", "Lote 3"));
        assertThat(r.status()).isEqualTo(201);                                                      // CT27: o Funcionário pode
        assertThat(r.corpo().get("quantidade_estoque").decimalValue()).isEqualByComparingTo("10");
        assertThat(r.corpo().get("data_movimentacao").asString()).isEqualTo("2026-10-28T15:00:00"); // A9: hora do Java (São Paulo)
        var estoque = chamar("GET", "/api/estoque", funcionario, null).corpo().get(0);
        assertThat(estoque.get("nome").asString()).isEqualTo("Açaí (polpa)");
        assertThat(estoque.get("quantidade_valida").decimalValue()).isEqualByComparingTo("10");
        assertThat(estoque.get("validade_mais_proxima").asString()).isEqualTo("2026-11-30");
        assertThat(jdbc.queryForObject("SELECT usuario_id FROM movimentacao_estoque", Integer.class)).isEqualTo(idUsuario("func@acai.com"));
    }

    @Test void s1VencidoNaoEntraNaVendaEODescarteZeraOVencido() throws Exception {               // S1, CT34
        int acai = idIngrediente("Açaí (polpa)");
        entrada(acai, "20", HOJE.minusDays(2));                                                    // aceita: nasce vencida
        entrada(acai, "20", HOJE.plusDays(1));
        var item = chamar("GET", "/api/estoque", admin, null).corpo().get(0);
        assertThat(item.get("quantidade_estoque").decimalValue()).isEqualByComparingTo("40");
        assertThat(item.get("quantidade_valida").decimalValue()).isEqualByComparingTo("20");
        assertThat(item.get("quantidade_vencida").decimalValue()).isEqualByComparingTo("20");
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, saida(acai, "CONVENCIONAL", "5")).status()).isEqualTo(201);
        assertThat(restantes(acai)).containsExactly("20.000", "15.000");                         // tirou da válida, não da vencida
        var descarte = chamar("POST", "/api/estoque/movimentacoes", admin, saida(acai, "DESCARTE_VENCIDO", "20"));
        assertThat(descarte.status()).isEqualTo(201);
        assertThat(descarte.corpo().get("quantidade_estoque").decimalValue()).isEqualByComparingTo("15");
        assertThat(restantes(acai)).containsExactly("0.000", "15.000");
        assertThat(violacoesDaInvariante()).isZero();
    }

    @Test void s2ASaidaSaiDaMenorValidadeMesmoQueTenhaChegadoDepois() throws Exception {          // S2
        int acai = idIngrediente("Açaí (polpa)");
        entrada(acai, "20", HOJE.plusDays(20));
        chamar("POST", "/api/estoque/movimentacoes", admin, saida(acai, "CONVENCIONAL", "10"));
        entrada(acai, "20", HOJE.plusDays(15));                                                    // chegou depois, vence antes
        chamar("POST", "/api/estoque/movimentacoes", admin, saida(acai, "CONVENCIONAL", "5"));
        assertThat(restantes(acai)).containsExactly("15.000", "10.000");
    }

    @Test void s3BaixaAtravessaLinhasESemValidadeFicaPorUltimo() throws Exception {               // S3
        int copo = idIngrediente("Copo descartável 500ml");
        entrada(copo, "10", null);
        entrada(copo, "3", HOJE.plusDays(1));
        entrada(copo, "4", HOJE.plusDays(5));
        chamar("POST", "/api/estoque/movimentacoes", admin, saida(copo, "CONVENCIONAL", "6"));
        assertThat(restantes(copo)).containsExactly("0.000", "1.000", "10.000");
        chamar("POST", "/api/estoque/movimentacoes", admin, saida(copo, "CONVENCIONAL", "5"));
        assertThat(restantes(copo)).containsExactly("0.000", "0.000", "6.000");
    }

    @Test void s4SoHaVencidoEntaoPedidoDa422ENadaMuda() throws Exception {                       // S4, CT25, CT29
        int acai = idIngrediente("Açaí (polpa)");
        entrada(acai, "50", HOJE.minusDays(1));
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "100", null);
        var r = chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 1))));
        assertThat(r.status()).isEqualTo(422);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo("Estoque insuficiente para o produto Açaí Tradicional 300ml");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pedido", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao_estoque WHERE tipo = 'SAIDA'", Integer.class)).isZero();
        assertThat(saldo(acai)).isEqualByComparingTo("50");
    }

    @Test void s5DescarteMaiorQueOVencidoDa422EOExatoFunciona() throws Exception {                // S5, CT21
        int granola = idIngrediente("Granola");
        entrada(granola, "8", HOJE.minusDays(3));
        entrada(granola, "4", HOJE.plusDays(30));
        var demais = chamar("POST", "/api/estoque/movimentacoes", admin, saida(granola, "DESCARTE_VENCIDO", "9"));
        assertThat(demais.status()).isEqualTo(422);
        assertThat(demais.corpo().get("mensagem").asString()).isEqualTo("Estoque insuficiente para o ingrediente Granola");
        assertThat(saldo(granola)).isEqualByComparingTo("12");
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, saida(granola, "DESCARTE_VENCIDO", "8")).status()).isEqualTo(201);
        assertThat(restantes(granola)).containsExactly("0.000", "4.000");
        var convencionalDemais = chamar("POST", "/api/estoque/movimentacoes", admin, saida(granola, "CONVENCIONAL", "4.001"));
        assertThat(convencionalDemais.status()).isEqualTo(422);
    }

    @Test void regrasDeValidacaoDaMovimentacao() throws Exception {                               // CT26, CT35
        int acai = idIngrediente("Açaí (polpa)");
        var comValidade = saida(acai, "CONVENCIONAL", "1"); comValidade.put("data_validade", "2026-12-01");
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, comValidade).corpo().get("campos").get("data_validade").asString())
                .isEqualTo("A validade só pode ser informada em entradas.");
        var semMotivo = saida(acai, null, "1");
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, semMotivo).corpo().get("campos").get("motivo").asString())
                .isEqualTo("Informe o motivo da saída.");
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, saida(acai, "PEDIDO", "1")).status()).isEqualTo(400);
        var entradaComMotivo = Map.of("ingrediente_id", acai, "tipo", "ENTRADA", "motivo", "CONVENCIONAL", "quantidade", 1);
        assertThat(chamar("POST", "/api/estoque/movimentacoes", admin, entradaComMotivo).status()).isEqualTo(400);
        var zero = chamar("POST", "/api/estoque/movimentacoes", admin, Map.of("ingrediente_id", acai, "tipo", "ENTRADA", "quantidade", 0));
        assertThat(zero.corpo().get("campos").get("quantidade").asString()).isEqualTo("A quantidade deve ser maior que zero.");
        var tipo = chamar("POST", "/api/estoque/movimentacoes", admin, Map.of("ingrediente_id", acai, "tipo", "AJUSTE", "quantidade", 1));
        assertThat(tipo.corpo().get("campos").get("tipo").asString()).isEqualTo("Tipo de movimentação inválido.");
        var inexistente = chamar("POST", "/api/estoque/movimentacoes", admin, Map.of("ingrediente_id", 999, "tipo", "ENTRADA", "quantidade", 1));
        assertThat(inexistente.status()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao_estoque", Integer.class)).isZero();
    }

    @Test void pedidoBaixaPorLinhasComUmaSaidaPorIngredienteEPrecoDoBanco() throws Exception {    // CT38, CT28, CT39
        int acai = idIngrediente("Açaí (polpa)"), leite = idIngrediente("Leite condensado"), copo = idIngrediente("Copo descartável 500ml");
        entrada(acai, "1", HOJE.plusDays(2));
        entrada(acai, "5", HOJE.plusDays(10));
        entrada(leite, "1", null);
        entrada(copo, "10", null);
        var r = chamar("POST", "/api/pedidos", funcionario, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 4))));
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("valor_total").decimalValue()).isEqualByComparingTo("51.60");   // 4 x 12,90 (preço do banco)
        assertThat(r.corpo().get("data_pedido").asString()).isEqualTo("2026-10-28T15:00:00");   // A9 / CT39
        assertThat(restantes(acai)).containsExactly("0.000", "4.800");                           // 1,200 kg: atravessa as duas linhas
        assertThat(saldo(leite)).isEqualByComparingTo("0.880");
        assertThat(saldo(copo)).isEqualByComparingTo("6");
        assertThat(jdbc.queryForList("SELECT motivo || ':' || quantidade FROM movimentacao_estoque WHERE tipo = 'SAIDA' ORDER BY ingrediente_id",
                String.class)).containsExactly("PEDIDO:1.200", "PEDIDO:0.120", "PEDIDO:4.000");
        assertThat(jdbc.queryForObject("SELECT preco_unitario_registrado FROM pedido_produto", java.math.BigDecimal.class))
                .isEqualByComparingTo("12.90");
        assertThat(violacoesDaInvariante()).isZero();
        var lista = chamar("GET", "/api/pedidos", admin, null).corpo();
        assertThat(lista.get(0).get("status").asString()).isEqualTo("ATIVO");                     // CT40
        assertThat(lista.get(0).get("usuario_nome").asString()).isEqualTo("Func");
        assertThat(lista.get(0).get("itens").get(0).get("nome").asString()).isEqualTo("Açaí Tradicional 300ml");
    }

    @Test void pedidoComProdutoRepetidoInexistenteOuQuantidadeZeroDa400() throws Exception {
        var repetido = chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(
                Map.of("produto_id", 1, "quantidade", 1), Map.of("produto_id", 1, "quantidade", 2))));
        assertThat(repetido.corpo().get("campos").get("itens").asString()).isEqualTo("Este produto já foi adicionado ao pedido.");
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 99, "quantidade", 1)))).status()).isEqualTo(400);
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 0)))).status()).isEqualTo(400);
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of())).status()).isEqualTo(400);
        jdbc.update("UPDATE produto SET ativo = false WHERE id = 2");
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 2, "quantidade", 1)))).status()).isEqualTo(400);
    }

    @Test void pedidoComUmItemSemEstoqueNaoGravaNenhumItem() throws Exception {                   // CT25
        entrada(idIngrediente("Açaí (polpa)"), "10", null);
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "50", null);
        var r = chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(          // o Especial precisa de granola, que está em 0
                Map.of("produto_id", 1, "quantidade", 1), Map.of("produto_id", 2, "quantidade", 1))));
        assertThat(r.status()).isEqualTo(422);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo("Estoque insuficiente para o produto Açaí Especial 500ml");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pedido_produto", Integer.class)).isZero();
        assertThat(saldo(idIngrediente("Açaí (polpa)"))).isEqualByComparingTo("10");
    }

    @Test void clienteInexistenteDa400ENadaFicaGravado() throws Exception {
        entrada(idIngrediente("Açaí (polpa)"), "10", null);
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "50", null);
        var r = chamar("POST", "/api/pedidos", admin, Map.of("cliente_id", 77, "itens", List.of(Map.of("produto_id", 1, "quantidade", 1))));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("cliente_id").asString()).isEqualTo("Cliente não encontrado.");
        assertThat(saldo(idIngrediente("Açaí (polpa)"))).isEqualByComparingTo("10");
    }

    @Test void linhasDoIngredienteTrazemASituacao() throws Exception {                            // A12
        int banana = idIngrediente("Banana");
        entrada(banana, "1", HOJE.minusDays(1));
        entrada(banana, "2", HOJE);                                       // vence hoje: ainda vale (PROXIMA)
        entrada(banana, "3", HOJE.plusDays(8));
        entrada(banana, "4", null);
        var r = chamar("GET", "/api/estoque/" + banana + "/linhas", funcionario, null).corpo();
        List<String> situacoes = new ArrayList<>();
        r.forEach(l -> situacoes.add(l.get("situacao").asString()));
        assertThat(situacoes).containsExactly("VENCIDA", "PROXIMA", "OK", "SEM_VALIDADE");
        assertThat(chamar("GET", "/api/estoque/999/linhas", admin, null).status()).isEqualTo(404);
    }

    @Test void descarteDeUmaLinhaVencidaZeraSoEssaLinha() throws Exception {                      // A12, CT71
        int acai = idIngrediente("Açaí (polpa)");
        int vencida = entrada(acai, "20", HOJE.minusDays(2));
        int valida = entrada(acai, "20", HOJE.plusDays(1));
        var r = chamar("POST", "/api/estoque/linhas/" + vencida + "/descarte", funcionario, null);
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("tipo").asString()).isEqualTo("SAIDA");
        assertThat(r.corpo().get("motivo").asString()).isEqualTo("DESCARTE_VENCIDO");
        assertThat(r.corpo().get("quantidade").decimalValue()).isEqualByComparingTo("20");
        assertThat(r.corpo().get("quantidade_estoque").decimalValue()).isEqualByComparingTo("20");
        assertThat(restantes(acai)).containsExactly("0.000", "20.000");
        assertThat(jdbc.queryForObject("SELECT usuario_id FROM movimentacao_estoque WHERE id = ?", Integer.class,
                r.corpo().get("id").asInt())).isEqualTo(idUsuario("func@acai.com"));
        assertThat(violacoesDaInvariante()).isZero();

        var deNovo = chamar("POST", "/api/estoque/linhas/" + vencida + "/descarte", admin, null);
        assertThat(deNovo.status()).isEqualTo(422);
        assertThat(deNovo.corpo().get("mensagem").asString()).isEqualTo("Esta linha já não tem saldo.");
        var naoVencida = chamar("POST", "/api/estoque/linhas/" + valida + "/descarte", admin, null);
        assertThat(naoVencida.status()).isEqualTo(422);
        assertThat(naoVencida.corpo().get("mensagem").asString()).isEqualTo("Só é possível descartar linhas vencidas.");
        assertThat(chamar("POST", "/api/estoque/linhas/999/descarte", admin, null).status()).isEqualTo(404);
        assertThat(chamar("POST", "/api/estoque/linhas/" + r.corpo().get("id").asInt() + "/descarte", admin, null).status())
                .isEqualTo(404);                                                                    // uma SAIDA não é linha
        assertThat(chamar("POST", "/api/estoque/linhas/" + vencida + "/descarte", null, null).status()).isEqualTo(401);
        assertThat(saldo(acai)).isEqualByComparingTo("20");
        assertThat(restantes(acai)).containsExactly("0.000", "20.000");
    }

    @Test void validadeUsaODiaDeSaoPauloEnaoODoServidor() throws Exception {                      // F3, A9
        int acai = idIngrediente("Açaí (polpa)");
        entrada(acai, "5", LocalDate.of(2026, 10, 28));
        relogio.em(java.time.LocalDateTime.of(2026, 10, 28, 23, 30));     // 23h30 em SP = 02h30 de 29/10 em UTC
        var item = chamar("GET", "/api/estoque", admin, null).corpo().get(0);
        assertThat(item.get("quantidade_valida").decimalValue()).isEqualByComparingTo("5");           // ainda é dia 28 para o dono
    }

    @Test void doisPedidosSimultaneosNaoVendemAlemDoEstoque() throws Exception {                   // concorrência (30/30 em SQL)
        int acai = idIngrediente("Açaí (polpa)");
        jdbc.update("INSERT INTO produto (nome, preco_venda) VALUES ('Balde de açaí', 50.00)");
        int balde = jdbc.queryForObject("SELECT id FROM produto WHERE nome = 'Balde de açaí'", Integer.class);
        jdbc.update("INSERT INTO produto_ingrediente VALUES (?, ?, 1.000)", balde, acai);
        int usuario = idUsuario("dono@acai.com");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int rodada = 0; rodada < 5; rodada++) {
                jdbc.update("UPDATE movimentacao_estoque SET quantidade_restante = 0 WHERE ingrediente_id = ? AND tipo = 'ENTRADA'", acai);
                jdbc.update("UPDATE ingrediente SET quantidade_estoque = 0 WHERE id = ?", acai);
                entrada(acai, "50", null);
                CountDownLatch largada = new CountDownLatch(1);
                List<Future<String>> f = new ArrayList<>();
                for (int t = 0; t < 2; t++) f.add(pool.submit(() -> {
                    largada.await();
                    try { pedidos.criar(new PedidoService.NovoPedido(null, List.of(new PedidoService.ItemNovo(balde, 30))), usuario); return "ok"; }
                    catch (ApiException e) { return String.valueOf(e.status()); }
                }));
                largada.countDown();
                List<String> resultados = new ArrayList<>(List.of(f.get(0).get(), f.get(1).get()));
                Collections.sort(resultados);
                assertThat(resultados).as("rodada " + rodada).containsExactly("422", "ok");
                assertThat(saldo(acai)).isEqualByComparingTo("20");
                assertThat(violacoesDaInvariante()).isZero();
            }
        } finally { pool.shutdownNow(); }
    }
}
