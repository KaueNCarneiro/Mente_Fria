package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * US17 com banco real. Pedidos (conta à mão):
 * 15/09 Dono 2x Tradicional (25,80) | 05/10 Func 3x Tradicional (38,70) | 20/10 Dono 1x Tradicional + 1x Especial (31,80)
 * | 28/10 Func 2x Especial (37,80, CANCELADO depois) | 31/10 23h30 Dono 1x Tradicional (12,90).
 */
class DashboardTest extends IntegracaoBase {

    private void pedido(String token, LocalDateTime quando, Map<?, ?>... itens) throws Exception {
        relogio.em(quando);
        var r = chamar("POST", "/api/pedidos", token, Map.of("itens", List.of(itens)));
        assertThat(r.status()).as(r.texto()).isEqualTo(201);
    }

    void cenario() throws Exception {
        entrada(idIngrediente("Açaí (polpa)"), "20", HOJE.plusDays(60));
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "100", null);
        entrada(idIngrediente("Granola"), "1", HOJE.plusDays(60));
        entrada(idIngrediente("Banana"), "1", HOJE.plusDays(60));
        entrada(idIngrediente("Morango"), "1", HOJE.minusDays(1));                    // vencido
        pedido(admin, LocalDateTime.of(2026, 9, 15, 10, 0), Map.of("produto_id", 1, "quantidade", 2));
        pedido(funcionario, LocalDateTime.of(2026, 10, 5, 10, 0), Map.of("produto_id", 1, "quantidade", 3));
        pedido(admin, LocalDateTime.of(2026, 10, 20, 10, 0), Map.of("produto_id", 1, "quantidade", 1), Map.of("produto_id", 2, "quantidade", 1));
        pedido(funcionario, LocalDateTime.of(2026, 10, 28, 15, 0), Map.of("produto_id", 2, "quantidade", 2));
        pedido(admin, LocalDateTime.of(2026, 10, 31, 23, 30), Map.of("produto_id", 1, "quantidade", 1));     // ainda é outubro em SP (A9)
        jdbc.update("UPDATE pedido SET status = 'CANCELADO', data_cancelamento = data_pedido, usuario_cancelamento_id = usuario_id "
                + "WHERE valor_total = 37.80");
        relogio.em(AGORA);
    }

    private List<String> ranking(JsonNode r) {
        List<String> l = new ArrayList<>();
        r.get("ranking").forEach(x -> l.add(x.get("rank").asInt() + ":" + x.get("usuario").asString() + ":" + x.get("quantidade_pedidos").asInt()
                + ":" + num(x.get("valor_total")) + ":" + x.get("tipos_de_produto").asString()));
        return l;
    }

    @Test void mesAtualSoComPedidosAtivos() throws Exception {
        cenario();
        var r = chamar("GET", "/api/dashboard", admin, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = r.corpo();
        assertThat(d.get("mes").asString()).isEqualTo("2026-10");                                 // padrão: mês atual
        assertThat(d.get("faturamento").get("valor_total").decimalValue()).isEqualByComparingTo("83.40");
        assertThat(d.get("faturamento").get("quantidade_pedidos").asInt()).isEqualTo(3);
        assertThat(d.get("mais_vendidos").get(0).get("nome").asString()).isEqualTo("Açaí Tradicional 300ml");
        assertThat(d.get("mais_vendidos").get(0).get("quantidade").asInt()).isEqualTo(5);
        assertThat(d.get("mais_vendidos").get(0).get("valor_total").decimalValue()).isEqualByComparingTo("64.50");
        assertThat(d.get("mais_vendidos").get(1).get("quantidade").asInt()).isEqualTo(1);        // o cancelado não conta
        List<String> ingredientes = new ArrayList<>();
        d.get("ingredientes_mais_utilizados").forEach(i -> ingredientes.add(i.get("nome").asString() + ":"
                + num(i.get("quantidade")) + ":" + num(i.get("custo_estimado"))));
        assertThat(ingredientes).containsExactly("Açaí (polpa):1.95:36.08", "Copo descartável 500ml:6:2.1",
                "Leite condensado:0.15:1.8", "Granola:0.04:0.88", "Banana:0.05:0.28");
        assertThat(ranking(d)).containsExactly("1:Dono:2:44.7:Açaí Especial 500ml, Açaí Tradicional 300ml", "2:Func:1:38.7:Açaí Tradicional 300ml");
    }

    @Test void filtrosDoRanking() throws Exception {
        cenario();
        assertThat(ranking(chamar("GET", "/api/dashboard?produto_id=2", admin, null).corpo())).containsExactly("1:Dono:1:18.9:Açaí Especial 500ml");
        assertThat(ranking(chamar("GET", "/api/dashboard?usuario_id=" + idUsuario("func@acai.com"), admin, null).corpo()))
                .containsExactly("1:Func:1:38.7:Açaí Tradicional 300ml");
        assertThat(ranking(chamar("GET", "/api/dashboard?valor_min=40", admin, null).corpo())).hasSize(1).first().asString().contains("Dono");
        assertThat(ranking(chamar("GET", "/api/dashboard?valor_max=40", admin, null).corpo())).hasSize(1).first().asString().contains("Func");
        assertThat(ranking(chamar("GET", "/api/dashboard?produto_id=1&produto_id=2", admin, null).corpo())).hasSize(2);
    }

    @Test void outroMesEErrosDeFiltro() throws Exception {
        cenario();
        var set = chamar("GET", "/api/dashboard?mes=2026-09", admin, null).corpo();
        assertThat(set.get("faturamento").get("valor_total").decimalValue()).isEqualByComparingTo("25.80");
        assertThat(ranking(set)).containsExactly("1:Dono:1:25.8:Açaí Tradicional 300ml");
        var mes = chamar("GET", "/api/dashboard?mes=10/2026", admin, null);
        assertThat(mes.status()).isEqualTo(400);
        assertThat(mes.corpo().get("campos").get("mes").asString()).isEqualTo("Informe o mês no formato AAAA-MM.");
        assertThat(chamar("GET", "/api/dashboard?valor_min=50&valor_max=10", admin, null).status()).isEqualTo(400);
        var vazio = chamar("GET", "/api/dashboard?mes=2025-01", admin, null).corpo();
        assertThat(vazio.get("faturamento").get("valor_total").decimalValue()).isEqualByComparingTo("0");
        assertThat(vazio.get("ranking").size()).isZero();
    }

    @Test void situacaoDoEstoqueEUltimoPlano() throws Exception {
        cenario();
        var antes = chamar("GET", "/api/dashboard", admin, null).corpo();
        assertThat(antes.get("ultimo_plano").isNull()).isTrue();
        assertThat(antes.get("otimizador_habilitado").asBoolean()).isTrue();         // padrão: ligado (A31)
        // válido <= mínimo: leite condensado, granola, banana, morango (vencido), leite em pó = 5; com vencido: morango = 1
        assertThat(antes.get("estoque").get("ingredientes_abaixo_do_minimo").asInt()).isEqualTo(5);
        assertThat(antes.get("estoque").get("ingredientes_com_vencido").asInt()).isEqualTo(1);
        when(otimizadorProducao.executar(anyString())).thenReturn("{\"status\":\"sucesso\",\"receita_estimada\":12.9,\"margem_estimada\":6.64,"
                + "\"recomendacao\":[{\"produto_id\":1,\"quantidade\":1},{\"produto_id\":2,\"quantidade\":0},{\"produto_id\":3,\"quantidade\":0}],\"consumo\":[]}");
        assertThat(chamar("POST", "/api/producao/recomendacao", admin, null).status()).isEqualTo(200);
        var depois = chamar("GET", "/api/dashboard", admin, null).corpo().get("ultimo_plano");
        assertThat(depois.get("receita_estimada").decimalValue()).isEqualByComparingTo("12.90");     // "receita máxima estimada" do Termo
        assertThat(depois.get("produtos_priorizados").get(0).asString()).isEqualTo("Açaí Tradicional 300ml");
    }

    @Test void funcionarioNaoVeODashboard() throws Exception {
        assertThat(chamar("GET", "/api/dashboard", funcionario, null).status()).isEqualTo(403);
    }
}
