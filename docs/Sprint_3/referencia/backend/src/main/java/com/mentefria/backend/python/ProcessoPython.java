package com.mentefria.backend.python;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Roda um módulo Python como processo filho: um JSON no stdin, um JSON no stdout (Contrato §1).
 * Cuidados (todos já causaram problema no projeto): UTF-8 sem BOM na ida; stdin fechado; timeout; stderr só no log;
 * credenciais GRB_* repassadas EXPLICITAMENTE (o .env do Spring não chega ao processo filho); uma execução por vez
 * (a licença WLS acadêmica permite poucas sessões simultâneas).
 */
@Component
public class ProcessoPython {
    private static final Logger log = LoggerFactory.getLogger(ProcessoPython.class);
    private static final List<String> CREDENCIAIS = List.of("GRB_WLSACCESSID", "GRB_WLSSECRET", "GRB_LICENSEID");
    private static final ReentrantLock UMA_POR_VEZ = new ReentrantLock(true);

    /** Falha técnica: vira 500 genérico no GlobalExceptionHandler (o detalhe fica só no log). */
    public static class FalhaPython extends RuntimeException {
        public FalhaPython(String mensagem, Throwable causa) { super(mensagem, causa); }
    }

    private final String comando;
    private final long timeoutSegundos;
    private final Environment ambiente;

    public ProcessoPython(@Value("${app.python.cmd}") String comando,
                          @Value("${app.otimizador.timeout-segundos:10}") long timeoutSegundos, Environment ambiente) {
        this.comando = comando; this.timeoutSegundos = timeoutSegundos; this.ambiente = ambiente;
    }

    public String executar(String script, String json) {
        boolean travou = false;
        try {
            travou = UMA_POR_VEZ.tryLock(timeoutSegundos, TimeUnit.SECONDS);
            if (!travou) throw new FalhaPython("Outra otimização em andamento por mais de " + timeoutSegundos + " s", null);
            return rodar(script, json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FalhaPython("Interrompido", e);
        } finally {
            if (travou) UMA_POR_VEZ.unlock();
        }
    }

    private String rodar(String script, String json) throws InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(comando, script);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        for (String nome : CREDENCIAIS) {                                   // nunca escrever esses valores em log
            String v = ambiente.getProperty(nome);
            if (v != null && !v.isBlank()) pb.environment().put(nome, v);
        }
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new FalhaPython("Não foi possível iniciar o Python (" + comando + ")", e);
        }
        CompletableFuture<String> saida = ler(p.getInputStream());
        CompletableFuture<String> erros = ler(p.getErrorStream());
        try (Writer w = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8)) {   // UTF-8 sem BOM; fechar = fim da entrada
            w.write(json);
        } catch (IOException e) {
            p.destroyForcibly();
            throw new FalhaPython("Falha ao enviar a entrada ao Python", e);
        }
        if (!p.waitFor(timeoutSegundos, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new FalhaPython("O Python passou de " + timeoutSegundos + " s", null);
        }
        try {
            String stderr = erros.get(5, TimeUnit.SECONDS);
            if (!stderr.isBlank()) log.warn("stderr do Python ({}): {}", script, stderr.strip());
            if (p.exitValue() != 0) throw new FalhaPython("Python terminou com código " + p.exitValue(), null);
            return saida.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new FalhaPython("Falha ao ler a resposta do Python", e);
        }
    }

    private static CompletableFuture<String> ler(InputStream in) {
        return CompletableFuture.supplyAsync(() -> {
            try (in) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            catch (IOException e) { throw new IllegalStateException(e); }
        });
    }
}
