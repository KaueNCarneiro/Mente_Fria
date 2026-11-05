package com.mentefria.backend.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mentefria.backend.auth.JwtService;
import com.mentefria.backend.saude.SaudeController;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comportamento HTTP da Fase 1 (segurança e erros), sem banco: CT05, CT08, 401/403 em JSON,
 * CORS (com PATCH), 400/404/500 em JSON e sem stack trace.
 */
@WebMvcTest(controllers = {SaudeController.class, RotasDeTesteController.class})
@ActiveProfiles("teste-seguranca")
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres!!",
        "app.jwt.validade-horas=2",
        "app.cors.allowed-origins=http://127.0.0.1:5500,http://localhost:5500"})
class SegurancaEErrosTest {
    static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!!";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @MockitoBean JdbcTemplate jdbc;

    String admin() { return "Bearer " + jwt.gerarToken(1, "ADMINISTRADOR"); }
    String funcionario() { return "Bearer " + jwt.gerarToken(2, "FUNCIONARIO"); }

    @Test void saudeEPublica() throws Exception {
        mvc.perform(get("/api/saude")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test void semTokenDa401EmJson() throws Exception {
        mvc.perform(get("/api/ingredientes"))
           .andExpect(status().isUnauthorized())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void tokenInvalidoDa401() throws Exception {
        mvc.perform(get("/api/ingredientes").header("Authorization", "Bearer abc.def.ghi"))
           .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void tokenExpiradoDa401() throws Exception {
        var chave = Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));
        String vencido = Jwts.builder().subject("1").claim("perfil", "ADMINISTRADOR")
                .issuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                .expiration(new Date(System.currentTimeMillis() - 3_600_000)).signWith(chave).compact();
        mvc.perform(get("/api/ingredientes").header("Authorization", "Bearer " + vencido))
           .andExpect(status().isUnauthorized());
    }

    @Test void comTokenListaIngredientes() throws Exception {
        mvc.perform(get("/api/ingredientes").header("Authorization", funcionario())).andExpect(status().isOk());
    }

    @Test void rotaInexistenteSemTokenDa401() throws Exception {
        mvc.perform(get("/api/nao-existe")).andExpect(status().isUnauthorized());
    }

    @Test void rotaInexistenteComTokenDa404EmJson() throws Exception {
        mvc.perform(get("/api/nao-existe").header("Authorization", admin()))
           .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void testePythonNaoExisteForaDoPerfilDev() throws Exception {
        mvc.perform(get("/api/teste/python")).andExpect(status().isUnauthorized());
    }

    @Test void funcionarioEmRotaDeAdministradorDa403EmJson() throws Exception {       // CT08
        mvc.perform(get("/api/usuarios").header("Authorization", funcionario()))
           .andExpect(status().isForbidden())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para esta ação."));
    }

    @Test void funcionarioNaoAlteraPreco() throws Exception {                          // CT05
        mvc.perform(put("/api/produtos/1/preco").header("Authorization", funcionario())
                .contentType(MediaType.APPLICATION_JSON).content("{\"preco_venda\":13.50}"))
           .andExpect(status().isForbidden());
    }

    @Test void funcionarioAcessaEstoque() throws Exception {
        mvc.perform(get("/api/estoque").header("Authorization", funcionario())).andExpect(status().isOk());
    }

    @Test void administradorAlteraPreco() throws Exception {
        mvc.perform(put("/api/produtos/1/preco").header("Authorization", admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"preco_venda\":13.50}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.preco_venda").value(13.50));
    }

    @Test void jsonMalformadoDa400EmJson() throws Exception {
        mvc.perform(post("/api/ingredientes").header("Authorization", admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": "))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void idNaoNumericoNaRotaDa400() throws Exception {
        mvc.perform(put("/api/produtos/abc/preco").header("Authorization", admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"preco_venda\":13.50}"))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void parametroObrigatorioAusenteDa400() throws Exception {
        mvc.perform(get("/api/pedidos").header("Authorization", admin()))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void erroInesperadoDa500SemDetalheInterno() throws Exception {
        mvc.perform(get("/api/estoque/falha").header("Authorization", admin()))
           .andExpect(status().isInternalServerError())
           .andExpect(jsonPath("$.mensagem").value("Não foi possível concluir a operação."))
           .andExpect(content().string(not(containsString("secreto"))))
           .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test void preflightCorsLiberaPatchParaOrigemDoFrontend() throws Exception {
        mvc.perform(options("/api/ingredientes/1")
                .header("Origin", "http://127.0.0.1:5500")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
           .andExpect(status().isOk())
           .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5500"))
           .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")));
    }

    @Test void preflightDeOrigemDesconhecidaERecusado() throws Exception {
        mvc.perform(options("/api/ingredientes")
                .header("Origin", "http://site-estranho.com")
                .header("Access-Control-Request-Method", "GET"))
           .andExpect(status().isForbidden());
    }
}
