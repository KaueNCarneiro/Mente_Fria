package com.mentefria.backend.python;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

/** Os cuidados do ProcessoPython, com um Python de verdade e scripts mínimos (sem Gurobi). */
@EnabledIfEnvironmentVariable(named = "MF_PYTHON", matches = ".+")
class ProcessoPythonTest {
    static final String PYTHON = System.getenv("MF_PYTHON");
    @TempDir Path pasta;

    private String script(String codigo) throws Exception {
        Path p = pasta.resolve("s" + System.nanoTime() + ".py");
        Files.writeString(p, codigo, StandardCharsets.UTF_8);
        return p.toString();
    }

    @Test void idaEVoltaEmUtf8() throws Exception {
        String s = script("import sys, json\nsys.stdin.reconfigure(encoding='utf-8')\nsys.stdout.reconfigure(encoding='utf-8')\n"
                + "print(json.dumps({'eco': json.load(sys.stdin)['texto'].upper()}, ensure_ascii=False))\n");
        String saida = new ProcessoPython(PYTHON, 10, new MockEnvironment()).executar(s, "{\"texto\":\"Açaí com paçoca\"}");
        assertThat(saida.trim()).isEqualTo("{\"eco\": \"AÇAÍ COM PAÇOCA\"}");
    }

    @Test void credenciaisGrbChegamAoProcessoFilho() throws Exception {
        String s = script("import os\nprint(os.environ.get('GRB_LICENSEID', 'ausente'))\n");
        var env = new MockEnvironment().withProperty("GRB_LICENSEID", "123456");   // como o Spring lê do .env
        assertThat(new ProcessoPython(PYTHON, 10, env).executar(s, "{}").trim()).isEqualTo("123456");
    }

    @Test void timeoutMataOProcessoEViraFalhaTecnica() throws Exception {
        String s = script("import time\ntime.sleep(10)\n");
        long inicio = System.nanoTime();
        assertThatThrownBy(() -> new ProcessoPython(PYTHON, 1, new MockEnvironment()).executar(s, "{}"))
                .isInstanceOf(ProcessoPython.FalhaPython.class).hasMessageContaining("passou de 1 s");
        assertThat((System.nanoTime() - inicio) / 1_000_000_000).isLessThan(5);
    }

    @Test void codigoDeSaidaDiferenteDeZeroEComandoInexistenteViramFalhaTecnica() throws Exception {
        String s = script("import sys\nsys.exit(3)\n");
        assertThatThrownBy(() -> new ProcessoPython(PYTHON, 10, new MockEnvironment()).executar(s, "{}"))
                .isInstanceOf(ProcessoPython.FalhaPython.class).hasMessageContaining("código 3");
        assertThatThrownBy(() -> new ProcessoPython("python_que_nao_existe", 10, new MockEnvironment()).executar(s, "{}"))
                .isInstanceOf(ProcessoPython.FalhaPython.class);
    }
}
