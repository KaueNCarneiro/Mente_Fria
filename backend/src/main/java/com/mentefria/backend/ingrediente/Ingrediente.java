package com.mentefria.backend.ingrediente;

import java.math.BigDecimal;

/** Registro completo do ingrediente (Contrato §2.3). quantidade_estoque é o saldo TOTAL (inclui linhas vencidas). */
public record Ingrediente(int id, String nome, String unidadeMedida, BigDecimal custoUnitario, BigDecimal porcaoPadrao,
                          BigDecimal quantidadeEstoque, BigDecimal quantidadeMinima) {

    /** Corpo de POST/PUT. quantidade_estoque NUNCA vem do cliente: só muda por movimentação (US9). */
    public record Dados(String nome, String unidadeMedida, BigDecimal custoUnitario, BigDecimal porcaoPadrao,
                        BigDecimal quantidadeMinima) {}
}
