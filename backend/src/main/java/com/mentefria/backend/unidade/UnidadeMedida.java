package com.mentefria.backend.unidade;

import java.math.BigDecimal;

/** Unidade de medida e porção padrão (lista do formulário de ingredientes; cadastro e alteração na US26, Tela 19). */
public record UnidadeMedida(String unidadeMedida, BigDecimal porcaoPadrao) {

    /** Corpo de POST /api/unidades-medida (Contrato §2.11). */
    public record Nova(String unidadeMedida, BigDecimal porcaoPadrao) {}

    /** Corpo de PATCH /api/unidades-medida/{unidade_medida}: só a porção; a unidade vem da URL. */
    public record NovaPorcao(BigDecimal porcaoPadrao) {}
}
