package com.mentefria.backend.python;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Os dois módulos Python como beans. Nos testes, cada um é trocado por um dublê com @MockitoBean
 * (sem Python e sem licença), devolvendo os JSONs de optimization/exemplos/.
 */
@Configuration
public class ModulosPython {

    /** Módulo de produção (otimizador.py, Contrato §1.1–1.4). */
    public interface OtimizadorProducao { String executar(String json); }

    /** Módulo de reposição (reposicao.py, Contrato v2 §1.6). */
    public interface OtimizadorReposicao { String executar(String json); }

    @Bean
    OtimizadorProducao otimizadorProducao(ProcessoPython processo, @Value("${app.otimizador.script}") String script) {
        return json -> processo.executar(script, json);
    }

    @Bean
    OtimizadorReposicao otimizadorReposicao(ProcessoPython processo, @Value("${app.reposicao.script}") String script) {
        return json -> processo.executar(script, json);
    }
}
