package com.mentefria.backend.unidade;

import java.math.BigDecimal;

/** Unidade de medida e porção padrão (lista do formulário de ingredientes). Só leitura na Sprint 3 (US26 fora). */
public record UnidadeMedida(String unidadeMedida, BigDecimal porcaoPadrao) {}
