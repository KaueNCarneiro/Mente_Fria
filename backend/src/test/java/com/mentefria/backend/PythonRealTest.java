package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mentefria.backend.python.ProcessoPython;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Ponta a ponta com o Python REAL (gurobipy instalado): banco real -> Java -> processo Python -> Java.
 * Só roda se MF_PYTHON apontar para o python que tem gurobipy (ex.: optimization\.venv\Scripts\python.exe).
 * Sem as variáveis GRB_*, o gurobipy usa a licença limitada do pip, que basta para estes modelos pequenos.
 */
@EnabledIfEnvironmentVariable(named = "MF_PYTHON", matches = ".+")
@ActiveProfiles("dev")                                            // para o eco (/api/teste/python)
@TestPropertySource(properties = "app.python.cmd=${MF_PYTHON}")
class PythonRealTest extends IntegracaoBase {
    @Autowired ProcessoPython processo;
    @Value("${app.otimizador.script}") String scriptProducao;
    @Value("${app.reposicao.script}") String scriptReposicao;

    @BeforeEach
    void usarOPythonDeVerdade() {                                 // os dublês da base passam a chamar o processo real
        when(otimizadorProducao.executar(anyString())).thenAnswer(i -> processo.executar(scriptProducao, i.getArgument(0)));
        when(otimizadorReposicao.executar(anyString())).thenAnswer(i -> processo.executar(scriptReposicao, i.getArgument(0)));
    }

    @Test void ecoComAcentosPeloPerfilDev() throws Exception {
        var r = chamar("GET", "/api/teste/python", null, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.texto().trim()).isEqualTo("{\"status\": \"sucesso\", \"echo\": \"AÇAÍ (POLPA)\"}");
    }

    @Test void producaoDaDemonstracaoDa151_1_0ComOGurobi() throws Exception {
        estoqueDaDemonstracao();
        long inicio = System.nanoTime();
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(r.status()).as(r.texto()).isEqualTo(200);
        assertThat(r.corpo().get("recomendacao").get(0).get("quantidade").asInt()).isEqualTo(151);
        assertThat(r.corpo().get("recomendacao").get(1).get("quantidade").asInt()).isEqualTo(1);
        assertThat(r.corpo().get("recomendacao").get(2).get("quantidade").asInt()).isZero();
        assertThat(r.corpo().get("margem_estimada").decimalValue()).isEqualByComparingTo("1011.71");
        System.out.println("[TEMPO] recomendação de produção ponta a ponta: " + ms + " ms");
    }

    @Test void reposicaoContaAMao() throws Exception {
        // Mesmo cenário do ReposicaoTest. Necessidade: granola 1,16; açaí 0,80; banana 2,20; leite condensado 0,90; copo 0.
        entrada(idIngrediente("Açaí (polpa)"), "20", HOJE.plusDays(60));
        entrada(idIngrediente("Leite condensado"), "5", null);
        entrada(idIngrediente("Copo descartável 500ml"), "100", null);
        entrada(idIngrediente("Granola"), "1", HOJE.plusDays(60));
        entrada(idIngrediente("Banana"), "1", HOJE.plusDays(60));
        relogio.em(LocalDateTime.of(2026, 10, 10, 12, 0));
        chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 10))));
        relogio.em(AGORA);
        chamar("POST", "/api/pedidos", admin, Map.of("itens", List.of(Map.of("produto_id", 1, "quantidade", 5), Map.of("produto_id", 2, "quantidade", 2))));

        var folga = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 100));
        assertThat(folga.status()).as(folga.texto()).isEqualTo(200);
        assertThat(folga.corpo().get("custo_total").decimalValue()).isEqualByComparingTo("63.22");   // 25,52 + 14,80 + 12,10 + 10,80
        assertThat(folga.corpo().get("cobertura_minima").decimalValue()).isEqualByComparingTo("1");
        assertThat(folga.corpo().get("itens").get(0).get("nome").asString()).isEqualTo("Granola");
        assertThat(folga.corpo().get("itens").get(0).get("custo_estimado").decimalValue()).isEqualByComparingTo("25.52");

        var curto = chamar("POST", "/api/reposicao/recomendacao", admin, Map.of("periodo_dias", 30, "orcamento", 30));
        assertThat(curto.corpo().get("orcamento_suficiente").asBoolean()).isFalse();
        assertThat(curto.corpo().get("valor_faltante").decimalValue()).isEqualByComparingTo("10.71");  // mínimos 40,71 - 30
    }
}
