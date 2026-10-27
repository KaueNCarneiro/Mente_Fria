package com.mentefria.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Fase 2 (US1–US7) com banco real: os 10 itens do gate do tutorial do Lucas (§3.5) e erros por campo. */
class Fase2CrudTest extends IntegracaoBase {

    @Test void registroNormalizaEmailESegundoRegistroDa409() throws Exception {            // CT01, CT01B
        jdbc.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(
                new org.springframework.core.io.ClassPathResource("schema/Script_DDL.sql")).execute(jdbc.getDataSource());
        var r = chamar("POST", "/api/auth/registro", null, Map.of("nome", " Dono ", "email", " DONO@Acai.com ", "senha", "senha1234"));
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("email").asString()).isEqualTo("dono@acai.com");
        assertThat(r.corpo().get("perfil").asString()).isEqualTo("ADMINISTRADOR");
        var de_novo = chamar("POST", "/api/auth/registro", null, Map.of("nome", "Outro", "email", "outro@acai.com", "senha", "senha1234"));
        assertThat(de_novo.status()).isEqualTo(409);
        assertThat(de_novo.corpo().get("mensagem").asString()).isEqualTo("O cadastro do Administrador já foi realizado.");
    }

    @Test void registroComTodosOsCamposInvalidosDevolveTodosDeUmaVez() throws Exception {       // CT02, US7
        jdbc.execute("DELETE FROM usuario");
        var r = chamar("POST", "/api/auth/registro", null, Map.of("nome", "", "email", "sem-arroba", "senha", "curta"));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("nome").asString()).isEqualTo("Informe o nome.");
        assertThat(r.corpo().get("campos").get("email").asString()).isEqualTo("Informe um e-mail válido.");
        assertThat(r.corpo().get("campos").get("senha").asString()).isEqualTo("A senha deve ter no mínimo 8 caracteres.");
    }

    @Test void loginDevolveTokenPerfilENome() throws Exception {                                 // CT06
        var r = chamar("POST", "/api/auth/login", null, Map.of("email", "DONO@acai.com ", "senha", "senha1234"));
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().get("token").asString()).isNotBlank();
        assertThat(r.corpo().get("perfil").asString()).isEqualTo("ADMINISTRADOR");
        assertThat(r.corpo().get("nome").asString()).isEqualTo("Dono");
    }

    @Test void senhaErradaEEmailInexistenteDaoAMesmaResposta() throws Exception {               // CT07
        var senha = chamar("POST", "/api/auth/login", null, Map.of("email", "dono@acai.com", "senha", "errada123"));
        var email = chamar("POST", "/api/auth/login", null, Map.of("email", "ninguem@acai.com", "senha", "senha1234"));
        jdbc.update("UPDATE usuario SET ativo = false WHERE email = 'func@acai.com'");
        var inativo = chamar("POST", "/api/auth/login", null, Map.of("email", "func@acai.com", "senha", "senha1234"));
        for (var r : List.of(senha, email, inativo)) {
            assertThat(r.status()).isEqualTo(401);
            assertThat(r.texto()).isEqualTo("{\"mensagem\":\"E-mail ou senha inválidos.\"}");
        }
    }

    @Test void listaIngredientesCompletaComEstoqueZero() throws Exception {                      // CT09
        var r = chamar("GET", "/api/ingredientes", funcionario, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().size()).isEqualTo(7);
        var acai = r.corpo().get(0);
        assertThat(acai.get("nome").asString()).isEqualTo("Açaí (polpa)");
        for (String campo : List.of("id", "unidade_medida", "custo_unitario", "porcao_padrao", "quantidade_estoque", "quantidade_minima"))
            assertThat(acai.has(campo)).as(campo).isTrue();
        assertThat(acai.get("quantidade_estoque").decimalValue()).isEqualByComparingTo("0");
    }

    @Test void funcionarioNaoCadastraIngredienteEAdministradorSim() throws Exception {            // CT04, CT05
        var corpo = Map.of("nome", "Paçoca", "unidade_medida", "kg", "custo_unitario", 30.00, "porcao_padrao", 0.020,
                "quantidade_minima", 1.0, "quantidade_estoque", 99);
        assertThat(chamar("POST", "/api/ingredientes", funcionario, corpo).status()).isEqualTo(403);
        var r = chamar("POST", "/api/ingredientes", admin, corpo);
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("quantidade_estoque").decimalValue()).isEqualByComparingTo("0");   // nunca vem do corpo
    }

    @Test void ingredienteComVariosCamposInvalidosDevolveTodos() throws Exception {              // CT10, CT11, CT18
        var r = chamar("POST", "/api/ingredientes", admin,
                Map.of("nome", "X", "unidade_medida", "kg", "custo_unitario", -1, "porcao_padrao", 0));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("custo_unitario").asString()).isEqualTo("O custo não pode ser negativo.");
        assertThat(r.corpo().get("campos").get("porcao_padrao").asString()).isEqualTo("A porção deve ser maior que zero.");
    }

    @Test void unidadeNaoCadastradaVemDoBancoComMensagemDoMapa() throws Exception {
        var r = chamar("POST", "/api/ingredientes", admin, Map.of("nome", "X", "unidade_medida", "xicara", "custo_unitario", 1, "porcao_padrao", 1));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("unidade_medida").asString()).isEqualTo("Selecione uma unidade de medida cadastrada.");
    }

    @Test void atualizarIngredienteInexistenteDa404() throws Exception {
        var r = chamar("PUT", "/api/ingredientes/999", admin, Map.of("nome", "X", "unidade_medida", "kg", "custo_unitario", 1, "porcao_padrao", 1));
        assertThat(r.status()).isEqualTo(404);
    }

    @Test void produtoSemAcaiVazioOuComIngredienteRepetidoDa400() throws Exception {               // CT13, CT15
        int granola = idIngrediente("Granola"), acai = idIngrediente("Açaí (polpa)");
        var semAcai = chamar("POST", "/api/produtos", admin, Map.of("nome", "Só granola", "preco_venda", 10,
                "composicao", List.of(Map.of("ingrediente_id", granola, "quantidade_utilizada", 0.1))));
        var vazio = chamar("POST", "/api/produtos", admin, Map.of("nome", "Vazio", "preco_venda", 10, "composicao", List.of()));
        var repetido = chamar("POST", "/api/produtos", admin, Map.of("nome", "Dobro", "preco_venda", 10, "composicao", List.of(
                Map.of("ingrediente_id", acai, "quantidade_utilizada", 0.1), Map.of("ingrediente_id", acai, "quantidade_utilizada", 0.2))));
        var zero = chamar("POST", "/api/produtos", admin, Map.of("nome", "Zero", "preco_venda", 10,
                "composicao", List.of(Map.of("ingrediente_id", acai, "quantidade_utilizada", 0))));
        assertThat(semAcai.corpo().get("campos").get("composicao").asString()).isEqualTo("O produto precisa ter o açaí entre os ingredientes.");
        assertThat(vazio.corpo().get("campos").get("composicao").asString()).isEqualTo("Adicione ao menos um ingrediente.");
        assertThat(repetido.corpo().get("campos").get("composicao").asString()).isEqualTo("Este ingrediente já foi adicionado ao produto.");
        assertThat(zero.corpo().get("campos").get("composicao").asString()).isEqualTo("A quantidade utilizada deve ser maior que zero.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM produto", Integer.class)).isEqualTo(3);   // nada gravado
    }

    @Test void produtoValidoEhGravadoComComposicaoNaMesmaTransacao() throws Exception {           // CT12
        int acai = idIngrediente("Açaí (polpa)"), copo = idIngrediente("Copo descartável 500ml");
        var r = chamar("POST", "/api/produtos", admin, Map.of("nome", "Açaí Kids 200ml", "preco_venda", 9.90, "composicao", List.of(
                Map.of("ingrediente_id", acai, "quantidade_utilizada", 0.200), Map.of("ingrediente_id", copo, "quantidade_utilizada", 1))));
        assertThat(r.status()).isEqualTo(201);
        assertThat(r.corpo().get("ativo").asBoolean()).isTrue();
        var lista = chamar("GET", "/api/produtos", funcionario, null).corpo();
        var kids = java.util.stream.StreamSupport.stream(lista.spliterator(), false)
                .filter(p -> p.get("nome").asString().equals("Açaí Kids 200ml")).findFirst().orElseThrow();
        assertThat(kids.get("composicao").size()).isEqualTo(2);
        assertThat(kids.get("composicao").get(0).get("ingrediente_nome").asString()).isEqualTo("Açaí (polpa)");
        assertThat(kids.get("composicao").get(0).get("unidade_medida").asString()).isEqualTo("kg");
    }

    @Test void ingredienteInexistenteNaComposicaoNaoGravaOProduto() throws Exception {
        int acai = idIngrediente("Açaí (polpa)");
        var r = chamar("POST", "/api/produtos", admin, Map.of("nome", "Fantasma", "preco_venda", 10, "composicao", List.of(
                Map.of("ingrediente_id", acai, "quantidade_utilizada", 0.1), Map.of("ingrediente_id", 999, "quantidade_utilizada", 0.1))));
        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo().get("campos").get("composicao").asString()).isEqualTo("Ingrediente não encontrado.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM produto", Integer.class)).isEqualTo(3);
    }

    @Test void precoZeroDa400PrecoValidoDa200FuncionarioDa403EInexistenteDa404() throws Exception {   // CT16, CT17, CT05
        assertThat(chamar("PUT", "/api/produtos/1/preco", admin, Map.of("preco_venda", 0)).status()).isEqualTo(400);
        var ok = chamar("PUT", "/api/produtos/1/preco", admin, Map.of("preco_venda", 13.50));
        assertThat(ok.status()).isEqualTo(200);
        assertThat(ok.corpo().get("preco_venda").decimalValue()).isEqualByComparingTo("13.50");
        assertThat(chamar("PUT", "/api/produtos/1/preco", funcionario, Map.of("preco_venda", 14)).status()).isEqualTo(403);
        assertThat(chamar("PUT", "/api/produtos/999/preco", admin, Map.of("preco_venda", 14)).status()).isEqualTo(404);
        assertThat(jdbc.queryForObject("SELECT preco_venda FROM produto WHERE id = 1", java.math.BigDecimal.class)).isEqualByComparingTo("13.50");
    }

    @Test void funcionarioComEmailRepetidoDa409EListaNuncaTrazSenha() throws Exception {          // CT03, US2
        var r = chamar("POST", "/api/usuarios", admin, Map.of("nome", "Outro", "email", "FUNC@acai.com", "senha", "senha1234"));
        assertThat(r.status()).isEqualTo(409);
        assertThat(r.corpo().get("campos").get("email").asString()).isEqualTo("Este e-mail já está cadastrado.");
        var lista = chamar("GET", "/api/usuarios", admin, null);
        assertThat(lista.corpo().size()).isEqualTo(1);
        assertThat(lista.texto()).doesNotContain("senha").doesNotContain("$2a$");
        assertThat(chamar("GET", "/api/usuarios", funcionario, null).status()).isEqualTo(403);                     // CT08
    }

    @Test void unidadesDeMedidaSoParaAdministrador() throws Exception {
        var r = chamar("GET", "/api/unidades-medida", admin, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo().size()).isEqualTo(5);
        assertThat(r.corpo().get(0).get("unidade_medida").asString()).isEqualTo("kg");
        assertThat(chamar("GET", "/api/unidades-medida", funcionario, null).status()).isEqualTo(403);
    }
}
