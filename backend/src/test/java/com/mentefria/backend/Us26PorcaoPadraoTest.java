package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** US26 (porção padrão por unidade de medida, Contrato §2.11) com banco real: um teste por caso do Plano (CT73–CT78). */
class Us26PorcaoPadraoTest extends IntegracaoBase {
    static final String MSG_UNIDADE = "Informe a unidade de medida (até 20 caracteres).";
    static final String MSG_FORMATO = "Use no máximo 3 casas decimais e um valor menor que 10.000.000.";

    private int quantasUnidades() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM unidade_medida_porcao_padrao", Integer.class);
    }

    private BigDecimal porcaoDaUnidade(String unidade) {
        return jdbc.queryForObject("SELECT porcao_padrao FROM unidade_medida_porcao_padrao WHERE unidade_medida = ?",
                BigDecimal.class, unidade);
    }

    private String campo(Resposta r, String nome) { return r.corpo().get("campos").get(nome).asString(); }

    @Test void cadastraUnidadeNovaNormalizada() throws Exception {                                   // CT73
        var r = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", " G ", "porcao_padrao", 40));
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("unidade_medida").asString()).isEqualTo("g");
        assertThat(r.corpo().get("porcao_padrao").decimalValue()).isEqualByComparingTo("40");
        var lista = chamar("GET", "/api/unidades-medida", admin, null);
        assertThat(lista.corpo().size()).isEqualTo(6);
        assertThat(lista.texto()).contains("\"unidade_medida\":\"g\"");
    }

    @Test void unidadeRepetidaDa409InclusiveComMaiuscula() throws Exception {                       // CT74
        chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", "g", "porcao_padrao", 40));
        for (String repetida : new String[] {"g", "G"}) {
            var r = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", repetida, "porcao_padrao", 50));
            assertThat(r.status()).isEqualTo(409);
            assertThat(r.corpo().get("mensagem").asString()).isEqualTo("Dados inválidos");
            assertThat(campo(r, "unidade_medida")).isEqualTo("Esta unidade já está cadastrada.");
        }
        assertThat(quantasUnidades()).isEqualTo(6);
        assertThat(porcaoDaUnidade("g")).isEqualByComparingTo("40");                                // nada mudou
    }

    @Test void dadosInvalidosNosLimitesDao400() throws Exception {                                  // CT75
        // POST: porção inválida, com a mensagem certa para cada entrada
        Object[][] porcoes = {{0, "A porção deve ser maior que zero."}, {-1, "A porção deve ser maior que zero."},
                {new BigDecimal("0.0001"), MSG_FORMATO}, {new BigDecimal("10000000"), MSG_FORMATO},
                {new BigDecimal("1E+7"), MSG_FORMATO}};                                            // notação científica também
        for (Object[] caso : porcoes) {
            var r = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", "cx", "porcao_padrao", caso[0]));
            assertThat(r.status()).as("porção %s", caso[0]).isEqualTo(400);
            assertThat(campo(r, "porcao_padrao")).isEqualTo(caso[1]);
        }
        var ausente = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", "cx"));
        assertThat(campo(ausente, "porcao_padrao")).isEqualTo("Informe a porção.");
        var semUnidade = chamar("POST", "/api/unidades-medida", admin, Map.of("porcao_padrao", 1));
        assertThat(campo(semUnidade, "unidade_medida")).isEqualTo(MSG_UNIDADE);
        var osDois = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", "", "porcao_padrao", 0));   // US7
        assertThat(campo(osDois, "unidade_medida")).isEqualTo(MSG_UNIDADE);
        assertThat(campo(osDois, "porcao_padrao")).isEqualTo("A porção deve ser maior que zero.");
        // POST: unidade inválida (vazia, só espaços, 21 caracteres)
        for (String unidade : new String[] {"", "   ", "a".repeat(21)}) {
            var r = chamar("POST", "/api/unidades-medida", admin, Map.of("unidade_medida", unidade, "porcao_padrao", 1));
            assertThat(r.status()).isEqualTo(400);
            assertThat(campo(r, "unidade_medida")).isEqualTo(MSG_UNIDADE);
        }
        assertThat(quantasUnidades()).isEqualTo(5);                                                // nada foi gravado
        // Aceitos: o maior valor da coluna e zeros à direita
        assertThat(chamar("POST", "/api/unidades-medida", admin,
                Map.of("unidade_medida", "cx", "porcao_padrao", new BigDecimal("9999999.999"))).status()).isEqualTo(201);
        assertThat(chamar("POST", "/api/unidades-medida", admin,
                Map.of("unidade_medida", "pct", "porcao_padrao", new BigDecimal("40.0000"))).status()).isEqualTo(201);
        // PATCH: porção 0 e ausente; e o corpo é validado antes de procurar a unidade (inexistente + porção 0 = 400)
        assertThat(campo(chamar("PATCH", "/api/unidades-medida/kg", admin, Map.of("porcao_padrao", 0)), "porcao_padrao"))
                .isEqualTo("A porção deve ser maior que zero.");
        assertThat(campo(chamar("PATCH", "/api/unidades-medida/kg", admin, Map.of()), "porcao_padrao")).isEqualTo("Informe a porção.");
        assertThat(chamar("PATCH", "/api/unidades-medida/xyz", admin, Map.of("porcao_padrao", 0)).status()).isEqualTo(400);
        assertThat(porcaoDaUnidade("kg")).isEqualByComparingTo("0.040");
    }

    @Test void alterarPorcaoPadraoNaoMudaOsIngredientes() throws Exception {                        // CT76, critério 3
        // Todos os ingredientes, inclusive os que tinham porção IGUAL ao padrão antigo do kg (Granola e Morango, 0,040):
        // um bug que "propagasse" o padrão novo para eles também tem de ser pego.
        var antes = jdbc.queryForList("SELECT id, porcao_padrao FROM ingrediente ORDER BY id");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ingrediente WHERE unidade_medida = 'kg' AND porcao_padrao = 0.040",
                Integer.class)).isGreaterThan(0);                                                   // o caso existe no seed
        var r = chamar("PATCH", "/api/unidades-medida/kg", admin, Map.of("porcao_padrao", new BigDecimal("0.050")));
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().get("unidade_medida").asString()).isEqualTo("kg");
        assertThat(r.corpo().get("porcao_padrao").decimalValue()).isEqualByComparingTo("0.050");
        assertThat(jdbc.queryForList("SELECT id, porcao_padrao FROM ingrediente ORDER BY id")).isEqualTo(antes);   // nenhum muda
        var maiuscula = chamar("PATCH", "/api/unidades-medida/KG", admin, Map.of("porcao_padrao", new BigDecimal("0.060")));
        assertThat(maiuscula.status()).isEqualTo(200);                                             // a URL também é normalizada
        assertThat(maiuscula.corpo().get("unidade_medida").asString()).isEqualTo("kg");
        assertThat(porcaoDaUnidade("kg")).isEqualByComparingTo("0.060");
    }

    @Test void alterarUnidadeInexistenteDa404() throws Exception {                                   // CT77
        var r = chamar("PATCH", "/api/unidades-medida/xyz", admin, Map.of("porcao_padrao", 1));
        assertThat(r.status()).isEqualTo(404);
        assertThat(r.corpo().get("mensagem").asString()).isEqualTo("Unidade de medida não encontrada.");
        assertThat(r.corpo().has("campos")).isFalse();
        assertThat(quantasUnidades()).isEqualTo(5);
    }

    @Test void soOAdministradorCadastraEAltera() throws Exception {                                // CT78
        assertThat(chamar("POST", "/api/unidades-medida", funcionario, Map.of("unidade_medida", "g", "porcao_padrao", 40)).status())
                .isEqualTo(403);
        assertThat(chamar("PATCH", "/api/unidades-medida/kg", funcionario, Map.of("porcao_padrao", 1)).status()).isEqualTo(403);
        assertThat(chamar("GET", "/api/unidades-medida", funcionario, null).status()).isEqualTo(403);
        assertThat(chamar("POST", "/api/unidades-medida", null, Map.of("unidade_medida", "g", "porcao_padrao", 40)).status())
                .isEqualTo(401);                                                                   // sem token
        assertThat(quantasUnidades()).isEqualTo(5);
        assertThat(porcaoDaUnidade("kg")).isEqualByComparingTo("0.040");
    }
}
