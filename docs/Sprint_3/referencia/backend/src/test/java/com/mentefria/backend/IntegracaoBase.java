package com.mentefria.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.mentefria.backend.python.ModulosPython.OtimizadorProducao;
import com.mentefria.backend.python.ModulosPython.OtimizadorReposicao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base dos testes de integração: PostgreSQL 16 real (Testcontainers), o MESMO bd/Script_DDL.sql da aplicação,
 * schema recriado antes de cada teste, relógio ajustável em America/Sao_Paulo e os dois módulos Python trocados por dublês.
 * Precisa do Docker Desktop ligado.
 */
@SpringBootTest(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres!!",
        "app.cors.allowed-origins=http://127.0.0.1:5500"})
@AutoConfigureMockMvc                                   // no Boot 4, @SpringBootTest sozinho NÃO traz MockMvc
@Import(IntegracaoBase.RelogioDeTeste.class)
public abstract class IntegracaoBase {
    public static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    /** "Agora" padrão dos testes: quarta, 28/10/2026, 15h em São Paulo. */
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 28, 15, 0);
    public static final LocalDate HOJE = AGORA.toLocalDate();

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");
    static { POSTGRES.start(); }                        // um container para todas as classes (o contexto do Spring é reaproveitado)

    /** Relógio que cada teste pode mover. Substitui o Clock do RelogioConfig (@Primary). */
    public static class RelogioAjustavel extends Clock {
        private volatile Instant instante = AGORA.atZone(SP).toInstant();
        public void em(LocalDateTime quando) { instante = quando.atZone(SP).toInstant(); }
        @Override public ZoneId getZone() { return SP; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instante, zone); }
        @Override public Instant instant() { return instante; }
    }

    @TestConfiguration
    static class RelogioDeTeste {
        @Bean @Primary RelogioAjustavel relogioDeTeste() { return new RelogioAjustavel(); }
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected JsonMapper json;
    @Autowired protected RelogioAjustavel relogio;
    @Autowired protected com.mentefria.backend.producao.UltimaRecomendacao ultimaRecomendacao;
    @MockitoBean protected OtimizadorProducao otimizadorProducao;
    @MockitoBean protected OtimizadorReposicao otimizadorReposicao;

    protected String admin;          // "Bearer ..." do Administrador criado em cada teste
    protected String funcionario;    // "Bearer ..." do Funcionário criado em cada teste

    @BeforeEach
    void recriarBancoEUsuarios() throws Exception {
        jdbc.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        new ResourceDatabasePopulator(new ClassPathResource("schema/Script_DDL.sql")).execute(jdbc.getDataSource());
        relogio.em(AGORA);
        ultimaRecomendacao.guardar(null);               // o contexto é reaproveitado entre testes: a memória também
        chamar("POST", "/api/auth/registro", null, Map.of("nome", "Dono", "email", "dono@acai.com", "senha", "senha1234"));
        admin = login("dono@acai.com", "senha1234");
        chamar("POST", "/api/usuarios", admin, Map.of("nome", "Func", "email", "func@acai.com", "senha", "senha1234"));
        funcionario = login("func@acai.com", "senha1234");
    }

    /** Resposta simplificada: status e corpo já lido como JSON. */
    public record Resposta(int status, JsonNode corpo, String texto) {}

    protected Resposta chamar(String metodo, String url, String token, Object corpo) throws Exception {
        var req = request(HttpMethod.valueOf(metodo), url).accept(MediaType.APPLICATION_JSON);
        if (token != null) req.header("Authorization", token);
        if (corpo != null) req.contentType(MediaType.APPLICATION_JSON).content(corpo instanceof String s ? s : json.writeValueAsString(corpo));
        MvcResult r = mvc.perform(req).andReturn();
        String texto = r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return new Resposta(r.getResponse().getStatus(), texto.isBlank() ? null : json.readTree(texto), texto);
    }

    protected String login(String email, String senha) throws Exception {
        var r = chamar("POST", "/api/auth/login", null, Map.of("email", email, "senha", senha));
        return "Bearer " + r.corpo().get("token").asString();
    }

    protected int idUsuario(String email) {
        return jdbc.queryForObject("SELECT id FROM usuario WHERE email = ?", Integer.class, email);
    }

    protected int idIngrediente(String nome) {
        return jdbc.queryForObject("SELECT id FROM ingrediente WHERE nome = ?", Integer.class, nome);
    }

    /** Entrada pela API (o caminho real), com validade opcional. Devolve o id da linha. */
    protected int entrada(int ingredienteId, String quantidade, LocalDate validade) throws Exception {
        var corpo = new java.util.LinkedHashMap<String, Object>();
        corpo.put("ingrediente_id", ingredienteId);
        corpo.put("tipo", "ENTRADA");
        corpo.put("quantidade", new BigDecimal(quantidade));
        if (validade != null) corpo.put("data_validade", validade.toString());
        var r = chamar("POST", "/api/estoque/movimentacoes", admin, corpo);
        if (r.status() != 201) throw new IllegalStateException("entrada falhou: " + r.texto());
        return r.corpo().get("id").asInt();
    }

    /** Quantas linhas violam a invariante quantidade_estoque = soma dos restantes (deve ser 0). */
    protected int violacoesDaInvariante() {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM ingrediente i WHERE i.quantidade_estoque <>
                  COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m
                            WHERE m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'), 0)""", Integer.class);
    }

    protected BigDecimal saldo(int ingredienteId) {
        return jdbc.queryForObject("SELECT quantidade_estoque FROM ingrediente WHERE id = ?", BigDecimal.class, ingredienteId);
    }

    protected static BigDecimal dec(String v) { return new BigDecimal(v); }

    /** Número do JSON como texto, sem zeros à direita ("17.000" e "17.0" viram "17"): compara valor, não formatação. */
    protected static String num(JsonNode n) { return n.decimalValue().stripTrailingZeros().toPlainString(); }

    /** O estoque VÁLIDO do seed_demo.sql (com 6 kg de granola vencida), montado pela API. Plano esperado: 151/1/0. */
    protected void estoqueDaDemonstracao() throws Exception {
        entrada(idIngrediente("Açaí (polpa)"), "45.750", HOJE.plusDays(60));
        entrada(idIngrediente("Leite condensado"), "14.220", HOJE.plusDays(90));
        entrada(idIngrediente("Granola"), "6.000", HOJE.minusDays(2));
        entrada(idIngrediente("Granola"), "5.160", HOJE.plusDays(45));
        entrada(idIngrediente("Banana"), "11.050", HOJE.plusDays(3));
        entrada(idIngrediente("Morango"), "1.980", HOJE.plusDays(10));
        entrada(idIngrediente("Leite em pó"), "4.760", HOJE.plusDays(120));
        entrada(idIngrediente("Copo descartável 500ml"), "269", null);
    }

    /** Conteúdo de um arquivo de optimization/exemplos/ (as saídas guardadas pelo Kauê na Fase P). */
    protected static String exemplo(String arquivo) throws java.io.IOException {
        return java.nio.file.Files.readString(java.nio.file.Path.of("..", "optimization", "exemplos", arquivo));
    }
}
