package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;

/**
 * US14 com banco real e o módulo de reposição trocado por um dublê. Cenário (conta à mão):
 * 10/10: 10x Tradicional; 28/10: 5x Tradicional + 2x Especial. Estoque inicial: açaí 20, leite condensado 5, copo 100,
 * granola 1, banana 1 (todos válidos).
 */
class ReposicaoTest extends IntegracaoBase {

    void cenario() throws Exception {
        entrada(idIngrediente("Açaí (polpa)"), "20", HOJE.plusDays(60));
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "100", null);
        entrada(idIngrediente("Granola"), "1", HOJE.plusDays(60));
        entrada(idIngrediente("Banana"), "1", HOJE.plusDays(60));
        relogio.em(LocalDateTime.of(2026, 10, 10, 12, 0));
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 10)))).status()).isEqualTo(201);
        relogio.em(AGORA);
        assertThat(chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(
                Map.of("produto_id", 1, "quantidade", 5), Map.of("produto_id", 2, "quantidade", 2)))).status()).isEqualTo(201);
    }

    private JsonNode enviadoAoPython() throws Exception {
        ArgumentCaptor<String> enviado = ArgumentCaptor.forClass(String.class);
        verify(otimizadorReposicao).executar(enviado.capture());
        return json.readTree(enviado.getValue());
    }

    private static final String RESPOSTA_PY = """
            {"status":"sucesso","orcamento_suficiente":true,"custo_total":63.22,"valor_faltante":0.0,"cobertura_minima":1.0,
             "itens":[{"ingrediente_id":3,"quantidade_sugerida":1.16,"quantidade_minima_compra":1.08,"quantidade_necessaria":1.16,"custo_estimado":25.52},
                      {"ingrediente_id":1,"quantidade_sugerida":0.8,"quantidade_minima_compra":0.0,"quantidade_necessaria":0.8,"custo_estimado":14.8},
                      {"ingrediente_id":4,"quantidade_sugerida":2.2,"quantidade_minima_compra":2.1,"quantidade_necessaria":2.2,"custo_estimado":12.1},
                      {"ingrediente_id":2,"quantidade_sugerida":0.9,"quantidade_minima_compra":0.45,"quantidade_necessaria":0.9,"custo_estimado":10.8},
                      {"ingrediente_id":7,"quantidade_sugerida":0.0,"quantidade_minima_compra":0.0,"quantidade_necessaria":0.0,"custo_estimado":0.0}]}""";

    @Test void enviaOConsumoDoPeriodoOSaldoValidoEOMinimo() throws Exception {
        cenario();
        when(otimizadorReposicao.executar(anyString())).thenReturn(RESPOSTA_PY);
        var r = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(r.status()).isEqualTo(200);
        JsonNode e = enviadoAoPython();
        assertThat(e.get("orcamento").decimalValue()).isEqualByComparingTo("100");
        List<String> ingredientes = new ArrayList<>();
        e.get("ingredientes").forEach(i -> ingredientes.add(i.get("id").asInt() + ":" + num(i.get("consumo_periodo"))
                + ":" + num(i.get("quantidade_estoque")) + ":" + num(i.get("quantidade_minima"))));
        // mais consumido primeiro: copo 17, açaí 5,4, leite 0,45, banana 0,10, granola 0,08 (saldo = válido depois das baixas)
        assertThat(ingredientes).containsExactly("7:17:83:50", "1:5.4:14.6:10", "2:0.45:4.55:5", "4:0.1:0.9:3", "3:0.08:0.92:2");
        JsonNode itens = r.corpo().get("itens");
        assertThat(itens.get(0).get("nome").asString()).isEqualTo("Granola");                // ordem do Python: maior custo primeiro
        assertThat(itens.get(0).get("quantidade_sugerida").decimalValue()).isEqualByComparingTo("1.16");
        assertThat(itens.get(0).get("unidade_medida").asString()).isEqualTo("kg");
        assertThat(r.corpo().get("custo_total").decimalValue()).isEqualByComparingTo("63.22");
        assertThat(r.corpo().get("periodo_dias").asInt()).isEqualTo(30);
    }

    @Test void periodoCurtoSoContaOsPedidosDoPeriodo() throws Exception {
        cenario();
        when(otimizadorReposicao.executar(anyString())).thenReturn(RESPOSTA_PY);
        chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 7, "orcamento", 100));
        JsonNode e = enviadoAoPython();
        assertThat(e.get("ingredientes").get(0).get("consumo_periodo").decimalValue()).isEqualByComparingTo("7");    // copo: só o de 28/10
    }

    @Test void pedidoCanceladoContaNoConsumoDaReposicao() throws Exception {                      // decisão de 14/10
        cenario();
        jdbc.update("UPDATE pedido SET status = 'CANCELADO', data_cancelamento = ?, usuario_cancelamento_id = usuario_id "
                + "WHERE data_pedido < '2026-10-20'", AGORA);
        when(otimizadorReposicao.executar(anyString())).thenReturn(RESPOSTA_PY);
        chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(enviadoAoPython().get("ingredientes").get(0).get("consumo_periodo").decimalValue()).isEqualByComparingTo("17");
    }

    @Test void ate30DiasEntramOs8MaisConsumidosEAcimaOs15() throws Exception {
        // 12 ingredientes extras num produto, com pedido gravado direto no banco (a consulta só lê pedidos x composição)
        jdbc.update("INSERT INTO produto (nome, preco_venda) VALUES ('Açaí Completo', 40.00)");
        for (int i = 1; i <= 12; i++) {
            jdbc.update("INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES (?, 'kg', 1.00, 0.010)", "Extra " + i);
            jdbc.update("INSERT INTO produto_ingrediente VALUES (4, (SELECT id FROM ingrediente WHERE nome = ?), ?)", "Extra " + i, 0.010 * i);
        }
        jdbc.update("INSERT INTO pedido (usuario_id, data_pedido, valor_total) VALUES (?, ?, 40.00)", idUsuario("dono@acai.com"), AGORA.minusHours(1));
        jdbc.update("INSERT INTO pedido_produto VALUES (1, 4, 1, 40.00)");
        when(otimizadorReposicao.executar(anyString())).thenReturn("{\"status\":\"sucesso\",\"orcamento_suficiente\":true,\"custo_total\":0,"
                + "\"valor_faltante\":0,\"cobertura_minima\":1,\"itens\":[]}");
        chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 10));
        assertThat(enviadoAoPython().get("ingredientes").size()).isEqualTo(8);
        org.mockito.Mockito.clearInvocations(otimizadorReposicao);
        chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 31, "orcamento", 10));
        JsonNode e = enviadoAoPython();
        assertThat(e.get("ingredientes").size()).isEqualTo(12);              // só 12 consumidos (< 15)
        assertThat(e.get("ingredientes").get(0).get("consumo_periodo").decimalValue()).isEqualByComparingTo("0.120");   // o maior primeiro
    }

    @Test void semPedidosNoPeriodoNaoChamaOPython() throws Exception {
        var r = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().get("itens").size()).isZero();
        assertThat(r.corpo().get("mensagem").asString()).contains("Não houve pedidos");
        verify(otimizadorReposicao, never()).executar(anyString());
    }

    @Test void validacaoDoPeriodoEDoOrcamento() throws Exception {
        var r = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 0, "orcamento", -5));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("periodo_dias").asString()).isEqualTo("Informe um período entre 1 e 365 dias.");
        assertThat(r.corpo().get("campos").get("orcamento").asString()).isEqualTo("O orçamento não pode ser negativo.");
        assertThat(chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 10.001)).status()).isEqualTo(400);
        assertThat(chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30)).status()).isEqualTo(400);
    }

    @Test void orcamentoInsuficienteEErroDoPython() throws Exception {
        cenario();
        when(otimizadorReposicao.executar(anyString())).thenReturn(RESPOSTA_PY.replace("\"orcamento_suficiente\":true", "\"orcamento_suficiente\":false")
                .replace("\"valor_faltante\":0.0", "\"valor_faltante\":10.71").replace("\"cobertura_minima\":1.0", "\"cobertura_minima\":null"));
        var r = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 30));
        assertThat(r.corpo().get("orcamento_suficiente").asBoolean()).isFalse();
        assertThat(r.corpo().get("valor_faltante").decimalValue()).isEqualByComparingTo("10.71");
        assertThat(r.corpo().get("mensagem").asString()).contains("não cobre nem os mínimos");
        when(otimizadorReposicao.executar(anyString())).thenReturn("{\"status\":\"erro\",\"mensagem\":\"Orçamento inválido\"}");
        assertThat(chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 30)).status()).isEqualTo(422);
    }

    @Test void funcionarioNaoGeraReposicao() throws Exception {
        assertThat(chamar("POST", "/api/reposicao/recomendacao", funcionario, Map.of("periodo_dias", 30, "orcamento", 10)).status()).isEqualTo(403);
    }
}
