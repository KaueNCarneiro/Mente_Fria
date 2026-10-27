package com.mentefria.backend.producao;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * Guarda na memória do backend o último resultado da Tela 12 (decisão D15, roteiro v4.6, Fase 6).
 * Não cria tabela (o DER diz que o resultado da otimização fica em memória). Some se o backend reiniciar
 * ou se o Render o desligar por inatividade: a Tela 14 e o dashboard pedem para gerar de novo.
 */
@Component
public class UltimaRecomendacao {
    private final AtomicReference<Recomendacao> ultima = new AtomicReference<>();

    public void guardar(Recomendacao r) { ultima.set(r); }

    public Optional<Recomendacao> ler() { return Optional.ofNullable(ultima.get()); }
}
