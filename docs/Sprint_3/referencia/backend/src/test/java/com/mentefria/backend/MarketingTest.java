package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mentefria.backend.marketing.MarketingService;
import org.junit.jupiter.api.Test;

/** US18: sugestões a partir da última recomendação de produção, guardada na memória do backend (D15). */
class MarketingTest extends IntegracaoBase {

    @Test void semCalculoPedeParaGerarNaTelaProducao() throws Exception {
        var r = chamar("GET", "/api/marketing/sugestoes", admin, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo(MarketingService.SEM_CALCULO);
        assertThat(r.corpo().get("sugestoes").size()).isZero();
        assertThat(r.corpo().get("calculado_em").isNull()).isTrue();
    }

    @Test void ateTresProdutosPriorizadosComIdeiasDiferentes() throws Exception {
        estoqueDaDemonstracao();
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_demo.json"));
        chamar("POST", "/api/producao/recomendacao", admin, null);
        var r = chamar("GET", "/api/marketing/sugestoes", admin, null).corpo();
        assertThat(r.get("calculado_em").asString()).isEqualTo("2026-10-28T15:00:00");
        assertThat(r.get("mensagem").isNull()).isTrue();
        var s = r.get("sugestoes");
        assertThat(s.size()).isEqualTo(2);                                       // o Premium veio 0: não é priorizado
        assertThat(s.get(0).get("produto").asString()).isEqualTo("Açaí Tradicional 300ml");
        assertThat(s.get(0).get("quantidade_recomendada").asInt()).isEqualTo(151);
        assertThat(s.get(0).get("selo").asString()).isEqualTo("Produto priorizado");
        assertThat(s.get(0).get("lucro_por_copo").decimalValue()).isEqualByComparingTo("6.64");
        assertThat(s.get(1).get("produto").asString()).isEqualTo("Açaí Especial 500ml");
        // 28/10 é o dia 301 do ano: a primeira ideia é a de índice (301 x 3) mod 18 = 3 (18 ideias no catálogo, A20)
        assertThat(s.get(0).get("ideia").asString()).isEqualTo("Foto bonita");
        assertThat(s.get(1).get("ideia").asString()).isEqualTo("Combo para dividir");
        assertThat(s.get(0).get("como_fazer").asString()).contains("Açaí Tradicional 300ml");
        var titulos = com.mentefria.backend.marketing.CatalogoIdeias.IDEIAS.stream()
                .map(com.mentefria.backend.marketing.CatalogoIdeias.Ideia::titulo).distinct().count();
        assertThat(titulos).isEqualTo(18);                                         // sem títulos repetidos
    }

    @Test void recomendacaoTodaZeroNaoTemPriorizado() throws Exception {
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_estoque_zero.json"));
        chamar("POST", "/api/producao/recomendacao", admin, null);
        var r = chamar("GET", "/api/marketing/sugestoes", admin, null).corpo();
        assertThat(r.get("mensagem").asString()).isEqualTo(MarketingService.SEM_PRIORIZADO);
        assertThat(r.get("sugestoes").size()).isZero();
    }

    @Test void funcionarioNaoVeMarketing() throws Exception {
        assertThat(chamar("GET", "/api/marketing/sugestoes", funcionario, null).status()).isEqualTo(403);
    }
}
