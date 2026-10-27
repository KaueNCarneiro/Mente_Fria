package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.mentefria.backend.marketing.MarketingService;
import com.mentefria.backend.python.ChaveOtimizador;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Plano B da licença (A31 = B1, CT72): com OTIMIZADOR_HABILITADO=false, como no Render enquanto a Gurobi não autorizar,
 * nada chama o Python. Produção e reposição respondem 503 com mensagem tratada; o resto do sistema segue normal.
 */
@TestPropertySource(properties = "app.otimizador.habilitado=false")
class OtimizadorDesligadoTest extends IntegracaoBase {

    private void umPedido() throws Exception {
        entrada(idIngrediente("Açaí (polpa)"), "20", HOJE.plusDays(60));
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "100", null);
        assertThat(chamar("POST", "/api/pedidos", funcionario,
                Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 2)))).status()).isEqualTo(201);
    }

    @Test void producaoResponde503SemChamarOPython() throws Exception {
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        assertThat(r.status()).isEqualTo(503);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo(ChaveOtimizador.MENSAGEM);
        assertThat(chamar("POST", "/api/producao/recomendacao", funcionario, null).status()).isEqualTo(403);  // permissão vem antes
        verify(otimizadorProducao, never()).executar(anyString());
    }

    @Test void reposicaoValidaPrimeiroDepoisResponde503() throws Exception {
        var invalida = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 0, "orcamento", 100));
        assertThat(invalida.status()).isEqualTo(400);                                   // o erro de campo continua aparecendo
        var semPedidos = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(semPedidos.status()).isEqualTo(200);                                 // sem consumo, o solver nem seria usado
        assertThat(semPedidos.corpo().get("itens").size()).isZero();
        umPedido();
        var r = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(r.status()).isEqualTo(503);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo(ChaveOtimizador.MENSAGEM);
        verify(otimizadorReposicao, never()).executar(anyString());
    }

    @Test void marketingEDashboardExplicamQueOPlanoSoExisteNaDemonstracaoLocal() throws Exception {
        umPedido();
        var m = chamar("GET", "/api/marketing/sugestoes", admin, null);
        assertThat(m.status()).isEqualTo(200);
        assertThat(m.corpo().get("mensagem").asString()).isEqualTo(MarketingService.SEM_OTIMIZADOR);
        assertThat(m.corpo().get("sugestoes").size()).isZero();
        var d = chamar("GET", "/api/dashboard", admin, null);
        assertThat(d.status()).isEqualTo(200);
        assertThat(d.corpo().get("otimizador_habilitado").asBoolean()).isFalse();
        assertThat(d.corpo().get("ultimo_plano").isNull()).isTrue();
        assertThat(d.corpo().get("faturamento").get("quantidade_pedidos").asInt()).isEqualTo(1);  // o resto do painel funciona
    }
}
