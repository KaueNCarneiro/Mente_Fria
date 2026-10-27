package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mentefria.backend.python.ProcessoPython.FalhaPython;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;

/** Fase 4A (US10, US11) com banco real e o Python trocado por um dublê que devolve as saídas guardadas. */
class Fase4ProducaoTest extends IntegracaoBase {

    @Test void demonstracaoDa151_1_0ComValoresRecalculadosEmBigDecimal() throws Exception {      // F8
        estoqueDaDemonstracao();
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_demo.json"));
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().get("calculado_em").asString()).isEqualTo("2026-10-28T15:00:00");
        assertThat(r.corpo().get("receita_estimada").decimalValue()).isEqualByComparingTo("1966.80");
        assertThat(r.corpo().get("margem_estimada").decimalValue()).isEqualByComparingTo("1011.71");
        JsonNode itens = r.corpo().get("recomendacao");
        assertThat(itens.get(0).get("nome").asString()).isEqualTo("Açaí Tradicional 300ml");
        assertThat(itens.get(0).get("quantidade").asInt()).isEqualTo(151);
        assertThat(itens.get(0).get("lucro_por_copo").decimalValue()).isEqualByComparingTo("6.64");
        assertThat(itens.get(1).get("lucro_por_copo").decimalValue()).isEqualByComparingTo("9.07");
        assertThat(itens.get(2).get("quantidade").asInt()).isZero();
        assertThat(itens.get(2).get("lucro_por_copo").decimalValue()).isEqualByComparingTo("10.63");
    }

    @Test void oPythonRecebeOSaldoValidoSemOVencidoEComCusto() throws Exception {                  // CT29, CT42, Contrato §1.1
        estoqueDaDemonstracao();
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_demo.json"));
        chamar("POST", "/api/producao/recomendacao", admin, null);
        ArgumentCaptor<String> enviado = ArgumentCaptor.forClass(String.class);
        verify(otimizadorProducao).executar(enviado.capture());
        JsonNode entrada = json.readTree(enviado.getValue());
        JsonNode granola = entrada.get("ingredientes").get(2);
        assertThat(granola.get("id").asInt()).isEqualTo(3);
        assertThat(granola.get("quantidade_estoque").decimalValue()).isEqualByComparingTo("5.160");     // 6 kg vencidos fora
        assertThat(granola.get("custo_unitario").decimalValue()).isEqualByComparingTo("22.00");
        assertThat(entrada.get("produtos").size()).isEqualTo(3);
        assertThat(entrada.get("produtos").get(0).get("composicao").get(0).has("quantidade_utilizada")).isTrue();
        confereComExemplo(enviado.getValue(), "entrada_demo.json");                                     // o mesmo conteúdo do exemplo do Kauê
    }

    @Test void estoqueZeradoEnviaTodosOsIngredientesComZero() throws Exception {                 // Contrato: inclusive os com 0
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_estoque_zero.json"));
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        assertThat(r.corpo().get("margem_estimada").decimalValue()).isEqualByComparingTo("0");
        ArgumentCaptor<String> enviado = ArgumentCaptor.forClass(String.class);
        verify(otimizadorProducao).executar(enviado.capture());
        confereComExemplo(enviado.getValue(), "entrada_estoque_zero.json");
    }

    /** Mesmo conteúdo do arquivo de exemplo: ingredientes iguais; produtos iguais (a ordem da composição não importa). */
    private void confereComExemplo(String enviado, String arquivo) throws Exception {
        JsonNode real = json.readTree(enviado), esperado = json.readTree(exemplo(arquivo));
        assertThat(real.get("ingredientes")).isEqualTo(esperado.get("ingredientes"));
        assertThat(real.get("produtos").size()).isEqualTo(esperado.get("produtos").size());
        for (int i = 0; i < esperado.get("produtos").size(); i++) {
            JsonNode a = real.get("produtos").get(i), b = esperado.get("produtos").get(i);
            assertThat(a.get("id")).isEqualTo(b.get("id"));
            assertThat(a.get("preco_venda")).isEqualTo(b.get("preco_venda"));
            java.util.Set<String> ca = new java.util.HashSet<>(), cb = new java.util.HashSet<>();
            a.get("composicao").forEach(c -> ca.add(c.toString()));
            b.get("composicao").forEach(c -> cb.add(c.toString()));
            assertThat(ca).isEqualTo(cb);
        }
    }

    @Test void funcionarioNaoGeraRecomendacao() throws Exception {
        assertThat(chamar("POST", "/api/producao/recomendacao", funcionario, null).status()).isEqualTo(403);
    }

    @Test void erroDeDadosDoPythonViraHttp422ComAMensagem() throws Exception {                    // Contrato §1.3
        when(otimizadorProducao.executar(anyString())).thenReturn("{\"status\":\"erro\",\"mensagem\":\"Nenhum produto ativo para otimização\"}");
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        assertThat(r.status()).isEqualTo(422);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo("Nenhum produto ativo para otimização");
    }

    @Test void falhaTecnicaDoPythonViraHttp500SemDetalhe() throws Exception {                    // CT44
        when(otimizadorProducao.executar(anyString())).thenThrow(new FalhaPython("Python terminou com código 1: licença", null));
        var r = chamar("POST", "/api/producao/recomendacao", admin, null);
        assertThat(r.status()).isEqualTo(500);
        assertThat(r.texto()).isEqualTo("{\"mensagem\":\"Não foi possível concluir a operação.\"}");
    }

    @Test void respostaIlegivelOuPlanoQueNaoCabeNoEstoqueDa500() throws Exception {
        when(otimizadorProducao.executar(anyString())).thenReturn("Academic license - for non-commercial use only");
        assertThat(chamar("POST", "/api/producao/recomendacao", admin, null).status()).isEqualTo(500);
        when(otimizadorProducao.executar(anyString())).thenReturn(exemplo("saida_demo.json"));   // 151 copos com estoque 0
        assertThat(chamar("POST", "/api/producao/recomendacao", admin, null).status()).isEqualTo(500);
    }
}
