package com.mentefria.backend.python;

import com.mentefria.backend.config.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Plano B da licença (A31 = B1, 27/10), reserva desde 28/10 (A38): liga e desliga o solver sem mudar código.
 * Padrão true, inclusive no Render (licença do pip autorizada pela Gurobi). Com OTIMIZADOR_HABILITADO=false,
 * produção e reposição respondem 503 sem chamar o Python (usar só se o solver der problema no Render).
 */
@Component
public class ChaveOtimizador {
    public static final String MENSAGEM = "A recomendação está disponível só na demonstração local da equipe.";

    private final boolean habilitado;

    public ChaveOtimizador(@Value("${app.otimizador.habilitado:true}") boolean habilitado) { this.habilitado = habilitado; }

    public boolean habilitado() { return habilitado; }

    /** Chamar ANTES de qualquer chamada ao Python. */
    public void exigir() {
        if (!habilitado) throw new ApiException(503, MENSAGEM, null);
    }
}
